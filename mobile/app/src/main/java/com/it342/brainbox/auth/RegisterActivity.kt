package com.it342.brainbox.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.it342.brainbox.R
import com.it342.brainbox.network.RetrofitClient
import com.it342.brainbox.network.models.RegisterRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameEditText = findViewById<TextInputEditText>(R.id.username_edit_text)
        val emailEditText = findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password_edit_text)
        val confirmPasswordEditText = findViewById<TextInputEditText>(R.id.confirm_password_edit_text)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginLink = findViewById<TextView>(R.id.login_link)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performRegister(username, email, password)
        }

        loginLink.setOnClickListener {
            finish()
        }
    }

    private fun performRegister(username: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val message = RetrofitClient.apiService.register(RegisterRequest(username, email, password))
                Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                // Navigate back to login
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
