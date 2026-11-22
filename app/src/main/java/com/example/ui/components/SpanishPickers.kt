package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar
import java.util.Locale

private val SPANISH_MONTHS = listOf(
  "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
  "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)

private val SPANISH_MONTHS_LOWER = listOf(
  "enero", "febrero", "marzo", "abril", "mayo", "junio",
  "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

private val SPANISH_WEEKDAYS = listOf("L", "M", "X", "J", "V", "S", "D")

data class SimpleDay(val year: Int, val monthZeroBased: Int, val day: Int) : Comparable<SimpleDay> {
  override fun compareTo(other: SimpleDay): Int {
    if (year != other.year) return year.compareTo(other.year)
    if (monthZeroBased != other.monthZeroBased) return monthZeroBased.compareTo(other.monthZeroBased)
    return day.compareTo(other.day)
  }
}

fun formatSpanishDateRange(start: SimpleDay, end: SimpleDay?): String {
  val endDay = end ?: start
  val (first, second) = if (start <= endDay) start to endDay else endDay to start
  val m1 = SPANISH_MONTHS_LOWER[first.monthZeroBased]
  val m2 = SPANISH_MONTHS_LOWER[second.monthZeroBased]

  return when {
    first == second -> "${first.day} de $m1 de ${first.year}"
    first.year == second.year && first.monthZeroBased == second.monthZeroBased ->
      "${first.day} al ${second.day} de $m1 de ${first.year}"
    first.year == second.year ->
      "${first.day} de $m1 al ${second.day} de $m2 de ${first.year}"
    else ->
      "${first.day} de $m1 de ${first.year} al ${second.day} de $m2 de ${second.year}"
  }
}

@Composable
fun SpanishDateRangePickerDialog(
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  val todayCal = remember { Calendar.getInstance() }
  var displayYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
  var displayMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }

  var startDate by remember { mutableStateOf<SimpleDay?>(null) }
  var endDate by remember { mutableStateOf<SimpleDay?>(null) }
  var showMonthYearPicker by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Seleccionar fecha de visita",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.primary,
        )

        // Selected range pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = when {
              startDate == null -> "Toca el día de inicio y luego el día final"
              endDate == null -> "Inicio: ${formatSpanishDateRange(startDate!!, null)} (toca día final)"
              else -> formatSpanishDateRange(startDate!!, endDate)
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
          )
        }

        // Month & year navigation bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = {
              if (displayMonth == 0) {
                displayMonth = 11
                displayYear -= 1
              } else {
                displayMonth -= 1
              }
            },
            modifier = Modifier.testTag("calendar_prev_month"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
              contentDescription = "Mes anterior",
              tint = MaterialTheme.colorScheme.primary,
            )
          }

          Surface(
            onClick = { showMonthYearPicker = !showMonthYearPicker },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
          ) {
            Text(
              text = "${SPANISH_MONTHS[displayMonth]} $displayYear",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
          }

          IconButton(
            onClick = {
              if (displayMonth == 11) {
                displayMonth = 0
                displayYear += 1
              } else {
                displayMonth += 1
              }
            },
            modifier = Modifier.testTag("calendar_next_month"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = "Mes siguiente",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }

        if (showMonthYearPicker) {
          // Quick Year + Month jump grid in Spanish
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedButton(onClick = { displayYear -= 1 }) { Text("${displayYear - 1}") }
              Text(
                text = "$displayYear",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
              )
              OutlinedButton(onClick = { displayYear += 1 }) { Text("${displayYear + 1}") }
            }
            for (rowIndex in 0 until 4) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                for (colIndex in 0 until 3) {
                  val m = rowIndex * 3 + colIndex
                  val isCurrent = m == displayMonth
                  Surface(
                    onClick = {
                      displayMonth = m
                      showMonthYearPicker = false
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f),
                  ) {
                    Text(
                      text = SPANISH_MONTHS[m].take(3),
                      style = MaterialTheme.typography.labelLarge,
                      color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(vertical = 8.dp),
                    )
                  }
                }
              }
            }
          }
        } else {
          // Weekday headers (L M X J V S D)
          Row(modifier = Modifier.fillMaxWidth()) {
            SPANISH_WEEKDAYS.forEach { wd ->
              Text(
                text = wd,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
              )
            }
          }

          val weeks = remember(displayYear, displayMonth) { buildMonthGrid(displayYear, displayMonth) }
          val rangeOrdered = remember(startDate, endDate) {
            val s = startDate
            val e = endDate
            if (s != null && e != null) {
              if (s <= e) s to e else e to s
            } else {
              null
            }
          }

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            weeks.forEach { week ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                week.forEach { day ->
                  if (day == null) {
                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                  } else {
                    val isStart = day == startDate
                    val isEnd = day == endDate
                    val inRange = rangeOrdered != null && day >= rangeOrdered.first && day <= rangeOrdered.second
                    val isEndpoint = isStart || isEnd

                    val bgColor = when {
                      isEndpoint -> MaterialTheme.colorScheme.primary
                      inRange -> MaterialTheme.colorScheme.primaryContainer
                      else -> Color.Transparent
                    }
                    val txtColor = when {
                      isEndpoint -> Color.White
                      inRange -> MaterialTheme.colorScheme.onPrimaryContainer
                      else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable {
                          when {
                            startDate == null || (startDate != null && endDate != null) -> {
                              startDate = day
                              endDate = null
                            }
                            else -> {
                              endDate = day
                            }
                          }
                        },
                      contentAlignment = Alignment.Center,
                    ) {
                      Text(
                        text = "${day.day}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isEndpoint) FontWeight.Bold else FontWeight.Normal,
                        color = txtColor,
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancelar")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            enabled = startDate != null,
            onClick = {
              val s = startDate ?: return@Button
              onConfirm(formatSpanishDateRange(s, endDate))
            },
            modifier = Modifier.testTag("calendar_confirm_button"),
          ) {
            Text("Aplicar")
          }
        }
      }
    }
  }
}

private fun buildMonthGrid(year: Int, monthZeroBased: Int): List<List<SimpleDay?>> {
  val cal = Calendar.getInstance().apply {
    set(Calendar.YEAR, year)
    set(Calendar.MONTH, monthZeroBased)
    set(Calendar.DAY_OF_MONTH, 1)
  }
  val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
  // Calendar.MONDAY == 2 -> offset 0; SUNDAY == 1 -> offset 6
  val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
  val leadingEmpty = (firstDayOfWeek + 5) % 7

  val cells = mutableListOf<SimpleDay?>()
  repeat(leadingEmpty) { cells.add(null) }
  for (d in 1..daysInMonth) {
    cells.add(SimpleDay(year, monthZeroBased, d))
  }
  while (cells.size % 7 != 0) {
    cells.add(null)
  }
  return cells.chunked(7)
}

@Composable
fun TimeSelectorField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  presets: List<String> = DEFAULT_TIME_PRESETS,
  testTag: String? = null,
) {
  var showDialog by remember { mutableStateOf(false) }

  Surface(
    onClick = { showDialog = true },
    shape = RoundedCornerShape(10.dp),
    color = if (value.isNotBlank()) {
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
      MaterialTheme.colorScheme.surface
    },
    border = BorderStroke(
      width = 1.dp,
      color = if (value.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
    ),
    modifier = modifier
      .height(56.dp)
      .let { if (testTag != null) it.testTag(testTag) else it },
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Icon(
          imageVector = Icons.Filled.AccessTime,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = value.ifBlank { "Elegir hora" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (value.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
            color = if (value.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      if (value.isNotBlank()) {
        IconButton(
          onClick = { onValueChange("") },
          modifier = Modifier.size(28.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Limpiar hora",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }

  if (showDialog) {
    SpanishTimePickerDialog(
      title = label,
      initialValue = value,
      presets = presets,
      onDismiss = { showDialog = false },
      onSelectTime = { chosen ->
        onValueChange(chosen)
        showDialog = false
      },
    )
  }
}

private val DEFAULT_TIME_PRESETS = listOf(
  "8:00 am", "8:30 am", "9:00 am", "9:30 am",
  "11:30 am", "2:00 pm", "2:30 pm", "3:00 pm",
  "6:00 pm", "7:00 pm", "7:30 pm",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpanishTimePickerDialog(
  title: String,
  initialValue: String,
  presets: List<String>,
  onDismiss: () -> Unit,
  onSelectTime: (String) -> Unit,
) {
  val parsed = remember(initialValue) { parseTimeString(initialValue) }
  var selectedHour by remember { mutableIntStateOf(parsed.first) }
  var selectedMinute by remember { mutableIntStateOf(parsed.second) }
  var isPm by remember { mutableStateOf(parsed.third) }

  val formattedPreview = formatTime12h(selectedHour, selectedMinute, isPm)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier
          .padding(18.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Seleccionar hora · $title",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary,
        )

        // Large visual clock display
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = formattedPreview,
              style = MaterialTheme.typography.headlineLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              PeriodChip(
                text = "AM",
                selected = !isPm,
                onClick = { isPm = false },
              )
              PeriodChip(
                text = "PM",
                selected = isPm,
                onClick = { isPm = true },
              )
            }
          }
        }

        // Quick 1-tap presets
        Text(
          text = "Horas más usadas (1 toque)",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          presets.forEach { preset ->
            AssistChip(
              onClick = { onSelectTime(preset) },
              label = { Text(preset) },
            )
          }
        }

        // Hour Grid (1..12)
        Text(
          text = "Hora",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          (1..12).forEach { hr ->
            val selected = hr == selectedHour
            SelectablePill(
              text = hr.toString(),
              selected = selected,
              onClick = { selectedHour = hr },
            )
          }
        }

        // Minute Grid (00, 05, ..., 55)
        Text(
          text = "Minutos",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          (0..55 step 5).forEach { min ->
            val selected = min == selectedMinute
            SelectablePill(
              text = String.format(Locale.US, "%02d", min),
              selected = selected,
              onClick = { selectedMinute = min },
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = { onSelectTime("") }) {
            Text("Dejar vacía")
          }
          Row {
            TextButton(onClick = onDismiss) {
              Text("Cancelar")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(
              onClick = { onSelectTime(formattedPreview) },
              modifier = Modifier.testTag("time_picker_confirm"),
            ) {
              Text("Aceptar")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PeriodChip(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(10.dp),
    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = if (selected) Color.White else MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    )
  }
}

@Composable
private fun SelectablePill(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .size(width = 46.dp, height = 40.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
      .border(
        width = 1.dp,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
      )
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
    )
  }
}

fun formatTime12h(hour12: Int, minute: Int, isPm: Boolean): String {
  val safeHour = hour12.coerceIn(1, 12)
  val safeMin = minute.coerceIn(0, 59)
  val suffix = if (isPm) "pm" else "am"
  return "$safeHour:${String.format(Locale.US, "%02d", safeMin)} $suffix"
}

private fun parseTimeString(raw: String): Triple<Int, Int, Boolean> {
  val clean = raw.trim().lowercase(Locale.US)
  val regex = Regex("""(\d{1,2}):(\d{2})\s*(am|pm)?""")
  val match = regex.find(clean)
  if (match != null) {
    val h = match.groupValues[1].toIntOrNull()?.coerceIn(1, 12) ?: 9
    val m = match.groupValues[2].toIntOrNull()?.coerceIn(0, 59) ?: 0
    val pm = match.groupValues[3] == "pm"
    return Triple(h, m, pm)
  }
  return Triple(9, 0, false)
}
