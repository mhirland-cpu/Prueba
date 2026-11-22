package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {
  @Query("SELECT * FROM drafts WHERE isBaseTemplate = 0 ORDER BY updatedAt DESC")
  fun observeDrafts(): Flow<List<DraftEntity>>

  @Query("SELECT * FROM drafts WHERE isBaseTemplate = 1 LIMIT 1")
  fun observeBaseTemplate(): Flow<DraftEntity?>

  @Query("SELECT * FROM drafts WHERE isBaseTemplate = 1 LIMIT 1")
  suspend fun getBaseTemplate(): DraftEntity?

  @Query("SELECT * FROM drafts WHERE id = :id LIMIT 1")
  suspend fun getDraftById(id: Long): DraftEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDraft(draft: DraftEntity): Long

  @Update
  suspend fun updateDraft(draft: DraftEntity)

  @Query("DELETE FROM drafts WHERE id = :id AND isBaseTemplate = 0")
  suspend fun deleteDraftById(id: Long)
}
