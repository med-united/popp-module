package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.thirdparty.auth.OidcSessionStore
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.callback_error_prefix
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.callback_invalid_params
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.callback_return_launcher
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.callback_state_mismatch
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.callback_validating
import org.jetbrains.compose.resources.stringResource

@Composable
fun PoppCallbackScreen(
    code: String?,
    state: String?,
    error: String?,
    onValidationFailed: () -> Unit,
    onSuccess: (code: String) -> Unit,
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentOnValidationFailed by rememberUpdatedState(onValidationFailed)
    val currentOnSuccess by rememberUpdatedState(onSuccess)

    val errorPrefix = stringResource(Res.string.callback_error_prefix)
    val invalidParamsMsg = stringResource(Res.string.callback_invalid_params)
    val stateMismatchMsg = stringResource(Res.string.callback_state_mismatch)

    LaunchedEffect(code, state, error) {
        if (error != null) {
            errorMessage = "$errorPrefix $error"
            return@LaunchedEffect
        }

        if (code == null || state == null) {
            errorMessage = invalidParamsMsg
            return@LaunchedEffect
        }

        val expectedState = OidcSessionStore.state.value
        if (expectedState == null || state != expectedState) {
            errorMessage = stateMismatchMsg
            currentOnValidationFailed()
            return@LaunchedEffect
        }

        currentOnSuccess(code)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (errorMessage != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
                Button(onClick = currentOnValidationFailed) {
                    Text(stringResource(Res.string.callback_return_launcher))
                }
            }
        } else {
            Text(stringResource(Res.string.callback_validating))
        }
    }
}
