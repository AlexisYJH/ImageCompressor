package com.example.imagecompressor

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.imagecompressor.CompressUtils.nativeCompress
import com.example.imagecompressor.CompressUtils.qualityCompress
import com.example.imagecompressor.CompressUtils.scaleCompress
import com.example.imagecompressor.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            onButtonClick(button1, ::qualityCompress)
            onButtonClick(button2, ::scaleCompress)
            onButtonClick(button3, ::nativeCompress)
        }
    }

    private fun onButtonClick(button: Button, compress: (Context) -> Boolean) {
        button.setOnClickListener {
            button.isEnabled = false;
            lifecycleScope.launch(Dispatchers.IO) {
                val time = System.currentTimeMillis()
                val success = compress(applicationContext)
                withContext(Dispatchers.Main) {
                    println("ImageCompressor ${button.text} ${System.currentTimeMillis()-time}ms")
                    Toast.makeText(
                        this@MainActivity,
                        "${button.text}${if (success) "成功" else "失败"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    button.isEnabled = true;
                }
            }
        }
    }
}