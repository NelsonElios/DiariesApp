package com.example.diariesapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.diariesapp.utils.Constants


@Entity(tableName = Constants.IMAGE_TO_DELETE_TABLE)
data class ImageToDelete(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteImagePath: String,
   // val imageUri: String?,
 //   val sessionUri: String?,
)
