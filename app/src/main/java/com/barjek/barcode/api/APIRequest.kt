package com.barjek.barcode.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.barjek.barcode.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class APIRequest(
    private val url: String,
    private val method: String = "GET",
    private val data: String? = null,
    private val photoDataByte: ByteArray? = null
) {
    private val base_url = "http://10.0.2.2:8080"
    private var conn = URL("$base_url/$url").openConnection() as HttpURLConnection

    private val boundary = "----Boundary${System.currentTimeMillis()}"

    init {
        conn.requestMethod = method
        if (photoDataByte != null && data != null) {
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        } else {
            conn.setRequestProperty("Content-Type", "application/json")
        }

        val apiKey = BuildConfig.API_KEY
        conn.setRequestProperty("API-KEY", apiKey)

        if (method == "POST" || method == "PATCH") {
            if (data != null) {
                if (photoDataByte != null) {
                    writeMultipartData(photoDataByte, data)
                } else {
                    conn.doOutput = true
                    val writer = OutputStreamWriter(conn.outputStream)
                    writer.write(data)
                    writer.flush()
                }
            }
        }
    }

    suspend fun execute(): APIResponse {
        return withContext(Dispatchers.IO) {
            val reader = when (conn.responseCode) {
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

    private fun writeMultipartData(photoDataByte: ByteArray?, data: String) {
        val outputStream = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8))

        writer.append("--$boundary\r\n")
        writer.append("Content-Disposition: form-data; name=\"data\"\r\n")
        writer.append("Content-Type: application/json\r\n\r\n")
        writer.append(data)
        writer.append("\r\n")

        val fileName = JSONObject(data).getString("foto")
        val filetype = "image/${fileName.substringAfterLast(".", "")}"

        writer.append("--$boundary\r\n")
        writer.append("Content-Disposition: form-data; name=\"foto\"; filename=\"$fileName\"\r\n")
        writer.append("Content-Type: $filetype\r\n\r\n")
        writer.flush()

        outputStream.write(photoDataByte)
        outputStream.flush()

        writer.append("\r\n--$boundary--\r\n")
        writer.flush()

        writer.close()
    }
}