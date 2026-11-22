package com.example.data

import com.example.model.BaseTemplateFactory
import com.example.model.VisitaFormData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DraftItem(
  val id: Long,
  val congregacion: String,
  val fechaVisita: String,
  val formData: VisitaFormData,
  val isBaseTemplate: Boolean,
  val createdAt: Long,
  val updatedAt: Long,
)

class DraftRepository(private val draftDao: DraftDao) {
  private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val adapter = moshi.adapter(VisitaFormData::class.java)

  fun observeDrafts(): Flow<List<DraftItem>> =
    draftDao.observeDrafts().map { list -> list.map { it.toDraftItem() } }

  fun observeBaseTemplate(): Flow<DraftItem?> =
    draftDao.observeBaseTemplate().map { it?.toDraftItem() }

  suspend fun ensureBaseTemplateSeeded(): DraftItem {
    val existing = draftDao.getBaseTemplate()
    if (existing != null) return existing.toDraftItem()
    val defaultForm = BaseTemplateFactory.createOriginalPdfBase()
    val entity = DraftEntity(
      congregacion = "Plantilla Base",
      fechaVisita = "",
      formJson = serialize(defaultForm),
      isBaseTemplate = true,
    )
    val id = draftDao.insertDraft(entity)
    return entity.copy(id = id).toDraftItem()
  }

  suspend fun createNewDraftFromBase(): DraftItem {
    val base = ensureBaseTemplateSeeded()
    val now = System.currentTimeMillis()
    val entity = DraftEntity(
      congregacion = base.formData.actividades.congregacion,
      fechaVisita = base.formData.actividades.fecha,
      formJson = serialize(base.formData),
      isBaseTemplate = false,
      createdAt = now,
      updatedAt = now,
    )
    val id = draftDao.insertDraft(entity)
    return entity.copy(id = id).toDraftItem()
  }

  suspend fun duplicateDraft(source: DraftItem): DraftItem {
    val now = System.currentTimeMillis()
    val copySuffix = if (source.congregacion.isNotBlank()) "${source.congregacion} (Copia)" else ""
    val updatedForm = source.formData.copy(
      actividades = source.formData.actividades.copy(congregacion = copySuffix)
    )
    val entity = DraftEntity(
      congregacion = copySuffix,
      fechaVisita = source.fechaVisita,
      formJson = serialize(updatedForm),
      isBaseTemplate = false,
      createdAt = now,
      updatedAt = now,
    )
    val id = draftDao.insertDraft(entity)
    return entity.copy(id = id).toDraftItem()
  }

  suspend fun saveDraft(draftId: Long, formData: VisitaFormData, isBaseTemplate: Boolean) {
    val existing = draftDao.getDraftById(draftId) ?: return
    val updated = existing.copy(
      congregacion = if (isBaseTemplate) "Plantilla Base" else formData.actividades.congregacion,
      fechaVisita = formData.actividades.fecha,
      formJson = serialize(formData),
      updatedAt = System.currentTimeMillis(),
    )
    draftDao.updateDraft(updated)
  }

  suspend fun resetBaseTemplateToPdfOriginal(): DraftItem {
    val base = ensureBaseTemplateSeeded()
    val original = BaseTemplateFactory.createOriginalPdfBase()
    saveDraft(base.id, original, isBaseTemplate = true)
    return base.copy(formData = original, updatedAt = System.currentTimeMillis())
  }

  suspend fun deleteDraft(id: Long) {
    draftDao.deleteDraftById(id)
  }

  private fun serialize(data: VisitaFormData): String = adapter.toJson(data)

  private fun deserialize(json: String): VisitaFormData =
    runCatching { adapter.fromJson(json) }.getOrNull() ?: BaseTemplateFactory.createOriginalPdfBase()

  private fun DraftEntity.toDraftItem(): DraftItem = DraftItem(
    id = id,
    congregacion = congregacion,
    fechaVisita = fechaVisita,
    formData = deserialize(formJson),
    isBaseTemplate = isBaseTemplate,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )
}
