package com.hse.management

import android.content.Context
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "records"
)
data class HseRecord(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Long = 0,

    val number: String,

    val module: String,

    val title: String,

    val type: String,

    val category: String = "",

    val description: String = "",

    val responsible: String = "",

    val priority: String = "Medium",

    val targetDate: String = "",

    val status: String = "Open",

    val source: String = "",

    val reference: String = "",

    val correctiveAction: String = "",

    val preventiveAction: String = "",

    val rootCause: String = "",

    val verification: String = "",

    val closeoutEvidence: String = "",

    val investigationMethod: String = "",

    val standard: String = "",

    val clause: String = "",

    val createdBy: String = "System User",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "users"
)
data class HseUser(

    @PrimaryKey
    val username: String,

    val displayName: String,

    val role: String,

    val passwordHash: String
)

@Entity(
    tableName = "audit_log"
)
data class AuditLog(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Long = 0,

    val username: String,

    val action: String,

    val module: String,

    val recordId: Long,

    val at: Long = System.currentTimeMillis()
)

@Dao
interface HseDao {

    @Query(
        "SELECT * FROM records ORDER BY id DESC"
    )
    fun records(): Flow<List<HseRecord>>

    @Query(
        "SELECT * FROM records " +
        "WHERE module = :module " +
        "ORDER BY id DESC"
    )
    fun recordsByModule(
        module: String
    ): Flow<List<HseRecord>>

    @Query(
        "SELECT COUNT(*) FROM records " +
        "WHERE module = 'Observation'"
    )
    fun observationCount(): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM records " +
        "WHERE module = 'Incident'"
    )
    fun incidentCount(): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM records " +
        "WHERE module = 'Audit'"
    )
    fun auditCount(): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM records " +
        "WHERE module = 'CAPA'"
    )
    fun capaCount(): Flow<Int>

    @Insert
    suspend fun insert(
        record: HseRecord
    ): Long

    @Update
    suspend fun update(
        record: HseRecord
    )

    @Delete
    suspend fun delete(
        record: HseRecord
    )

    @Insert
    suspend fun log(
        log: AuditLog
    )

    @Query(
        "SELECT * FROM users " +
        "WHERE username = :u " +
        "LIMIT 1"
    )
    suspend fun user(
        u: String
    ): HseUser?

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun upsertUser(
        user: HseUser
    )
}

@Database(
    entities = [
        HseRecord::class,
        HseUser::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HseDatabase : RoomDatabase() {

    abstract fun dao(): HseDao

    companion object {

        @Volatile
        private var INSTANCE: HseDatabase? = null

        fun get(
            context: Context
        ): HseDatabase {

            return INSTANCE
                ?: synchronized(this) {

                    INSTANCE
                        ?: Room.databaseBuilder(
                            context.applicationContext,
                            HseDatabase::class.java,
                            "hse_management.db"
                        )
                        .build()
                        .also {
                            INSTANCE = it
                        }
                }
        }
    }
}
