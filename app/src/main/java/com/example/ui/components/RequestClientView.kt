package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ServerRoute

@Composable
fun RequestClientView(
    routes: List<ServerRoute>,
    isServerRunning: Boolean,
    onSendRequest: (String, String, String, String, (Int, String, Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var method by remember { mutableStateOf("GET") }
    var path by remember { mutableStateOf("/") }
    var reqBody by remember { mutableStateOf("") }
    var reqHeader by remember { mutableStateOf("Content-Type: application/json") }

    // Loading & Response result models
    var isLoading by remember { mutableStateOf(false) }
    var resCode by remember { mutableStateOf<Int?>(null) }
    var resContent by remember { mutableStateOf<String?>(null) }
    var resLatencyMs by remember { mutableStateOf<Int?>(null) }

    val methods = listOf("GET", "POST", "PUT", "DELETE")

    // Filter available routes by selected method to suggest endpoints
    val suggestedPaths = routes.filter { it.method.equals(method, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
            .padding(16.dp)
    ) {
        // Upper section: Server Info warning
        if (!isServerRunning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF5E57).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFFF5E57).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Error detail", tint = Color(0xFFFF5E57), modifier = Modifier.size(16.dp))
                    Text(
                        text = "SERVER IS OFFLINE. Start Node.js server inside the 'Terminal' tab to connect.",
                        color = Color(0xFFFF8A80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Method & Path Selector Header Group
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown selection (as single Row of buttons for easy tapping)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF131A26))
                    .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                methods.forEach { m ->
                    val isSel = method == m
                    val badgeCol = when (m) {
                        "GET" -> Color(0xFF2ECC71)
                        "POST" -> Color(0xFF3498DB)
                        "PUT" -> Color(0xFFE67E22)
                        "DELETE" -> Color(0xFFE74C3C)
                        else -> Color(0xFF94A3B8)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) badgeCol.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (isSel) badgeCol else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable {
                                method = m
                                // If some default routes exists for this method, select first
                                val defaults = routes.filter { it.method.equals(m, ignoreCase = true) }
                                if (defaults.isNotEmpty()) {
                                    path = defaults.first().path
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = m,
                            color = if (isSel) badgeCol else Color(0xFF64748B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Simulated base url prefix and endpoint path input
            OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2ECC71),
                    unfocusedBorderColor = Color(0xFF232D3F)
                ),
                prefix = {
                    Text(
                        "http://localhost:3000",
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Endpoint Selection list
        Column {
            Text(
                "READY ENDPOINTS ($method)",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (suggestedPaths.isEmpty()) {
                Text(
                    "No endpoints registered. Type customized path manually.",
                    color = Color(0xFF475569),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(suggestedPaths) { item ->
                        val isSel = path == item.path
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF2ECC71).copy(alpha = 0.15f) else Color(0xFF131A26))
                                .border(1.dp, if (isSel) Color(0xFF2ECC71) else Color(0xFF232D3F), RoundedCornerShape(6.dp))
                                .clickable { path = item.path }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                item.path,
                                color = if (isSel) Color(0xFF2ECC71) else Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Request details tabs (Headers or Body selection if POST/PUT)
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuration controls side
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    "REQUEST DEFINITIONS",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Show request body pre-fill options if POST/PUT is active
                val requiresBody = method == "POST" || method == "PUT"

                OutlinedTextField(
                    value = reqHeader,
                    onValueChange = { reqHeader = it },
                    label = { Text("Headers (Header: value)", fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF2ECC71),
                        unfocusedBorderColor = Color(0xFF232D3F)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    singleLine = true
                )

                if (requiresBody) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HTTP Request Payload", color = Color(0xFF64748B), fontSize = 10.sp)
                        Text(
                            "PREFILL JSON",
                            color = Color(0xFF00FFCC),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    reqBody = "{\n  \"name\": \"Asep Story\",\n  \"role\": \"Lead Engineer\",\n  \"createdAt\": \"2026-05-23\"\n}"
                                }
                                .padding(2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = reqBody,
                        onValueChange = { reqBody = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF00FFCC),
                            unfocusedTextColor = Color(0xFF00FFCC),
                            focusedBorderColor = Color(0xFF2ECC71),
                            unfocusedBorderColor = Color(0xFF232D3F)
                        ),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        placeholder = { Text("{\n  \"key\": \"value\"\n}", color = Color(0xFF475569), fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131A26))
                            .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GET and DELETE requests do not transmit JSON bodies. Set headers above and send.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isLoading = true
                        resCode = null
                        resContent = null
                        resLatencyMs = null

                        onSendRequest(method, path, reqBody, reqHeader) { code, body, lat ->
                            isLoading = false
                            resCode = code
                            resContent = body
                            resLatencyMs = lat
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Kirim", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Text("SEND REQUEST ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Results UI side
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            ) {
                Text(
                    "RESPONSE RESULT",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F141C))
                        .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Upper metadata headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF090D14))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (resCode != null) {
                                val statusColor = when (resCode) {
                                    in 200..299 -> Color(0xFF2ECC71) // success
                                    in 400..499 -> Color(0xFFE67E22) // bad/not found
                                    in 500..599 -> Color(0xFFE74C3C) // server crash
                                    else -> Color(0xFFE74C3C) // network fail
                                }
                                val statusText = when (resCode) {
                                    0 -> "FAIL"
                                    200 -> "200 OK"
                                    201 -> "201 Created"
                                    400 -> "400 Bad Request"
                                    401 -> "401 Unauthorized"
                                    404 -> "404 Not Found"
                                    500 -> "500 App Crash"
                                    else -> "$resCode Server Resp"
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (resLatencyMs != null && resLatencyMs!! > 0) {
                                    Text(
                                        text = "TIME: ${resLatencyMs}ms",
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Text(
                                    "No Active Session",
                                    color = Color(0xFF475569),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Payload viewer body
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF2ECC71),
                                    modifier = Modifier.align(Alignment.Center).size(24.dp)
                                )
                            } else if (resContent != null) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        Text(
                                            text = resContent!!,
                                            color = if (resCode in 200..299) Color(0xFF2ECC71) else Color(0xFFFF5E57),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Ready to send",
                                            tint = Color(0xFF1E293B),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Send Request to display response logs...",
                                            color = Color(0xFF475569),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
