package com.taruninc.kit.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
//
//    @Query("SELECT * FROM user_list WHERE userFirstName IS :userId")
//    fun getInfoByF(userId: String): User
//

    @Query("SELECT * FROM user_list")
    fun getAllUsers(): LiveData<List<User>>

    @Insert
    suspend fun insertNewUser(newUser: User)

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(user: User)

}