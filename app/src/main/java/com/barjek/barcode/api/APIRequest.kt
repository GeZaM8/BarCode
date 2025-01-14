package com.barjek.barcode.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.barjek.barcode.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class APIRequest(
    private val url: String,
    private val method: String = "GET",
    private val data: String? = null
) {
    private var conn = URL("https://backend24.site/Rian/XI/barcode/$url").openConnection() as HttpURLConnection

    init {
        conn.requestMethod = method
        conn.setRequestProperty("Content-Type", "application/json")

        val apiKey = BuildConfig.API_KEY
        conn.setRequestProperty("API-KEy", apiKey)

        if (method == "POST" && data != null) {
            conn.doOutput = true
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(data)
            writer.flush()
        }
    }

    suspend fun execute(): APIResponse {
        return withContext(Dispatchers.IO) {
            val reader = when (conn.responseCode){
                in 200 until 300 -> BufferedReader(InputStreamReader(conn.inputStream))
                else -> BufferedReader(InputStreamReader(conn.errorStream))
            }
            val builder = StringBuilder().append(reader.readText())
            APIResponse(builder.toString(), conn.responseCode)
        }
    }

    suspend fun getImage(): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                BitmapFactory.decodeStream(conn.inputStream)
            } catch (e: Exception) {
                null
            }
        }
    }
}