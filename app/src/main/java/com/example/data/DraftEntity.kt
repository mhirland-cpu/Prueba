package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drafts")
data class DraftEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val congregacion: String,
  val fechaVisita: String,
  val formJson: String,
  val isBaseTemplate: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)
