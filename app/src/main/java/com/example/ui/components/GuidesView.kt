package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuidesView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
            .padding(16.dp)
    ) {
        Text(
            text = "EXPRESS.JS DEVELOPMENT TUTORIALS",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Section 1: Basic Express Server
            item {
                CodeGuideCard(
                    title = "1. Basic Express.js Server Setup",
                    description = "Express is a minimal and flexible Node.js web application framework that provides a robust set of features for web and mobile applications.",
                    codeBlock = """
// 1. Import Express package
const express = require('express');
const app = express();

// 2. Define standard GET endpoint
app.get('/', (req, res) => {
  res.status(200).send('Hello, Welcome to Node.js!');
});

// 3. Listen on port 3000
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${'$'}{PORT}`);
});
""".trimIndent()
                )
            }

            // Section 2: Building REST APIs (RESTful endpoints)
            item {
                CodeGuideCard(
                    title = "2. Defining REST API Mappings",
                    description = "REST APIs use HTTP requests to GET, PUT, POST, and DELETE data representation blocks cleanly.",
                    codeBlock = """
// Middleware to parse incoming JSON bodies
app.use(express.json());

const users = [];

// GET: Retrieve list of users
app.get('/api/users', (req, res) => {
  res.status(200).json(users);
});

// POST: Add new user record
app.post('/api/users', (req, res) => {
  const newUser = req.body;
  if (!newUser.name) {
    return res.status(400).json({ error: 'Name required!' });
  }
  users.push(newUser);
  res.status(201).json({ success: true, user: newUser });
});
""".trimIndent()
                )
            }

            // Section 3: Database setup with Mongoose
            item {
                CodeGuideCard(
                    title = "3. MongoDB Connectivity (Mongoose ODM)",
                    description = "Mongoose provides a straight-forward, schema-based solution to model your application data with MongoDB integrations.",
                    codeBlock = """
const mongoose = require('mongoose');

// Connect to MongoDB Database URL
const DB_URI = 'mongodb://localhost:27017/my_database';
mongoose.connect(DB_URI)
  .then(() => console.log('Successfully connected to MongoDB ⚡'))
  .catch(err => console.error('Database connection failed', err));

// Define Schema representation
val userSchema = new mongoose.Schema({
  name: String,
  email: { type: String, unique: true },
  role: { type: String, default: 'Developer' }
});

const User = mongoose.model('User', userSchema);
""".trimIndent()
                )
            }

            // Section 4: Process variables
            item {
                CodeGuideCard(
                    title = "4. Safe Environment Config (.env)",
                    description = "Always store secret API keys, credentials, and ports in a local .env file and load them into Node.js processes.",
                    codeBlock = """
// Install dotenv: npm install dotenv
require('dotenv').config();

// Access key variables securely from memory
const DB_PASSWORD = process.env.DATABASE_PASSWORD;
const JWT_SECRET = process.env.JWT_SECRET_KEY;
const API_PORT = process.env.PORT || 3000;

console.log(`App running securely using profile variables!`);
""".trimIndent()
                )
            }
        }
    }
}

@Composable
fun CodeGuideCard(
    title: String,
    description: String,
    codeBlock: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232D3F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                description,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF090D14))
                    .padding(10.dp)
            ) {
                Text(
                    text = codeBlock,
                    color = Color(0xFF00FFCC),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
