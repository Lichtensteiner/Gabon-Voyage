package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiAnalyticsService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateAnalysis(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE" || apiKey == "MY_GEMINI_API_KEY" || apiKey == "MY_NEW_API_KEY_DEFAULT_VALUE") {
            return@withContext "⚠️ Clé API Gemini non configurée.\n\nVeuillez ajouter votre clé API Gemini dans le panneau Secrets (Secrets Panel) de Google AI Studio avec la clé GEMINI_API_KEY."
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            
            // Build Request JSON using standard Android JSONObject
            val partObj = JSONObject().put("text", prompt)
            val partsArr = JSONArray().put(partObj)
            val contentObj = JSONObject().put("parts", partsArr)
            val contentsArr = JSONArray().put(contentObj)
            val requestBodyJson = JSONObject().put("contents", contentsArr)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBodyJson.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e("GeminiService", "API Error: ${response.code} - $errorBody")
                    return@withContext "Erreur de l'API Gemini (Code ${response.code}) : ${response.message}\nDétails: $errorBody"
                }

                val responseBodyStr = response.body?.string()
                if (responseBodyStr.isNullOrEmpty()) {
                    return@withContext "Réponse vide reçue du modèle."
                }

                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext "Aucun résultat généré par l'IA."
                }

                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts == null || parts.length() == 0) {
                    return@withContext "Contenu généré vide."
                }

                val text = parts.getJSONObject(0).optString("text")
                if (text.isNullOrEmpty()) {
                    "Texte généré vide."
                } else {
                    text
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Exception in generateAnalysis", e)
            "Une exception est survenue lors de la communication avec l'IA: ${e.localizedMessage}"
        }
    }
}
