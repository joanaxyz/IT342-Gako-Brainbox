package edu.cit.gako.brainbox.app

import android.app.Application

class BrainBoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BrainBoxAppGraph.from(this)
    }
}
