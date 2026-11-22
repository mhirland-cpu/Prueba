package com.example.pdf

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import com.example.model.VisitaFormData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfPreviewHelper {
  fun createDraftPdfFile(context: Context, formData: VisitaFormData): File {
    val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
    val rawName = buildString {
      append("Arreglos_Visita")
      val cong = formData.actividades.congregacion.trim()
      if (cong.isNotBlank()) {
        append("_")
        append(cong.replace(Regex("[^A-Za-z0-9áéíóúÁÉÍÓÚñÑ]+"), "_").trim('_'))
      } else {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        append("_$stamp")
      }
    }
    val outFile = File(dir, "$rawName.pdf")
    return PdfFormGenerator.generatePdfFile(context, formData, outFile)
  }

  fun renderPdfPages(file: File, renderWidthPx: Int = 1224): List<Bitmap> {
    val bitmaps = mutableListOf<Bitmap>()
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
      PdfRenderer(pfd).use { renderer ->
        for (i in 0 until renderer.pageCount) {
          renderer.openPage(i).use { page ->
            val aspect = page.height.toFloat() / page.width.toFloat()
            val heightPx = (renderWidthPx * aspect).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(renderWidthPx, heightPx, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
          }
        }
      }
    }
    return bitmaps
  }

  fun getShareUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

  fun shareToWhatsApp(context: Context, pdfFile: File, congregacion: String, fecha: String) {
    val uri = getShareUri(context, pdfFile)
    val caption = buildString {
      append("Arreglos para la visita del superintendente de circuito")
      if (congregacion.isNotBlank()) append(" — Congregación $congregacion")
      if (fecha.isNotBlank()) append(" ($fecha)")
    }

    val whatsAppPackage = findInstalledWhatsAppPackage(context)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_TEXT, caption)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      if (whatsAppPackage != null) {
        setPackage(whatsAppPackage)
      }
    }

    if (whatsAppPackage != null) {
      context.startActivity(sendIntent)
    } else {
      val chooser = Intent.createChooser(sendIntent, "Enviar formulario PDF")
      context.startActivity(chooser)
    }
  }

  fun shareGeneric(context: Context, pdfFile: File, congregacion: String) {
    val uri = getShareUri(context, pdfFile)
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(
        Intent.EXTRA_SUBJECT,
        if (congregacion.isNotBlank()) "Arreglos Visita - $congregacion" else "Arreglos Visita",
      )
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
  }

  private fun findInstalledWhatsAppPackage(context: Context): String? {
    val pm = context.packageManager
    val candidates = listOf("com.whatsapp", "com.whatsapp.w4b")
    for (pkg in candidates) {
      val installed = runCatching {
        pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
      }.isSuccess
      if (installed) return pkg
    }
    return null
  }
}
