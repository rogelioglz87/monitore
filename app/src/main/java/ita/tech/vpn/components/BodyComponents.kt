package ita.tech.vpn.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ModalAlerta(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    icono: @Composable () -> Unit,
    showCancelButton: Boolean = false,
){
    AlertDialog(
        onDismissRequest = { onDismiss() },
        icon = { icono() },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(body) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            if (showCancelButton) {
                FilledTonalButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}