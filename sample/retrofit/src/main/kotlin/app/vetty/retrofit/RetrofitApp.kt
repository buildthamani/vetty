package app.vetty.retrofit

import android.app.Application
import app.thamani.vetty.core.Vetty

class RetrofitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Vetty.init {
            enabled = true
        }
    }
}
