package com.mora.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMsg(val role: String, val text: String)

class ImageData(val mime: String, val base64: String)

sealed class AiResult {
    data class Ok(val text: String) : AiResult()
    data class Fail(val kind: String, val detail: String = "") : AiResult()
}

object AiClient {
    private const val ENDPOINT = "https://mora-ai.fanantenanaranaivoarison2.workers.dev"

    suspend fun chat(
        system: String,
        history: List<ChatMsg>,
        image: ImageData? = null
    ): AiResult =
        withContext(Dispatchers.IO) {
            try {
                val msgs = JSONArray()
                history.forEach {
                    msgs.put(JSONObject().put("role", it.role).put("text", it.text))
                }
                val json = JSONObject().put("system", system).put("messages", msgs)
                if (image != null) {
                    json.put(
                        "image",
                        JSONObject().put("mime", image.mime).put("data", image.base64)
                    )
                }
                val body = json.toString()

                val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 60000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val txt = stream?.bufferedReader()?.use { it.readText() } ?: ""
                conn.disconnect()

                if (code in 200..299) {
                    val t = JSONObject(txt).optString("text", "")
                    if (t.isNotEmpty()) AiResult.Ok(t) else AiResult.Fail("server", "empty")
                } else {
                    AiResult.Fail("server", "HTTP $code: " + txt.take(300))
                }
            } catch (e: Exception) {
                AiResult.Fail("network", e.javaClass.simpleName)
            }
        }
}
