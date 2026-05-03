package com.android.fitstore

import android.app.Application
import com.fitstore.di.networkModule
import com.fitstore.di.sharedModule
import com.fitstore.di.targetModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

class MyApplication: Application() {
    companion object {
        lateinit var instance: MyApplication
            private set
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        GlobalContext.startKoin {
            androidContext(this@MyApplication)
            modules(sharedModule, targetModule, networkModule)
        }
    }
}