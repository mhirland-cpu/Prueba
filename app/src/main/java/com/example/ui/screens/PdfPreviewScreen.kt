package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.VisitaFormData
import com.example.pdf.PdfPreviewHelper
import com.example.ui.PdfRenderState
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
  formData: VisitaFormData,
  pdfState: PdfRenderState,
  onRetry: () -> Unit,
  onEditForm: () -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  val context = LocalContext.current
  val readyFile = (pdfState as? PdfRenderState.Ready)?.file
  var zoomScale by remember { mutableFloatStateOf(1f) }

  val saveDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/pdf"),
  ) { uri ->
    if (uri != null && readyFile != null) {
      val saved = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { out ->
          readyFile.inputStream().use { input -> input.copyTo(out) }
        }
      }.isSuccess
      Toast.makeText(
        context,
        if (saved) "PDF guardado correctamente" else "No se pudo guardar el archivo",
        Toast.LENGTH_SHORT,
      ).show()
    }
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("preview_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Volver",
            )
          }
        },
        title = {
          Column {
            Text(
              text = "Vista previa PDF (4 páginas)",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = formData.actividades.congregacion.ifBlank { "Documento listo para WhatsApp" },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        actions = {
          if (pdfState is PdfRenderState.Ready) {
            IconButton(
              onClick = { zoomScale = if (zoomScale > 1.1f) 1f else 1.65f },
              modifier = Modifier.testTag("preview_zoom_button"),
            ) {
              Icon(
                imageVector = if (zoomScale > 1.1f) Icons.Filled.ZoomOut else Icons.Filled.ZoomIn,
                contentDescription = if (zoomScale > 1.1f) "Alejar vista" else "Acercar vista",
              )
            }
          }
          IconButton(
            onClick = onEditForm,
            modifier = Modifier.testTag("preview_edit_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = "Seguir editando",
            )
          }
        },
      )
    },
    bottomBar = {
      if (pdfState is PdfRenderState.Ready) {
        Surface(
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 6.dp,
          shadowElevation = 8.dp,
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = {
                PdfPreviewHelper.shareToWhatsApp(
                  context = context,
                  pdfFile = pdfState.file,
                  congregacion = formData.actividades.congregacion,
                  fecha = formData.actividades.fecha,
                )
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = WhatsAppGreen,
                contentColor = Color.White,
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("send_whatsapp_button"),
            ) {
              Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Enviar por WhatsApp")
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = {
                  PdfPreviewHelper.shareGeneric(
                    context = context,
                    pdfFile = pdfState.file,
                    congregacion = formData.actividades.congregacion,
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("share_generic_button"),
              ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compartir")
              }

              OutlinedButton(
                onClick = { saveDocumentLauncher.launch(pdfState.file.name) },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("save_pdf_file_button"),
              ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar PDF")
              }
            }
          }
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.Center,
    ) {
      when (pdfState) {
        is PdfRenderState.Idle,
        is PdfRenderState.Generating,
        -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CircularProgressIndicator()
            Text(
              text = "Generando documento PDF exacto...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        is PdfRenderState.Error -> {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = pdfState.message,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry) {
              Icon(Icons.Filled.Refresh, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Reintentar")
            }
          }
        }

        is PdfRenderState.Ready -> {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .widthIn(max = 760.dp)
              .let { mod ->
                if (zoomScale > 1.1f) mod.horizontalScroll(rememberScrollState()) else mod
              },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            itemsIndexed(pdfState.pages) { index, bitmap ->
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "Página ${index + 1} de ${pdfState.pages.size}",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Card(
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                  colors = CardDefaults.cardColors(containerColor = Color.White),
                  elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                  modifier = if (zoomScale > 1.1f) {
                    Modifier.width((360 * zoomScale).dp)
                  } else {
                    Modifier.fillMaxWidth()
                  },
                ) {
                  Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Página ${index + 1} del formulario PDF",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                      .fillMaxWidth()
                      .aspectRatio(612f / 792f)
                      .background(Color.White),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
