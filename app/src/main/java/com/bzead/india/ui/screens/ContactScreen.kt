package com.bzead.india.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val WHATSAPP_NUMBER = "447555394997"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Us", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Contact Us",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "We'd love to hear from you. Send us a message and we'll get back to you as soon as possible.",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )

            // Contact Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        leadingIcon = { Icon(Icons.Default.Message, contentDescription = null, tint = Color.Gray) },
                        placeholder = { Text("How can we help you?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 5
                    )

                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && email.isNotBlank() && message.isNotBlank()) {
                                val text = "*Name:* ${fullName.trim()}\n*Email:* ${email.trim()}\n*Message:* ${message.trim()}"
                                val url = "https://wa.me/$WHATSAPP_NUMBER?text=${Uri.encode(text)}"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (ignored: Exception) {}
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = fullName.isNotBlank() && email.isNotBlank() && message.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Send via WhatsApp",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Contact information (web Contact.tsx "Get in Touch")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Get in Touch", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Email", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("support@bzead.com", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Admin Contact", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("admin@bzead.com", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Phone", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("+44 7555394997", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Address", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("46/A1, PKP BUILDING, KANNUR, KERALA, INDIA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Response Times", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Text("• General enquiries: within 24–48 hours", fontSize = 13.sp, color = Color(0xFF475569))
                    Text("• Order-related issues: within 24 hours", fontSize = 13.sp, color = Color(0xFF475569))
                    Text("• Account security concerns: within 12 hours", fontSize = 13.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Before You Write", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Text(
                        "For account-related issues such as password resets, OTP problems, or email verification, please try the self-service options on the login page first. If the issue persists, include your registered email address in your message so we can assist you more quickly.",
                        fontSize = 13.sp, color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}
