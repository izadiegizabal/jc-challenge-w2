/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.theme.MyTheme

@ExperimentalAnimationApi
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

val calculateTotal: (Int, Int, Int) -> Int = { h, m, s -> h * 60 * 60 + m * 60 + s }
val getText: (Int) -> String = { total ->
    val hours = total / 3600
    val mins = (total % 3600) / 60
    val sec = total - hours * 60 * 60 - mins * 60
    "${if (hours < 10) "0$hours" else hours}:${if (mins < 10) "0$mins" else mins}:${if (sec < 10) "0$sec" else sec}"
}

// Start building your app here!
@ExperimentalAnimationApi
@Composable
fun MyApp() {
    Surface(color = MaterialTheme.colors.background) {
        var hours = remember { mutableStateOf(0) }
        var mins = remember { mutableStateOf(0) }
        var secs = remember { mutableStateOf(0) }
        var total = remember { mutableStateOf(0) }
        var current = remember { mutableStateOf(0) }
        var started = remember { mutableStateOf(false) }
        val handler = remember { mutableStateOf(Handler(Looper.getMainLooper())) }
        val progress = remember { mutableStateOf(1f) }
        val text = remember { mutableStateOf("00:00:00") }

        fun reset() {
            current.value = total.value
            progress.value = 1f
            started.value = false
            handler.value.removeCallbacksAndMessages(null)
            text.value = getText(total.value)
        }

        fun changeInput(tot: Int) {
            total.value = tot
            text.value = getText(total.value)
            reset()
        }

        val remRunnable = object : Runnable {
            override fun run() {
                if (current.value > 0) {
                    current.value--
                    progress.value =
                        if (total.value == 0) 0f else current.value.toFloat() / total.value.toFloat()
                    text.value = getText(current.value)
                    handler.value.postDelayed(this, 1000)
                } else {
                    reset()
                    text.value = "DONE!"
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Timer ⏲", style = MaterialTheme.typography.h3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    TimeInput("Hours") { hoursS ->
                        hoursS.toIntOrNull()?.let {
                            hours.value = it
                            changeInput(calculateTotal(it, mins.value, secs.value))
                        }
                    }
                }
                Box(Modifier.weight(1f)) {
                    TimeInput("Minutes") { minsS ->
                        minsS.toIntOrNull()?.let {
                            mins.value = it
                            changeInput(calculateTotal(hours.value, it, secs.value))
                        }
                    }
                }
                Box(Modifier.weight(1f)) {
                    TimeInput("Seconds") { secS ->
                        secS.toIntOrNull()?.let {
                            secs.value = it
                            changeInput(calculateTotal(hours.value, mins.value, it))
                        }
                    }
                }
            }

            CountDownIndicator(
                Modifier.padding(top = 50.dp),
                progress = progress.value,
                time = text.value,
                size = 300,
                stroke = 12
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        started.value = !started.value
                        if (started.value)
                            handler.value.post(remRunnable)
                        else
                            handler.value.removeCallbacksAndMessages(null)
                    }
                ) {
                    Text(if (started.value) "PAUSE" else "START")
                }
                Button(
                    { reset() }, Modifier.padding(start = 16.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Text("RESET")
                }
            }
        }
    }
}

@Composable
fun TimeInput(label: String, update: (text: String) -> Unit) {
    var fieldText = remember { mutableStateOf("") }

    OutlinedTextField(
        value = fieldText.value,
        onValueChange = { fieldText.value = it; update(fieldText.value) },
        label = { Text(label) },
        placeholder = { Text("00") },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun CountDownIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    time: String,
    size: Int,
    stroke: Int
) {

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    Column(modifier = modifier) {
        Box {
            CircularProgressIndicatorBackGround(
                modifier = Modifier
                    .height(size.dp)
                    .width(size.dp),
                color = MaterialTheme.colors.secondary,
                stroke
            )

            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .height(size.dp)
                    .width(size.dp),
                color = MaterialTheme.colors.primary,
                strokeWidth = stroke.dp,
            )

            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(
                    text = time,
                    color = MaterialTheme.colors.onBackground,
                    style = MaterialTheme.typography.h3,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CircularProgressIndicatorBackGround(
    modifier: Modifier = Modifier,
    color: Color,
    stroke: Int
) {
    val style = with(LocalDensity.current) { Stroke(stroke.dp.toPx()) }

    Canvas(
        modifier = modifier,
        onDraw = {

            val innerRadius = (size.minDimension - style.width) / 2

            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 360f,
                topLeft = Offset(
                    (size / 2.0f).width - innerRadius,
                    (size / 2.0f).height - innerRadius
                ),
                size = Size(innerRadius * 2, innerRadius * 2),
                useCenter = false,
                style = style
            )
        }
    )
}

@ExperimentalAnimationApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@ExperimentalAnimationApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
