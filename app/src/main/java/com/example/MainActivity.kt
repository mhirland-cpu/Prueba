package com.example

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.firebase.CloudSyncStatus
import com.example.firebase.CloudUiState
import com.example.firebase.SharedArregloRepository
import com.example.firebase.SharedArregloViewModel
import com.example.firebase.SharedPdfState
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.authStateFlow
import com.example.firebase.signOutFromGoogle
import com.example.model.BaseTemplateFactory
import com.example.model.VisitaFormData
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.PdfRenderState
import com.example.ui.SaveStatus
import com.example.ui.components.GoogleSignInCard
import com.example.ui.screens.FormEditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PdfPreviewScreen
import com.example.ui.screens.SharedCollabEditorInfo
import com.example.ui.screens.SharedHomeTab
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

private sealed interface SharedSubScreen {
  data class Editor(val code: String) : SharedSubScreen
  data class Preview(val code: String) : SharedSubScreen
}

class MainActivity : ComponentActivity() {
  override fun attachBaseContext(newBase: Context) {
    val spanishLocale = Locale.forLanguageTag("es-ES")
    Locale.setDefault(spanishLocale)
    val config = Configuration(newBase.resources.configuration)
    config.setLocale(spanishLocale)
    super.attachBaseContext(newBase.createConfigurationContext(config))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
    )
    setContent {
      MyApplicationTheme {
        ArreglosAppContent()
      }
    }
  }
}

@Composable
private fun ArreglosAppContent() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val auth = remember { Firebase.auth }
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {},
      onUnauthenticated = {},
      scope = scope,
    )
  }

  val localViewModel: MainViewModel = viewModel()
  val currentScreen by localViewModel.currentScreen.collectAsStateWithLifecycle()
  val drafts by localViewModel.drafts.collectAsStateWithLifecycle()
  val baseTemplate by localViewModel.baseTemplate.collectAsStateWithLifecycle()
  val editingFormData by localViewModel.editingFormData.collectAsStateWithLifecycle()
  val selectedTab by localViewModel.selectedTab.collectAsStateWithLifecycle()
  val saveStatus by localViewModel.saveStatus.collectAsStateWithLifecycle()
  val pdfState by localViewModel.pdfState.collectAsStateWithLifecycle()

  var selectedHomeMode by remember { mutableIntStateOf(0) }
  var pendingUploadFormData by remember { mutableStateOf<VisitaFormData?>(null) }

  val user = currentUser
  if (user == null) {
    // Unauthenticated path: zero Firestore listeners or ViewModels exist
    LocalNavigationContent(
      currentScreen = currentScreen,
      drafts = drafts,
      editingFormData = editingFormData,
      selectedTab = selectedTab,
      saveStatus = saveStatus,
      pdfState = pdfState,
      selectedHomeMode = selectedHomeMode,
      onSelectHomeMode = { selectedHomeMode = it },
      localViewModel = localViewModel,
      onShareDraftToCloud = { draft ->
        pendingUploadFormData = draft.formData
        selectedHomeMode = 1
      },
      cloudTabContent = {
        Box(modifier = Modifier.padding(16.dp)) {
          GoogleSignInCard(
            onAuthSuccess = {
              selectedHomeMode = 1
            },
          )
        }
      },
    )
  } else {
    // Authenticated path: keyed by uid so account switches / sign-out tear down cleanly
    key(user.uid) {
      AuthenticatedAppContent(
        user = user,
        credentialManager = credentialManager,
        localViewModel = localViewModel,
        currentScreen = currentScreen,
        drafts = drafts,
        baseTemplateData = baseTemplate?.formData ?: BaseTemplateFactory.createOriginalPdfBase(),
        editingFormData = editingFormData,
        selectedTab = selectedTab,
        saveStatus = saveStatus,
        pdfState = pdfState,
        selectedHomeMode = selectedHomeMode,
        onSelectHomeMode = { selectedHomeMode = it },
        pendingUploadFormData = pendingUploadFormData,
        onClearPendingUpload = { pendingUploadFormData = null },
      )
    }
  }
}

@Composable
private fun AuthenticatedAppContent(
  user: FirebaseUser,
  credentialManager: CredentialManager,
  localViewModel: MainViewModel,
  currentScreen: AppScreen,
  drafts: List<com.example.data.DraftItem>,
  baseTemplateData: VisitaFormData,
  editingFormData: VisitaFormData,
  selectedTab: Int,
  saveStatus: SaveStatus,
  pdfState: PdfRenderState,
  selectedHomeMode: Int,
  onSelectHomeMode: (Int) -> Unit,
  pendingUploadFormData: VisitaFormData?,
  onClearPendingUpload: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val sharedViewModel: SharedArregloViewModel = viewModel(
    key = user.uid,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val databaseId = app.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        SharedArregloViewModel(
          repository = SharedArregloRepository(db),
          app = app,
          currentUserId = user.uid,
        )
      }
    },
  )

  var sharedScreen by remember { mutableStateOf<SharedSubScreen?>(null) }

  val sharedArreglosState by sharedViewModel.sharedArreglos.collectAsStateWithLifecycle()
  val sharedDetailState by sharedViewModel.activeDetail.collectAsStateWithLifecycle()
  val sharedFormData by sharedViewModel.editingFormData.collectAsStateWithLifecycle()
  val sharedTab by sharedViewModel.activeTab.collectAsStateWithLifecycle()
  val sharedSyncStatus by sharedViewModel.syncStatus.collectAsStateWithLifecycle()
  val sharedPdfState by sharedViewModel.pdfState.collectAsStateWithLifecycle()
  val isActionBusy by sharedViewModel.isActionBusy.collectAsStateWithLifecycle()
  val actionError by sharedViewModel.actionError.collectAsStateWithLifecycle()

  LaunchedEffect(pendingUploadFormData) {
    if (pendingUploadFormData != null) {
      val data = pendingUploadFormData
      onClearPendingUpload()
      sharedViewModel.createSharedArreglo(data) { code ->
        sharedScreen = SharedSubScreen.Editor(code)
      }
    }
  }

  val activeSharedScreen = sharedScreen
  if (activeSharedScreen != null) {
    val detail = (sharedDetailState as? CloudUiState.Success)?.data
    val mappedPdfState = when (val s = sharedPdfState) {
      is SharedPdfState.Idle -> PdfRenderState.Idle
      is SharedPdfState.Generating -> PdfRenderState.Generating
      is SharedPdfState.Ready -> PdfRenderState.Ready(s.file, s.pages)
      is SharedPdfState.Error -> PdfRenderState.Error(s.message)
    }

    AnimatedContent(
      targetState = activeSharedScreen,
      label = "shared_screen_transition",
    ) { target ->
      when (target) {
        is SharedSubScreen.Editor -> {
          FormEditorScreen(
            formData = sharedFormData,
            isBaseTemplate = false,
            selectedTab = sharedTab,
            saveStatus = if (sharedSyncStatus == CloudSyncStatus.SYNCED) SaveStatus.SAVED else SaveStatus.SAVING,
            onSelectTab = sharedViewModel::selectTab,
            onUpdateForm = sharedViewModel::updateSharedForm,
            onSaveNow = { sharedViewModel.saveSharedSectionNow() },
            onResetBaseTemplate = {},
            onOpenPreview = {
              sharedViewModel.flushAndGeneratePdf {
                sharedScreen = SharedSubScreen.Preview(target.code)
              }
            },
            onBack = {
              sharedViewModel.closeActiveSharedArreglo()
              sharedScreen = null
            },
            sharedInfo = SharedCollabEditorInfo(
              code = target.code,
              memberNames = detail?.summary?.memberNames ?: emptyMap(),
              sectionMetas = detail?.sectionMetas ?: emptyMap(),
              syncStatus = sharedSyncStatus,
            ),
          )
        }

        is SharedSubScreen.Preview -> {
          PdfPreviewScreen(
            formData = sharedFormData,
            pdfState = mappedPdfState,
            onRetry = sharedViewModel::regeneratePdf,
            onEditForm = { sharedScreen = SharedSubScreen.Editor(target.code) },
            onBack = { sharedScreen = SharedSubScreen.Editor(target.code) },
          )
        }
      }
    }
  } else {
    LocalNavigationContent(
      currentScreen = currentScreen,
      drafts = drafts,
      editingFormData = editingFormData,
      selectedTab = selectedTab,
      saveStatus = saveStatus,
      pdfState = pdfState,
      selectedHomeMode = selectedHomeMode,
      onSelectHomeMode = onSelectHomeMode,
      localViewModel = localViewModel,
      onShareDraftToCloud = { draft ->
        onSelectHomeMode(1)
        sharedViewModel.createSharedArreglo(draft.formData) { code ->
          sharedScreen = SharedSubScreen.Editor(code)
        }
      },
      cloudTabContent = {
        SharedHomeTab(
          userName = user.displayName ?: user.email?.substringBefore('@') ?: "Usuario",
          userEmail = user.email.orEmpty(),
          currentUserId = user.uid,
          arreglosState = sharedArreglosState,
          isActionBusy = isActionBusy,
          actionError = actionError,
          onClearError = sharedViewModel::clearError,
          onCreateSharedFromBase = {
            sharedViewModel.createSharedArreglo(baseTemplateData) { code ->
              sharedScreen = SharedSubScreen.Editor(code)
            }
          },
          onJoinByCode = { code, onJoined ->
            sharedViewModel.joinSharedArreglo(code) { joinedCode ->
              onJoined()
              sharedScreen = SharedSubScreen.Editor(joinedCode)
            }
          },
          onOpenShared = { summary ->
            sharedViewModel.openSharedArreglo(summary.id)
            sharedScreen = SharedSubScreen.Editor(summary.id)
          },
          onPreviewShared = { summary ->
            sharedViewModel.previewSharedSummary(summary) {
              sharedScreen = SharedSubScreen.Preview(summary.id)
            }
          },
          onDeleteOrLeaveShared = sharedViewModel::deleteOrLeaveSharedArreglo,
          onSignOut = {
            sharedViewModel.closeActiveSharedArreglo()
            sharedScreen = null
            signOutFromGoogle(credentialManager, scope)
          },
        )
      },
    )
  }
}

@Composable
private fun LocalNavigationContent(
  currentScreen: AppScreen,
  drafts: List<com.example.data.DraftItem>,
  editingFormData: VisitaFormData,
  selectedTab: Int,
  saveStatus: SaveStatus,
  pdfState: PdfRenderState,
  selectedHomeMode: Int,
  onSelectHomeMode: (Int) -> Unit,
  localViewModel: MainViewModel,
  onShareDraftToCloud: (com.example.data.DraftItem) -> Unit,
  cloudTabContent: @Composable () -> Unit,
) {
  AnimatedContent(
    targetState = currentScreen,
    label = "screen_transition",
  ) { screen ->
    when (screen) {
      is AppScreen.Home -> {
        HomeScreen(
          drafts = drafts,
          selectedMode = selectedHomeMode,
          onSelectMode = onSelectHomeMode,
          onCreateNewDraft = localViewModel::createNewDraftFromBase,
          onOpenDraft = localViewModel::openDraft,
          onPreviewDraft = localViewModel::openDraftPreview,
          onDuplicateDraft = localViewModel::duplicateDraft,
          onShareDraftToCloud = onShareDraftToCloud,
          onDeleteDraft = localViewModel::deleteDraft,
          onOpenBaseTemplate = localViewModel::openBaseTemplateEditor,
          cloudTabContent = cloudTabContent,
        )
      }

      is AppScreen.Editor -> {
        FormEditorScreen(
          formData = editingFormData,
          isBaseTemplate = screen.isBaseTemplate,
          selectedTab = selectedTab,
          saveStatus = saveStatus,
          onSelectTab = localViewModel::selectTab,
          onUpdateForm = localViewModel::updateForm,
          onSaveNow = localViewModel::saveImmediatelyAndNotify,
          onResetBaseTemplate = localViewModel::resetBaseTemplateToOriginal,
          onOpenPreview = localViewModel::openPreviewFromEditor,
          onBack = localViewModel::navigateBackToHome,
        )
      }

      is AppScreen.Preview -> {
        PdfPreviewScreen(
          formData = editingFormData,
          pdfState = pdfState,
          onRetry = localViewModel::regeneratePreview,
          onEditForm = localViewModel::navigateBackToEditor,
          onBack = localViewModel::navigateBackToEditor,
        )
      }
    }
  }
}
