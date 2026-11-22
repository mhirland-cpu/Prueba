package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.DaySchedule
import com.example.model.ServiceOnlyDay
import com.example.model.ServicePastoringDay
import com.example.model.VisitaFormData
import com.example.ui.components.SpanishDateRangePickerDialog
import com.example.ui.components.TimeSelectorField
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.DayDomingoBg
import com.example.ui.theme.DayDomingoText
import com.example.ui.theme.DayJuevesBg
import com.example.ui.theme.DayJuevesText
import com.example.ui.theme.DayMiercolesBg
import com.example.ui.theme.DayMiercolesText
import com.example.ui.theme.DaySabadoBg
import com.example.ui.theme.DaySabadoText
import com.example.ui.theme.DayViernesBg
import com.example.ui.theme.DayViernesText

@Composable
fun ActividadesTab(
  formData: VisitaFormData,
  onUpdate: ((VisitaFormData) -> VisitaFormData) -> Unit,
) {
  val a = formData.actividades
  var showDateRangePicker by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    SectionCard(
      title = "Datos de la visita",
      headerBg = MaterialTheme.colorScheme.tertiaryContainer,
      headerTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = a.fecha,
          onValueChange = { v -> onUpdate { it.copy(actividades = it.actividades.copy(fecha = v)) } },
          label = { Text("Fecha de visita") },
          placeholder = { Text("Ej. 10 al 15 de marzo de 2026") },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("input_fecha"),
        )
        OutlinedButton(
          onClick = { showDateRangePicker = true },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .height(54.dp)
            .testTag("pick_fecha_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = "Abrir calendario en español",
            tint = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Calendario")
        }
      }

      OutlinedTextField(
        value = a.congregacion,
        onValueChange = { v -> onUpdate { it.copy(actividades = it.actividades.copy(congregacion = v)) } },
        label = { Text("Congregación") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_congregacion"),
      )
    }

    DayScheduleEditorCard(
      dayName = "Miércoles",
      headerBg = DayMiercolesBg,
      headerTextColor = DayMiercolesText,
      schedule = a.miercoles,
      initiallyExpanded = true,
      onScheduleChange = { s -> onUpdate { it.copy(actividades = it.actividades.copy(miercoles = s)) } },
    )

    DayScheduleEditorCard(
      dayName = "Jueves",
      headerBg = DayJuevesBg,
      headerTextColor = DayJuevesText,
      schedule = a.jueves,
      initiallyExpanded = false,
      onScheduleChange = { s -> onUpdate { it.copy(actividades = it.actividades.copy(jueves = s)) } },
    )

    DayScheduleEditorCard(
      dayName = "Viernes",
      headerBg = DayViernesBg,
      headerTextColor = DayViernesText,
      schedule = a.viernes,
      initiallyExpanded = false,
      onScheduleChange = { s -> onUpdate { it.copy(actividades = it.actividades.copy(viernes = s)) } },
      extraContent = {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Reunión ancianos, ministeriales",
          style = MaterialTheme.typography.labelLarge,
          color = CrimsonAccent,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = a.reunionAncianosLugar,
            onValueChange = { v ->
              onUpdate { it.copy(actividades = it.actividades.copy(reunionAncianosLugar = v)) }
            },
            label = { Text("Lugar") },
            singleLine = true,
            modifier = Modifier
              .weight(1.3f)
              .testTag("input_viernes_ancianos_lugar"),
          )
          TimeSelectorField(
            label = "Hora",
            value = a.reunionAncianosHora,
            onValueChange = { v ->
              onUpdate { it.copy(actividades = it.actividades.copy(reunionAncianosHora = v)) }
            },
            presets = listOf("6:30 pm", "7:00 pm", "7:30 pm", "8:00 pm"),
            modifier = Modifier.weight(1f),
            testTag = "input_viernes_ancianos_hora",
          )
        }
      },
    )

    DayScheduleEditorCard(
      dayName = "Sábado",
      headerBg = DaySabadoBg,
      headerTextColor = DaySabadoText,
      schedule = a.sabado,
      initiallyExpanded = false,
      onScheduleChange = { s -> onUpdate { it.copy(actividades = it.actividades.copy(sabado = s)) } },
    )

    // Domingo (Salida AM, Almuerzo y opción de Reunión de precursores)
    ExpandableDayCard(
      title = "Domingo",
      headerBg = DayDomingoBg,
      headerTextColor = DayDomingoText,
      badgeText = if (a.domingoReunionPrecursoresEnabled) "Con precursores" else null,
      initiallyExpanded = false,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = a.domingoSalidaAmLugar,
          onValueChange = { v ->
            onUpdate { it.copy(actividades = it.actividades.copy(domingoSalidaAmLugar = v)) }
          },
          label = { Text("Salida AM · Lugar") },
          singleLine = true,
          modifier = Modifier.weight(1.3f),
        )
        TimeSelectorField(
          label = "Hora AM",
          value = a.domingoSalidaAmHora,
          onValueChange = { v ->
            onUpdate { it.copy(actividades = it.actividades.copy(domingoSalidaAmHora = v)) }
          },
          presets = listOf("8:00 am", "8:30 am", "9:00 am", "9:30 am", "10:00 am"),
          modifier = Modifier.weight(1f),
        )
      }
      QuickLugarChips { chosen ->
        onUpdate { it.copy(actividades = it.actividades.copy(domingoSalidaAmLugar = chosen)) }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = a.domingoAlmuerzoFamilia,
          onValueChange = { v ->
            onUpdate { it.copy(actividades = it.actividades.copy(domingoAlmuerzoFamilia = v)) }
          },
          label = { Text("Almuerzo (Familia)") },
          singleLine = true,
          modifier = Modifier.weight(1.3f),
        )
        OutlinedTextField(
          value = a.domingoAlmuerzoTel,
          onValueChange = { v ->
            onUpdate { it.copy(actividades = it.actividades.copy(domingoAlmuerzoTel = v)) }
          },
          label = { Text("Tel.") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier.weight(1f),
        )
      }

      ReunionPrecursoresToggleBlock(
        dayName = "Domingo",
        enabled = a.domingoReunionPrecursoresEnabled,
        lugar = a.domingoReunionPrecursoresLugar,
        hora = a.domingoReunionPrecursoresHora,
        onToggle = { enabled ->
          onUpdate { it.copy(actividades = it.actividades.copy(domingoReunionPrecursoresEnabled = enabled)) }
        },
        onLugarChange = { lugar ->
          onUpdate { it.copy(actividades = it.actividades.copy(domingoReunionPrecursoresLugar = lugar)) }
        },
        onHoraChange = { hora ->
          onUpdate { it.copy(actividades = it.actividades.copy(domingoReunionPrecursoresHora = hora)) }
        },
      )
    }

    // Reuniones de congregación
    SectionCard(
      title = "Reuniones de congregación",
      headerBg = MaterialTheme.colorScheme.tertiaryContainer,
      headerTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
      Text(
        text = "Vida y Ministerio Cristianos",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = a.reunionVidaMinisterioDia,
          onValueChange = { v ->
            onUpdate { it.copy(actividades = it.actividades.copy(reunionVidaMinisterioDia = v)) }
          },
          label = { Text("Día") },
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
        TimeSelectorField(
          label = "Hora",
          value = a.reunionVidaMinisterioHora,
          onValueChange = { v ->
            onUpdate { it.copy(actividades = it.actividades.copy(reunionVidaMinisterioHora = v)) }
          },
          presets = listOf("6:30 pm", "7:00 pm", "7:30 pm", "8:00 pm"),
          modifier = Modifier.weight(1f),
        )
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Discurso público y estudio de La Atalaya",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = a.reunionDiscursoAtalayaDia,
          onValueChange = { v ->
            onUpdate { it.copy(actividades = it.actividades.copy(reunionDiscursoAtalayaDia = v)) }
          },
          label = { Text("Día") },
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
        TimeSelectorField(
          label = "Hora",
          value = a.reunionDiscursoAtalayaHora,
          onValueChange = { v ->
            onUpdate { it.copy(actividades = it.actividades.copy(reunionDiscursoAtalayaHora = v)) }
          },
          presets = listOf("9:00 am", "10:00 am", "4:00 pm", "5:00 pm", "6:00 pm"),
          modifier = Modifier.weight(1f),
        )
      }
    }
  }

  if (showDateRangePicker) {
    SpanishDateRangePickerDialog(
      onDismiss = { showDateRangePicker = false },
      onConfirm = { formatted ->
        onUpdate { it.copy(actividades = it.actividades.copy(fecha = formatted)) }
        showDateRangePicker = false
      },
    )
  }
}

@Composable
fun ServicioPastoreoTab(
  formData: VisitaFormData,
  onUpdate: ((VisitaFormData) -> VisitaFormData) -> Unit,
) {
  val s = formData.servicioPastoreo
  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    SectionCard(
      title = "Título del documento (Página 2)",
      headerBg = MaterialTheme.colorScheme.primaryContainer,
      headerTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
      OutlinedTextField(
        value = s.nombrePersona,
        onValueChange = { v ->
          onUpdate { it.copy(servicioPastoreo = it.servicioPastoreo.copy(nombrePersona = v)) }
        },
        label = { Text("Arreglos de servicio/pastoreo para:") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_sup_nombre"),
      )
    }

    ServicePastoringDayCard(
      dayName = "Miércoles",
      headerBg = DayMiercolesBg,
      headerTextColor = DayMiercolesText,
      day = s.miercoles,
      initiallyExpanded = true,
      onChange = { d -> onUpdate { it.copy(servicioPastoreo = it.servicioPastoreo.copy(miercoles = d)) } },
    )
    ServicePastoringDayCard(
      dayName = "Jueves",
      headerBg = DayJuevesBg,
      headerTextColor = DayJuevesText,
      day = s.jueves,
      initiallyExpanded = false,
      onChange = { d -> onUpdate { it.copy(servicioPastoreo = it.servicioPastoreo.copy(jueves = d)) } },
    )
    ServicePastoringDayCard(
      dayName = "Viernes",
      headerBg = DayViernesBg,
      headerTextColor = DayViernesText,
      day = s.viernes,
      initiallyExpanded = false,
      onChange = { d -> onUpdate { it.copy(servicioPastoreo = it.servicioPastoreo.copy(viernes = d)) } },
    )

    SectionCard(
      title = "Notas adicionales (Página 2)",
      headerBg = MaterialTheme.colorScheme.secondaryContainer,
      headerTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
      OutlinedTextField(
        value = s.notasAdicionales,
        onValueChange = { v ->
          onUpdate { it.copy(servicioPastoreo = it.servicioPastoreo.copy(notasAdicionales = v)) }
        },
        label = { Text("Notas adicionales") },
        minLines = 3,
        maxLines = 6,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_sup_notas"),
      )
    }
  }
}

@Composable
fun ServicioEsposaTab(
  formData: VisitaFormData,
  onUpdate: ((VisitaFormData) -> VisitaFormData) -> Unit,
) {
  val s = formData.servicioEsposa
  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    SectionCard(
      title = "Título del documento (Página 3)",
      headerBg = MaterialTheme.colorScheme.tertiaryContainer,
      headerTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
      OutlinedTextField(
        value = s.nombrePersona,
        onValueChange = { v ->
          onUpdate { it.copy(servicioEsposa = it.servicioEsposa.copy(nombrePersona = v)) }
        },
        label = { Text("Arreglos de servicio para:") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_esposa_nombre"),
      )
    }

    ServiceOnlyDayCard(
      dayName = "Miércoles",
      headerBg = DayMiercolesBg,
      headerTextColor = DayMiercolesText,
      day = s.miercoles,
      initiallyExpanded = true,
      onChange = { d -> onUpdate { it.copy(servicioEsposa = it.servicioEsposa.copy(miercoles = d)) } },
    )
    ServiceOnlyDayCard(
      dayName = "Jueves",
      headerBg = DayJuevesBg,
      headerTextColor = DayJuevesText,
      day = s.jueves,
      initiallyExpanded = false,
      onChange = { d -> onUpdate { it.copy(servicioEsposa = it.servicioEsposa.copy(jueves = d)) } },
    )
    ServiceOnlyDayCard(
      dayName = "Viernes",
      headerBg = DayViernesBg,
      headerTextColor = DayViernesText,
      day = s.viernes,
      initiallyExpanded = false,
      onChange = { d -> onUpdate { it.copy(servicioEsposa = it.servicioEsposa.copy(viernes = d)) } },
    )

    SectionCard(
      title = "Notas adicionales (Página 3)",
      headerBg = MaterialTheme.colorScheme.secondaryContainer,
      headerTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
      OutlinedTextField(
        value = s.notasAdicionales,
        onValueChange = { v ->
          onUpdate { it.copy(servicioEsposa = it.servicioEsposa.copy(notasAdicionales = v)) }
        },
        label = { Text("Notas adicionales") },
        minLines = 3,
        maxLines = 6,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_esposa_notas"),
      )
    }
  }
}

@Composable
fun NotaPastoreoTab(
  formData: VisitaFormData,
  onUpdate: ((VisitaFormData) -> VisitaFormData) -> Unit,
) {
  val n = formData.notaPastoreo
  var showNoteText by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { showNoteText = !showNoteText },
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Nota al coordinador (texto fijo en PDF)",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
          Icon(
            imageVector = if (showNoteText) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = "Ver u ocultar nota",
          )
        }
        AnimatedVisibility(visible = showNoteText) {
          Text(
            text = "Incluye automáticamente las instrucciones del Formulario S-61, Alojamiento (Cap. 15 Pastoreen), Recomendaciones (jw.hub), Reuniones para el servicio, Medio día libre y Visitas de Pastoreo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(top = 8.dp),
          )
        }
      }
    }

    SectionCard(
      title = "Visitas de Pastoreo",
      headerBg = MaterialTheme.colorScheme.primaryContainer,
      headerTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
      PastoringVisitRow(
        day = "Miércoles",
        badgeBg = DayMiercolesBg,
        badgeText = DayMiercolesText,
        persona = n.visitaMiercolesPersona,
        situacion = n.visitaMiercolesSituacion,
        onPersonaChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(visitaMiercolesPersona = v)) } },
        onSituacionChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(visitaMiercolesSituacion = v)) } },
      )
      PastoringVisitRow(
        day = "Jueves",
        badgeBg = DayJuevesBg,
        badgeText = DayJuevesText,
        persona = n.visitaJuevesPersona,
        situacion = n.visitaJuevesSituacion,
        onPersonaChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(visitaJuevesPersona = v)) } },
        onSituacionChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(visitaJuevesSituacion = v)) } },
      )
      PastoringVisitRow(
        day = "Viernes",
        badgeBg = DayViernesBg,
        badgeText = DayViernesText,
        persona = n.visitaViernesPersona,
        situacion = n.visitaViernesSituacion,
        onPersonaChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(visitaViernesPersona = v)) } },
        onSituacionChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(visitaViernesSituacion = v)) } },
      )
    }

    SectionCard(
      title = "Asuntos sugeridos por el cuerpo de ancianos",
      headerBg = MaterialTheme.colorScheme.tertiaryContainer,
      headerTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
      OutlinedTextField(
        value = n.asunto1,
        onValueChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(asunto1 = v)) } },
        label = { Text("Asunto 1") },
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = n.asunto2,
        onValueChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(asunto2 = v)) } },
        label = { Text("Asunto 2") },
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = n.asunto3,
        onValueChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(asunto3 = v)) } },
        label = { Text("Asunto 3") },
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = n.asunto4,
        onValueChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(asunto4 = v)) } },
        label = { Text("Asunto 4") },
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = n.codigoPie,
        onValueChange = { v -> onUpdate { it.copy(notaPastoreo = it.notaPastoreo.copy(codigoPie = v)) } },
        label = { Text("Referencia pie de página (ej. Ago/26)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun DayScheduleEditorCard(
  dayName: String,
  headerBg: Color,
  headerTextColor: Color,
  schedule: DaySchedule,
  initiallyExpanded: Boolean,
  onScheduleChange: (DaySchedule) -> Unit,
  extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
  ExpandableDayCard(
    title = dayName,
    headerBg = headerBg,
    headerTextColor = headerTextColor,
    badgeText = if (schedule.reunionPrecursoresEnabled) "Con precursores" else null,
    initiallyExpanded = initiallyExpanded,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = schedule.salidaAmLugar,
        onValueChange = { onScheduleChange(schedule.copy(salidaAmLugar = it)) },
        label = { Text("Salida AM · Lugar") },
        singleLine = true,
        modifier = Modifier.weight(1.3f),
      )
      TimeSelectorField(
        label = "Hora AM",
        value = schedule.salidaAmHora,
        onValueChange = { onScheduleChange(schedule.copy(salidaAmHora = it)) },
        presets = listOf("8:00 am", "8:30 am", "9:00 am", "9:30 am", "10:00 am"),
        modifier = Modifier.weight(1f),
        testTag = "time_${dayName.lowercase()}_am",
      )
    }
    QuickLugarChips { chosen -> onScheduleChange(schedule.copy(salidaAmLugar = chosen)) }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = schedule.salidaPmLugar,
        onValueChange = { onScheduleChange(schedule.copy(salidaPmLugar = it)) },
        label = { Text("Salida PM · Lugar") },
        singleLine = true,
        modifier = Modifier.weight(1.3f),
      )
      TimeSelectorField(
        label = "Hora PM",
        value = schedule.salidaPmHora,
        onValueChange = { onScheduleChange(schedule.copy(salidaPmHora = it)) },
        presets = listOf("2:00 pm", "2:30 pm", "3:00 pm", "3:30 pm", "4:00 pm"),
        modifier = Modifier.weight(1f),
        testTag = "time_${dayName.lowercase()}_pm",
      )
    }
    QuickLugarChips { chosen -> onScheduleChange(schedule.copy(salidaPmLugar = chosen)) }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      OutlinedTextField(
        value = schedule.almuerzoFamilia,
        onValueChange = { onScheduleChange(schedule.copy(almuerzoFamilia = it)) },
        label = { Text("Almuerzo (Familia)") },
        singleLine = true,
        modifier = Modifier.weight(1.3f),
      )
      OutlinedTextField(
        value = schedule.almuerzoTel,
        onValueChange = { onScheduleChange(schedule.copy(almuerzoTel = it)) },
        label = { Text("Tel.") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.weight(1f),
      )
    }

    ReunionPrecursoresToggleBlock(
      dayName = dayName,
      enabled = schedule.reunionPrecursoresEnabled,
      lugar = schedule.reunionPrecursoresLugar,
      hora = schedule.reunionPrecursoresHora,
      onToggle = { enabled -> onScheduleChange(schedule.copy(reunionPrecursoresEnabled = enabled)) },
      onLugarChange = { lugar -> onScheduleChange(schedule.copy(reunionPrecursoresLugar = lugar)) },
      onHoraChange = { hora -> onScheduleChange(schedule.copy(reunionPrecursoresHora = hora)) },
    )

    extraContent?.invoke(this)
  }
}

@Composable
private fun ReunionPrecursoresToggleBlock(
  dayName: String,
  enabled: Boolean,
  lugar: String,
  hora: String,
  onToggle: (Boolean) -> Unit,
  onLugarChange: (String) -> Unit,
  onHoraChange: (String) -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (enabled) Color(0xFFFFF0EE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    border = BorderStroke(
      width = 1.dp,
      color = if (enabled) CrimsonAccent.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    ),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onToggle(!enabled) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Reunión de precursores ($dayName)",
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) CrimsonAccent else MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (enabled) "Se incluirá este día en el PDF" else "Activar si la reunión es este día",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Switch(
          checked = enabled,
          onCheckedChange = onToggle,
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = CrimsonAccent,
          ),
          modifier = Modifier.testTag("switch_precursores_${dayName.lowercase()}"),
        )
      }

      AnimatedVisibility(visible = enabled) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = lugar,
            onValueChange = onLugarChange,
            label = { Text("Lugar") },
            singleLine = true,
            modifier = Modifier
              .weight(1.3f)
              .testTag("input_precursores_lugar_${dayName.lowercase()}"),
          )
          TimeSelectorField(
            label = "Hora",
            value = hora,
            onValueChange = onHoraChange,
            presets = listOf("10:30 am", "11:00 am", "11:30 am", "12:00 pm", "6:00 pm", "7:00 pm"),
            modifier = Modifier.weight(1f),
            testTag = "input_precursores_hora_${dayName.lowercase()}",
          )
        }
      }
    }
  }
}

@Composable
private fun ServicePastoringDayCard(
  dayName: String,
  headerBg: Color,
  headerTextColor: Color,
  day: ServicePastoringDay,
  initiallyExpanded: Boolean,
  onChange: (ServicePastoringDay) -> Unit,
) {
  ExpandableDayCard(
    title = dayName,
    headerBg = headerBg,
    headerTextColor = headerTextColor,
    initiallyExpanded = initiallyExpanded,
  ) {
    Text(text = "1ra hora", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = day.hora1Con,
        onValueChange = { onChange(day.copy(hora1Con = it)) },
        label = { Text("1ra hora con") },
        singleLine = true,
        modifier = Modifier.weight(1.3f),
      )
      OutlinedTextField(
        value = day.hora1Tel,
        onValueChange = { onChange(day.copy(hora1Tel = it)) },
        label = { Text("Tel.") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.weight(1f),
      )
    }
    OutlinedTextField(
      value = day.hora1EstudioPastoreo,
      onValueChange = { onChange(day.copy(hora1EstudioPastoreo = it)) },
      label = { Text("Estudio/Pastoreo") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = day.hora1Publicacion,
      onValueChange = { onChange(day.copy(hora1Publicacion = it)) },
      label = { Text("Publicación") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(6.dp))
    Text(text = "2da hora", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = day.hora2Con,
        onValueChange = { onChange(day.copy(hora2Con = it)) },
        label = { Text("2da hora con") },
        singleLine = true,
        modifier = Modifier.weight(1.3f),
      )
      OutlinedTextField(
        value = day.hora2Tel,
        onValueChange = { onChange(day.copy(hora2Tel = it)) },
        label = { Text("Tel.") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.weight(1f),
      )
    }
    OutlinedTextField(
      value = day.hora2EstudioPastoreo,
      onValueChange = { onChange(day.copy(hora2EstudioPastoreo = it)) },
      label = { Text("Estudio/Pastoreo") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = day.hora2Publicacion,
      onValueChange = { onChange(day.copy(hora2Publicacion = it)) },
      label = { Text("Publicación") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun ServiceOnlyDayCard(
  dayName: String,
  headerBg: Color,
  headerTextColor: Color,
  day: ServiceOnlyDay,
  initiallyExpanded: Boolean,
  onChange: (ServiceOnlyDay) -> Unit,
) {
  ExpandableDayCard(
    title = dayName,
    headerBg = headerBg,
    headerTextColor = headerTextColor,
    initiallyExpanded = initiallyExpanded,
  ) {
    Text(text = "1ra hora", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = day.hora1Con,
        onValueChange = { onChange(day.copy(hora1Con = it)) },
        label = { Text("1ra hora con") },
        singleLine = true,
        modifier = Modifier.weight(1.3f),
      )
      OutlinedTextField(
        value = day.hora1Tel,
        onValueChange = { onChange(day.copy(hora1Tel = it)) },
        label = { Text("Tel.") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.weight(1f),
      )
    }
    OutlinedTextField(
      value = day.hora1Publicacion,
      onValueChange = { onChange(day.copy(hora1Publicacion = it)) },
      label = { Text("Publicación") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(6.dp))
    Text(text = "2da hora", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = day.hora2Con,
        onValueChange = { onChange(day.copy(hora2Con = it)) },
        label = { Text("2da hora con") },
        singleLine = true,
        modifier = Modifier.weight(1.3f),
      )
      OutlinedTextField(
        value = day.hora2Tel,
        onValueChange = { onChange(day.copy(hora2Tel = it)) },
        label = { Text("Tel.") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.weight(1f),
      )
    }
    OutlinedTextField(
      value = day.hora2Publicacion,
      onValueChange = { onChange(day.copy(hora2Publicacion = it)) },
      label = { Text("Publicación") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun PastoringVisitRow(
  day: String,
  badgeBg: Color,
  badgeText: Color,
  persona: String,
  situacion: String,
  onPersonaChange: (String) -> Unit,
  onSituacionChange: (String) -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = badgeBg,
      ) {
        Text(
          text = day,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = badgeText,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
      OutlinedTextField(
        value = persona,
        onValueChange = onPersonaChange,
        label = { Text("Persona o Familia") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = situacion,
        onValueChange = onSituacionChange,
        label = { Text("Situación por la que se visita") },
        minLines = 2,
        maxLines = 5,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
fun SectionCard(
  title: String,
  headerBg: Color,
  headerTextColor: Color,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(headerBg)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = headerTextColor,
        )
      }
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
      )
    }
  }
}

@Composable
fun ExpandableDayCard(
  title: String,
  headerBg: Color,
  headerTextColor: Color,
  badgeText: String? = null,
  initiallyExpanded: Boolean,
  content: @Composable ColumnScope.() -> Unit,
) {
  var expanded by remember { mutableStateOf(initiallyExpanded) }
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(headerBg)
          .clickable { expanded = !expanded }
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = headerTextColor,
          )
          if (badgeText != null) {
            Surface(
              shape = RoundedCornerShape(50),
              color = CrimsonAccent.copy(alpha = 0.14f),
            ) {
              Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = CrimsonAccent,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
          }
        }
        Icon(
          imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
          contentDescription = if (expanded) "Contraer $title" else "Expandir $title",
          tint = headerTextColor,
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          content = content,
        )
      }
    }
  }
}

@Composable
private fun QuickLugarChips(onPick: (String) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    listOf("Salón del Reino", "Casa de la familia").forEach { place ->
      AssistChip(
        onClick = { onPick(place) },
        label = { Text(place, style = MaterialTheme.typography.labelSmall) },
      )
    }
  }
}
