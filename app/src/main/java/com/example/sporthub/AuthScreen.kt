package com.example.sporthub

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(onBack: () -> Unit, onLoginSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth().align(Alignment.Start)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isRegisterMode) "Create Account" else "Welcome Back",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Sign in to keep your sports world synced",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isRegisterMode) {
            SportTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                icon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        SportTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            icon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        SportTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            icon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    if (isRegisterMode && name.isEmpty()) {
                        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    if (isRegisterMode) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = task.result?.user?.uid
                                    val userMap = hashMapOf(
                                        "name" to name,
                                        "email" to email,
                                        "role" to "user"
                                    )
                                    if (uid != null) {
                                        db.collection("users").document(uid).set(userMap)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(context, "Account Created!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                onLoginSuccess()
                                            }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isRegisterMode) "Sign Up" else "Log In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(
                text = if (isRegisterMode) "Already have an account? Log In" else "Don't have an account? Sign Up",
                color = Color.Gray
            )
        }
    }
}
@Composable
fun SportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFFFF3B30)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFF3B30),
            unfocusedBorderColor = Color(0xFF2C2C2E),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFFFF3B30)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}