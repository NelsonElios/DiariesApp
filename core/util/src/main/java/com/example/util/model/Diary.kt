package com.example.util.model


import com.example.util.toRealmInstant
import io.realm.kotlin.ext.realmListOf
//import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.time.Instant


// This class will be store in our mongoDb . and we have to use the var for the variables

open class Diary : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()
    var ownerId: String = ""
    var title: String = ""
    var description: String = ""
    var mood: String = Mood.Neutral.name
    var images: RealmList<String> = realmListOf()
    var date: RealmInstant = Instant.now().toRealmInstant()

}

