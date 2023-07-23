package com.example.mongo.repository

import android.util.Log
import coil.network.HttpException
import com.example.util.Constants.APP_ID
import com.example.util.RequestState
import com.example.util.model.Diary
import com.example.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel

import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.io.IOException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime


// this is where we configure de realm ( client ) to communicate with the servers (Mongo Atlas)
object MongoDb : MongoRepository {
    private val app = App.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    // Just to initialize the configuration right after calling the MongoDb
    init {
        configureRealm()
    }

    override fun configureRealm() {
        if (user !== null) {
            val config = SyncConfiguration
                .Builder(user, setOf(Diary::class))
                .initialSubscriptions { realm ->
                    add(
                        query = realm.query<Diary>(
                            "ownerId == $0",
                            user.id
                        ), // we are subscribing to all diaries that contains the ownerId of a currently
                        // authenticated User
                        name = "User's diaries"
                    )

                }.log(LogLevel.ALL)
                .build()
            //val config = RealmConfiguration.create(schema = setOf(Diary::class))

            realm = Realm.open(config)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        return if (user != null) {
            try {
                //  Log.d("REALM_NULL", realm.query<Diary>(query = "_id == $0", "").toString())

                realm.query<Diary>(query = "ownerId == $0", user.id)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map { result ->

                        Log.d("REALM_RESULT", result.toString())
                        val data = result.list.groupBy {
                            it.date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }

                        Log.d("TRYYY", data.toString())
                        RequestState.Success(data = data)
                    }

            } catch (e: HttpException) {
                Log.d("CATCH", e.toString())
                flow { emit(RequestState.Error(e)) }
            } catch (e: IOException) {
                Log.d("CATCH_2", e.toString())
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override fun getFilteredDiaries(zonedDateTime: ZonedDateTime): Flow<Diaries> {
        return if (user != null) {
            try {
                //  Log.d("REALM_NULL", realm.query<Diary>(query = "_id == $0", "").toString())
                realm.query<Diary>(
                    "ownerId == $0 AND date < $1 ANS date > $2",
                    user.id,
                    RealmInstant.from(
                        LocalDateTime.of(
                            zonedDateTime.toLocalDate().plusDays(1),
                            LocalTime.MIDNIGHT
                        ).toEpochSecond(zonedDateTime.offset),
                        0
                    ),
                    RealmInstant.from(
                        LocalDateTime.of(
                            zonedDateTime.toLocalDate(),
                            LocalTime.MIDNIGHT
                        ).toEpochSecond(zonedDateTime.offset),
                        0
                    ),
                ).asFlow().map {
                    RequestState.Success(
                        data = it.list.groupBy { diary ->
                            diary.date
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                    )
                }


            } catch (e: HttpException) {
                Log.d("CATCH", e.toString())
                flow { emit(RequestState.Error(e)) }
            } catch (e: IOException) {
                Log.d("CATCH_2", e.toString())
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override fun getSelectedDiary(diaryId: ObjectId): Flow<RequestState<Diary>> {
        /*return if (user != null) {

            try {
                realm.query<Diary>(query = "_id == $0", diaryId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }

            } catch (e: HttpException) {
                flow { RequestState.Error(e) }
            } catch (e: IOException) {
                flow { RequestState.Error(e) }
            }

        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }

        }*/

        // 2 ways to return a flow . the one commented or the one below :


        if (user != null) {
            return flow {
                try {
                    val diary = realm.query<Diary>(query = "_id == $0", diaryId).find().first()
                    emit(RequestState.Success(data = diary))

                } catch (e: HttpException) {

                    emit(RequestState.Error(e))

                } catch (e: IOException) {
                    emit(RequestState.Error(e))
                }
            }
        } else {
            return flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override suspend fun insertNewDiary(diary: Diary): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                try {
                    val addedDiary = copyToRealm(diary.apply { ownerId = user.id })
                    RequestState.Success(data = addedDiary)
                } catch (e: HttpException) {
                    RequestState.Error(e)
                } catch (e: IOException) {
                    RequestState.Error(e)
                }
            }

        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }

    }

    override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                val query = query<Diary>(query = "_id == $0", diary._id).first().find()
                if (query != null) {
                    query.title = diary.title
                    query.description = diary.description
                    query.mood = diary.mood
                    query.date = diary.date
                    RequestState.Success(data = query)
                } else {
                    RequestState.Error(error = java.lang.Exception("Queried diary does not exist"))
                }
            }

        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteDiary(id: ObjectId): RequestState<Boolean> {
        return if (user != null) {
            realm.write {
                try {
                    val diary =
                        query<Diary>(query = " _id == $0 AND ownerId == $1", id, user.id).first()
                            .find()
                    if (diary != null) {
                        delete(diary)
                        RequestState.Success(data = true)
                    } else {
                        RequestState.Error(java.lang.Exception("diary does not exist"))
                    }
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }

        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteAllDiaries(): RequestState<Boolean> {
        return if (user != null) {
            realm.write {
                val diaries = this.query<Diary>("ownerId == $0", user.id).find()
                try {
                    delete(diaries)
                    RequestState.Success(data = true)

                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }

        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

}


// Custom error when User is not authenticated.
private class UserNotAuthenticatedException : Exception("User is not authenticated")

