package com.mora.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ChatStore(context: Context) {
    private val dao = MoraDatabase.get(context).chatDao()

    val chats: Flow<List<ChatEntity>> = dao.chats()

    fun messages(chatId: Long): Flow<List<MessageEntity>> = dao.messages(chatId)

    suspend fun history(chatId: Long): List<MessageEntity> = dao.messagesOnce(chatId)

    suspend fun newChat(persona: String, title: String): Long =
        dao.insertChat(ChatEntity(title = title, persona = persona))

    suspend fun rename(chat: ChatEntity, title: String) {
        dao.updateChat(chat.copy(title = title, updatedAt = System.currentTimeMillis()))
    }

    suspend fun setPersona(chat: ChatEntity, persona: String) {
        dao.updateChat(chat.copy(persona = persona, updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(chatId: Long) {
        dao.deleteMessages(chatId)
        dao.deleteChat(chatId)
    }

    suspend fun add(chat: ChatEntity, role: String, text: String) {
        dao.insertMessage(MessageEntity(chatId = chat.id, role = role, text = text))
        dao.updateChat(chat.copy(updatedAt = System.currentTimeMillis()))
    }
}
