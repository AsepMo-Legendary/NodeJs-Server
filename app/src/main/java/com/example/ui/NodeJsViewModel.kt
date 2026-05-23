package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.server.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.random.Random

class NodeJsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = RouteRepository(db.routeDao())

    // UI visible flows
    val routes: StateFlow<List<ServerRoute>> = repository.allRoutes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _stats = MutableStateFlow(ServerStats())
    val stats: StateFlow<ServerStats> = _stats.asStateFlow()

    private val _logs = MutableStateFlow<List<TerminalLog>>(emptyList())
    val logs: StateFlow<List<TerminalLog>> = _logs.asStateFlow()

    // Server uptime trace
    private val _uptimeSeconds = MutableStateFlow(0)
    val uptimeSeconds: StateFlow<Int> = _uptimeSeconds.asStateFlow()

    init {
        // Populating database if blank
        viewModelScope.launch {
            // Wait for first collect to see if populated or check count
            val pathCount = repository.getCount()
            if (pathCount == 0) {
                // Populate default routes
                repository.insert(
                    ServerRoute(
                        method = "GET",
                        path = "/",
                        responseCode = 200,
                        responseBody = "{\n  \"message\": \"Welcome to NodeJS Server Simulator!\",\n  \"framework\": \"Express 4.19\",\n  \"status\": \"healthy\"\n}",
                        description = "Default Root Endpoint"
                    )
                )
                repository.insert(
                    ServerRoute(
                        method = "GET",
                        path = "/api/status",
                        responseCode = 200,
                        responseBody = "{\n  \"online\": true,\n  \"uptime\": \"running\",\n  \"activeDB\": \"MongoDB 7.0\",\n  \"environment\": \"production\"\n}",
                        description = "System Status Check"
                    )
                )
                repository.insert(
                    ServerRoute(
                        method = "GET",
                        path = "/api/users",
                        responseCode = 200,
                        responseBody = "[\n  {\n    \"id\": 1,\n    \"name\": \"Jane Foster\",\n    \"role\": \"Senior DevOps\"\n  },\n  {\n    \"id\": 2,\n    \"name\": \"Budi Santoso\",\n    \"role\": \"Backend Lead\"\n  }\n]",
                        description = "Get Users List"
                    )
                )
                repository.insert(
                    ServerRoute(
                        method = "POST",
                        path = "/api/users",
                        responseCode = 201,
                        responseBody = "{\n  \"success\": true,\n  \"message\": \"Simulated User Budi created successfully in MongoDB!\",\n  \"userId\": 1092\n}",
                        description = "Create User Mock Account"
                    )
                )
            }
            
            // Add initial system boot logs
            addSystemLogs()
        }

        // Start simulated clock loop for server runtime
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_stats.value.isRunning) {
                    _uptimeSeconds.value += 1
                }
            }
        }
    }

    private fun addSystemLogs() {
        addLog("Initializing local developer workspace environment...", LogType.SYSTEM)
        addLog("Checking dependencies in package.json...", LogType.SYSTEM)
        addLog("Found: express@4.19.2, nodemon@3.1.0, mongoose@8.4.0", LogType.INFO)
        addLog("Ready. Click 'START SERVER' to boot up your local Express app.", LogType.INFO)
    }

    fun startServer() {
        if (_stats.value.isRunning) return
        _stats.value = _stats.value.copy(isRunning = true)
        _uptimeSeconds.value = 0

        viewModelScope.launch {
            addLog("npm run dev", LogType.STDOUT)
            delay(300)
            addLog("> nodeserver@1.0.0 dev", LogType.STDOUT)
            addLog("> nodemon index.js", LogType.STDOUT)
            delay(400)
            addLog("[nodemon] 3.1.0 starting 'node index.js'", LogType.SYSTEM)
            addLog("[nodemon] watching path(s): *.*", LogType.SYSTEM)
            addLog("[nodemon] watching extensions: js,mjs,json", LogType.SYSTEM)
            delay(500)
            addLog("[Express] Connecting to simulated database at mongodb://127.0.0.1:27017/express_db...", LogType.SYSTEM)
            delay(600)
            addLog("🔑 [Mongoose] Successfully connected to MongoDB!", LogType.SUCCESS)
            addLog("🚀 Express Server is now running on http://localhost:3000", LogType.SUCCESS)
        }
    }

    fun stopServer() {
        if (!_stats.value.isRunning) return
        _stats.value = _stats.value.copy(isRunning = false)
        viewModelScope.launch {
            addLog("[nodemon] app crashed - waiting for file changes before starting...", LogType.SYSTEM)
            addLog("🛑 Server standard process exited with code 0 (SIGTERM)", LogType.ERROR)
        }
    }

    fun restartServer() {
        viewModelScope.launch {
            stopServer()
            delay(1200)
            startServer()
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun addLog(msg: String, type: LogType) {
        val log = TerminalLog(message = msg, type = type)
        // Keep logs capped at 100 entries for stability
        val currentList = _logs.value.toMutableList()
        currentList.add(log)
        if (currentList.size > 100) {
            currentList.removeAt(0)
        }
        _logs.value = currentList
    }

    fun addNewRoute(method: String, path: String, code: Int, body: String, desc: String, onFinished: (Boolean) -> Unit) {
        if (path.isEmpty() || !path.startsWith("/")) {
            onFinished(false)
            return
        }
        
        viewModelScope.launch {
            try {
                if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                    JSONObject(body)
                }
            } catch (e: Exception) {
                try {
                    JSONArray(body)
                } catch (e2: Exception) {
                    onFinished(false)
                    return@launch
                }
            }

            val route = ServerRoute(
                method = method.uppercase(),
                path = path.trim(),
                responseCode = code,
                responseBody = body,
                description = desc
            )
            repository.insert(route)
            
            if (_stats.value.isRunning) {
                addLog("[nodemon] restarting due to changes...", LogType.SYSTEM)
                addLog("[nodemon] starting 'node index.js'", LogType.SYSTEM)
                addLog("✨ Route mapped: ${route.method} ${route.path}", LogType.SUCCESS)
            }
            onFinished(true)
        }
    }

    fun deleteRoute(route: ServerRoute) {
        viewModelScope.launch {
            repository.delete(route)
            if (_stats.value.isRunning) {
                addLog("[nodemon] restarting due to route removal...", LogType.SYSTEM)
                addLog("[nodemon] starting 'node index.js'", LogType.SYSTEM)
                addLog("🗑️ Route removed: ${route.method} ${route.path}", LogType.ERROR)
            }
        }
    }

    fun simulateClientRequest(
        method: String,
        urlPath: String,
        bodyText: String,
        headerText: String,
        onResponse: (Int, String, Int) -> Unit
    ) {
        viewModelScope.launch {
            if (!_stats.value.isRunning) {
                onResponse(0, "Error: Connection refused. Server on port 3000 is not running.", 0)
                return@launch
            }

            val latency = Random.nextInt(10, 45)
            
            addLog("--> $method $urlPath", LogType.HTTP_IN)
            if (bodyText.isNotBlank() && (method == "POST" || method == "PUT")) {
                addLog("    [Payload] ${bodyText.replace("\n", " ")}", LogType.INFO)
            }

            delay(latency.toLong())

            val cleanPath = if (urlPath.contains("?")) urlPath.substringBefore("?") else urlPath
            val matchedRoute = routes.value.find { 
                it.method.equals(method, ignoreCase = true) && it.path.trim().equals(cleanPath.trim(), ignoreCase = true)
            }

            if (matchedRoute != null) {
                val code = matchedRoute.responseCode
                val responseStr = matchedRoute.responseBody
                
                val logType = if (code in 200..299) LogType.SUCCESS else LogType.ERROR
                addLog("<-- $code $method $cleanPath ($latency ms)", logType)
                
                val success = code in 200..299
                _stats.value = _stats.value.copy(
                    totalRequests = _stats.value.totalRequests + 1,
                    successfulRequests = _stats.value.successfulRequests + (if (success) 1 else 0),
                    failedRequests = _stats.value.failedRequests + (if (success) 0 else 1),
                    avgLatencyMs = ((_stats.value.avgLatencyMs * 4) + latency) / 5
                )

                onResponse(code, responseStr, latency)
            } else {
                addLog("<-- 404 $method $cleanPath ($latency ms)", LogType.ERROR)
                addLog("⚠️ [Express Error] Cannot $method $cleanPath", LogType.ERROR)
                
                _stats.value = _stats.value.copy(
                    totalRequests = _stats.value.totalRequests + 1,
                    failedRequests = _stats.value.failedRequests + 1,
                    avgLatencyMs = ((_stats.value.avgLatencyMs * 4) + latency) / 5
                )

                val notFoundBody = "{\n  \"status\": 404,\n  \"error\": \"Not Found\",\n  \"message\": \"Cannot $method $cleanPath\"\n}"
                onResponse(404, notFoundBody, latency)
            }
        }
    }
}
