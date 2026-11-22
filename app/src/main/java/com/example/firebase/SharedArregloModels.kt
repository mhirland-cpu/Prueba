package com.example.firebase

import androidx.annotation.Keep
import com.example.model.ActividadesPage
import com.example.model.DaySchedule
import com.example.model.NotaPastoreoPage
import com.example.model.ServiceOnlyDay
import com.example.model.ServicePastoringDay
import com.example.model.ServicioEsposaPage
import com.example.model.ServicioPastoreoPage
import com.example.model.VisitaFormData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.IgnoreExtraProperties

enum class ArregloSectionKey(val docId: String, val labelEs: String) {
  ACTIVIDADES("actividades", "1. Actividades"),
  SERVICIO_PASTOREO("servicioPastoreo", "2. Sup. Circuito"),
  SERVICIO_ESPOSA("servicioEsposa", "3. Esposa"),
  NOTA_PASTOREO("notaPastoreo", "4. Pastoreo y Nota");

  companion object {
    fun fromTabIndex(index: Int): ArregloSectionKey = when (index) {
      0 -> ACTIVIDADES
      1 -> SERVICIO_PASTOREO
      2 -> SERVICIO_ESPOSA
      else -> NOTA_PASTOREO
    }
  }
}

@Keep
@IgnoreExtraProperties
data class SharedArregloSummary(
  val id: String = "",
  val ownerId: String = "",
  val ownerName: String = "",
  val memberUids: List<String> = emptyList(),
  val memberNames: Map<String, String> = emptyMap(),
  val congregacion: String = "",
  val fechaVisita: String = "",
  val lastEditedByUid: String = "",
  val lastEditedByName: String = "",
  val createdAt: Timestamp? = null,
  val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class SectionEditMeta(
  val lastEditedByUid: String = "",
  val lastEditedByName: String = "",
  val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class SharedArregloDetail(
  val summary: SharedArregloSummary = SharedArregloSummary(),
  val formData: VisitaFormData = VisitaFormData(),
  val sectionMetas: Map<ArregloSectionKey, SectionEditMeta> = emptyMap(),
)

internal fun DaySchedule.toFirestoreMap(): Map<String, Any> = mapOf(
  "salidaAmLugar" to salidaAmLugar.take(200),
  "salidaAmHora" to salidaAmHora.take(50),
  "salidaPmLugar" to salidaPmLugar.take(200),
  "salidaPmHora" to salidaPmHora.take(50),
  "almuerzoFamilia" to almuerzoFamilia.take(200),
  "almuerzoTel" to almuerzoTel.take(50),
  "reunionPrecursoresEnabled" to reunionPrecursoresEnabled,
  "reunionPrecursoresLugar" to reunionPrecursoresLugar.take(200),
  "reunionPrecursoresHora" to reunionPrecursoresHora.take(50),
)

internal fun ActividadesPage.toFirestoreMap(): Map<String, Any> = mapOf(
  "fecha" to fecha.take(200),
  "congregacion" to congregacion.take(200),
  "miercoles" to miercoles.toFirestoreMap(),
  "jueves" to jueves.toFirestoreMap(),
  "viernes" to viernes.toFirestoreMap(),
  "reunionAncianosLugar" to reunionAncianosLugar.take(200),
  "reunionAncianosHora" to reunionAncianosHora.take(50),
  "sabado" to sabado.toFirestoreMap(),
  "domingoSalidaAmLugar" to domingoSalidaAmLugar.take(200),
  "domingoSalidaAmHora" to domingoSalidaAmHora.take(50),
  "domingoAlmuerzoFamilia" to domingoAlmuerzoFamilia.take(200),
  "domingoAlmuerzoTel" to domingoAlmuerzoTel.take(50),
  "domingoReunionPrecursoresEnabled" to domingoReunionPrecursoresEnabled,
  "domingoReunionPrecursoresLugar" to domingoReunionPrecursoresLugar.take(200),
  "domingoReunionPrecursoresHora" to domingoReunionPrecursoresHora.take(50),
  "reunionVidaMinisterioDia" to reunionVidaMinisterioDia.take(100),
  "reunionVidaMinisterioHora" to reunionVidaMinisterioHora.take(50),
  "reunionDiscursoAtalayaDia" to reunionDiscursoAtalayaDia.take(100),
  "reunionDiscursoAtalayaHora" to reunionDiscursoAtalayaHora.take(50),
)

internal fun ServicePastoringDay.toFirestoreMap(): Map<String, Any> = mapOf(
  "hora1Con" to hora1Con.take(200),
  "hora1Tel" to hora1Tel.take(50),
  "hora1EstudioPastoreo" to hora1EstudioPastoreo.take(200),
  "hora1Publicacion" to hora1Publicacion.take(200),
  "hora2Con" to hora2Con.take(200),
  "hora2Tel" to hora2Tel.take(50),
  "hora2EstudioPastoreo" to hora2EstudioPastoreo.take(200),
  "hora2Publicacion" to hora2Publicacion.take(200),
)

internal fun ServicioPastoreoPage.toFirestoreMap(): Map<String, Any> = mapOf(
  "nombrePersona" to nombrePersona.take(200),
  "miercoles" to miercoles.toFirestoreMap(),
  "jueves" to jueves.toFirestoreMap(),
  "viernes" to viernes.toFirestoreMap(),
  "notasAdicionales" to notasAdicionales.take(2000),
)

internal fun ServiceOnlyDay.toFirestoreMap(): Map<String, Any> = mapOf(
  "hora1Con" to hora1Con.take(200),
  "hora1Tel" to hora1Tel.take(50),
  "hora1Publicacion" to hora1Publicacion.take(200),
  "hora2Con" to hora2Con.take(200),
  "hora2Tel" to hora2Tel.take(50),
  "hora2Publicacion" to hora2Publicacion.take(200),
)

internal fun ServicioEsposaPage.toFirestoreMap(): Map<String, Any> = mapOf(
  "nombrePersona" to nombrePersona.take(200),
  "miercoles" to miercoles.toFirestoreMap(),
  "jueves" to jueves.toFirestoreMap(),
  "viernes" to viernes.toFirestoreMap(),
  "notasAdicionales" to notasAdicionales.take(2000),
)

internal fun NotaPastoreoPage.toFirestoreMap(): Map<String, Any> = mapOf(
  "visitaMiercolesPersona" to visitaMiercolesPersona.take(200),
  "visitaMiercolesSituacion" to visitaMiercolesSituacion.take(3000),
  "visitaJuevesPersona" to visitaJuevesPersona.take(200),
  "visitaJuevesSituacion" to visitaJuevesSituacion.take(3000),
  "visitaViernesPersona" to visitaViernesPersona.take(200),
  "visitaViernesSituacion" to visitaViernesSituacion.take(3000),
  "asunto1" to asunto1.take(1500),
  "asunto2" to asunto2.take(1500),
  "asunto3" to asunto3.take(1500),
  "asunto4" to asunto4.take(1500),
  "codigoPie" to codigoPie.take(50),
)

private fun Map<*, *>?.str(key: String, default: String = ""): String =
  (this?.get(key) as? String) ?: default

private fun Map<*, *>?.bool(key: String, default: Boolean = false): Boolean =
  (this?.get(key) as? Boolean) ?: default

private fun Map<*, *>?.subMap(key: String): Map<*, *>? =
  this?.get(key) as? Map<*, *>

private fun Map<*, *>?.toDaySchedule(): DaySchedule = DaySchedule(
  salidaAmLugar = str("salidaAmLugar"),
  salidaAmHora = str("salidaAmHora"),
  salidaPmLugar = str("salidaPmLugar"),
  salidaPmHora = str("salidaPmHora"),
  almuerzoFamilia = str("almuerzoFamilia"),
  almuerzoTel = str("almuerzoTel"),
  reunionPrecursoresEnabled = bool("reunionPrecursoresEnabled", false),
  reunionPrecursoresLugar = str("reunionPrecursoresLugar", "Salón del Reino"),
  reunionPrecursoresHora = str("reunionPrecursoresHora", "11:30 am"),
)

internal fun Map<*, *>?.toActividadesPage(): ActividadesPage = ActividadesPage(
  fecha = str("fecha"),
  congregacion = str("congregacion"),
  miercoles = subMap("miercoles").toDaySchedule(),
  jueves = subMap("jueves").toDaySchedule(),
  viernes = subMap("viernes").toDaySchedule(),
  reunionAncianosLugar = str("reunionAncianosLugar"),
  reunionAncianosHora = str("reunionAncianosHora"),
  sabado = subMap("sabado").toDaySchedule(),
  domingoSalidaAmLugar = str("domingoSalidaAmLugar"),
  domingoSalidaAmHora = str("domingoSalidaAmHora"),
  domingoAlmuerzoFamilia = str("domingoAlmuerzoFamilia"),
  domingoAlmuerzoTel = str("domingoAlmuerzoTel"),
  domingoReunionPrecursoresEnabled = bool("domingoReunionPrecursoresEnabled", false),
  domingoReunionPrecursoresLugar = str("domingoReunionPrecursoresLugar", "Salón del Reino"),
  domingoReunionPrecursoresHora = str("domingoReunionPrecursoresHora", "11:30 am"),
  reunionVidaMinisterioDia = str("reunionVidaMinisterioDia"),
  reunionVidaMinisterioHora = str("reunionVidaMinisterioHora"),
  reunionDiscursoAtalayaDia = str("reunionDiscursoAtalayaDia"),
  reunionDiscursoAtalayaHora = str("reunionDiscursoAtalayaHora"),
)

private fun Map<*, *>?.toServicePastoringDay(): ServicePastoringDay = ServicePastoringDay(
  hora1Con = str("hora1Con"),
  hora1Tel = str("hora1Tel"),
  hora1EstudioPastoreo = str("hora1EstudioPastoreo"),
  hora1Publicacion = str("hora1Publicacion"),
  hora2Con = str("hora2Con"),
  hora2Tel = str("hora2Tel"),
  hora2EstudioPastoreo = str("hora2EstudioPastoreo"),
  hora2Publicacion = str("hora2Publicacion"),
)

internal fun Map<*, *>?.toServicioPastoreoPage(): ServicioPastoreoPage = ServicioPastoreoPage(
  nombrePersona = str("nombrePersona", "Obed Paulino"),
  miercoles = subMap("miercoles").toServicePastoringDay(),
  jueves = subMap("jueves").toServicePastoringDay(),
  viernes = subMap("viernes").toServicePastoringDay(),
  notasAdicionales = str("notasAdicionales"),
)

private fun Map<*, *>?.toServiceOnlyDay(): ServiceOnlyDay = ServiceOnlyDay(
  hora1Con = str("hora1Con"),
  hora1Tel = str("hora1Tel"),
  hora1Publicacion = str("hora1Publicacion"),
  hora2Con = str("hora2Con"),
  hora2Tel = str("hora2Tel"),
  hora2Publicacion = str("hora2Publicacion"),
)

internal fun Map<*, *>?.toServicioEsposaPage(): ServicioEsposaPage = ServicioEsposaPage(
  nombrePersona = str("nombrePersona", "Mercedes Paulino"),
  miercoles = subMap("miercoles").toServiceOnlyDay(),
  jueves = subMap("jueves").toServiceOnlyDay(),
  viernes = subMap("viernes").toServiceOnlyDay(),
  notasAdicionales = str("notasAdicionales"),
)

internal fun Map<*, *>?.toNotaPastoreoPage(): NotaPastoreoPage = NotaPastoreoPage(
  visitaMiercolesPersona = str("visitaMiercolesPersona"),
  visitaMiercolesSituacion = str("visitaMiercolesSituacion"),
  visitaJuevesPersona = str("visitaJuevesPersona"),
  visitaJuevesSituacion = str("visitaJuevesSituacion"),
  visitaViernesPersona = str("visitaViernesPersona"),
  visitaViernesSituacion = str("visitaViernesSituacion"),
  asunto1 = str("asunto1"),
  asunto2 = str("asunto2"),
  asunto3 = str("asunto3"),
  asunto4 = str("asunto4"),
  codigoPie = str("codigoPie", "Ago/26"),
)

fun DocumentSnapshot.toSharedArregloSummary(): SharedArregloSummary? {
  if (!exists()) return null
  val data = data ?: return null
  val memberUids = (data["memberUids"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
  val rawNames = data["memberNames"] as? Map<*, *>
  val memberNames = buildMap {
    rawNames?.forEach { (k, v) ->
      if (k is String && v is String) put(k, v)
    }
  }
  return SharedArregloSummary(
    id = id,
    ownerId = data.str("ownerId"),
    ownerName = data.str("ownerName"),
    memberUids = memberUids,
    memberNames = memberNames,
    congregacion = data.str("congregacion"),
    fechaVisita = data.str("fechaVisita"),
    lastEditedByUid = data.str("lastEditedByUid"),
    lastEditedByName = data.str("lastEditedByName"),
    createdAt = getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
    updatedAt = getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
  )
}

fun DocumentSnapshot.toSectionEditMeta(): SectionEditMeta {
  val data = data
  return SectionEditMeta(
    lastEditedByUid = data.str("lastEditedByUid"),
    lastEditedByName = data.str("lastEditedByName"),
    updatedAt = getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
  )
}

fun DocumentSnapshot.sectionPayloadMap(): Map<*, *>? = data?.get("payload") as? Map<*, *>
