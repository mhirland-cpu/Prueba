package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.StyleSpan
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.example.model.DaySchedule
import com.example.model.ServiceOnlyDay
import com.example.model.ServicePastoringDay
import com.example.model.VisitaFormData
import java.io.File
import java.io.FileOutputStream

object PdfFormGenerator {
  private const val PAGE_WIDTH = 612
  private const val PAGE_HEIGHT = 792
  private const val LEFT = 36f
  private const val RIGHT = 576f
  private const val CONTENT_WIDTH = RIGHT - LEFT

  private val COLOR_PEACH = Color.rgb(251, 229, 214)
  private val COLOR_BLUE_HEADER = Color.rgb(217, 226, 243)
  private val COLOR_CRIMSON = Color.rgb(192, 0, 0)
  private val COLOR_GRAY_HEADER = Color.rgb(127, 127, 127)
  private val COLOR_GRAY_CELL = Color.rgb(217, 217, 217)

  fun generatePdfFile(context: Context, formData: VisitaFormData, outputFile: File): File {
    outputFile.parentFile?.mkdirs()
    val document = PdfDocument()

    try {
      for (pageNumber in 1..4) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        drawPage(pageNumber, page.canvas, formData)
        document.finishPage(page)
      }

      FileOutputStream(outputFile).use { out ->
        document.writeTo(out)
      }
    } finally {
      document.close()
    }

    return outputFile
  }

  internal fun drawPage(pageNumber: Int, canvas: Canvas, formData: VisitaFormData) {
    when (pageNumber) {
      1 -> drawPage1(canvas, formData)
      2 -> drawPage2(canvas, formData)
      3 -> drawPage3(canvas, formData)
      4 -> drawPage4(canvas, formData)
    }
  }

  private fun createPaints(): PdfPaints = PdfPaints()

  private class PdfPaints {
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      style = Paint.Style.STROKE
      strokeWidth = 0.75f
    }

    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
    }

    val title = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 14f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }

    val dayBannerText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 10.5f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
      textAlign = Paint.Align.CENTER
    }

    val labelBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 10f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    val labelBoldItalic = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 10f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
    }

    val labelRedBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = COLOR_CRIMSON
      textSize = 10f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    val regular = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 9.5f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    val smallValue = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 9f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    val bodyPara = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 9.5f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    val whiteHeader = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = 9.5f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }

    val cellCenteredBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 9.5f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }

    val footer = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textSize = 8f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
      textAlign = Paint.Align.RIGHT
    }
  }

  private fun drawPage1(canvas: Canvas, formData: VisitaFormData) {
    val p = createPaints()
    val a = formData.actividades

    // Title
    canvas.drawText(
      "Actividades durante la visita del superintendente de circuito",
      PAGE_WIDTH / 2f,
      44f,
      p.title,
    )

    // Fecha / Congregación bar
    val topBarTop = 58f
    val topBarBottom = 76f
    val splitX = 234f
    fillRect(canvas, LEFT, topBarTop, RIGHT, topBarBottom, COLOR_PEACH, p)
    canvas.drawLine(splitX, topBarTop, splitX, topBarBottom, p.stroke)
    drawLabelAndValue(
      canvas = canvas,
      label = "Fecha:",
      value = a.fecha,
      x = LEFT + 5f,
      baselineY = topBarTop + 13f,
      maxRight = splitX - 4f,
      labelPaint = p.labelBold,
      valuePaint = p.regular,
    )
    drawLabelAndValue(
      canvas = canvas,
      label = "Congregación:",
      value = a.congregacion,
      x = splitX + 5f,
      baselineY = topBarTop + 13f,
      maxRight = RIGHT - 4f,
      labelPaint = p.labelBold,
      valuePaint = p.regular,
    )

    val extraCount = listOf(
      a.miercoles.reunionPrecursoresEnabled,
      a.jueves.reunionPrecursoresEnabled,
      a.viernes.reunionPrecursoresEnabled,
      a.sabado.reunionPrecursoresEnabled,
      a.domingoReunionPrecursoresEnabled,
    ).count { it }
    val dayGap = when {
      extraCount >= 4 -> 6f
      extraCount >= 2 -> 9f
      else -> 13f
    }

    var y = 88f

    // Miércoles
    y = drawStandardDayPage1(
      canvas = canvas,
      p = p,
      startY = y,
      dayName = "Miércoles",
      schedule = a.miercoles,
      extraMeetings = buildList {
        if (a.miercoles.reunionPrecursoresEnabled) {
          add(Triple("Reunión de precursores", 200f, a.miercoles.reunionPrecursoresLugar to a.miercoles.reunionPrecursoresHora))
        }
      },
    )

    y += dayGap

    // Jueves
    y = drawStandardDayPage1(
      canvas = canvas,
      p = p,
      startY = y,
      dayName = "Jueves",
      schedule = a.jueves,
      extraMeetings = buildList {
        if (a.jueves.reunionPrecursoresEnabled) {
          add(Triple("Reunión de precursores", 200f, a.jueves.reunionPrecursoresLugar to a.jueves.reunionPrecursoresHora))
        }
      },
    )

    y += dayGap

    // Viernes
    y = drawStandardDayPage1(
      canvas = canvas,
      p = p,
      startY = y,
      dayName = "Viernes",
      schedule = a.viernes,
      extraMeetings = buildList {
        if (a.viernes.reunionPrecursoresEnabled) {
          add(Triple("Reunión de precursores", 200f, a.viernes.reunionPrecursoresLugar to a.viernes.reunionPrecursoresHora))
        }
        add(Triple("Reunión ancianos, ministeriales", 232f, a.reunionAncianosLugar to a.reunionAncianosHora))
      },
    )

    y += dayGap

    // Sábado
    y = drawStandardDayPage1(
      canvas = canvas,
      p = p,
      startY = y,
      dayName = "Sábado",
      schedule = a.sabado,
      extraMeetings = buildList {
        if (a.sabado.reunionPrecursoresEnabled) {
          add(Triple("Reunión de precursores", 200f, a.sabado.reunionPrecursoresLugar to a.sabado.reunionPrecursoresHora))
        }
      },
    )

    y += dayGap

    // Domingo
    y = drawDomingoPage1(
      canvas = canvas,
      p = p,
      startY = y,
      salidaLugar = a.domingoSalidaAmLugar,
      salidaHora = a.domingoSalidaAmHora,
      almuerzoFamilia = a.domingoAlmuerzoFamilia,
      almuerzoTel = a.domingoAlmuerzoTel,
      reunionPrecursoresEnabled = a.domingoReunionPrecursoresEnabled,
      reunionPrecursoresLugar = a.domingoReunionPrecursoresLugar,
      reunionPrecursoresHora = a.domingoReunionPrecursoresHora,
    )

    // Reuniones de congregación at bottom
    val meetingsTop = minOf(maxOf(y + 18f, 650f), 710f)
    val rowH = 19f
    val col1Right = 296f
    val col2Right = 462f

    // Header
    fillRect(canvas, LEFT, meetingsTop, RIGHT, meetingsTop + rowH, COLOR_PEACH, p)
    canvas.drawText(
      "Reuniones de congregación",
      PAGE_WIDTH / 2f,
      meetingsTop + 13.5f,
      p.dayBannerText,
    )

    // Row 1: Vida y Ministerio
    val r1Top = meetingsTop + rowH
    val r1Bot = r1Top + rowH
    drawRectStroke(canvas, LEFT, r1Top, RIGHT, r1Bot, p)
    canvas.drawLine(col1Right, r1Top, col1Right, r1Bot, p.stroke)
    canvas.drawLine(col2Right, r1Top, col2Right, r1Bot, p.stroke)
    canvas.drawText("Vida y Ministerio Cristianos", LEFT + 5f, r1Top + 13f, p.labelBoldItalic)
    drawLabelAndValue(
      canvas,
      "Día:",
      a.reunionVidaMinisterioDia,
      col1Right + 5f,
      r1Top + 13f,
      col2Right - 4f,
      p.regular,
      p.regular,
    )
    drawLabelAndValue(
      canvas,
      "Hora:",
      a.reunionVidaMinisterioHora,
      col2Right + 5f,
      r1Top + 13f,
      RIGHT - 4f,
      p.regular,
      p.regular,
    )

    // Row 2: Discurso público
    val r2Top = r1Bot
    val r2Bot = r2Top + rowH
    drawRectStroke(canvas, LEFT, r2Top, RIGHT, r2Bot, p)
    canvas.drawLine(col1Right, r2Top, col1Right, r2Bot, p.stroke)
    canvas.drawLine(col2Right, r2Top, col2Right, r2Bot, p.stroke)
    canvas.drawText("Discurso público y estudio de La Atalaya", LEFT + 5f, r2Top + 13f, p.labelBoldItalic)
    drawLabelAndValue(
      canvas,
      "Día:",
      a.reunionDiscursoAtalayaDia,
      col1Right + 5f,
      r2Top + 13f,
      col2Right - 4f,
      p.regular,
      p.regular,
    )
    drawLabelAndValue(
      canvas,
      "Hora:",
      a.reunionDiscursoAtalayaHora,
      col2Right + 5f,
      r2Top + 13f,
      RIGHT - 4f,
      p.regular,
      p.regular,
    )
  }

  private fun drawStandardDayPage1(
    canvas: Canvas,
    p: PdfPaints,
    startY: Float,
    dayName: String,
    schedule: DaySchedule,
    extraMeetings: List<Triple<String, Float, Pair<String, String>>>,
  ): Float {
    var y = startY
    val bannerH = 17.5f
    fillRect(canvas, LEFT, y, RIGHT, y + bannerH, COLOR_BLUE_HEADER, p)
    canvas.drawText(dayName, PAGE_WIDTH / 2f, y + 12.5f, p.dayBannerText)
    y += bannerH + 5f

    val boxLeft = 122f
    val horaDividerX = 485f
    val rowH = 16f

    // Salida am
    canvas.drawText("Salida am", LEFT, y + 11.5f, p.labelBold)
    drawRectStroke(canvas, boxLeft, y, RIGHT, y + rowH, p)
    canvas.drawLine(horaDividerX, y, horaDividerX, y + rowH, p.stroke)
    drawLabelAndValue(canvas, "Lugar:", schedule.salidaAmLugar, boxLeft + 5f, y + 11.5f, horaDividerX - 4f, p.regular, p.regular)
    drawLabelAndValue(canvas, "Hora:", schedule.salidaAmHora, horaDividerX + 5f, y + 11.5f, RIGHT - 4f, p.regular, p.regular)
    y += rowH

    // Salida pm
    canvas.drawText("Salida pm", LEFT, y + 11.5f, p.labelBold)
    drawRectStroke(canvas, boxLeft, y, RIGHT, y + rowH, p)
    canvas.drawLine(horaDividerX, y, horaDividerX, y + rowH, p.stroke)
    drawLabelAndValue(canvas, "Lugar:", schedule.salidaPmLugar, boxLeft + 5f, y + 11.5f, horaDividerX - 4f, p.regular, p.regular)
    drawLabelAndValue(canvas, "Hora:", schedule.salidaPmHora, horaDividerX + 5f, y + 11.5f, RIGHT - 4f, p.regular, p.regular)
    y += rowH + 4f

    // Almuerzo
    canvas.drawText("Almuerzo", LEFT, y + 11.5f, p.labelBold)
    drawRectStroke(canvas, boxLeft, y, RIGHT, y + rowH, p)
    drawSingleLineClipped(canvas, schedule.almuerzoFamilia, boxLeft + 5f, y + 11.5f, 444f, p.smallValue)
    drawLabelAndValue(canvas, "Tel.", schedule.almuerzoTel, 450f, y + 11.5f, RIGHT - 4f, p.regular, p.smallValue)
    y += rowH

    extraMeetings.forEach { (label, extraBoxLeft, lugarHora) ->
      y += 4.5f
      canvas.drawText(label, LEFT, y + 11.5f, p.labelRedBold)
      drawRectStroke(canvas, extraBoxLeft, y, RIGHT, y + rowH, p)
      canvas.drawLine(horaDividerX, y, horaDividerX, y + rowH, p.stroke)
      drawLabelAndValue(canvas, "Lugar:", lugarHora.first, extraBoxLeft + 5f, y + 11.5f, horaDividerX - 4f, p.regular, p.regular)
      drawLabelAndValue(canvas, "Hora:", lugarHora.second, horaDividerX + 5f, y + 11.5f, RIGHT - 4f, p.regular, p.regular)
      y += rowH
    }

    return y
  }

  private fun drawDomingoPage1(
    canvas: Canvas,
    p: PdfPaints,
    startY: Float,
    salidaLugar: String,
    salidaHora: String,
    almuerzoFamilia: String,
    almuerzoTel: String,
    reunionPrecursoresEnabled: Boolean,
    reunionPrecursoresLugar: String,
    reunionPrecursoresHora: String,
  ): Float {
    var y = startY
    val bannerH = 17.5f
    fillRect(canvas, LEFT, y, RIGHT, y + bannerH, COLOR_BLUE_HEADER, p)
    canvas.drawText("Domingo", PAGE_WIDTH / 2f, y + 12.5f, p.dayBannerText)
    y += bannerH + 5f

    val boxLeft = 122f
    val horaDividerX = 485f
    val rowH = 16f

    // Salida am:
    canvas.drawText("Salida am:", LEFT, y + 11.5f, p.labelBold)
    drawRectStroke(canvas, boxLeft, y, RIGHT, y + rowH, p)
    canvas.drawLine(horaDividerX, y, horaDividerX, y + rowH, p.stroke)
    drawLabelAndValue(canvas, "Lugar:", salidaLugar, boxLeft + 5f, y + 11.5f, horaDividerX - 4f, p.regular, p.regular)
    drawLabelAndValue(canvas, "Hora:", salidaHora, horaDividerX + 5f, y + 11.5f, RIGHT - 4f, p.regular, p.regular)
    y += rowH + 4f

    // Almuerzo:
    canvas.drawText("Almuerzo:", LEFT, y + 11.5f, p.labelBold)
    drawRectStroke(canvas, boxLeft, y, RIGHT, y + rowH, p)
    drawSingleLineClipped(canvas, almuerzoFamilia, boxLeft + 5f, y + 11.5f, 444f, p.smallValue)
    drawLabelAndValue(canvas, "Tel.", almuerzoTel, 450f, y + 11.5f, RIGHT - 4f, p.regular, p.smallValue)
    y += rowH

    if (reunionPrecursoresEnabled) {
      y += 4.5f
      canvas.drawText("Reunión de precursores", LEFT, y + 11.5f, p.labelRedBold)
      drawRectStroke(canvas, 200f, y, RIGHT, y + rowH, p)
      canvas.drawLine(horaDividerX, y, horaDividerX, y + rowH, p.stroke)
      drawLabelAndValue(canvas, "Lugar:", reunionPrecursoresLugar, 200f + 5f, y + 11.5f, horaDividerX - 4f, p.regular, p.regular)
      drawLabelAndValue(canvas, "Hora:", reunionPrecursoresHora, horaDividerX + 5f, y + 11.5f, RIGHT - 4f, p.regular, p.regular)
      y += rowH
    }

    return y
  }

  private fun drawPage2(canvas: Canvas, formData: VisitaFormData) {
    val p = createPaints()
    val s = formData.servicioPastoreo
    val personName = s.nombrePersona.ifBlank { "Obed Paulino" }

    canvas.drawText(
      "Arreglos de servicio/pastoreo para $personName",
      PAGE_WIDTH / 2f,
      54f,
      p.title,
    )

    var y = 76f
    y = drawServicePastoringDay(canvas, p, y, "Miércoles", s.miercoles)
    y += 14f
    y = drawServicePastoringDay(canvas, p, y, "Jueves", s.jueves)
    y += 14f
    y = drawServicePastoringDay(canvas, p, y, "Viernes", s.viernes)

    y += 22f
    canvas.drawText("Notas adicionales:", LEFT, y, p.labelBoldItalic)
    val notesTop = y + 12f
    val notesBottom = 716f
    drawRectStroke(canvas, LEFT, notesTop, RIGHT, notesBottom, p)
    if (s.notasAdicionales.isNotBlank()) {
      drawWrappedTextInBox(
        canvas = canvas,
        text = s.notasAdicionales,
        left = LEFT + 8f,
        top = notesTop + 8f,
        right = RIGHT - 8f,
        bottom = notesBottom - 8f,
        paint = p.bodyPara,
      )
    }
  }

  private fun drawServicePastoringDay(
    canvas: Canvas,
    p: PdfPaints,
    startY: Float,
    dayName: String,
    day: ServicePastoringDay,
  ): Float {
    var y = startY
    val bannerH = 17.5f
    fillRect(canvas, LEFT, y, RIGHT, y + bannerH, COLOR_BLUE_HEADER, p)
    canvas.drawText(dayName, PAGE_WIDTH / 2f, y + 12f, p.dayBannerText)
    y += bannerH + 4f

    val boxLeft = 158f
    val telDividerX = 396f
    val rowH = 17f

    val rows = listOf(
      Triple("1ra hora con:", true, day.hora1Con to day.hora1Tel),
      Triple("Estudio/Pastoreo:", false, day.hora1EstudioPastoreo to ""),
      Triple("Publicación:", false, day.hora1Publicacion to ""),
      Triple("2da hora con:", true, day.hora2Con to day.hora2Tel),
      Triple("Estudio/Pastoreo:", false, day.hora2EstudioPastoreo to ""),
      Triple("Publicación:", false, day.hora2Publicacion to ""),
    )

    val tableTop = y
    rows.forEachIndexed { index, (label, hasTel, values) ->
      val rTop = tableTop + index * rowH
      val rBot = rTop + rowH
      canvas.drawText(label, LEFT, rTop + 12f, if (hasTel) p.labelBold else p.regular)
      drawRectStroke(canvas, boxLeft, rTop, RIGHT, rBot, p)
      if (hasTel) {
        canvas.drawLine(telDividerX, rTop, telDividerX, rBot, p.stroke)
        drawSingleLineClipped(canvas, values.first, boxLeft + 5f, rTop + 12f, telDividerX - 4f, p.smallValue)
        drawLabelAndValue(canvas, "Tel.", values.second, telDividerX + 5f, rTop + 12f, RIGHT - 4f, p.regular, p.smallValue)
      } else {
        drawSingleLineClipped(canvas, values.first, boxLeft + 5f, rTop + 12f, RIGHT - 4f, p.smallValue)
      }
    }

    return tableTop + rows.size * rowH
  }

  private fun drawPage3(canvas: Canvas, formData: VisitaFormData) {
    val p = createPaints()
    val s = formData.servicioEsposa
    val personName = s.nombrePersona.ifBlank { "Mercedes Paulino" }

    canvas.drawText(
      "Arreglos de servicio para $personName",
      PAGE_WIDTH / 2f,
      54f,
      p.title,
    )

    var y = 76f
    y = drawServiceOnlyDay(canvas, p, y, "Miércoles", s.miercoles)
    y += 14f
    y = drawServiceOnlyDay(canvas, p, y, "Jueves", s.jueves)
    y += 14f
    y = drawServiceOnlyDay(canvas, p, y, "Viernes", s.viernes)

    y += 24f
    canvas.drawText("Notas adicionales:", LEFT, y, p.labelBoldItalic)
    val notesTop = y + 12f
    val notesBottom = 618f
    drawRectStroke(canvas, LEFT, notesTop, RIGHT, notesBottom, p)
    if (s.notasAdicionales.isNotBlank()) {
      drawWrappedTextInBox(
        canvas = canvas,
        text = s.notasAdicionales,
        left = LEFT + 8f,
        top = notesTop + 8f,
        right = RIGHT - 8f,
        bottom = notesBottom - 8f,
        paint = p.bodyPara,
      )
    }
  }

  private fun drawServiceOnlyDay(
    canvas: Canvas,
    p: PdfPaints,
    startY: Float,
    dayName: String,
    day: ServiceOnlyDay,
  ): Float {
    var y = startY
    val bannerH = 17.5f
    fillRect(canvas, LEFT, y, RIGHT, y + bannerH, COLOR_BLUE_HEADER, p)
    canvas.drawText(dayName, PAGE_WIDTH / 2f, y + 12f, p.dayBannerText)
    y += bannerH + 4f

    val boxLeft = 136f
    val telDividerX = 396f
    val rowH = 17f

    val rows = listOf(
      Triple("1ra hora con:", true, day.hora1Con to day.hora1Tel),
      Triple("Publicación:", false, day.hora1Publicacion to ""),
      Triple("2da hora con:", true, day.hora2Con to day.hora2Tel),
      Triple("Publicación:", false, day.hora2Publicacion to ""),
    )

    val tableTop = y
    rows.forEachIndexed { index, (label, hasTel, values) ->
      val rTop = tableTop + index * rowH
      val rBot = rTop + rowH
      canvas.drawText(label, LEFT, rTop + 12f, if (hasTel) p.labelBold else p.regular)
      drawRectStroke(canvas, boxLeft, rTop, RIGHT, rBot, p)
      if (hasTel) {
        canvas.drawLine(telDividerX, rTop, telDividerX, rBot, p.stroke)
        drawSingleLineClipped(canvas, values.first, boxLeft + 5f, rTop + 12f, telDividerX - 4f, p.smallValue)
        drawLabelAndValue(canvas, "Tel.", values.second, telDividerX + 5f, rTop + 12f, RIGHT - 4f, p.regular, p.smallValue)
      } else {
        drawSingleLineClipped(canvas, values.first, boxLeft + 5f, rTop + 12f, RIGHT - 4f, p.smallValue)
      }
    }

    return tableTop + rows.size * rowH
  }

  private fun drawPage4(canvas: Canvas, formData: VisitaFormData) {
    val p = createPaints()
    val nota = formData.notaPastoreo
    val rightEdge = 568f

    canvas.drawText("Nota al coordinador del cuerpo de ancianos:", LEFT, 48f, p.labelBold)

    var y = 64f
    val bulletX = 52f
    val textX = 66f
    val textWidth = (rightEdge - textX).toInt()

    val paragraphs = listOf(
      buildBulletSpan(
        lead = "Formulario S-61:",
        body = " Favor de seguir las instrucciones sobre los documentos a entregar al viajante. Necesito las s-21, los 6 meses de la contabilidad más recientes, el s-88, y el s-28. El s-61 ha de completarse respondiendo con un cotejo las preguntas que contiene. Favor de imprimir en físico o en PDF y enviar al viajante.",
      ),
      buildBulletSpan(
        lead = "Alojamiento.",
        body = " Favor de consultar el capítulo 15 del ",
        italicWord1 = "Pastoreen",
        body2 = " al respecto, además de repasar otros recordatorios oportunos.",
        boldItalicWord1 = true,
      ),
      buildBulletSpan(
        lead = "Recomendaciones.",
        body = " Recuerden enviar mediante ",
        italicWord1 = "jw.hub",
        body2 = " las recomendaciones de nuevos ancianos y siervos ministeriales. Esto ha de hacerse al menos un mes antes de la visita.",
      ),
      buildBulletSpan(
        lead = "Reuniones para el servicio del campo.",
        body = " Se celebrarán de miércoles a domingo a una hora acordada con los ancianos, tomando en cuenta las circunstancias locales.",
      ),
      buildBulletSpan(
        lead = "Medio día libre.",
        body = " Preferimos viernes en la tarde. Por supuesto, debido a circunstancias locales tal vez decidan que ese medio día libre sea en otro momento de la semana.",
      ),
      buildBulletSpan(
        lead = "Visitas de Pastoreo:",
        body = " Favor de completar la siguiente información. Planeen la visita a una hora que sea conveniente para el publicador, ya sea por la mañana, en la tarde o por la noche (",
        italicWord1 = "solo miércoles, si es necesario",
        body2 = "). En algunos casos, podrían llevarse a cabo en horas en las que la predicación de casa en casa sea menos eficaz.",
      ),
    )

    p.fill.color = Color.BLACK
    paragraphs.forEach { spanned ->
      // Draw solid square bullet
      canvas.drawRect(bulletX, y + 3f, bulletX + 5f, y + 8f, p.fill)
      val layout = StaticLayout.Builder.obtain(spanned, 0, spanned.length, p.bodyPara, textWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(1.5f, 1.0f)
        .setIncludePad(false)
        .build()
      canvas.withTranslation(textX, y) {
        layout.draw(this)
      }
      y += layout.height + 9f
    }

    y += 4f

    // Visitas de Pastoreo table
    val tableLeft = 76f
    val col1Right = 138f
    val col2Right = 250f
    val tableRight = rightEdge
    val headerH = 18f

    fillRect(canvas, tableLeft, y, tableRight, y + headerH, COLOR_GRAY_HEADER, p)
    canvas.drawLine(col1Right, y, col1Right, y + headerH, p.stroke)
    canvas.drawLine(col2Right, y, col2Right, y + headerH, p.stroke)
    canvas.drawText("Día", (tableLeft + col1Right) / 2f, y + 12.5f, p.whiteHeader)
    canvas.drawText("Persona o Familia", (col1Right + col2Right) / 2f, y + 12.5f, p.whiteHeader)
    canvas.drawText(
      "Situación por la que se visita a la persona o Familia",
      (col2Right + tableRight) / 2f,
      y + 12.5f,
      p.whiteHeader,
    )
    y += headerH

    val pastoringRows = listOf(
      Triple("Miércoles", nota.visitaMiercolesPersona, nota.visitaMiercolesSituacion),
      Triple("Jueves", nota.visitaJuevesPersona, nota.visitaJuevesSituacion),
      Triple("Viernes", nota.visitaViernesPersona, nota.visitaViernesSituacion),
    )

    pastoringRows.forEach { (day, persona, situacion) ->
      val personaLayout = buildStaticLayout(persona, p.bodyPara, (col2Right - col1Right - 10f).toInt())
      val sitLayout = buildStaticLayout(situacion, p.bodyPara, (tableRight - col2Right - 10f).toInt())
      val contentHeight = maxOf(personaLayout.height, sitLayout.height) + 10f
      val rowH = maxOf(46f, minOf(contentHeight, 68f))

      fillRect(canvas, tableLeft, y, col1Right, y + rowH, COLOR_GRAY_CELL, p)
      drawRectStroke(canvas, col1Right, y, col2Right, y + rowH, p)
      drawRectStroke(canvas, col2Right, y, tableRight, y + rowH, p)

      canvas.drawText(day, (tableLeft + col1Right) / 2f, y + (rowH / 2f) + 3.5f, p.cellCenteredBold)

      if (persona.isNotBlank()) {
        canvas.withSave {
          clipRect(col1Right + 3f, y + 3f, col2Right - 3f, y + rowH - 3f)
          translate(col1Right + 5f, y + 5f)
          personaLayout.draw(this)
        }
      }
      if (situacion.isNotBlank()) {
        canvas.withSave {
          clipRect(col2Right + 3f, y + 3f, tableRight - 3f, y + rowH - 3f)
          translate(col2Right + 5f, y + 5f)
          sitLayout.draw(this)
        }
      }
      y += rowH
    }

    y += 16f

    // Reunión con los Ancianos bullet
    p.fill.color = Color.BLACK
    canvas.drawRect(bulletX, y + 3f, bulletX + 5f, y + 8f, p.fill)
    val reunionSpan = buildBulletSpan(
      lead = "Reunión con los Ancianos:",
      body = " Escriba los puntos que como cuerpo desean tratar.",
    )
    val reunionLayout = StaticLayout.Builder.obtain(reunionSpan, 0, reunionSpan.length, p.bodyPara, textWidth)
      .setIncludePad(false)
      .build()
    canvas.withTranslation(textX, y) {
      reunionLayout.draw(this)
    }
    y += reunionLayout.height + 10f

    // Asuntos sugeridos table
    fillRect(canvas, tableLeft, y, tableRight, y + headerH, COLOR_GRAY_HEADER, p)
    canvas.drawText(
      "Asuntos sugeridos por el cuerpo de ancianos",
      (tableLeft + tableRight) / 2f,
      y + 12.5f,
      p.whiteHeader,
    )
    y += headerH

    val asuntos = listOf(nota.asunto1, nota.asunto2, nota.asunto3, nota.asunto4)
    val asuntoWidth = (tableRight - tableLeft - 10f).toInt()
    asuntos.forEach { asunto ->
      val layout = buildStaticLayout(asunto, p.bodyPara, asuntoWidth)
      val rowH = maxOf(19f, minOf(layout.height + 8f, 36f))
      drawRectStroke(canvas, tableLeft, y, tableRight, y + rowH, p)
      if (asunto.isNotBlank()) {
        canvas.withSave {
          clipRect(tableLeft + 4f, y + 2f, tableRight - 4f, y + rowH - 2f)
          translate(tableLeft + 5f, y + 4f)
          layout.draw(this)
        }
      }
      y += rowH
    }

    // Footer code
    val footerText = nota.codigoPie.ifBlank { "Ago/26" }
    canvas.drawText(footerText, rightEdge, maxOf(y + 16f, 756f), p.footer)
  }

  private fun buildBulletSpan(
    lead: String,
    body: String,
    italicWord1: String? = null,
    body2: String = "",
    boldItalicWord1: Boolean = false,
  ): CharSequence {
    val sb = SpannableStringBuilder()
    sb.append(lead)
    sb.setSpan(StyleSpan(Typeface.BOLD), 0, lead.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sb.append(body)
    if (italicWord1 != null) {
      val start = sb.length
      sb.append(italicWord1)
      val style = if (boldItalicWord1) Typeface.BOLD_ITALIC else Typeface.ITALIC
      sb.setSpan(StyleSpan(style), start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      sb.append(body2)
    }
    return sb
  }

  private fun buildStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
    val safeWidth = maxOf(width, 20)
    return StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
      .setAlignment(Layout.Alignment.ALIGN_NORMAL)
      .setLineSpacing(1.0f, 1.0f)
      .setIncludePad(false)
      .build()
  }

  private fun fillRect(
    canvas: Canvas,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    fillColor: Int,
    p: PdfPaints,
  ) {
    p.fill.color = fillColor
    canvas.drawRect(left, top, right, bottom, p.fill)
    canvas.drawRect(left, top, right, bottom, p.stroke)
  }

  private fun drawRectStroke(
    canvas: Canvas,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    p: PdfPaints,
  ) {
    canvas.drawRect(left, top, right, bottom, p.stroke)
  }

  private fun drawLabelAndValue(
    canvas: Canvas,
    label: String,
    value: String,
    x: Float,
    baselineY: Float,
    maxRight: Float,
    labelPaint: TextPaint,
    valuePaint: TextPaint,
  ) {
    canvas.drawText(label, x, baselineY, labelPaint)
    if (value.isNotBlank()) {
      val labelWidth = labelPaint.measureText(label)
      val valueX = x + labelWidth + 4f
      val available = maxRight - valueX
      if (available > 6f) {
        val clipped = TextUtils.ellipsize(value, valuePaint, available, TextUtils.TruncateAt.END).toString()
        canvas.drawText(clipped, valueX, baselineY, valuePaint)
      }
    }
  }

  private fun drawSingleLineClipped(
    canvas: Canvas,
    text: String,
    x: Float,
    baselineY: Float,
    maxRight: Float,
    paint: TextPaint,
  ) {
    if (text.isBlank()) return
    val available = maxRight - x
    if (available > 6f) {
      val clipped = TextUtils.ellipsize(text, paint, available, TextUtils.TruncateAt.END).toString()
      canvas.drawText(clipped, x, baselineY, paint)
    }
  }

  private fun drawWrappedTextInBox(
    canvas: Canvas,
    text: String,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    paint: TextPaint,
  ) {
    val width = (right - left).toInt().coerceAtLeast(20)
    val layout = buildStaticLayout(text, paint, width)
    canvas.withSave {
      clipRect(RectF(left, top, right, bottom))
      translate(left, top)
      layout.draw(this)
    }
  }
}
