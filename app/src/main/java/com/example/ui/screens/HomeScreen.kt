package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DraftItem
import com.example.ui.components.ShareAppApkDialog
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  drafts: List<DraftItem>,
  selectedMode: Int,
  onSelectMode: (Int) -> Unit,
  onCreateNewDraft: () -> Unit,
  onOpenDraft: (DraftItem) -> Unit,
  onPreviewDraft: (DraftItem) -> Unit,
  onDuplicateDraft: (DraftItem) -> Unit,
  onShareDraftToCloud: (DraftItem) -> Unit,
  onDeleteDraft: (DraftItem) -> Unit,
  onOpenBaseTemplate: () -> Unit,
  cloudTabContent: @Composable () -> Unit,
) {
  var draftToDelete by remember { mutableStateOf<DraftItem?>(null) }
  var showShareApkDialog by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      Column {
        TopAppBar(
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
          ),
          title = {
            Column {
              Text(
                text = "Arreglos Visita",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "Formulario oficial · 4 páginas PDF",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          actions = {
            IconButton(
              onClick = { showShareApkDialog = true },
              modifier = Modifier.testTag("share_apk_topbar_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.InstallMobile,
                contentDescription = "Compartir o guardar APK de la app",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
            Surface(
              color = if (selectedMode == 0) {
                MaterialTheme.colorScheme.secondaryContainer
              } else {
                MaterialTheme.colorScheme.primaryContainer
              },
              shape = RoundedCornerShape(50),
              modifier = Modifier.padding(end = 12.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = if (selectedMode == 0) Icons.Filled.OfflinePin else Icons.Filled.Cloud,
                  contentDescription = if (selectedMode == 0) "Disponible sin conexión" else "Modo colaborativo en línea",
                  tint = if (selectedMode == 0) WhatsAppGreen else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (selectedMode == 0) "Sin conexión" else "En línea",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (selectedMode == 0) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                  } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  },
                )
              }
            }
          },
        )

        TabRow(
          selectedTabIndex = selectedMode,
          containerColor = MaterialTheme.colorScheme.background,
        ) {
          Tab(
            selected = selectedMode == 0,
            onClick = { onSelectMode(0) },
            icon = { Icon(Icons.Filled.OfflinePin, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Locales (${drafts.size})", maxLines = 1) },
            modifier = Modifier.testTag("home_tab_local"),
          )
          Tab(
            selected = selectedMode == 1,
            onClick = { onSelectMode(1) },
            icon = { Icon(Icons.Filled.People, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Colaborar en línea", maxLines = 1) },
            modifier = Modifier.testTag("home_tab_cloud"),
          )
        }
      }
    },
    floatingActionButton = {
      if (selectedMode == 0) {
        FloatingActionButton(
          onClick = onCreateNewDraft,
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.testTag("fab_new_draft"),
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Nuevo arreglo desde plantilla base",
          )
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      if (selectedMode == 0) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          item {
            HeroCreateCard(
              onCreateNewDraft = onCreateNewDraft,
              onOpenBaseTemplate = onOpenBaseTemplate,
            )
          }

          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Borradores guardados (${drafts.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
              )
              if (drafts.isNotEmpty()) {
                Text(
                  text = "Guardado automático local",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          if (drafts.isEmpty()) {
            item {
              EmptyDraftsCard(onCreateNewDraft = onCreateNewDraft)
            }
          } else {
            items(drafts, key = { it.id }) { draft ->
              DraftCard(
                draft = draft,
                onEdit = { onOpenDraft(draft) },
                onPreview = { onPreviewDraft(draft) },
                onDuplicate = { onDuplicateDraft(draft) },
                onShareToCloud = { onShareDraftToCloud(draft) },
                onDelete = { draftToDelete = draft },
              )
            }
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
          contentAlignment = Alignment.TopCenter,
        ) {
          cloudTabContent()
        }
      }
    }
  }

  AnimatedVisibility(visible = draftToDelete != null) {
    draftToDelete?.let { item ->
      AlertDialog(
        onDismissRequest = { draftToDelete = null },
        title = { Text("Eliminar borrador") },
        text = {
          val label = item.congregacion.ifBlank { "este borrador sin congregación" }
          Text("¿Seguro que deseas eliminar \"$label\"? Esta acción no se puede deshacer.")
        },
        confirmButton = {
          Button(
            onClick = {
              onDeleteDraft(item)
              draftToDelete = null
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
            modifier = Modifier.testTag("confirm_delete_draft_button"),
          ) {
            Text("Eliminar")
          }
        },
        dismissButton = {
          TextButton(onClick = { draftToDelete = null }) {
            Text("Cancelar")
          }
        },
      )
    }
  }

  if (showShareApkDialog) {
    ShareAppApkDialog(onDismiss = { showShareApkDialog = false })
  }
}

@Composable
private fun HeroCreateCard(
  onCreateNewDraft: () -> Unit,
  onOpenBaseTemplate: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              Color(0xFF1A569B),
              Color(0xFF1F7A8C),
              Color(0xFF1B8A6B),
            )
          )
        )
        .padding(20.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.Description,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(24.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Nuevo documento de visita",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
              text = "Parte de los datos base precargados del PDF",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.90f),
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onCreateNewDraft,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("create_new_draft_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Crear borrador")
          }

          OutlinedButton(
            onClick = onOpenBaseTemplate,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f)),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
              .height(48.dp)
              .testTag("edit_base_template_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Tune,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Plantilla base")
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyDraftsCard(onCreateNewDraft: () -> Unit) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Description,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp),
        )
      }
      Text(
        text = "Sin borradores todavía",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "Toca \"Crear borrador\" para llenar el formulario con los datos base ya incluidos y generar tu PDF para WhatsApp.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Button(
        onClick = onCreateNewDraft,
        modifier = Modifier
          .height(48.dp)
          .testTag("empty_create_button"),
      ) {
        Text("Empezar primer arreglo")
      }
    }
  }
}

@Composable
private fun DraftCard(
  draft: DraftItem,
  onEdit: () -> Unit,
  onPreview: () -> Unit,
  onDuplicate: () -> Unit,
  onShareToCloud: () -> Unit,
  onDelete: () -> Unit,
) {
  val (filled, total) = remember(draft.formData) { draft.formData.filledCount() }
  val progress = (filled.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)
  val updatedText = remember(draft.updatedAt) {
    SimpleDateFormat("d MMM yyyy · h:mm a", Locale.forLanguageTag("es-ES")).format(Date(draft.updatedAt))
  }

  Card(
    onClick = onEdit,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("draft_card_${draft.id}"),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = draft.congregacion.ifBlank { "Congregación sin definir" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = buildString {
              append(if (draft.fechaVisita.isNotBlank()) "Visita: ${draft.fechaVisita}" else "Fecha pendiente")
              append(" · Editado $updatedText")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Row {
          IconButton(
            onClick = onShareToCloud,
            modifier = Modifier.testTag("share_cloud_draft_${draft.id}"),
          ) {
            Icon(
              imageVector = Icons.Filled.CloudUpload,
              contentDescription = "Compartir para editar en línea",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
          IconButton(
            onClick = onDuplicate,
            modifier = Modifier.testTag("duplicate_draft_${draft.id}"),
          ) {
            Icon(
              imageVector = Icons.Filled.ContentCopy,
              contentDescription = "Duplicar borrador",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_draft_${draft.id}"),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = "Eliminar borrador",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Campos completados",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "$filled de $total",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = onEdit,
          contentPadding = PaddingValues(horizontal = 10.dp),
          modifier = Modifier
            .weight(0.78f)
            .height(48.dp)
            .testTag("edit_draft_${draft.id}"),
        ) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Editar", maxLines = 1)
        }

        Button(
          onClick = onPreview,
          colors = ButtonDefaults.buttonColors(
            containerColor = WhatsAppGreen,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
          contentPadding = PaddingValues(horizontal = 10.dp),
          modifier = Modifier
            .weight(1.22f)
            .height(48.dp)
            .testTag("preview_draft_${draft.id}"),
        ) {
          Icon(
            imageVector = Icons.Filled.PictureAsPdf,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Ver / Enviar PDF", maxLines = 1)
        }
      }
    }
  }
}
