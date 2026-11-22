package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ApkExporter {
  private const val APK_MIME = "application/vnd.android.package-archive"
  private const val EXPORT_FILENAME = "Arreglos_Visita.apk"

  suspend fun prepareExportableApk(context: Context): File = withContext(Dispatchers.IO) {
    val sourcePath = context.applicationInfo.publicSourceDir
      ?: context.applicationInfo.sourceDir
      ?: error("No se encontró el archivo APK instalado en este dispositivo.")

    val sourceFile = File(sourcePath)
    require(sourceFile.exists()) { "No se pudo leer el archivo APK instalado." }

    val targetDir = File(context.cacheDir, "apks").apply { mkdirs() }
    val targetFile = File(targetDir, EXPORT_FILENAME)
    sourceFile.inputStream().use { input ->
      targetFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    targetFile
  }

  fun shareApk(context: Context, apkFile: File, preferWhatsApp: Boolean) {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      apkFile,
    )
    val baseIntent = Intent(Intent.ACTION_SEND).apply {
      type = APK_MIME
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(
        Intent.EXTRA_TEXT,
        "Te comparto el instalador (APK) de Arreglos Visita para Android.",
      )
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    if (preferWhatsApp) {
      val whatsappIntent = Intent(baseIntent).apply { `package` = "com.whatsapp" }
      if (whatsappIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(whatsappIntent)
        return
      }
      val businessIntent = Intent(baseIntent).apply { `package` = "com.whatsapp.w4b" }
      if (businessIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(businessIntent)
        return
      }
      Toast.makeText(
        context,
        "WhatsApp no está disponible aquí; elige otra app para compartir el APK.",
        Toast.LENGTH_SHORT,
      ).show()
    }

    context.startActivity(Intent.createChooser(baseIntent, "Compartir archivo APK"))
  }
}
