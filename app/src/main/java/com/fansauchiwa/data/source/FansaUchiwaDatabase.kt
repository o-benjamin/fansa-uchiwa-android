package com.fansauchiwa.data.source

import android.content.Context
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fansauchiwa.data.Converters

@Database(
    entities = [
        FansaUchiwaEntity::class,
        EventEntity::class,
        EventUchiwaCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FansaUchiwaDatabase : RoomDatabase() {
    abstract fun uchiwaDao(): FansaUchiwaDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `events` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `eventDateEpochDay` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_events_eventDateEpochDay` ON `events` (`eventDateEpochDay`)"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `event_uchiwa_cross_refs` (
                        `eventId` TEXT NOT NULL,
                        `uchiwaId` TEXT NOT NULL,
                        PRIMARY KEY(`eventId`, `uchiwaId`),
                        FOREIGN KEY(`eventId`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`uchiwaId`) REFERENCES `fansa_uchiwa_data`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_event_uchiwa_cross_refs_eventId` ON `event_uchiwa_cross_refs` (`eventId`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_event_uchiwa_cross_refs_uchiwaId` ON `event_uchiwa_cross_refs` (`uchiwaId`)"
                )
            }
        }

        fun build(context: Context): FansaUchiwaDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FansaUchiwaDatabase::class.java,
                "uchiwaData.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
