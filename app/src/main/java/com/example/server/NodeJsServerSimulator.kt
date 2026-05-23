package com.example.server

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogType {
    SYSTEM,   // Nodemon, MongoDB, NPM
    STDOUT,   // console.log()
    HTTP_IN,  // Express GET/POST
    SUCCESS,  // 2xx response log or launch
    ERROR,    // 4xx/5xx response log or app error
    INFO      // standard dev outputs
}

data class TerminalLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val type: LogType,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

data class ServerStats(
    val isRunning: Boolean = false,
    val totalRequests: Int = 0,
    val successfulRequests: Int = 0,
    val failedRequests: Int = 0,
    val avgLatencyMs: Int = 12,
    val port: Int = 3000,
    val expressVersion: String = "4.19.2",
    val nodeVersion: String = "22.2.0",
    val databaseUrl: String = "mongodb://127.0.0.1:27017/express_db"
)
