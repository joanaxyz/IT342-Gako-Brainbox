package edu.cit.gako.brainbox.data.local.database

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import edu.cit.gako.brainbox.data.local.BrainBoxConverters
import edu.cit.gako.brainbox.data.local.dao.ConflictDraftDao
import edu.cit.gako.brainbox.data.local.dao.NotebookDao
import edu.cit.gako.brainbox.data.local.dao.OfflinePackDao
import edu.cit.gako.brainbox.data.local.dao.PendingMutationDao
import edu.cit.gako.brainbox.data.local.entity.ConflictDraftEntity
import edu.cit.gako.brainbox.data.local.entity.NotebookEntity
import edu.cit.gako.brainbox.data.local.entity.OfflinePackEntity
import edu.cit.gako.brainbox.data.local.entity.PendingMutationEntity

@Database(
    entities = [
        NotebookEntity::class,
        OfflinePackEntity::class,
        PendingMutationEntity::class,
        ConflictDraftEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(BrainBoxConverters::class)
abstract class BrainBoxLocalDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun offlinePackDao(): OfflinePackDao
    abstract fun pendingMutationDao(): PendingMutationDao
    abstract fun conflictDraftDao(): ConflictDraftDao

    companion object {
        @Volatile
        private var instance: BrainBoxLocalDatabase? = null

        fun getInstance(context: Context): BrainBoxLocalDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BrainBoxLocalDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        private const val DATABASE_NAME = "brainbox-offline.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE offline_packs ADD COLUMN quizPayloadJson TEXT")
                db.execSQL("ALTER TABLE offline_packs ADD COLUMN flashcardPayloadJson TEXT")
                db.execSQL("ALTER TABLE offline_packs ADD COLUMN playlistPayloadJson TEXT")
            }
        }
    }
}

