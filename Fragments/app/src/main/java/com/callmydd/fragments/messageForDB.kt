package com.callmydd.fragments

import androidx.room.*

@Entity
data class MessageForDB(
    @PrimaryKey val uid : Int,
    @ColumnInfo val time: String,
    @ColumnInfo val name: String,
    @ColumnInfo val text: String?= null,
    @ColumnInfo val url: String? = null
)

@Dao
interface MessageDao {
    @Insert
    fun insert(vararg message: MessageForDB?)

    @get:Query("SELECT * FROM MessageForDB")
    val allPeople: List<MessageForDB?>?
}

@Database(
    entities = [MessageForDB::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao?
}