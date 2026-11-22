package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WhatsAppGreen
import com.example.util.ApkExporter
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ShareAppApkDialog(onDismiss: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var isBusy by remember { mutableStateOf(false) }
  var pendingSaveFile by remember { mutableStateOf<File?>(null) }

  val saveApkLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive"),
  ) { uri ->
    val file = pendingSaveFile
    if (uri != null && file != null) {
      scope.launch {
        isBusy = true
        val ok = withContext(Dispatchers.IO) {
          runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
              file.inputStream().use { input -> input.copyTo(out) }
            }
          }.isSuccess
        }
        isBusy = false
        Toast.makeText(
          context,
          if (ok) "APK guardado correctamente" else "No se pudo guardar el archivo APK",
          Toast.LENGTH_SHORT,
        ).show()
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Filled.InstallMobile,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
    },
    title = { Text("Compartir o guardar APK") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Puedes enviar el instalador (Arreglos_Visita.apk) a tu colaborador por WhatsApp o guardarlo en tu teléfono como respaldo.",
          style = MaterialTheme.typography.bodyMedium,
        )

        Button(
          onClick = {
            scope.launch {
              isBusy = true
              runCatching { ApkExporter.prepareExportableApk(context) }
                .onSuccess { file ->
                  isBusy = false
                  ApkExporter.shareApk(context, file, preferWhatsApp = true)
                }
                .onFailure { err ->
                  isBusy = false
                  Toast.makeText(
                    context,
                    err.localizedMessage ?: "No se pudo preparar el APK",
                    Toast.LENGTH_SHORT,
                  ).show()
                }
            }
          },
          enabled = !isBusy,
          colors = ButtonDefaults.buttonColors(
            containerColor = WhatsAppGreen,
            contentColor = Color.White,
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("send_apk_whatsapp_button"),
        ) {
          if (isBusy) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              color = Color.White,
              strokeWidth = 2.dp,
            )
          } else {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enviar APK por WhatsApp")
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = {
              scope.launch {
                isBusy = true
                runCatching { ApkExporter.prepareExportableApk(context) }
                  .onSuccess { file ->
                    isBusy = false
                    ApkExporter.shareApk(context, file, preferWhatsApp = false)
                  }
                  .onFailure { err ->
                    isBusy = false
                    Toast.makeText(
                      context,
                      err.localizedMessage ?: "No se pudo preparar el APK",
                      Toast.LENGTH_SHORT,
                    ).show()
                  }
              }
            },
            enabled = !isBusy,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("share_apk_generic_button"),
          ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Compartir", maxLines = 1)
          }

          OutlinedButton(
            onClick = {
              scope.launch {
                isBusy = true
                runCatching { ApkExporter.prepareExportableApk(context) }
                  .onSuccess { file ->
                    pendingSaveFile = file
                    isBusy = false
                    saveApkLauncher.launch("Arreglos_Visita.apk")
                  }
                  .onFailure { err ->
                    isBusy = false
                    Toast.makeText(
                      context,
                      err.localizedMessage ?: "No se pudo preparar el APK",
                      Toast.LENGTH_SHORT,
                    ).show()
                  }
              }
            },
            enabled = !isBusy,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("save_apk_button"),
          ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Guardar", maxLines = 1)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Cerrar")
      }
    },
  )
}
