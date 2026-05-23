package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ServerRoute

@Composable
fun RouteEditorView(
    routes: List<ServerRoute>,
    onAddRoute: (String, String, Int, String, String, (Boolean) -> Unit) -> Unit,
    onDeleteRoute: (ServerRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRouteForDetail by remember { mutableStateOf<ServerRoute?>(null) }
    var showCodeView by remember { mutableStateOf(false) }

    // State for selected detail, default to first route if none selected and routes not empty
    LaunchedEffect(routes) {
        if (selectedRouteForDetail == null && routes.isNotEmpty()) {
            selectedRouteForDetail = routes.first()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
            .padding(16.dp)
    ) {
        // Upper selection controls: Routes list vs Code representation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF131A26))
                .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { showCodeView = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showCodeView) Color(0xFF2ECC71).copy(alpha = 0.15f) else Color.Transparent,
                    contentColor = if (!showCodeView) Color(0xFF2ECC71) else Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Routes list", modifier = Modifier.size(16.dp))
                    Text("Router Map", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { showCodeView = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showCodeView) Color(0xFF2ECC71).copy(alpha = 0.15f) else Color.Transparent,
                    contentColor = if (showCodeView) Color(0xFF2ECC71) else Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Code View", modifier = Modifier.size(16.dp))
                    Text("Express index.js Source", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showCodeView) {
            // Render index.js file code mockup representation
            MockNodeCodeView(routes = routes)
        } else {
            // Active Routes List
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left hand sidebar: Route entries list
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE ROUTES (${routes.size})",
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )

                        FilledIconButton(
                            onClick = { showAddDialog = true },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2ECC71)),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add routes", tint = Color.Black, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (routes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF131A26))
                                .border(1.dp, Color(0xFF222C3E), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active router definitions. Click + to add one.",
                                color = Color(0xFF475569),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            items(routes) { route ->
                                val isSelected = selectedRouteForDetail?.id == route.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF212B3B) else Color(0xFF131A26))
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF2ECC71) else Color(0xFF232D3F),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedRouteForDetail = route }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            MethodBadge(method = route.method)
                                            Text(
                                                text = route.path,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        if (route.description.isNotBlank()) {
                                            Text(
                                                text = route.description,
                                                color = Color(0xFF64748B),
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteRoute(route) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = Color(0xFFE74C3C),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right hand sidebar: Response Body details
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "RESPONSE STRUCTURE",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F141C))
                            .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val route = selectedRouteForDetail
                        if (route == null) {
                            Text(
                                "Select a route on the left to inspect its response configurations.",
                                color = Color(0xFF475569),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Status Code",
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (route.responseCode in 200..299) Color(0xFF2ECC71).copy(alpha = 0.2f)
                                                else Color(0xFFE74C3C).copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${route.responseCode}",
                                            color = if (route.responseCode in 200..299) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "JSON Payload",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF070A0F))
                                        .padding(8.dp)
                                ) {
                                    LazyColumn {
                                        item {
                                            Text(
                                                text = route.responseBody,
                                                color = Color(0xFF00FFCC),
                                                fontSize = 11.sp,
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

    // Modal dialogue to create a route
    if (showAddDialog) {
        AddRouteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { method, path, code, body, desc ->
                onAddRoute(method, path, code, body, desc) { success ->
                    if (success) {
                        showAddDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun MethodBadge(method: String, modifier: Modifier = Modifier) {
    val badgeColor = when (method.uppercase()) {
        "GET" -> Color(0xFF2ECC71)
        "POST" -> Color(0xFF3498DB)
        "PUT" -> Color(0xFFE67E22)
        "DELETE" -> Color(0xFFE74C3C)
        else -> Color(0xFFBDC3C7)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = method.uppercase(),
            color = badgeColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun MockNodeCodeView(routes: List<ServerRoute>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F141C))
            .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                CodeLine("const express = require('express');", Color(0xFF9E77C1))
                CodeLine("const app = express();", Color(0xFF9E77C1))
                CodeLine("app.use(express.json());", Color(0xFFE5C07B))
                Spacer(modifier = Modifier.height(12.dp))
                CodeLine("// Simulated Middlewares", Color(0xFF64748B))
                CodeLine("app.use((req, res, next) => {", Color(0xFFE5C07B))
                CodeLine("  console.log(`[Express] Request received: ${'$'}{req.method} ${'$'}{req.url}`);", Color(0xFF98C379))
                CodeLine("  next();", Color(0xFF9E77C1))
                CodeLine("});", Color(0xFFE5C07B))
                Spacer(modifier = Modifier.height(12.dp))

                CodeLine("// Custom Router Handlers", Color(0xFF64748B))
                routes.forEach { r ->
                    CodeLine("app.${r.method.lowercase()}('${r.path}', (req, res) => {", Color(0xFFD19A66))
                    if (r.method.uppercase() == "POST" || r.method.uppercase() == "PUT") {
                        CodeLine("  const body = req.body;", Color(0xFF475569))
                        CodeLine("  console.log('Incoming Payload:', body);", Color(0xFF64748B))
                    }
                    CodeLine("  res.status(${r.responseCode}).json(", Color(0xFFE5C07B))
                    
                    // Display response lines inside Express handler block
                    val bodyLines = r.responseBody.split("\n")
                    bodyLines.forEach { bl ->
                        CodeLine("    $bl", Color(0xFF00FFCC))
                    }
                    
                    CodeLine("  );", Color(0xFFE5C07B))
                    CodeLine("});", Color(0xFFD19A66))
                    Spacer(modifier = Modifier.height(10.dp))
                }

                CodeLine("// Server port setup", Color(0xFF64748B))
                CodeLine("app.listen(3000, () => {", Color(0xFFE5C07B))
                CodeLine("  console.log('🚀 Express Server is now running on port 3000');", Color(0xFF98C379))
                CodeLine("});", Color(0xFFE5C07B))
            }
        }
    }
}

@Composable
fun CodeLine(code: String, color: Color) {
    Text(
        text = code,
        color = color,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 16.sp
    )
}

@Composable
fun AddRouteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, String) -> Unit
) {
    var method by remember { mutableStateOf("GET") }
    var path by remember { mutableStateOf("") }
    var codeStr by remember { mutableStateOf("200") }
    var body by remember { mutableStateOf("{\n  \"success\": true,\n  \"data\": \"Your record!\"\n}") }
    var desc by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val methods = listOf("GET", "POST", "PUT", "DELETE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Custom Router Node",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = Color(0xFF131A26),
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Method Selector Dropdown Custom Row
                    Text("HTTP Request Method", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        methods.forEach { m ->
                            val isSel = method == m
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFF2ECC71) else Color(0xFF0F141C))
                                    .border(1.dp, if (isSel) Color(0xFF2ECC71) else Color(0xFF232D3F), RoundedCornerShape(6.dp))
                                    .clickable { method = m }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = m,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                item {
                    // Path Target
                    OutlinedTextField(
                        value = path,
                        onValueChange = {
                            path = it
                            if (it.isNotBlank() && !it.startsWith("/")) {
                                validationError = "Path must start with leading slash (e.g. /api/users)"
                            } else {
                                validationError = null
                            }
                        },
                        label = { Text("Endpoint Route Path", fontSize = 11.sp) },
                        placeholder = { Text("/api/v1/custom", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF2ECC71),
                            unfocusedBorderColor = Color(0xFF232D3F)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )
                }

                item {
                    // Status Code details
                    OutlinedTextField(
                        value = codeStr,
                        onValueChange = { codeStr = it },
                        label = { Text("HTTP Status Code", fontSize = 11.sp) },
                        placeholder = { Text("200", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF2ECC71),
                            unfocusedBorderColor = Color(0xFF232D3F)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )
                }

                item {
                    // Short desc
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Short Description (Optional)", fontSize = 11.sp) },
                        placeholder = { Text("Get simulated dashboard items", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF2ECC71),
                            unfocusedBorderColor = Color(0xFF232D3F)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                    )
                }

                item {
                    // JSON Return Body editor with helper templates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mock JSON Return Payload", color = Color(0xFF94A3B8), fontSize = 11.sp)

                        // Prefilled presets
                        Text(
                            text = "PREFILL SIMPLE",
                            color = Color(0xFF00FFCC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    body = "{\n  \"status\": \"success\",\n  \"data\": {\n    \"id\": 99,\n    \"active\": true\n  }\n}"
                                }
                                .padding(4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF00FFCC),
                            unfocusedTextColor = Color(0xFF00FFCC),
                            focusedBorderColor = Color(0xFF2ECC71),
                            unfocusedBorderColor = Color(0xFF232D3F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 15,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )
                }

                if (validationError != null) {
                    item {
                        Text(text = validationError!!, color = Color(0xFFE74C3C), fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val codeInt = codeStr.toIntOrNull() ?: 200
                    if (path.isBlank() || !path.startsWith("/")) {
                        validationError = "Path must start with leading slash (e.g. /api/something)"
                    } else {
                        onConfirm(method, path, codeInt, body, desc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
            ) {
                Text("Ciptakan Route", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
