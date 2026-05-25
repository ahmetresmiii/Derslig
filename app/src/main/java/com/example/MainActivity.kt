package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.DersligApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DersligViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by lazy {
        DersligViewModel(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-To-Edge enabled matches the edge-to-edge guideline of the workspace
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                DersligApp(viewModel = viewModel)
            }
        }
    }
}
