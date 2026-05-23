package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class ServerRoute(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val method: String, // GET, POST, PUT, DELETE
    val path: String, // e.g. "/api/users"
    val responseCode: Int = 200,
    val responseBody: String, // JSON payload string
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
