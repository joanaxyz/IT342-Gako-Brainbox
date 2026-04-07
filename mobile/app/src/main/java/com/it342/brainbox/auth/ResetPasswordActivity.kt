package com.it342.brainbox.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.it342.brainbox.R
import com.it342.brainbox.network.RetrofitClient
import com.it342.brainbox.network.models.ResetPasswordRequest
import com.it342.brainbox.network.models.VerifyCodeRequest
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var email: String
    private var resetToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        email = intent.getStringExtra("EMAIL") ?: ""
        if (email.isEmpty()) {
            Toast.makeText(this, "Email is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val codeStepLayout = findViewById<LinearLayout>(R.id.code_step_layout)
        val passwordStepLayout = findViewById<LinearLayout>(R.id.password_step_layout)
        
        val codeEditText = findViewById<TextInputEditText>(R.id.code_edit_text)
        val verifyCodeButton = findViewById<Button>(R.id.verify_code_button)
        
        val newPasswordEditText = findViewById<TextInputEditText>(R.id.new_password_edit_text)
        val confirmPasswordEditText = findViewById<TextInputEditText>(R.id.confirm_password_edit_text)
        val resetPasswordButton = findViewById<Button>(R.id.reset_password_button)
        
        val backToLogin = findViewById<TextView>(R.id.back_to_login)

        verifyCodeButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyCode(code, codeStepLayout, passwordStepLayout)
        }

        resetPasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetPassword(newPassword)
        }

        backToLogin.setOnClickListener {
            finish()
        }
    }

    private fun verifyCode(code: String, codeLayout: View, passwordLayout: View) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.verifyCode(VerifyCodeRequest(email, code))
                resetToken = response.resetToken
                codeLayout.visibility = View.GONE
                passwordLayout.visibility = View.VISIBLE
                Toast.makeText(this@ResetPasswordActivity, "Code verified successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ResetPasswordActivity, "Invalid code: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetPassword(newPassword: String) {
        val token = resetToken ?: return
        lifecycleScope.launch {
            try {
                val message = RetrofitClient.apiService.resetPassword(ResetPasswordRequest(token, newPassword))
                Toast.makeText(this@ResetPasswordActivity, message, Toast.LENGTH_LONG).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ResetPasswordActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
