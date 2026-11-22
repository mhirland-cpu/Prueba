package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.firebase.CloudUiState
import com.example.firebase.SharedArregloSummary
import com.example.ui.components.JoinByCodeDialog
import com.example.ui.components.ShareCollaboratorDialog
import com.example.ui.components.copyCodeToClipboard
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SharedHomeTab(
  userName: String,
  userEmail: String,
  currentUserId: String,
  arreglosState: CloudUiState<List<SharedArregloSummary>>,
  isActionBusy: Boolean,
  actionError: String?,
  onClearError: () -> Unit,
  onCreateSharedFromBase: () -> Unit,
  onJoinByCode: (String, () -> Unit) -> Unit,
  onOpenShared: (SharedArregloSummary) -> Unit,
  onPreviewShared: (SharedArregloSummary) -> Unit,
  onDeleteOrLeaveShared: (SharedArregloSummary) -> Unit,
  onSignOut: () -> Unit,
) {
  val context = LocalContext.current
  var showJoinDialog by remember { mutableStateOf(false) }
  var inviteItem by remember { mutableStateOf<SharedArregloSummary?>(null) }
  var deleteOrLeaveItem by remember { mutableStateOf<SharedArregloSummary?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 720.dp),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = userName.ifBlank { "Usuario conectado" },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              if (userEmail.isNotBlank()) {
                Text(
                  text = userEmail,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }
          TextButton(
            onClick = onSignOut,
            modifier = Modifier.testTag("sign_out_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ExitToApp,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Salir")
          }
        }
      }
    }

    item {
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
                  Color(0xFF28559A),
                  Color(0xFF4A6FA5),
                  Color(0xFF1F7A8C),
                ),
              ),
            )
            .padding(20.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Filled.People,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp),
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Arreglo colaborativo",
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White,
                )
                Text(
                  text = "Llenen distintas partes a la vez y generen el PDF final",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.9f),
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Button(
                onClick = onCreateSharedFromBase,
                enabled = !isActionBusy,
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                  contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("create_shared_arreglo_button"),
              ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear nuevo")
              }

              OutlinedButton(
                onClick = {
                  onClearError()
                  showJoinDialog = true
                },
                enabled = !isActionBusy,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("open_join_code_dialog_button"),
              ) {
                Icon(Icons.Filled.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Unirse")
              }
            }
          }
        }
      }
    }

    if (!actionError.isNullOrBlank()) {
      item {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = actionError,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClearError) {
              Text("Cerrar")
            }
          }
        }
      }
    }

    when (arreglosState) {
      is CloudUiState.Loading -> {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator()
          }
        }
      }

      is CloudUiState.Error -> {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = arreglosState.message,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.padding(16.dp),
            )
          }
        }
      }

      is CloudUiState.Success -> {
        val list = arreglosState.data
        item {
          Text(
            text = "Arreglos compartidos (${list.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
        }

        if (list.isEmpty()) {
          item {
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.CloudDone,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(36.dp),
                )
                Text(
                  text = "Sin arreglos compartidos aún",
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = "Crea un nuevo arreglo compartido o toca \"Unirse\" para escribir el código que te enviaron. También puedes subir un borrador local desde la pestaña Local.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        } else {
          items(list, key = { it.id }) { item ->
            SharedArregloCard(
              item = item,
              isOwner = item.ownerId == currentUserId,
              onEdit = { onOpenShared(item) },
              onPreview = { onPreviewShared(item) },
              onInvite = { inviteItem = item },
              onCopyCode = { copyCodeToClipboard(context, item.id) },
              onDeleteOrLeave = { deleteOrLeaveItem = item },
            )
          }
        }
      }
    }
  }

  if (showJoinDialog) {
    JoinByCodeDialog(
      isBusy = isActionBusy,
      errorMessage = actionError,
      onDismiss = {
        onClearError()
        showJoinDialog = false
      },
      onConfirmCode = { code ->
        onJoinByCode(code) {
          showJoinDialog = false
        }
      },
    )
  }

  inviteItem?.let { item ->
    ShareCollaboratorDialog(
      code = item.id,
      congregacion = item.congregacion,
      memberNames = item.memberNames,
      onDismiss = { inviteItem = null },
    )
  }

  deleteOrLeaveItem?.let { item ->
    val isOwner = item.ownerId == currentUserId
    AlertDialog(
      onDismissRequest = { deleteOrLeaveItem = null },
      title = { Text(if (isOwner) "Eliminar arreglo compartido" else "Salir del arreglo") },
      text = {
        val label = item.congregacion.ifBlank { item.id }
        Text(
          if (isOwner) {
            "¿Seguro que deseas eliminar \"$label\" para todos los colaboradores?"
          } else {
            "¿Deseas salir del arreglo \"$label\"? Podrás volver a entrar luego con el código ${item.id}."
          },
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteOrLeaveShared(item)
            deleteOrLeaveItem = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
          ),
        ) {
          Text(if (isOwner) "Eliminar" else "Salir")
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteOrLeaveItem = null }) {
          Text("Cancelar")
        }
      },
    )
  }
}

@Composable
private fun SharedArregloCard(
  item: SharedArregloSummary,
  isOwner: Boolean,
  onEdit: () -> Unit,
  onPreview: () -> Unit,
  onInvite: () -> Unit,
  onCopyCode: () -> Unit,
  onDeleteOrLeave: () -> Unit,
) {
  val updatedText = remember(item.updatedAt, item.createdAt) {
    val date = (item.updatedAt ?: item.createdAt)?.toDate()
    if (date != null) {
      SimpleDateFormat("d MMM · h:mm a", Locale.forLanguageTag("es-ES")).format(date)
    } else {
      "Reciente"
    }
  }

  Card(
    onClick = onEdit,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("shared_card_${item.id}"),
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
            text = item.congregacion.ifBlank { "Congregación sin definir" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = buildString {
              append(if (item.fechaVisita.isNotBlank()) "Visita: ${item.fechaVisita}" else "Fecha pendiente")
              append(" · $updatedText")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Row {
          IconButton(onClick = onInvite) {
            Icon(
              imageVector = Icons.Filled.Share,
              contentDescription = "Invitar colaborador",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
          IconButton(onClick = onDeleteOrLeave) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = if (isOwner) "Eliminar arreglo" else "Salir del arreglo",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          onClick = onCopyCode,
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(8.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Código: ${item.id}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Filled.ContentCopy,
              contentDescription = "Copiar código",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(14.dp),
            )
          }
        }

        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(8.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.People,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${item.memberUids.size} participante(s)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }

      if (item.lastEditedByName.isNotBlank()) {
        Text(
          text = "Última edición por ${item.lastEditedByName} · ${item.memberNames.values.joinToString(", ")}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
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
            .weight(0.85f)
            .height(48.dp),
        ) {
          Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Editar juntos", maxLines = 1)
        }

        Button(
          onClick = onPreview,
          colors = ButtonDefaults.buttonColors(
            containerColor = WhatsAppGreen,
            contentColor = Color.White,
          ),
          contentPadding = PaddingValues(horizontal = 10.dp),
          modifier = Modifier
            .weight(1.15f)
            .height(48.dp),
        ) {
          Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Ver / Enviar PDF", maxLines = 1)
        }
      }
    }
  }
}
