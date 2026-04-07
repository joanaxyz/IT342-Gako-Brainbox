package com.it342.brainbox.auth

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.it342.brainbox.R
import com.it342.brainbox.network.RetrofitClient
import com.it342.brainbox.network.SessionManager
import com.it342.brainbox.network.models.UserProfile
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserProfile()
                
                findViewById<TextView>(R.id.username_value).text = response.username
                findViewById<TextView>(R.id.email_value).text = response.email
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error fetching profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
