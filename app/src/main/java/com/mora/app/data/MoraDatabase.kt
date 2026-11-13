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

@Entity(tableName = "money")
data class MoneyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Double,
    val note: String,
    val settled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val persona: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val role: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY done ASC, createdAt DESC")
    fun all(): Flow<List<TaskEntity>>
    @Insert suspend fun insert(task: TaskEntity)
    @Update suspend fun update(task: TaskEntity)
    @Query("DELETE FROM tasks WHERE id = :id") suspend fun delete(id: Long)
}

@Dao
interface MoneyDao {
    @Query("SELECT * FROM money ORDER BY createdAt DESC")
    fun all(): Flow<List<MoneyEntity>>
    @Insert suspend fun insert(item: MoneyEntity)
    @Update suspend fun update(item: MoneyEntity)
    @Query("DELETE FROM money WHERE id = :id") suspend fun delete(id: Long)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    fun chats(): Flow<List<ChatEntity>>
    @Insert suspend fun insertChat(chat: ChatEntity): Long
    @Update suspend fun updateChat(chat: ChatEntity)
    @Query("DELETE FROM chats WHERE id = :id") suspend fun deleteChat(id: Long)
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY id ASC")
    fun messages(chatId: Long): Flow<List<MessageEntity>>
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY id ASC")
    suspend fun messagesOnce(chatId: Long): List<MessageEntity>
    @Insert suspend fun insertMessage(m: MessageEntity)
    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessages(chatId: Long)
}

@Database(
    entities = [TaskEntity::class, MoneyEntity::class, ChatEntity::class, MessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MoraDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun moneyDao(): MoneyDao
    abstract fun chatDao(): ChatDao

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
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
