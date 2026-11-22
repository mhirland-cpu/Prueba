package com.example

import com.example.firebase.toActividadesPage
import com.example.firebase.toFirestoreMap
import com.example.firebase.toNotaPastoreoPage
import com.example.firebase.toServicioEsposaPage
import com.example.firebase.toServicioPastoreoPage
import com.example.model.BaseTemplateFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class SharedArregloModelsTest {

  @Test
  fun `base template round-trips cleanly through Firestore maps`() {
    val base = BaseTemplateFactory.createOriginalPdfBase()
    val custom = base.copy(
      actividades = base.actividades.copy(
        congregacion = "Villa Francisca",
        fecha = "10 al 15 de noviembre de 2026",
        domingoReunionPrecursoresEnabled = true,
      ),
      notaPastoreo = base.notaPastoreo.copy(
        asunto1 = "Coordinar predicación",
      ),
    )

    val actRoundTrip = custom.actividades.toFirestoreMap().toActividadesPage()
    val pastRoundTrip = custom.servicioPastoreo.toFirestoreMap().toServicioPastoreoPage()
    val esposaRoundTrip = custom.servicioEsposa.toFirestoreMap().toServicioEsposaPage()
    val notaRoundTrip = custom.notaPastoreo.toFirestoreMap().toNotaPastoreoPage()

    assertEquals(custom.actividades, actRoundTrip)
    assertEquals(custom.servicioPastoreo, pastRoundTrip)
    assertEquals(custom.servicioEsposa, esposaRoundTrip)
    assertEquals(custom.notaPastoreo, notaRoundTrip)
  }
}
