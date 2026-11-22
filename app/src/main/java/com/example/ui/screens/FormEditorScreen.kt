package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.firebase.ArregloSectionKey
import com.example.firebase.CloudSyncStatus
import com.example.firebase.SectionEditMeta
import com.example.model.VisitaFormData
import com.example.ui.SaveStatus
import com.example.ui.components.ShareCollaboratorDialog
import com.example.ui.theme.WhatsAppGreen

data class SharedCollabEditorInfo(
  val code: String,
  val memberNames: Map<String, String>,
  val sectionMetas: Map<ArregloSectionKey, SectionEditMeta>,
  val syncStatus: CloudSyncStatus,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormEditorScreen(
  formData: VisitaFormData,
  isBaseTemplate: Boolean,
  selectedTab: Int,
  saveStatus: SaveStatus,
  onSelectTab: (Int) -> Unit,
  onUpdateForm: ((VisitaFormData) -> VisitaFormData) -> Unit,
  onSaveNow: () -> Unit,
  onResetBaseTemplate: () -> Unit,
  onOpenPreview: () -> Unit,
  onBack: () -> Unit,
  sharedInfo: SharedCollabEditorInfo? = null,
) {
  BackHandler(onBack = onBack)

  var showResetConfirm by remember { mutableStateOf(false) }
  var showInviteDialog by remember { mutableStateOf(false) }
  val tabs = remember {
    listOf("1. Actividades", "2. Sup. Circuito", "3. Esposa", "4. Pastoreo y Nota")
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      Column {
        TopAppBar(
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
          ),
          navigationIcon = {
            IconButton(
              onClick = onBack,
              modifier = Modifier.testTag("editor_back_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver al inicio",
              )
            }
          },
          title = {
            Column {
              Text(
                text = if (isBaseTemplate) {
                  "Editar Plantilla Base"
                } else {
                  formData.actividades.congregacion.ifBlank {
                    if (sharedInfo != null) "Arreglo ${sharedInfo.code}" else "Nuevo Arreglo"
                  }
                },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                if (sharedInfo != null) {
                  val (icon, tint, label) = when (sharedInfo.syncStatus) {
                    CloudSyncStatus.SYNCED -> Triple(
                      Icons.Filled.CloudDone,
                      WhatsAppGreen,
                      "En línea · Código ${sharedInfo.code}",
                    )
                    CloudSyncStatus.SYNCING -> Triple(
                      Icons.Filled.Sync,
                      MaterialTheme.colorScheme.primary,
                      "Sincronizando cambios...",
                    )
                    CloudSyncStatus.ERROR -> Triple(
                      Icons.Filled.CloudOff,
                      MaterialTheme.colorScheme.error,
                      "Sin conexión para sincronizar",
                    )
                  }
                  Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(13.dp),
                  )
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                } else {
                  Icon(
                    imageVector = if (saveStatus == SaveStatus.SAVED) Icons.Filled.CheckCircle else Icons.Filled.Sync,
                    contentDescription = null,
                    tint = if (saveStatus == SaveStatus.SAVED) WhatsAppGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp),
                  )
                  Text(
                    text = if (saveStatus == SaveStatus.SAVED) "Guardado local" else "Guardando...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          },
          actions = {
            if (sharedInfo != null) {
              IconButton(
                onClick = { showInviteDialog = true },
                modifier = Modifier.testTag("editor_invite_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.GroupAdd,
                  contentDescription = "Invitar colaborador",
                  tint = MaterialTheme.colorScheme.primary,
                )
              }
            }
            if (isBaseTemplate) {
              IconButton(
                onClick = { showResetConfirm = true },
                modifier = Modifier.testTag("reset_base_template_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.RestartAlt,
                  contentDescription = "Restaurar plantilla original del PDF",
                )
              }
            }
            TextButton(
              onClick = onSaveNow,
              modifier = Modifier.testTag("save_draft_button"),
            ) {
              Text("Guardar")
            }
          },
        )

        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surface,
          edgePadding = 12.dp,
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { onSelectTab(index) },
              text = { Text(title, maxLines = 1) },
              modifier = Modifier.testTag("editor_tab_$index"),
            )
          }
        }
      }
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (selectedTab > 0) {
            OutlinedButton(
              onClick = { onSelectTab(selectedTab - 1) },
              modifier = Modifier
                .height(48.dp)
                .testTag("prev_section_button"),
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Anterior")
            }
          }

          if (selectedTab < tabs.lastIndex) {
            OutlinedButton(
              onClick = { onSelectTab(selectedTab + 1) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("next_section_button"),
            ) {
              Text("Siguiente")
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
          }

          Button(
            onClick = onOpenPreview,
            colors = ButtonDefaults.buttonColors(
              containerColor = WhatsAppGreen,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
              .weight(1.2f)
              .height(48.dp)
              .testTag("generate_pdf_button"),
          ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Ver PDF")
          }
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .imePadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 720.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 16.dp),
      ) {
        if (sharedInfo != null) {
          val sectionKey = ArregloSectionKey.fromTabIndex(selectedTab)
          val sectionMeta = sharedInfo.sectionMetas[sectionKey]
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
              ) {
                Icon(
                  imageVector = Icons.Filled.People,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  val lastEditor = sectionMeta?.lastEditedByName?.takeIf { it.isNotBlank() }
                  Text(
                    text = if (lastEditor != null) {
                      "Última edición en esta parte: $lastEditor"
                    } else {
                      "Edición colaborativa en tiempo real"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                  )
                  Text(
                    text = "${sharedInfo.memberNames.size} participante(s) · Código ${sharedInfo.code}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              TextButton(onClick = { showInviteDialog = true }) {
                Text("Invitar")
              }
            }
          }
        }

        when (selectedTab) {
          0 -> ActividadesTab(formData = formData, onUpdate = onUpdateForm)
          1 -> ServicioPastoreoTab(formData = formData, onUpdate = onUpdateForm)
          2 -> ServicioEsposaTab(formData = formData, onUpdate = onUpdateForm)
          3 -> NotaPastoreoTab(formData = formData, onUpdate = onUpdateForm)
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  if (showResetConfirm) {
    AlertDialog(
      onDismissRequest = { showResetConfirm = false },
      title = { Text("Restaurar plantilla original") },
      text = {
        Text("¿Deseas restaurar la plantilla base con los datos originales del PDF (familias de almuerzo, horarios y pastoreo base)?")
      },
      confirmButton = {
        Button(
          onClick = {
            onResetBaseTemplate()
            showResetConfirm = false
          },
        ) {
          Text("Restaurar")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirm = false }) {
          Text("Cancelar")
        }
      },
    )
  }

  if (showInviteDialog && sharedInfo != null) {
    ShareCollaboratorDialog(
      code = sharedInfo.code,
      congregacion = formData.actividades.congregacion,
      memberNames = sharedInfo.memberNames,
      onDismiss = { showInviteDialog = false },
    )
  }
}
