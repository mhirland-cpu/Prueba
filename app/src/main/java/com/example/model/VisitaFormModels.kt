package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DaySchedule(
  val salidaAmLugar: String = "",
  val salidaAmHora: String = "",
  val salidaPmLugar: String = "",
  val salidaPmHora: String = "",
  val almuerzoFamilia: String = "",
  val almuerzoTel: String = "",
  val reunionPrecursoresEnabled: Boolean = false,
  val reunionPrecursoresLugar: String = "Salón del Reino",
  val reunionPrecursoresHora: String = "11:30 am",
)

@JsonClass(generateAdapter = true)
data class ActividadesPage(
  val fecha: String = "",
  val congregacion: String = "",
  val miercoles: DaySchedule = DaySchedule(),
  val jueves: DaySchedule = DaySchedule(),
  val viernes: DaySchedule = DaySchedule(),
  val reunionAncianosLugar: String = "",
  val reunionAncianosHora: String = "",
  val sabado: DaySchedule = DaySchedule(),
  val domingoSalidaAmLugar: String = "",
  val domingoSalidaAmHora: String = "",
  val domingoAlmuerzoFamilia: String = "",
  val domingoAlmuerzoTel: String = "",
  val domingoReunionPrecursoresEnabled: Boolean = false,
  val domingoReunionPrecursoresLugar: String = "Salón del Reino",
  val domingoReunionPrecursoresHora: String = "11:30 am",
  val reunionVidaMinisterioDia: String = "",
  val reunionVidaMinisterioHora: String = "",
  val reunionDiscursoAtalayaDia: String = "",
  val reunionDiscursoAtalayaHora: String = "",
)

@JsonClass(generateAdapter = true)
data class ServicePastoringDay(
  val hora1Con: String = "",
  val hora1Tel: String = "",
  val hora1EstudioPastoreo: String = "",
  val hora1Publicacion: String = "",
  val hora2Con: String = "",
  val hora2Tel: String = "",
  val hora2EstudioPastoreo: String = "",
  val hora2Publicacion: String = "",
)

@JsonClass(generateAdapter = true)
data class ServicioPastoreoPage(
  val nombrePersona: String = "Obed Paulino",
  val miercoles: ServicePastoringDay = ServicePastoringDay(),
  val jueves: ServicePastoringDay = ServicePastoringDay(),
  val viernes: ServicePastoringDay = ServicePastoringDay(),
  val notasAdicionales: String = "",
)

@JsonClass(generateAdapter = true)
data class ServiceOnlyDay(
  val hora1Con: String = "",
  val hora1Tel: String = "",
  val hora1Publicacion: String = "",
  val hora2Con: String = "",
  val hora2Tel: String = "",
  val hora2Publicacion: String = "",
)

@JsonClass(generateAdapter = true)
data class ServicioEsposaPage(
  val nombrePersona: String = "Mercedes Paulino",
  val miercoles: ServiceOnlyDay = ServiceOnlyDay(),
  val jueves: ServiceOnlyDay = ServiceOnlyDay(),
  val viernes: ServiceOnlyDay = ServiceOnlyDay(),
  val notasAdicionales: String = "",
)

@JsonClass(generateAdapter = true)
data class NotaPastoreoPage(
  val visitaMiercolesPersona: String = "",
  val visitaMiercolesSituacion: String = "",
  val visitaJuevesPersona: String = "",
  val visitaJuevesSituacion: String = "",
  val visitaViernesPersona: String = "",
  val visitaViernesSituacion: String = "",
  val asunto1: String = "",
  val asunto2: String = "",
  val asunto3: String = "",
  val asunto4: String = "",
  val codigoPie: String = "Ago/26",
)

@JsonClass(generateAdapter = true)
data class VisitaFormData(
  val actividades: ActividadesPage = ActividadesPage(),
  val servicioPastoreo: ServicioPastoreoPage = ServicioPastoreoPage(),
  val servicioEsposa: ServicioEsposaPage = ServicioEsposaPage(),
  val notaPastoreo: NotaPastoreoPage = NotaPastoreoPage(),
) {
  fun filledCount(): Pair<Int, Int> {
    val fields = listOf(
      actividades.fecha,
      actividades.congregacion,
      actividades.miercoles.salidaAmLugar,
      actividades.miercoles.salidaAmHora,
      actividades.miercoles.salidaPmLugar,
      actividades.miercoles.salidaPmHora,
      actividades.miercoles.almuerzoFamilia,
      actividades.jueves.salidaAmLugar,
      actividades.jueves.salidaPmLugar,
      actividades.jueves.almuerzoFamilia,
      actividades.viernes.salidaAmLugar,
      actividades.viernes.salidaPmLugar,
      actividades.viernes.almuerzoFamilia,
      actividades.reunionAncianosLugar,
      actividades.sabado.salidaAmLugar,
      actividades.sabado.almuerzoFamilia,
      actividades.domingoSalidaAmLugar,
      actividades.domingoAlmuerzoFamilia,
      actividades.reunionVidaMinisterioDia,
      actividades.reunionDiscursoAtalayaDia,
      servicioPastoreo.miercoles.hora1Con,
      servicioPastoreo.miercoles.hora2Con,
      servicioPastoreo.jueves.hora1Con,
      servicioPastoreo.jueves.hora2Con,
      servicioPastoreo.viernes.hora1Con,
      servicioPastoreo.viernes.hora2Con,
      servicioEsposa.miercoles.hora1Con,
      servicioEsposa.jueves.hora1Con,
      servicioEsposa.viernes.hora1Con,
      notaPastoreo.visitaMiercolesPersona,
      notaPastoreo.visitaJuevesPersona,
      notaPastoreo.visitaViernesPersona,
      notaPastoreo.asunto1,
    )
    return fields.count { it.isNotBlank() } to fields.size
  }
}

object BaseTemplateFactory {
  fun createOriginalPdfBase(): VisitaFormData = VisitaFormData(
    actividades = ActividadesPage(
      fecha = "",
      congregacion = "",
      miercoles = DaySchedule(
        almuerzoFamilia = "Familia Almonte Figueroa",
        almuerzoTel = "849-203-1217",
        reunionPrecursoresEnabled = true,
        reunionPrecursoresLugar = "Salón del Reino",
        reunionPrecursoresHora = "11:30 am",
      ),
      jueves = DaySchedule(
        almuerzoFamilia = "Familia Minaya",
        almuerzoTel = "829-810-4097",
      ),
      viernes = DaySchedule(
        almuerzoFamilia = "Familia Ozuna",
        almuerzoTel = "829-271-2707",
      ),
      reunionAncianosLugar = "Salón del Reino",
      reunionAncianosHora = "7:00 pm",
      sabado = DaySchedule(),
      reunionVidaMinisterioDia = "Martes",
      reunionVidaMinisterioHora = "7:00 pm",
      reunionDiscursoAtalayaDia = "Sábado",
      reunionDiscursoAtalayaHora = "6:00 pm",
    ),
    servicioPastoreo = ServicioPastoreoPage(
      nombrePersona = "Obed Paulino",
      miercoles = ServicePastoringDay(
        hora1Con = "Benjamín Herrera",
        hora1Tel = "(809) 919-4150",
      ),
      jueves = ServicePastoringDay(
        hora1Con = "Misael Hirland",
        hora1Tel = "(809) 978-4209",
      ),
      viernes = ServicePastoringDay(),
    ),
    servicioEsposa = ServicioEsposaPage(
      nombrePersona = "Mercedes Paulino",
    ),
    notaPastoreo = NotaPastoreoPage(
      visitaMiercolesPersona = "Sergia Viera",
      visitaMiercolesSituacion = "Hermana Mayor lucha contra los achaques de la vejez, hace un tiempo después de la pandemia un vehículo la atropello, causándole lesiones, aunque está mejor de salud aún sufre las secuelas del accidente. Ella fue precursora especial junto a su esposo.",
      codigoPie = "Ago/26",
    ),
  )
}
