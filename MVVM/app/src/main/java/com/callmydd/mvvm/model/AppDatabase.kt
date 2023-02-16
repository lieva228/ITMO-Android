package com.callmydd.mvvm.model

import androidx.room.*

@Database(entities = [MessageDB::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao?
}

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg message: MessageDB?)

    @get:Query("SELECT * FROM MessageDB")
    val allMessage: List<MessageDB?>
}