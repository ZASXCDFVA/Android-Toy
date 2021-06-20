package com.github.azsxcdfva.toy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserAccessor {
    @Query("SELECT EXISTS(SELECT * FROM users WHERE username = :username AND password = :password)")
    suspend fun verify(username: String, password: String): Boolean

    @Insert
    suspend fun register(user: User)
}
