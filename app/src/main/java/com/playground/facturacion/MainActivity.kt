package com.playground.facturacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.playground.facturacion.ui.FacturacionApp
import com.playground.facturacion.ui.theme.FacturacionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    FacturacionTheme {
        FacturacionApp()
    }
}
