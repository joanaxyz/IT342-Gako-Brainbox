package edu.cit.gako.brainbox.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.R
import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.AuthStage
import edu.cit.gako.brainbox.shared.AuthHeader
import edu.cit.gako.brainbox.shared.BrandedTextField
import edu.cit.gako.brainbox.shared.CodeInputRow
import edu.cit.gako.brainbox.shared.InlinePrompt
import edu.cit.gako.brainbox.shared.LogoMark
import edu.cit.gako.brainbox.shared.PrimaryActionButton
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun AuthScene(
    state: AppState,
    onLogin: (String, String) -> Unit,
    onGoogleLogin: (String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onSendResetCode: (String) -> Unit,
    onVerifyResetCode: (String) -> Unit,
    onResetPassword: (String) -> Unit,
    onAuthStageChange: (AuthStage) -> Unit,
    onFeatureRequest: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Accent.copy(alpha = 0.08f),
                            Color.Transparent,
                            White.copy(alpha = 0.2f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            AuthBrandHeader()

            AuthCard {
                when (state.authStage) {
                    AuthStage.LOGIN -> LoginPane(
                        isBusy = state.isBusy,
                        onSubmit = onLogin,
                        onGoogleLogin = onGoogleLogin,
                        onForgotPassword = { onAuthStageChange(AuthStage.FORGOT_EMAIL) },
                        onRegister = { onAuthStageChange(AuthStage.REGISTER) }
                    )
                    AuthStage.REGISTER -> RegisterPane(
                        isBusy = state.isBusy,
                        onSubmit = onRegister,
                        onLogin = { onAuthStageChange(AuthStage.LOGIN) }
                    )
                    AuthStage.FORGOT_EMAIL -> ForgotPasswordPane(
                        isBusy = state.isBusy,
                        onSubmit = onSendResetCode,
                        onBack = { onAuthStageChange(AuthStage.LOGIN) }
                    )
                    AuthStage.FORGOT_CODE -> VerifyCodePane(
                        email = state.pendingResetEmail,
                        isBusy = state.isBusy,
                        onSubmit = onVerifyResetCode,
                        onBack = { onAuthStageChange(AuthStage.FORGOT_EMAIL) }
                    )
                    AuthStage.RESET_PASSWORD -> ResetPasswordPane(
                        email = state.pendingResetEmail,
                        isBusy = state.isBusy,
                        onSubmit = onResetPassword,
                        onBack = { onAuthStageChange(AuthStage.FORGOT_CODE) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthBrandHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LogoMark(size = 68)
        Text(
            text = "BrainBox",
            style = MaterialTheme.typography.headlineLarge,
            color = Ink
        )
    }
}

@Composable
private fun AuthCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = White.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, Border),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content
        )
    }
}

@Composable
private fun LoginPane(
    isBusy: Boolean,
    onSubmit: (String, String) -> Unit,
    onGoogleLogin: (String) -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthHeader(
        title = "Welcome back",
        subtitle = "Sign in to continue."
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BrandedTextField("Username or email", username, { username = it }, "Enter your username or email")
        BrandedTextField("Password", password, { password = it }, "Enter your password", isPassword = true)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        androidx.compose.material3.TextButton(onClick = onForgotPassword) {
            Text("Forgot password?", color = Accent, style = MaterialTheme.typography.bodySmall)
        }
    }

    PrimaryActionButton("Log In", isBusy) {
        onSubmit(username.trim(), password)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
        Text("or", style = MaterialTheme.typography.bodySmall, color = Ink3)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
    }

    GoogleSignInButton(isBusy = isBusy, onClick = { onGoogleLogin("__TRIGGER_GOOGLE_SIGN_IN__") })

    InlinePrompt("Don't have an account?", "Sign up", onRegister)
}

@Composable
private fun GoogleSignInButton(isBusy: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isBusy,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Accent
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.labelLarge,
                color = Ink
            )
        }
    }
}

@Composable
private fun RegisterPane(
    isBusy: Boolean,
    onSubmit: (String, String, String) -> Unit,
    onLogin: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthHeader(
        title = "Create your account",
        subtitle = "Start using BrainBox."
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BrandedTextField("Username", username, { username = it }, "Choose a username")
        BrandedTextField("Email", email, { email = it }, "Enter your email")
        BrandedTextField("Password", password, { password = it }, "Create a password", isPassword = true)
    }

    PrimaryActionButton("Create Account", isBusy) {
        onSubmit(username.trim(), email.trim(), password)
    }

    InlinePrompt("Already have an account?", "Log in", onLogin)
}

@Composable
private fun ForgotPasswordPane(
    isBusy: Boolean,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }

    AuthHeader(
        title = "Reset your password",
        subtitle = "We'll send a six-digit code to your email."
    )

    BrandedTextField("Email", email, { email = it }, "Enter your email")
    PrimaryActionButton("Send Code", isBusy) {
        onSubmit(email.trim())
    }
    InlinePrompt("Remembered your password?", "Back to login", onBack)
}

@Composable
private fun VerifyCodePane(
    email: String,
    isBusy: Boolean,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    var code by rememberSaveable { mutableStateOf("") }

    AuthHeader(
        title = "Enter your code",
        subtitle = "We sent a code to $email."
    )

    CodeInputRow(value = code, onValueChange = { code = it })
    PrimaryActionButton("Verify Code", isBusy) {
        onSubmit(code)
    }
    InlinePrompt("Need a new code?", "Go back", onBack)
}

@Composable
private fun ResetPasswordPane(
    email: String,
    isBusy: Boolean,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }

    AuthHeader(
        title = "Set a new password",
        subtitle = "You're resetting the password for $email."
    )

    BrandedTextField("New password", password, { password = it }, "Enter a new password", isPassword = true)
    PrimaryActionButton("Save Password", isBusy) {
        onSubmit(password)
    }
    InlinePrompt("Need to verify again?", "Back", onBack)
}

