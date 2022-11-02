package com.ft.ltd

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ft.ltd.ui.theme.MyFirstComposeAppTheme

class MainActivity : ComponentActivity() {

    class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("clicked action")
        }
    }

    private val isPipSupported by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.hasSystemFeature(
                PackageManager.FEATURE_PICTURE_IN_PICTURE
            )
        } else {
            false
        }
    }

    private var videoViewBounds = android.graphics.Rect()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFirstComposeAppTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Text(
                        text = "Simple PictureInPicture App",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
                AndroidView(
                    factory = {
                        VideoView(it, null).apply {
                            setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.taylor_swift_delicate}"))
                            start()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            videoViewBounds = it
                                .boundsInWindow()
                                .toAndroidRect()
                        }
                )
            }
        }
    }

    private fun updatePipParams(): PictureInPictureParams? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder()
                .setSourceRectHint(videoViewBounds)
                .setAspectRatio(Rational(16, 9))
                .setActions(
                    listOf(
                        RemoteAction(
                            Icon.createWithResource(
                                applicationContext,
                                R.drawable.ic_baseline_pause_24
                            ),
                            "Pause",
                            "Pause Play Content",
                            PendingIntent.getBroadcast(
                                applicationContext,
                                0,
                                Intent(applicationContext, MyReceiver::class.java),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    )
                ).build()
        } else null
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isPipSupported) {
            return
        }
        updatePipParams()?.let { params ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(params)
            }
        }

    }
}