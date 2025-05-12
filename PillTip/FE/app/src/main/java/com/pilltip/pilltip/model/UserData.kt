package com.pilltip.pilltip.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.Date
import java.util.UUID

@Entity(tableName = "user_data_table")
data class UserData (
    @PrimaryKey
    val id : UUID = UUID.randomUUID(),

    @ColumnInfo(name = "kakao_id")
    val kakaoId : Long,

    @ColumnInfo(name = "nickname")
    val nickname : String,

    @ColumnInfo(name = "profile_img")
    val profileImg : String,

    @ColumnInfo(name = "email")
    val email : String,

    @ColumnInfo(name = "gender")
    val gender : String,

    @ColumnInfo(name = "birth_day")
    val birthYear : Int,

    @ColumnInfo(name = "birth_month")
    val birthMonth : Int,
)