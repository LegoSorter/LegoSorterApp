package com.lsorter

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class App: Application() {
    companion object {
        private var instance: App? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        fun sharedPreferences(): SharedPreferences{
            return applicationContext().getSharedPreferences(
                applicationContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        }

    }

    init {
        instance = this
    }
    override fun onCreate() {
        super.onCreate()

        val context: Context = App.applicationContext()
    }
}
