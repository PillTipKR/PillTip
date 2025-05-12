package com.pilltip.pilltip.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

@Dao
interface UserDatabaseDao {
    @Query("SELECT * FROM user_data_table")
    fun getAll(): List<UserData>

    @Query("SELECT * FROM user_data_table WHERE id = :id")
    fun getUserById(id: UUID): UserData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userData: UserData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(userData: UserData)

    @Query("DELETE FROM user_data_table")
    fun deleteAll()

    @Query("DELETE FROM user_data_table WHERE id = :id")
    fun deleteById(id: UUID)
}
