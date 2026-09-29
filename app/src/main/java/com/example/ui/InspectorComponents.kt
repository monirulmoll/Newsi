package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CanvasComponentEntity

@Composable
fun ComponentPropertyInspectorSheet(
    component: CanvasComponentEntity,
    onSaveComponent: (CanvasComponentEntity) -> Unit,
    onDuplicateComponent: (CanvasComponentEntity) -> Unit,
    onDeleteComponent: (CanvasComponentEntity) -> Unit,
    onTriggerLive: (CanvasComponentEntity, String) -> Unit,
    onOpenCodeEditor: () -> Unit,
    onClose: () -> Unit
) {
    var label by remember(component.id, component.label) { mutableStateOf(component.label) }
    var posX by remember(component.id, component.posXDp) { mutableStateOf(component.posXDp.toString()) }
    var posY by remember(component.id, component.posYDp) { mutableStateOf(component.posYDp.toString()) }
    var widthDp by remember(component.id, component.widthDp) { mutableStateOf(component.widthDp.toString()) }
    var heightDp by remember(component.id, component.heightDp) { mutableStateOf(component.heightDp.toString()) }
    var byteOffset by remember(component.id, component.byteOffsetHex) { mutableStateOf(component.byteOffsetHex) }
    var onPayload by remember(component.id, component.onPayloadHex) { mutableStateOf(component.onPayloadHex) }
    var offPayload by remember(component.id, component.offPayloadHex) { mutableStateOf(component.offPayloadHex) }
    var bgHex by remember(component.id, component.bgColorHex) { mutableStateOf(component.bgColorHex) }
    var textHex by remember(component.id, component.textColorHex) { mutableStateOf(component.textColorHex) }
    var currentValue by remember(component.id, component.currentValue) { mutableStateOf(component.currentValue) }
    var targetFile by remember(component.id, component.targetFilePath) { mutableStateOf(component.targetFilePath) }

    fun buildUpdated(): CanvasComponentEntity {
        return component.copy(
            label = label.trim().ifEmpty { component.label },
            posXDp = posX.toIntOrNull()?.coerceAtLeast(0) ?: component.posXDp,
            posYDp = posY.toIntOrNull()?.coerceAtLeast(0) ?: component.posYDp,
            widthDp = widthDp.toIntOrNull()?.coerceIn(48, 400) ?: component.widthDp,
            heightDp = heightDp.toIntOrNull()?.coerceIn(32, 300) ?: component.heightDp,
            byteOffsetHex = byteOffset.trim().ifEmpty { "0x00" },
            onPayloadHex = onPayload.trim().ifEmpty { "01 00 00 00" },
            offPayloadHex = offPayload.trim().ifEmpty { "00 00 00 00" },
            bgColorHex = bgHex.trim().ifEmpty { "#2563EB" },
            textColorHex = textHex.trim().ifEmpty { "#FFFFFF" },
            currentValue = currentValue.trim(),
            targetFilePath = targetFile.trim()
        )
    }

    Surface(
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inspect ${component.type} (#${component.id})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                IconButton(onClick = onClose, modifier = Modifier.testTag("inspector_close_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Close inspector")
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Widget Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("inspector_label_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = posX,
                    onValueChange = { posX = it },
                    label = { Text("X (dp)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = posY,
                    onValueChange = { posY = it },
                    label = { Text("Y (dp)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = widthDp,
                    onValueChange = { widthDp = it },
                    label = { Text("W (dp)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = heightDp,
                    onValueChange = { heightDp = it },
                    label = { Text("H (dp)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = byteOffset,
                    onValueChange = { byteOffset = it },
                    label = { Text("Offset Hex") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = onPayload,
                    onValueChange = { onPayload = it },
                    label = { Text("ON Payload") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = offPayload,
                    onValueChange = { offPayload = it },
                    label = { Text("OFF Payload") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = bgHex,
                    onValueChange = { bgHex = it },
                    label = { Text("BG Hex") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = textHex,
                    onValueChange = { textHex = it },
                    label = { Text("Text Hex") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    label = { Text("Value") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = targetFile,
                onValueChange = { targetFile = it },
                label = { Text("Target Binary State File") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSaveComponent(buildUpdated()) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.weight(1f).testTag("inspector_save_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Save")
                }
                OutlinedButton(
                    onClick = {
                        val updated = buildUpdated()
                        onTriggerLive(updated, updated.currentValue)
                    },
                    modifier = Modifier.weight(1f).testTag("inspector_test_live_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Test Write")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onDuplicateComponent(buildUpdated()) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Duplicate")
                }
                OutlinedButton(
                    onClick = onOpenCodeEditor,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Code")
                }
                OutlinedButton(
                    onClick = { onDeleteComponent(component) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", color = Color(0xFFDC2626))
                }
            }
        }
    }
}
