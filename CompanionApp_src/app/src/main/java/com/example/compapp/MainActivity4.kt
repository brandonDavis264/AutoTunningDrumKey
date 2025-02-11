package com.example.compapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.compapp.ui.theme.CompAppTheme
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.cos
import kotlin.math.sin

class MainActivity4 : ComponentActivity() {
    private val radius = 300
    private var currentRotation = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        val frameLayout: FrameLayout = findViewById(R.id.frameLayout)
        val lugCount = 8 // replace with user input value
        var curLug = 0

        frameLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                frameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                spawnButtons(frameLayout, lugCount)
            }
        })

        val capture: Button = findViewById(R.id.captureSection)
        val sect: ImageView = findViewById(R.id.section)
        capture.setOnClickListener {
            sect.setImageResource(R.drawable.greentri) //Just turn the thing Green!
        }

        val nxt: Button = findViewById(R.id.nxtSection)

        nxt.setOnClickListener {
            if (curLug % 2 == 0)  {
                currentRotation = (currentRotation + 180f) % 360
            }
            else {
                currentRotation = (currentRotation + 180f + (360/lugCount)) % 360
            } // Ensure rotation stays within 0-360
            sect.setImageResource(R.drawable.redtri)
            curLug = (curLug + 1) % lugCount
            val animator = ObjectAnimator.ofFloat(sect, "rotation", currentRotation)
            animator.duration = 300
            animator.interpolator = LinearInterpolator()
            animator.start()
        }
    }

    private fun spawnButtons(frameLayout: FrameLayout, count: Int) {
        val centerX = frameLayout.width / 2
        val centerY = frameLayout.height / 2

        for (i in 0 until count) {
            val angle = i * 2 * Math.PI / count
            val x = (centerX + radius * cos(angle)).toFloat()
            val y = (centerY + radius * sin(angle)).toFloat()

            val button = Button(this).apply {
                text = "Button ${i + 1}"
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply{
                    leftMargin = (x).toInt() - 100
                    topMargin = (y).toInt() - 50
                }
            }
            frameLayout.addView(button)
        }
    }
}