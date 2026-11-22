package com.example.firebase

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BaseTemplateFactory
import com.example.model.VisitaFormData
import com.example.pdf.PdfPreviewHelper
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface CloudUiState<out T> {
  data object Loading : CloudUiState<Nothing>
  data class Success<T>(val data: T) : CloudUiState<T>
  data class Error(val message: String) : CloudUiState<Nothing>
}

sealed interface SharedPdfState {
  data object Idle : SharedPdfState
  data object Generating : SharedPdfState
  data class Ready(val file: File, val pages: List<Bitmap>) : SharedPdfState
  data class Error(val message: String) : SharedPdfState
}

enum class CloudSyncStatus {
  SYNCED,
  SYNCING,
  ERROR,
}

class SharedArregloViewModel(
  private val repository: SharedArregloRepository,
  private val app: Application,
  val currentUserId: String,
) : ViewModel() {

  val sharedArreglos: StateFlow<CloudUiState<List<SharedArregloSummary>>> =
    repository.observeSharedArreglos(currentUserId)
      .map<List<SharedArregloSummary>, CloudUiState<List<SharedArregloSummary>>> {
        CloudUiState.Success(it)
      }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing shared arreglos", rootError)
        emit(
          CloudUiState.Error(
            rootError.localizedMessage ?: "No se pudieron cargar los arreglos compartidos.",
          ),
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CloudUiState.Loading,
      )

  private val _activeArregloId = MutableStateFlow<String?>(null)
  val activeArregloId: StateFlow<String?> = _activeArregloId.asStateFlow()

  private val _activeTab = MutableStateFlow(0)
  val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

  private val _editingFormData = MutableStateFlow(BaseTemplateFactory.createOriginalPdfBase())
  val editingFormData: StateFlow<VisitaFormData> = _editingFormData.asStateFlow()

  private val _syncStatus = MutableStateFlow(CloudSyncStatus.SYNCED)
  val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

  private val _actionError = MutableStateFlow<String?>(null)
  val actionError: StateFlow<String?> = _actionError.asStateFlow()

  private val _isActionBusy = MutableStateFlow(false)
  val isActionBusy: StateFlow<Boolean> = _isActionBusy.asStateFlow()

  private val _pdfState = MutableStateFlow<SharedPdfState>(SharedPdfState.Idle)
  val pdfState: StateFlow<SharedPdfState> = _pdfState.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  val activeDetail: StateFlow<CloudUiState<SharedArregloDetail?>> =
    _activeArregloId
      .flatMapLatest { id ->
        if (id == null) {
          flowOf(CloudUiState.Success(null))
        } else {
          repository.observeSharedArregloDetail(id)
            .map<SharedArregloDetail?, CloudUiState<SharedArregloDetail?>> { detail ->
              if (detail != null) {
                mergeRemoteDetailIntoLocalState(detail)
              }
              CloudUiState.Success(detail)
            }
            .catch { error ->
              val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
              if (firestoreError == null && error is CancellationException) throw error
              val rootError = firestoreError ?: error
              Log.w(TAG, "Error observing shared arreglo detail", rootError)
              emit(
                CloudUiState.Error(
                  rootError.localizedMessage ?: "No se pudo sincronizar el arreglo compartido.",
                ),
              )
            }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CloudUiState.Loading,
      )

  private val dirtySections = mutableSetOf<ArregloSectionKey>()
  private val sectionJobs = mutableMapOf<ArregloSectionKey, Job>()
  private var hasSeededCurrentDoc = false

  private fun mergeRemoteDetailIntoLocalState(detail: SharedArregloDetail) {
    val current = _editingFormData.value
    val remote = detail.formData
    if (!hasSeededCurrentDoc) {
      _editingFormData.value = remote
      hasSeededCurrentDoc = true
      return
    }
    var merged = current
    if (!dirtySections.contains(ArregloSectionKey.ACTIVIDADES)) {
      merged = merged.copy(actividades = remote.actividades)
    }
    if (!dirtySections.contains(ArregloSectionKey.SERVICIO_PASTOREO)) {
      merged = merged.copy(servicioPastoreo = remote.servicioPastoreo)
    }
    if (!dirtySections.contains(ArregloSectionKey.SERVICIO_ESPOSA)) {
      merged = merged.copy(servicioEsposa = remote.servicioEsposa)
    }
    if (!dirtySections.contains(ArregloSectionKey.NOTA_PASTOREO)) {
      merged = merged.copy(notaPastoreo = remote.notaPastoreo)
    }
    _editingFormData.value = merged
  }

  fun clearError() {
    _actionError.value = null
  }

  fun createSharedArreglo(
    initialData: VisitaFormData,
    onSuccess: (String) -> Unit,
  ) {
    viewModelScope.launch {
      _isActionBusy.value = true
      _actionError.value = null
      val result = repository.createSharedArreglo(initialData)
      _isActionBusy.value = false
      result.fold(
        onSuccess = { code ->
          openSharedArreglo(code, initialFormData = initialData)
          onSuccess(code)
        },
        onFailure = { err ->
          _actionError.value = err.localizedMessage ?: "No se pudo crear el arreglo compartido."
        },
      )
    }
  }

  fun joinSharedArreglo(
    rawCode: String,
    onSuccess: (String) -> Unit,
  ) {
    val normalized = rawCode.trim().uppercase()
    val existingList = (sharedArreglos.value as? CloudUiState.Success)?.data.orEmpty()
    val alreadyMember = existingList.any { it.id.equals(normalized, ignoreCase = true) }
    if (alreadyMember) {
      openSharedArreglo(normalized)
      onSuccess(normalized)
      return
    }
    viewModelScope.launch {
      _isActionBusy.value = true
      _actionError.value = null
      val result = repository.joinByCode(normalized)
      _isActionBusy.value = false
      result.fold(
        onSuccess = { code ->
          openSharedArreglo(code)
          onSuccess(code)
        },
        onFailure = { err ->
          _actionError.value = err.localizedMessage ?: "No se pudo unir con ese código."
        },
      )
    }
  }

  fun openSharedArreglo(
    arregloId: String,
    initialFormData: VisitaFormData = BaseTemplateFactory.createOriginalPdfBase(),
    tabIndex: Int = 0,
  ) {
    cancelPendingSectionJobs()
    hasSeededCurrentDoc = false
    _editingFormData.value = initialFormData
    _activeTab.value = tabIndex.coerceIn(0, 3)
    _syncStatus.value = CloudSyncStatus.SYNCED
    _pdfState.value = SharedPdfState.Idle
    _activeArregloId.value = arregloId
  }

  fun closeActiveSharedArreglo() {
    flushAllDirtySectionsAsync()
    _pdfState.value = SharedPdfState.Idle
    _activeArregloId.value = null
    hasSeededCurrentDoc = false
  }

  fun selectTab(index: Int) {
    _activeTab.value = index.coerceIn(0, 3)
  }

  fun updateSharedForm(transform: (VisitaFormData) -> VisitaFormData) {
    val arregloId = _activeArregloId.value ?: return
    val previous = _editingFormData.value
    val updated = transform(previous)
    _editingFormData.value = updated

    val changedSections = buildList {
      if (updated.actividades != previous.actividades) add(ArregloSectionKey.ACTIVIDADES)
      if (updated.servicioPastoreo != previous.servicioPastoreo) add(ArregloSectionKey.SERVICIO_PASTOREO)
      if (updated.servicioEsposa != previous.servicioEsposa) add(ArregloSectionKey.SERVICIO_ESPOSA)
      if (updated.notaPastoreo != previous.notaPastoreo) add(ArregloSectionKey.NOTA_PASTOREO)
    }
    if (changedSections.isEmpty()) return

    _syncStatus.value = CloudSyncStatus.SYNCING
    changedSections.forEach { section ->
      dirtySections.add(section)
      sectionJobs[section]?.cancel()
      sectionJobs[section] = viewModelScope.launch {
        delay(500L)
        val snapshot = _editingFormData.value
        val res = repository.updateSection(arregloId, section, snapshot)
        dirtySections.remove(section)
        res.fold(
          onSuccess = {
            if (dirtySections.isEmpty()) {
              _syncStatus.value = CloudSyncStatus.SYNCED
            }
          },
          onFailure = { err ->
            _syncStatus.value = CloudSyncStatus.ERROR
            _actionError.value = err.localizedMessage ?: "Error al sincronizar cambios en línea."
          },
        )
      }
    }
  }

  fun saveSharedSectionNow(sectionIndex: Int = _activeTab.value) {
    val arregloId = _activeArregloId.value ?: return
    val section = ArregloSectionKey.fromTabIndex(sectionIndex)
    sectionJobs[section]?.cancel()
    dirtySections.add(section)
    viewModelScope.launch {
      _syncStatus.value = CloudSyncStatus.SYNCING
      val res = repository.updateSection(arregloId, section, _editingFormData.value)
      dirtySections.remove(section)
      res.fold(
        onSuccess = {
          if (dirtySections.isEmpty()) _syncStatus.value = CloudSyncStatus.SYNCED
        },
        onFailure = { err ->
          _syncStatus.value = CloudSyncStatus.ERROR
          _actionError.value = err.localizedMessage ?: "No se pudieron guardar los cambios."
        },
      )
    }
  }

  fun flushAndGeneratePdf(onReadyToShowPreview: () -> Unit) {
    val arregloId = _activeArregloId.value
    val snapshot = _editingFormData.value
    val toFlush = dirtySections.toList()
    cancelPendingSectionJobs()

    viewModelScope.launch {
      if (arregloId != null && toFlush.isNotEmpty()) {
        _syncStatus.value = CloudSyncStatus.SYNCING
        toFlush.forEach { section ->
          repository.updateSection(arregloId, section, snapshot)
        }
        _syncStatus.value = CloudSyncStatus.SYNCED
      }
      onReadyToShowPreview()
      generatePdf(snapshot)
    }
  }

  fun previewSharedSummary(summary: SharedArregloSummary, onReadyToShowPreview: () -> Unit) {
    openSharedArreglo(summary.id)
    onReadyToShowPreview()
    viewModelScope.launch {
      _pdfState.value = SharedPdfState.Generating
      // Wait briefly for the 5 snapshots to arrive if opening directly from list
      var waited = 0
      while (waited < 40) {
        val current = activeDetail.value
        if (current is CloudUiState.Success && current.data != null) {
          generatePdf(current.data.formData)
          return@launch
        }
        if (current is CloudUiState.Error) {
          _pdfState.value = SharedPdfState.Error(current.message)
          return@launch
        }
        delay(100L)
        waited++
      }
      generatePdf(_editingFormData.value)
    }
  }

  fun regeneratePdf() {
    generatePdf(_editingFormData.value)
  }

  private fun generatePdf(formData: VisitaFormData) {
    _pdfState.value = SharedPdfState.Generating
    viewModelScope.launch {
      val result = withContext(Dispatchers.IO) {
        runCatching {
          val file = PdfPreviewHelper.createDraftPdfFile(app, formData)
          val pages = PdfPreviewHelper.renderPdfPages(file)
          file to pages
        }
      }
      _pdfState.value = result.fold(
        onSuccess = { (file, pages) -> SharedPdfState.Ready(file, pages) },
        onFailure = { err ->
          SharedPdfState.Error(err.localizedMessage ?: "No se pudo generar el PDF.")
        },
      )
    }
  }

  fun deleteOrLeaveSharedArreglo(summary: SharedArregloSummary) {
    viewModelScope.launch {
      _isActionBusy.value = true
      val result = if (summary.ownerId == currentUserId) {
        repository.deleteSharedArreglo(summary.id)
      } else {
        repository.leaveSharedArreglo(summary.id)
      }
      _isActionBusy.value = false
      result.onFailure { err ->
        _actionError.value = err.localizedMessage ?: "No se pudo completar la acción."
      }
    }
  }

  private fun flushAllDirtySectionsAsync() {
    val arregloId = _activeArregloId.value ?: return
    val snapshot = _editingFormData.value
    val toFlush = dirtySections.toList()
    cancelPendingSectionJobs()
    if (toFlush.isEmpty()) return
    viewModelScope.launch {
      toFlush.forEach { section ->
        repository.updateSection(arregloId, section, snapshot)
      }
    }
  }

  private fun cancelPendingSectionJobs() {
    sectionJobs.values.forEach { it.cancel() }
    sectionJobs.clear()
    dirtySections.clear()
  }

  private companion object {
    const val TAG = "SharedArregloVM"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
