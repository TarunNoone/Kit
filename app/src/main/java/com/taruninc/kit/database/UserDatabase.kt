package com.taruninc.kit.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version=1)
abstract class UserDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context):UserDatabase {
            if(INSTANCE == null) {
                synchronized(context) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        UserDatabase::class.java,
                        "event_database"
                    ).build()
                }
            }

            return INSTANCE!! //This variable cannot be null
        }
    }
}