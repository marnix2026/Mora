package com.mora.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// type: "INCOME" (fidiram-bola), "EXPENSE" (lany),
//       "DEBT_OWED" (trosa tsy maintsy aloako), "DEBT_RECEIVABLE" (trosa tokony horaisiko)
@Entity(tableName = "money")
data class MoneyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Double,
    val note: String,
    val settled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY done ASC, createdAt DESC")
    fun all(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface MoneyDao {
    @Query("SELECT * FROM money ORDER BY createdAt DESC")
    fun all(): Flow<List<MoneyEntity>>

    @Insert
    suspend fun insert(item: MoneyEntity)

    @Update
    suspend fun update(item: MoneyEntity)

    @Query("DELETE FROM money WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(
    entities = [TaskEntity::class, MoneyEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MoraDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun moneyDao(): MoneyDao

    companion object {
        @Volatile
        private var instance: MoraDatabase? = null

        fun get(context: Context): MoraDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoraDatabase::class.java,
                    "mora.db"
                )
                    // Fotoana fanandramana: raha miova ny rafitra, averina vaovao ny angona.
                    // Ho soloina amin'ny fifindra-monina marina alohan'ny famoahana.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
