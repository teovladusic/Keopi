package com.techpuzzle.keopi.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.techpuzzle.keopi.data.entities.CafeBar

@Database(
    entities = [CafeBar::class],
    version = 1
)
abstract class KeopiDatabase : RoomDatabase() {
    abstract fun getDao() : KeopiDao
}