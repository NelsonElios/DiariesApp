package com.example.diariesapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.diariesapp.data.database.dao.ImageDao
import com.example.diariesapp.data.database.dao.ImageToDeleteDao
import com.example.diariesapp.data.database.entity.ImageToDelete
import com.example.diariesapp.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase : RoomDatabase() {
    abstract val imageToUploadDao: ImageDao
    abstract val imageToDeleteDao: ImageToDeleteDao
}