package com.fansauchiwa.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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
    version = 5,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `events` ADD COLUMN `remindEnabled` INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                var hasThumbnail = false
                val cursor = database.query("PRAGMA table_info(`events`)")
                try {
                    while (cursor.moveToNext()) {
                        val nameIndex = cursor.getColumnIndex("name")
                        if (nameIndex != -1) {
                            if (cursor.getString(nameIndex) == "thumbnailImagePath") {
                                hasThumbnail = true
                                break
                            }
                        }
                    }
                } finally {
                    cursor.close()
                }

                // テーブルを再作成してスキーマを正規化する
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `events_new` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `eventDateEpochDay` INTEGER NOT NULL,
                        `remindEnabled` INTEGER NOT NULL DEFAULT 1,
                        `thumbnailImagePath` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                if (hasThumbnail) {
                    database.execSQL(
                        "INSERT INTO `events_new` (`id`, `name`, `eventDateEpochDay`, `remindEnabled`, `thumbnailImagePath`) SELECT `id`, `name`, `eventDateEpochDay`, `remindEnabled`, `thumbnailImagePath` FROM `events`"
                    )
                } else {
                    database.execSQL(
                        "INSERT INTO `events_new` (`id`, `name`, `eventDateEpochDay`, `remindEnabled`) SELECT `id`, `name`, `eventDateEpochDay`, `remindEnabled` FROM `events`"
                    )
                }

                database.execSQL("DROP TABLE `events`")
                database.execSQL("ALTER TABLE `events_new` RENAME TO `events`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_events_eventDateEpochDay` ON `events` (`eventDateEpochDay`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `fansa_uchiwa_data` ADD COLUMN `overallBorderColorValue` INTEGER NOT NULL DEFAULT -224742753697792"
                )
                database.execSQL(
                    "ALTER TABLE `fansa_uchiwa_data` ADD COLUMN `overallBorderWidth` REAL NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE `fansa_uchiwa_data` ADD COLUMN `isOverallBorderPuffyEnabled` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun build(context: Context): FansaUchiwaDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FansaUchiwaDatabase::class.java,
                "uchiwaData.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
        }
    }
}
