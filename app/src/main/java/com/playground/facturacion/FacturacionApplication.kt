package com.playground.facturacion

import android.app.Application
import com.playground.facturacion.core.AppContainer

class FacturacionApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
