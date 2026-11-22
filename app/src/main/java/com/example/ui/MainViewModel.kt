package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DraftItem
import com.example.data.DraftRepository
import com.example.model.BaseTemplateFactory
import com.example.model.VisitaFormData
import com.example.pdf.PdfPreviewHelper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AppScreen {
  data object Home : AppScreen
  data class Editor(val draftId: Long, val isBaseTemplate: Boolean) : AppScreen
  data class Preview(val draftId: Long, val isBaseTemplate: Boolean) : AppScreen
}

sealed interface PdfRenderState {
  data object Idle : PdfRenderState
  data object Generating : PdfRenderState
  data class Ready(val file: File, val pages: List<Bitmap>) : PdfRenderState
  data class Error(val message: String) : PdfRenderState
}

enum class SaveStatus {
  SAVED,
  SAVING,
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = DraftRepository(AppDatabase.getInstance(application).draftDao())

  val drafts: StateFlow<List<DraftItem>> = repository.observeDrafts()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val baseTemplate: StateFlow<DraftItem?> = repository.observeBaseTemplate()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _editingDraftId = MutableStateFlow<Long?>(null)
  private val _isEditingBaseTemplate = MutableStateFlow(false)

  private val _editingFormData = MutableStateFlow(BaseTemplateFactory.createOriginalPdfBase())
  val editingFormData: StateFlow<VisitaFormData> = _editingFormData.asStateFlow()

  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  private val _saveStatus = MutableStateFlow(SaveStatus.SAVED)
  val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

  private val _pdfState = MutableStateFlow<PdfRenderState>(PdfRenderState.Idle)
  val pdfState: StateFlow<PdfRenderState> = _pdfState.asStateFlow()

  private var autoSaveJob: Job? = null

  init {
    viewModelScope.launch {
      repository.ensureBaseTemplateSeeded()
    }
  }

  fun createNewDraftFromBase() {
    viewModelScope.launch {
      val created = repository.createNewDraftFromBase()
      openEditor(created, tabIndex = 0)
    }
  }

  fun openBaseTemplateEditor() {
    viewModelScope.launch {
      val base = repository.ensureBaseTemplateSeeded()
      openEditor(base, tabIndex = 0)
    }
  }

  fun openDraft(item: DraftItem) {
    openEditor(item, tabIndex = 0)
  }

  fun openDraftPreview(item: DraftItem) {
    _editingDraftId.value = item.id
    _isEditingBaseTemplate.value = item.isBaseTemplate
    _editingFormData.value = item.formData
    _currentScreen.value = AppScreen.Preview(item.id, item.isBaseTemplate)
    generateAndRenderPdf(item.formData)
  }

  private fun openEditor(item: DraftItem, tabIndex: Int) {
    _editingDraftId.value = item.id
    _isEditingBaseTemplate.value = item.isBaseTemplate
    _editingFormData.value = item.formData
    _selectedTab.value = tabIndex
    _saveStatus.value = SaveStatus.SAVED
    _currentScreen.value = AppScreen.Editor(item.id, item.isBaseTemplate)
  }

  fun selectTab(index: Int) {
    _selectedTab.value = index.coerceIn(0, 3)
  }

  fun updateForm(transform: (VisitaFormData) -> VisitaFormData) {
    val updated = transform(_editingFormData.value)
    _editingFormData.value = updated
    _saveStatus.value = SaveStatus.SAVING

    val draftId = _editingDraftId.value ?: return
    val isBase = _isEditingBaseTemplate.value
    autoSaveJob?.cancel()
    autoSaveJob = viewModelScope.launch {
      delay(400L)
      repository.saveDraft(draftId, updated, isBase)
      _saveStatus.value = SaveStatus.SAVED
    }
  }

  fun saveImmediatelyAndNotify() {
    val draftId = _editingDraftId.value ?: return
    val isBase = _isEditingBaseTemplate.value
    autoSaveJob?.cancel()
    viewModelScope.launch {
      _saveStatus.value = SaveStatus.SAVING
      repository.saveDraft(draftId, _editingFormData.value, isBase)
      _saveStatus.value = SaveStatus.SAVED
    }
  }

  fun resetBaseTemplateToOriginal() {
    viewModelScope.launch {
      val updated = repository.resetBaseTemplateToPdfOriginal()
      if (_isEditingBaseTemplate.value) {
        _editingFormData.value = updated.formData
        _saveStatus.value = SaveStatus.SAVED
      }
    }
  }

  fun duplicateDraft(item: DraftItem) {
    viewModelScope.launch {
      repository.duplicateDraft(item)
    }
  }

  fun deleteDraft(item: DraftItem) {
    viewModelScope.launch {
      repository.deleteDraft(item.id)
    }
  }

  fun openPreviewFromEditor() {
    val draftId = _editingDraftId.value ?: return
    val isBase = _isEditingBaseTemplate.value
    val snapshot = _editingFormData.value
    autoSaveJob?.cancel()
    viewModelScope.launch {
      repository.saveDraft(draftId, snapshot, isBase)
      _saveStatus.value = SaveStatus.SAVED
      _currentScreen.value = AppScreen.Preview(draftId, isBase)
      generateAndRenderPdf(snapshot)
    }
  }

  fun regeneratePreview() {
    generateAndRenderPdf(_editingFormData.value)
  }

  private fun generateAndRenderPdf(formData: VisitaFormData) {
    _pdfState.value = PdfRenderState.Generating
    viewModelScope.launch {
      val result = withContext(Dispatchers.IO) {
        runCatching {
          val app = getApplication<Application>()
          val file = PdfPreviewHelper.createDraftPdfFile(app, formData)
          val pages = PdfPreviewHelper.renderPdfPages(file)
          file to pages
        }
      }
      _pdfState.value = result.fold(
        onSuccess = { (file, pages) -> PdfRenderState.Ready(file, pages) },
        onFailure = { err ->
          PdfRenderState.Error(err.localizedMessage ?: "No se pudo generar el PDF")
        },
      )
    }
  }

  fun navigateBackToEditor() {
    val draftId = _editingDraftId.value ?: return
    val isBase = _isEditingBaseTemplate.value
    _pdfState.value = PdfRenderState.Idle
    _currentScreen.value = AppScreen.Editor(draftId, isBase)
  }

  fun navigateBackToHome() {
    val draftId = _editingDraftId.value
    val isBase = _isEditingBaseTemplate.value
    val snapshot = _editingFormData.value
    autoSaveJob?.cancel()
    if (draftId != null) {
      viewModelScope.launch {
        repository.saveDraft(draftId, snapshot, isBase)
        _saveStatus.value = SaveStatus.SAVED
      }
    }
    _pdfState.value = PdfRenderState.Idle
    _currentScreen.value = AppScreen.Home
  }
}
