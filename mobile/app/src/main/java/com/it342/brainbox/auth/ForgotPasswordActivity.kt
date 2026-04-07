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
import com.it342.brainbox.network.models.ForgotPasswordRequest
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val emailEditText = findViewById<TextInputEditText>(R.id.email_edit_text)
        val resetButton = findViewById<Button>(R.id.reset_button)
        val backToLogin = findViewById<TextView>(R.id.back_to_login)

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performForgotPassword(email)
        }

        backToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performForgotPassword(email: String) {
        lifecycleScope.launch {
            try {
                val message = RetrofitClient.apiService.forgotPassword(ForgotPasswordRequest(email))
                Toast.makeText(this@ForgotPasswordActivity, message, Toast.LENGTH_LONG).show()
                
                val intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                intent.putExtra("EMAIL", email)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ForgotPasswordActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
