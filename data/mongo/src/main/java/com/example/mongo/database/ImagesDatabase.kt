package com.example.mongo.database

import androidx.room.Database
import androidx.room.RoomDatabase

import com.example.mongo.database.dao.ImageDao
import com.example.mongo.database.dao.ImageToDeleteDao
import com.example.mongo.database.entity.ImageToDelete
import com.example.mongo.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase : RoomDatabase() {
    abstract val imageToUploadDao: ImageDao
    abstract val imageToDeleteDao: ImageToDeleteDao
}