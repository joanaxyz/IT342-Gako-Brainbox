package com.it342.brainbox.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.it342.brainbox.DashboardActivity
import com.it342.brainbox.R
import com.it342.brainbox.network.RetrofitClient
import com.it342.brainbox.network.SessionManager
import com.it342.brainbox.network.models.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        // Redirect if already logged in
        if (sessionManager.fetchAuthToken() != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        val usernameEditText = findViewById<TextInputEditText>(R.id.username_edit_text)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password_edit_text)
        val loginButton = findViewById<Button>(R.id.login_button)
        val signupLink = findViewById<TextView>(R.id.signup_link)
        val forgotPassword = findViewById<TextView>(R.id.forgot_password)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }

        signupLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                sessionManager.saveAuthToken(response.accessToken)
                sessionManager.saveRefreshToken(response.refreshToken)
                sessionManager.saveUsername(username)

                Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
