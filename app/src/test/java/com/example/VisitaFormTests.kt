package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.DraftRepository
import com.example.model.BaseTemplateFactory
import com.example.pdf.PdfFormGenerator
import com.example.ui.components.SimpleDay
import com.example.ui.components.formatSpanishDateRange
import com.example.ui.components.formatTime12h
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VisitaFormTests {

  @Test
  fun `base template matches reference pdf prefilled fields`() {
    val base = BaseTemplateFactory.createOriginalPdfBase()
    assertEquals("Familia Almonte Figueroa", base.actividades.miercoles.almuerzoFamilia)
    assertEquals("849-203-1217", base.actividades.miercoles.almuerzoTel)
    assertTrue(base.actividades.miercoles.reunionPrecursoresEnabled)
    assertEquals("Salón del Reino", base.actividades.miercoles.reunionPrecursoresLugar)
    assertEquals("11:30 am", base.actividades.miercoles.reunionPrecursoresHora)
    assertFalse(base.actividades.jueves.reunionPrecursoresEnabled)
    assertFalse(base.actividades.viernes.reunionPrecursoresEnabled)
    assertFalse(base.actividades.sabado.reunionPrecursoresEnabled)
    assertFalse(base.actividades.domingoReunionPrecursoresEnabled)
    assertEquals("Familia Minaya", base.actividades.jueves.almuerzoFamilia)
    assertEquals("Familia Ozuna", base.actividades.viernes.almuerzoFamilia)
    assertEquals("Martes", base.actividades.reunionVidaMinisterioDia)
    assertEquals("7:00 pm", base.actividades.reunionVidaMinisterioHora)
    assertEquals("Sábado", base.actividades.reunionDiscursoAtalayaDia)
    assertEquals("6:00 pm", base.actividades.reunionDiscursoAtalayaHora)
    assertEquals("Obed Paulino", base.servicioPastoreo.nombrePersona)
    assertEquals("Benjamín Herrera", base.servicioPastoreo.miercoles.hora1Con)
    assertEquals("Misael Hirland", base.servicioPastoreo.jueves.hora1Con)
    assertEquals("Mercedes Paulino", base.servicioEsposa.nombrePersona)
    assertEquals("Sergia Viera", base.notaPastoreo.visitaMiercolesPersona)
    assertEquals("Ago/26", base.notaPastoreo.codigoPie)
  }

  @Test
  fun `spanish date range and 12h time formatters produce Spanish output`() {
    val sameMonth = formatSpanishDateRange(
      SimpleDay(2026, 2, 10),
      SimpleDay(2026, 2, 15),
    )
    assertEquals("10 al 15 de marzo de 2026", sameMonth)

    val crossMonth = formatSpanishDateRange(
      SimpleDay(2026, 2, 30),
      SimpleDay(2026, 3, 5),
    )
    assertEquals("30 de marzo al 5 de abril de 2026", crossMonth)

    assertEquals("8:30 am", formatTime12h(8, 30, false))
    assertEquals("7:05 pm", formatTime12h(7, 5, true))
  }

  @Test
  fun `pdf generator draws all 4 pages cleanly even with custom pioneers meeting day`() {
    val base = BaseTemplateFactory.createOriginalPdfBase()
    val form = base.copy(
      actividades = base.actividades.copy(
        fecha = "10 al 15 de marzo de 2026",
        congregacion = "Villa Progreso",
        miercoles = base.actividades.miercoles.copy(reunionPrecursoresEnabled = false),
        sabado = base.actividades.sabado.copy(
          reunionPrecursoresEnabled = true,
          reunionPrecursoresLugar = "Salón del Reino",
          reunionPrecursoresHora = "11:00 am",
        ),
      ),
      notaPastoreo = base.notaPastoreo.copy(
        asunto1 = "Revisar territorios rurales",
        asunto2 = "Horarios de predicación de fin de semana",
      ),
    )

    for (pageNum in 1..4) {
      val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      canvas.drawColor(Color.WHITE)
      PdfFormGenerator.drawPage(pageNum, canvas, form)
      assertEquals(612, bitmap.width)
      assertEquals(792, bitmap.height)
    }
  }

  @Test
  fun `room draft repository creates saves duplicates and deletes drafts offline`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    try {
      val repo = DraftRepository(db.draftDao())
      val draft = repo.createNewDraftFromBase()
      val updatedForm = draft.formData.copy(
        actividades = draft.formData.actividades.copy(
          congregacion = "Norte Central",
          fecha = "3 al 8 de noviembre de 2026",
        )
      )
      repo.saveDraft(draft.id, updatedForm, isBaseTemplate = false)

      val listAfterSave = repo.observeDrafts().first()
      assertEquals(1, listAfterSave.size)
      assertEquals("Norte Central", listAfterSave.first().congregacion)

      val copy = repo.duplicateDraft(listAfterSave.first())
      val listAfterCopy = repo.observeDrafts().first()
      assertEquals(2, listAfterCopy.size)
      assertTrue(listAfterCopy.any { it.id == copy.id && it.congregacion.contains("Copia") })

      repo.deleteDraft(copy.id)
      val listAfterDelete = repo.observeDrafts().first()
      assertEquals(1, listAfterDelete.size)
    } finally {
      db.close()
    }
  }
}
