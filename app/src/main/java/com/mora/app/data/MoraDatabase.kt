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

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class MoraDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: MoraDatabase? = null

        fun get(context: Context): MoraDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoraDatabase::class.java,
                    "mora.db"
                ).build().also { instance = it }
            }
    }
}
