package compose.project.demo.contexts.auth.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (email: String, password: String) -> Unit,
    onNavigateRegister: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Text(
                    text = "⚔️ HabitQuest",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Sign in with your account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        localError = null
                    },
                    label = { Text("Email") },
                    leadingIcon = { Text("👤", modifier = Modifier.padding(start = 4.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Text("🔒", modifier = Modifier.padding(start = 4.dp)) },
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                )

                // Error message
                val displayedError = localError ?: errorMessage
                if (displayedError != null) {
                    Text(
                        text = displayedError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Login button
                Button(
                    onClick = {
                        when {
                            email.isBlank() -> localError = "Enter your email"
                            !email.contains("@") -> localError = "Enter a valid email"
                            password.isBlank() -> localError = "Enter your password"
                            else -> onLogin(email.trim(), password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(text = "Sign in", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Divider
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "  or  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                // Register button
                OutlinedButton(
                    onClick = onNavigateRegister,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(text = "Create an account", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
