package com.example.firebase

import android.content.Context
import com.example.R
import com.example.model.VisitaFormData
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.security.SecureRandom
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SharedArregloRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id),
    ),
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("Debes iniciar sesión con Google para colaborar en línea.")

  private fun currentUserDisplayName(): String {
    val user = auth.currentUser
    val raw = user?.displayName?.takeIf { it.isNotBlank() }
      ?: user?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
      ?: "Colaborador"
    return raw.trim().take(100).ifBlank { "Colaborador" }
  }

  fun observeSharedArreglos(userId: String): Flow<List<SharedArregloSummary>> = flow {
    require(userId.isNotBlank()) { "El usuario autenticado es obligatorio." }
    emitAll(
      db.collection(COLLECTION_SHARED_ARREGLOS)
        .whereArrayContains("memberUids", userId)
        .snapshots()
        .map { snapshot ->
          snapshot.documents
            .mapNotNull { it.toSharedArregloSummary() }
            .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          logAndRethrowIfActive(error, userId, OperationType.LIST, COLLECTION_SHARED_ARREGLOS)
        },
    )
  }

  fun observeSharedArregloDetail(arregloId: String): Flow<SharedArregloDetail?> = flow {
    val uid = requireUserId()
    val parentRef = db.collection(COLLECTION_SHARED_ARREGLOS).document(arregloId)
    val sectionsRef = parentRef.collection(SUBCOLLECTION_SECTIONS)

    val parentFlow = parentRef.snapshots()
    val actFlow = sectionsRef.document(ArregloSectionKey.ACTIVIDADES.docId).snapshots()
    val pastFlow = sectionsRef.document(ArregloSectionKey.SERVICIO_PASTOREO.docId).snapshots()
    val esposaFlow = sectionsRef.document(ArregloSectionKey.SERVICIO_ESPOSA.docId).snapshots()
    val notaFlow = sectionsRef.document(ArregloSectionKey.NOTA_PASTOREO.docId).snapshots()

    emitAll(
      combine(parentFlow, actFlow, pastFlow, esposaFlow, notaFlow) {
          parentSnap: DocumentSnapshot,
          actSnap: DocumentSnapshot,
          pastSnap: DocumentSnapshot,
          esposaSnap: DocumentSnapshot,
          notaSnap: DocumentSnapshot,
        ->
        val summary = parentSnap.toSharedArregloSummary() ?: return@combine null
        val formData = VisitaFormData(
          actividades = actSnap.sectionPayloadMap().toActividadesPage(),
          servicioPastoreo = pastSnap.sectionPayloadMap().toServicioPastoreoPage(),
          servicioEsposa = esposaSnap.sectionPayloadMap().toServicioEsposaPage(),
          notaPastoreo = notaSnap.sectionPayloadMap().toNotaPastoreoPage(),
        )
        val metas = mapOf(
          ArregloSectionKey.ACTIVIDADES to actSnap.toSectionEditMeta(),
          ArregloSectionKey.SERVICIO_PASTOREO to pastSnap.toSectionEditMeta(),
          ArregloSectionKey.SERVICIO_ESPOSA to esposaSnap.toSectionEditMeta(),
          ArregloSectionKey.NOTA_PASTOREO to notaSnap.toSectionEditMeta(),
        )
        SharedArregloDetail(
          summary = summary,
          formData = formData,
          sectionMetas = metas,
        )
      }.catch { error ->
        logAndRethrowIfActive(
          error = error,
          uid = uid,
          operationType = OperationType.GET,
          path = "$COLLECTION_SHARED_ARREGLOS/$arregloId",
        )
      },
    )
  }

  suspend fun createSharedArreglo(initialData: VisitaFormData): Result<String> {
    var path = COLLECTION_SHARED_ARREGLOS
    return try {
      val uid = requireUserId()
      val displayName = currentUserDisplayName()
      val code = generateShareCode()
      path = "$COLLECTION_SHARED_ARREGLOS/$code"

      val parentRef = db.collection(COLLECTION_SHARED_ARREGLOS).document(code)
      val sectionsRef = parentRef.collection(SUBCOLLECTION_SECTIONS)
      val batch = db.batch()

      batch.set(
        parentRef,
        mapOf(
          "ownerId" to uid,
          "ownerName" to displayName,
          "memberUids" to listOf(uid),
          "memberNames" to mapOf(uid to displayName),
          "congregacion" to initialData.actividades.congregacion.take(200),
          "fechaVisita" to initialData.actividades.fecha.take(200),
          "lastEditedByUid" to uid,
          "lastEditedByName" to displayName,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )

      batch.set(
        sectionsRef.document(ArregloSectionKey.ACTIVIDADES.docId),
        sectionCreateMap(
          sectionId = ArregloSectionKey.ACTIVIDADES.docId,
          uid = uid,
          displayName = displayName,
          payload = initialData.actividades.toFirestoreMap(),
        ),
      )
      batch.set(
        sectionsRef.document(ArregloSectionKey.SERVICIO_PASTOREO.docId),
        sectionCreateMap(
          sectionId = ArregloSectionKey.SERVICIO_PASTOREO.docId,
          uid = uid,
          displayName = displayName,
          payload = initialData.servicioPastoreo.toFirestoreMap(),
        ),
      )
      batch.set(
        sectionsRef.document(ArregloSectionKey.SERVICIO_ESPOSA.docId),
        sectionCreateMap(
          sectionId = ArregloSectionKey.SERVICIO_ESPOSA.docId,
          uid = uid,
          displayName = displayName,
          payload = initialData.servicioEsposa.toFirestoreMap(),
        ),
      )
      batch.set(
        sectionsRef.document(ArregloSectionKey.NOTA_PASTOREO.docId),
        sectionCreateMap(
          sectionId = ArregloSectionKey.NOTA_PASTOREO.docId,
          uid = uid,
          displayName = displayName,
          payload = initialData.notaPastoreo.toFirestoreMap(),
        ),
      )

      batch.commit().await()
      Result.success(code)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun joinByCode(rawCode: String): Result<String> {
    val code = rawCode.trim().uppercase()
    if (!VALID_ID_REGEX.matches(code)) {
      return Result.failure(
        IllegalArgumentException("El código debe tener entre 6 y 20 caracteres alfanuméricos."),
      )
    }
    val path = "$COLLECTION_SHARED_ARREGLOS/$code"
    return try {
      val uid = requireUserId()
      val displayName = currentUserDisplayName()
      db.collection(COLLECTION_SHARED_ARREGLOS)
        .document(code)
        .update(
          mapOf(
            "memberUids" to FieldValue.arrayUnion(uid),
            "memberNames.$uid" to displayName,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
        .await()
      Result.success(code)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      val friendly = when ((e as? FirebaseFirestoreException)?.code) {
        FirebaseFirestoreException.Code.NOT_FOUND ->
          IllegalStateException("No se encontró ningún arreglo compartido con el código $code.")
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
          IllegalStateException("No se pudo unir al arreglo $code. Verifica el código o el límite de colaboradores.")
        else -> e
      }
      Result.failure(friendly)
    }
  }

  suspend fun updateSection(
    arregloId: String,
    section: ArregloSectionKey,
    formData: VisitaFormData,
  ): Result<Unit> {
    val path = "$COLLECTION_SHARED_ARREGLOS/$arregloId/$SUBCOLLECTION_SECTIONS/${section.docId}"
    return try {
      val uid = requireUserId()
      require(VALID_ID_REGEX.matches(arregloId)) { "Código de arreglo inválido." }
      val displayName = currentUserDisplayName()

      val payload = when (section) {
        ArregloSectionKey.ACTIVIDADES -> formData.actividades.toFirestoreMap()
        ArregloSectionKey.SERVICIO_PASTOREO -> formData.servicioPastoreo.toFirestoreMap()
        ArregloSectionKey.SERVICIO_ESPOSA -> formData.servicioEsposa.toFirestoreMap()
        ArregloSectionKey.NOTA_PASTOREO -> formData.notaPastoreo.toFirestoreMap()
      }

      val parentRef = db.collection(COLLECTION_SHARED_ARREGLOS).document(arregloId)
      val sectionRef = parentRef.collection(SUBCOLLECTION_SECTIONS).document(section.docId)
      val batch = db.batch()

      batch.update(
        sectionRef,
        mapOf(
          "payload" to payload,
          "lastEditedByUid" to uid,
          "lastEditedByName" to displayName,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )

      val parentUpdates = mutableMapOf<String, Any>(
        "lastEditedByUid" to uid,
        "lastEditedByName" to displayName,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      if (section == ArregloSectionKey.ACTIVIDADES) {
        parentUpdates["congregacion"] = formData.actividades.congregacion.take(200)
        parentUpdates["fechaVisita"] = formData.actividades.fecha.take(200)
      }
      batch.update(parentRef, parentUpdates)

      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun leaveSharedArreglo(arregloId: String): Result<Unit> {
    val path = "$COLLECTION_SHARED_ARREGLOS/$arregloId"
    return try {
      val uid = requireUserId()
      db.collection(COLLECTION_SHARED_ARREGLOS)
        .document(arregloId)
        .update(
          mapOf(
            "memberUids" to FieldValue.arrayRemove(uid),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteSharedArreglo(arregloId: String): Result<Unit> {
    val path = "$COLLECTION_SHARED_ARREGLOS/$arregloId"
    return try {
      requireUserId()
      val parentRef = db.collection(COLLECTION_SHARED_ARREGLOS).document(arregloId)
      val sectionsRef = parentRef.collection(SUBCOLLECTION_SECTIONS)
      val batch = db.batch()
      ArregloSectionKey.entries.forEach { section ->
        batch.delete(sectionsRef.document(section.docId))
      }
      batch.delete(parentRef)
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }

  private fun sectionCreateMap(
    sectionId: String,
    uid: String,
    displayName: String,
    payload: Map<String, Any>,
  ): Map<String, Any> = mapOf(
    "sectionType" to sectionId,
    "lastEditedByUid" to uid,
    "lastEditedByName" to displayName,
    "payload" to payload,
    "updatedAt" to FieldValue.serverTimestamp(),
  )

  private fun logAndRethrowIfActive(
    error: Throwable,
    uid: String,
    operationType: OperationType,
    path: String,
  ): Nothing {
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val exceptionToLog = firestoreError ?: (error as? Exception)
    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
      handleFirestoreError(exceptionToLog, operationType, path)
    }
    throw error
  }

  private fun generateShareCode(): String {
    val chars = CODE_ALPHABET
    return buildString(8) {
      repeat(8) {
        append(chars[secureRandom.nextInt(chars.length)])
      }
    }
  }

  companion object {
    const val COLLECTION_SHARED_ARREGLOS = "shared_arreglos"
    const val SUBCOLLECTION_SECTIONS = "sections"
    private const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val VALID_ID_REGEX = Regex("^[a-zA-Z0-9_\\-]{6,128}$")
    private val secureRandom = SecureRandom()
  }
}
