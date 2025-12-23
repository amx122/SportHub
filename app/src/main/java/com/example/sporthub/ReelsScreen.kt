package com.example.sporthub

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.sporthub.model.Reel
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen() {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var reels by remember { mutableStateOf(FakeReelsData.list) }

    var showUploadDialog by remember { mutableStateOf(false) }
    var newReelDescription by remember { mutableStateOf("") }
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            showUploadDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val pagerState = rememberPagerState(pageCount = { reels.size })

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ReelItem(reel = reels[page], isPlaying = (pagerState.currentPage == page))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha=0.6f), Color.Transparent)))
        )

        Text(
            text = "SportPulse Reels",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 16.dp)
        )
        IconButton(
            onClick = {
                videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 32.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFFFF3B30), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
        if (showUploadDialog) {
            AlertDialog(
                onDismissRequest = { showUploadDialog = false },
                title = { Text("New Highlight") },
                text = {
                    OutlinedTextField(
                        value = newReelDescription,
                        onValueChange = { newReelDescription = it },
                        label = { Text("Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF3B30),
                            focusedLabelColor = Color(0xFFFF3B30)
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val randomVideoUrl = FakeReelsData.list.random().videoUrl
                            val userName = auth.currentUser?.email?.split("@")?.get(0) ?: "Me"

                            val newReel = Reel(
                                id = UUID.randomUUID().toString(),
                                videoUrl = randomVideoUrl,
                                description = newReelDescription,
                                author = userName,
                                likes = 0
                            )
                            reels = listOf(newReel) + reels

                            newReelDescription = ""
                            showUploadDialog = false
                            Toast.makeText(context, "Posted!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                    ) {
                        Text("Post")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUploadDialog = false }) { Text("Cancel", color = Color.Gray) }
                },
                containerColor = Color(0xFF1C1C1E),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ReelItem(reel: Reel, isPlaying: Boolean) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(reel.likes) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 1.0f
        }
    }

    DisposableEffect(reel.videoUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(reel.videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize().clickable {
                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
            }
        )

        Box(modifier = Modifier.fillMaxWidth().height(250.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha=0.9f)))))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).fillMaxWidth(0.85f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).background(Color(0xFFFF3B30), CircleShape), contentAlignment = Alignment.Center) {
                    Text(reel.author.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(reel.author, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(reel.description, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))
        }
        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { isLiked = !isLiked; if(isLiked) likeCount++ else likeCount-- }) {
                Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Like", tint = if (isLiked) Color.Red else Color.White, modifier = Modifier.size(32.dp))
            }
            Text("$likeCount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            IconButton(onClick = { Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text("Share", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}