package com.callmydd.mvvm.model

import androidx.room.*

@Entity
data class MessageDB(
    @PrimaryKey val uid : Int,
    @ColumnInfo val from: String,
    @ColumnInfo val to: String,
    @ColumnInfo val text: String?= null,
    @ColumnInfo val url: String? = null
)