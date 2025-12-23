package com.example.sporthub

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val type: String = "text",
    val base64Content: String = "",
    val timestamp: Long = 0
)

@Composable
fun MatchChatSection(matchId: Int) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("Fan") }
    var isRecording by remember { mutableStateOf(false) }

    val recorder = remember { AudioRecorder(context) }
    var audioFile: File? by remember { mutableStateOf(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val base64Image = FileUtils.uriToBase64(context, uri)
            if (base64Image != null) {
                sendComment(db, auth, matchId, currentUserName, "Sent a photo", "image", base64Image)
            } else {
                Toast.makeText(context, "Image too large or error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc -> if (doc.exists()) currentUserName = doc.getString("name") ?: "Fan" }
        }
    }

    LaunchedEffect(matchId) {
        db.collection("matches").document(matchId.toString()).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    comments = snapshot.documents.map { doc ->
                        Comment(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "Unknown",
                            text = doc.getString("text") ?: "",
                            type = doc.getString("type") ?: "text",
                            base64Content = doc.getString("base64Content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxWidth().height(400.dp).padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), reverseLayout = true) {
            items(comments.reversed()) { comment ->
                CommentItem(comment, comment.userId == auth.currentUser?.uid)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = Color.Gray)
            }

            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                placeholder = { Text(if (isRecording) "Recording..." else "Message...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if(isRecording) Color.Red else Color(0xFFFF3B30),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (newCommentText.isNotBlank()) {
                IconButton(
                    onClick = {
                        if (auth.currentUser != null) {
                            sendComment(db, auth, matchId, currentUserName, newCommentText, "text", "")
                            newCommentText = ""
                        }
                    },
                    modifier = Modifier.background(Color(0xFFFF3B30), CircleShape).size(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(if (isRecording) Color.Red else Color(0xFF2C2C2E), CircleShape)
                        .size(50.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        return@detectTapGestures
                                    }

                                    try {
                                        File(context.cacheDir, "audio_temp.mp3").also {
                                            recorder.start(it)
                                            audioFile = it
                                        }
                                        isRecording = true
                                        tryAwaitRelease()

                                        recorder.stop()
                                        isRecording = false

                                        audioFile?.let { file ->
                                            val base64Audio = FileUtils.fileToBase64(file)
                                            if (base64Audio != null) {
                                                sendComment(db, auth, matchId, currentUserName, "Voice message", "audio", base64Audio)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isRecording = false
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Record", tint = Color.White)
                }
            }
        }
    }
}
fun sendComment(db: FirebaseFirestore, auth: FirebaseAuth, matchId: Int, userName: String, text: String, type: String, base64: String) {
    val data = hashMapOf(
        "userId" to auth.currentUser!!.uid,
        "userName" to userName,
        "text" to text,
        "type" to type,
        "base64Content" to base64,
        "timestamp" to System.currentTimeMillis()
    )
    db.collection("matches").document(matchId.toString())
        .collection("comments").add(data)
}

@Composable
fun CommentItem(comment: Comment, isMe: Boolean) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(comment.userName, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp))
        }

        Box(
            modifier = Modifier
                .background(
                    color = if (isMe) Color(0xFFFF3B30) else Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(8.dp)
        ) {
            when (comment.type) {
                "text" -> {
                    Text(text = comment.text, color = Color.White, modifier = Modifier.padding(4.dp))
                }
                "image" -> {
                    val imageModel = if (comment.base64Content.isNotEmpty()) {
                        "data:image/jpeg;base64,${comment.base64Content}"
                    } else null

                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                "audio" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (comment.base64Content.isNotEmpty()) {
                                try {
                                    val tempFile = FileUtils.base64ToFile(context, comment.base64Content)
                                    if (tempFile != null) {
                                        val mediaPlayer = MediaPlayer()
                                        mediaPlayer.setDataSource(tempFile.absolutePath)
                                        mediaPlayer.prepare()
                                        mediaPlayer.start()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error playing", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                        }
                        Text("Voice Message", color = Color.White, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        Text(timeFormat.format(Date(comment.timestamp)), color = Color.DarkGray, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
    }
}