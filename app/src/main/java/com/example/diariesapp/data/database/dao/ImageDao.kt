package com.example.diariesapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.example.diariesapp.data.database.entity.ImageToUpload

@Dao
interface ImageDao {
    @Query("SELECT * FROM image_to_upload_table ORDER BY id ASC")
    suspend fun getAllImages(): List<ImageToUpload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)

    @Query("DELETE FROM image_to_upload_table WHERE id = :imageId")
    suspend fun deleteImage(imageId: Int)
}