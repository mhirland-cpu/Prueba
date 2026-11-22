package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.firebase.onGoogleSignInClicked
import com.example.ui.theme.WhatsAppGreen

@Composable
fun GoogleSignInCard(
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier = Modifier
          .size(60.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.People,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp),
        )
      }

      Text(
        text = "Trabaja en equipo en tiempo real",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "Inicia sesión con Google para crear un arreglo compartido con un código de invitación. Así una persona llena una parte y otra persona llena la otra parte al mismo tiempo.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (errorMsg != null) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = errorMsg.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
          )
        }
      }

      Button(
        onClick = {
          errorMsg = null
          isLoading = true
          onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
              isLoading = false
              onAuthSuccess()
            },
            onAuthError = { msg ->
              isLoading = false
              errorMsg = msg
            },
            scope = scope,
            onAuthCancelled = {
              isLoading = false
            },
          )
        },
        enabled = !isLoading,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("google_sign_in_button"),
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp,
          )
        } else {
          Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Iniciar sesión con Google")
        }
      }
    }
  }
}

@Composable
fun JoinByCodeDialog(
  isBusy: Boolean,
  errorMessage: String?,
  onDismiss: () -> Unit,
  onConfirmCode: (String) -> Unit,
) {
  var code by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Filled.GroupAdd,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
    },
    title = { Text("Unirse a un arreglo") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Escribe el código de 8 caracteres que te compartieron para editar el mismo formulario juntos.",
          style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
          value = code,
          onValueChange = { input ->
            code = input.uppercase().filter { it.isLetterOrDigit() || it == '-' || it == '_' }.take(20)
          },
          label = { Text("Código compartido") },
          placeholder = { Text("Ej: AR7K4M9P") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("join_code_input"),
        )
        if (!errorMessage.isNullOrBlank()) {
          Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmCode(code) },
        enabled = !isBusy && code.trim().length >= 6,
        modifier = Modifier.testTag("confirm_join_code_button"),
      ) {
        if (isBusy) {
          CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp,
          )
        } else {
          Text("Unirse")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    },
  )
}

@Composable
fun ShareCollaboratorDialog(
  code: String,
  congregacion: String,
  memberNames: Map<String, String>,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Filled.CloudDone,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
    },
    title = { Text("Invitar colaborador") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Comparte este código con la otra persona para que abra la sección \"En línea\" y llene su parte del formulario:",
          style = MaterialTheme.typography.bodyMedium,
        )

        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Código del arreglo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Text(
                text = code,
                style = MaterialTheme.typography.headlineSmall.copy(
                  letterSpacing = 2.sp,
                  fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
            OutlinedButton(
              onClick = {
                copyCodeToClipboard(context, code)
              },
              modifier = Modifier.testTag("copy_invite_code_button"),
            ) {
              Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Copiar")
            }
          }
        }

        if (memberNames.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Participantes activos (${memberNames.size}):",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = memberNames.values.joinToString(" · "),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        Button(
          onClick = {
            shareCodeViaWhatsAppOrSystem(context, code, congregacion)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = WhatsAppGreen,
            contentColor = Color.White,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("share_code_whatsapp_button"),
        ) {
          Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Enviar código por WhatsApp")
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Listo")
      }
    },
  )
}

fun copyCodeToClipboard(context: Context, code: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  clipboard?.setPrimaryClip(ClipData.newPlainText("Código Arreglo Visita", code))
  Toast.makeText(context, "Código $code copiado", Toast.LENGTH_SHORT).show()
}

fun shareCodeViaWhatsAppOrSystem(context: Context, code: String, congregacion: String) {
  val label = if (congregacion.isBlank()) "nuestro arreglo de visita" else "el arreglo de la congregación $congregacion"
  val message = "Te invito a llenar juntos $label en Arreglos Visita.\n\nCódigo para unirte: $code"
  val sendIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, message)
  }
  val whatsappIntent = Intent(sendIntent).apply { `package` = "com.whatsapp" }
  if (whatsappIntent.resolveActivity(context.packageManager) != null) {
    context.startActivity(whatsappIntent)
  } else {
    context.startActivity(Intent.createChooser(sendIntent, "Compartir código de invitación"))
  }
}
