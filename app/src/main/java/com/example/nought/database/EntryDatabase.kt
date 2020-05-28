package com.example.nought.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Entry::class], version = 1, exportSchema = false)
abstract class EntryDatabase : RoomDatabase() {
    abstract val entryDatabaseDao: EntryDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: EntryDatabase? = null

        fun getInstance(context: Context): EntryDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        EntryDatabase::class.java,
                        "entries_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}