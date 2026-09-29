package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CanvasComponentEntity

private val DockBg = Color(0xFF0E1528)
private val DockCardBorder = Color(0xFF233052)
private val DockInputBg = Color(0xFF131C33)
private val DockIndigo = Color(0xFF5B46F6)
private val DockPurple = Color(0xFF7C3AED)
private val DockTextSecondary = Color(0xFF94A3B8)

@Composable
fun PropertyInspectorBottomDock(
    component: CanvasComponentEntity,
    hasStoragePermission: Boolean = true,
    isAutoFixSize: Boolean = true,
    onToggleAutoFixSize: () -> Unit = {},
    onOpenEditCode: () -> Unit = {},
    onOpenEditFloatingPanel: () -> Unit = {},
    onUpdateComponent: (CanvasComponentEntity) -> Unit,
    onSaveDesign: (CanvasComponentEntity) -> Unit = {},
    onPickImageUri: (Uri) -> Unit = {},
    onPickSoundUri: (Uri, Boolean) -> Unit = { _, _ -> },
    onDuplicateComponent: () -> Unit = {},
    onDeleteComponent: () -> Unit = {},
    onTestTriggerWrite: (CanvasComponentEntity) -> Unit = {},
    onCloseDock: () -> Unit = {}
) {
    ComponentPropertyInspectorSheet(
        component = component,
        isAutoFixSize = isAutoFixSize,
        onToggleAutoFixSize = onToggleAutoFixSize,
        onPickImageUri = onPickImageUri,
        onOpenEditFloatingPanel = onOpenEditFloatingPanel,
        onSaveComponent = { updated ->
            onUpdateComponent(updated)
            onSaveDesign(updated)
        },
        onDuplicateComponent = { onDuplicateComponent() },
        onDeleteComponent = { onDeleteComponent() },
        onTriggerLive = { updated, _ -> onTestTriggerWrite(updated) },
        onOpenCodeEditor = onOpenEditCode,
        onClose = onCloseDock
    )
}

@Composable
fun ComponentPropertyInspectorSheet(
    component: CanvasComponentEntity,
    isAutoFixSize: Boolean = true,
    onToggleAutoFixSize: () -> Unit = {},
    onPickImageUri: (Uri) -> Unit = {},
    onOpenEditFloatingPanel: () -> Unit = {},
    onSaveComponent: (CanvasComponentEntity) -> Unit,
    onDuplicateComponent: (CanvasComponentEntity) -> Unit,
    onDeleteComponent: (CanvasComponentEntity) -> Unit,
    onTriggerLive: (CanvasComponentEntity, String) -> Unit,
    onOpenCodeEditor: () -> Unit,
    onClose: () -> Unit
) {
    var activeTab by remember { mutableStateOf("General") }
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickImageUri(uri)
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            targetFile = uri.path ?: targetFile
        }
    }

    fun buildUpdated(): CanvasComponentEntity {
        return component.copy(
            label = label.trim().ifEmpty { component.label },
            posXDp = posX.toIntOrNull()?.coerceAtLeast(0) ?: component.posXDp,
            posYDp = posY.toIntOrNull()?.coerceAtLeast(0) ?: component.posYDp,
            widthDp = widthDp.toIntOrNull()?.coerceIn(48, 400) ?: component.widthDp,
            heightDp = heightDp.toIntOrNull()?.coerceIn(32, 300) ?: component.heightDp,
            byteOffsetHex = byteOffset.trim().ifEmpty { "0x04" },
            onPayloadHex = onPayload.trim().ifEmpty { "On" },
            offPayloadHex = offPayload.trim().ifEmpty { "Off" },
            bgColorHex = bgHex.trim().ifEmpty { "#131C33" },
            textColorHex = textHex.trim().ifEmpty { "#FFFFFF" },
            currentValue = currentValue.trim(),
            targetFilePath = targetFile.trim()
        )
    }

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = DockBg,
        border = BorderStroke(1.dp, DockCardBorder),
        tonalElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drag Handle Pill
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF334155))
                    .align(Alignment.CenterHorizontally)
            )

            // Header Row matching screenshot ("File Picker Widget / Switch Widget" + "Widget #X" + Close '✕')
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(DockIndigo, DockPurple))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Token,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${component.type.lowercase().replaceFirstChar { it.uppercase() }} Widget",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Configure widget properties & target path",
                            color = DockTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = DockInputBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DockCardBorder),
                        modifier = Modifier.clickable { onOpenEditFloatingPanel() }
                    ) {
                        Text(
                            text = "Widget #${component.id}",
                            color = DockTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("inspector_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close inspector",
                            tint = DockTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3 Tabs: General | Style | Advanced (Matching Screenshot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("General", "Style", "Advanced").forEach { tab ->
                    val isSelected = activeTab == tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) DockIndigo else DockInputBg,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF818CF8) else DockCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clickable { activeTab = tab }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.White else DockTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            when (activeTab) {
                "General" -> {
                    // 1. Widget Name / Label
                    InspectorFieldRow(
                        title = "Widget Name / Label",
                        leadingIcon = Icons.Default.Edit,
                        value = label,
                        onValueChange = {
                            label = it
                            onSaveComponent(buildUpdated())
                        },
                        placeholder = "Enter widget name...",
                        testTag = "inspector_label_input",
                        trailingFolderClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    // 2. Target Path (with Folder picker button)
                    InspectorFieldRow(
                        title = "Target Path",
                        leadingIcon = Icons.Default.Folder,
                        value = targetFile,
                        onValueChange = {
                            targetFile = it
                            onSaveComponent(buildUpdated())
                        },
                        placeholder = "/storage/emulated/0/app.apk",
                        testTag = "inspector_target_path_input",
                        trailingFolderClick = {
                            documentPickerLauncher.launch(arrayOf("*/*"))
                        }
                    )

                    // 3. Byte Offset & ON/OFF Payload
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            InspectorFieldRow(
                                title = "Offset Hex",
                                leadingIcon = Icons.Default.Description,
                                value = byteOffset,
                                onValueChange = {
                                    byteOffset = it
                                    onSaveComponent(buildUpdated())
                                },
                                placeholder = "0x04",
                                testTag = "inspector_offset_input",
                                trailingFolderClick = null
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            InspectorFieldRow(
                                title = "ON Value",
                                leadingIcon = Icons.Default.Check,
                                value = onPayload,
                                onValueChange = {
                                    onPayload = it
                                    onSaveComponent(buildUpdated())
                                },
                                placeholder = "On",
                                testTag = "inspector_on_payload_input",
                                trailingFolderClick = null
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            InspectorFieldRow(
                                title = "OFF Value",
                                leadingIcon = Icons.Default.Close,
                                value = offPayload,
                                onValueChange = {
                                    offPayload = it
                                    onSaveComponent(buildUpdated())
                                },
                                placeholder = "Off",
                                testTag = "inspector_off_payload_input",
                                trailingFolderClick = null
                            )
                        }
                    }
                }

                "Style" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            InspectorFieldRow(
                                title = "BG Hex",
                                leadingIcon = Icons.Default.Image,
                                value = bgHex,
                                onValueChange = {
                                    bgHex = it
                                    onSaveComponent(buildUpdated())
                                },
                                placeholder = "#131C33",
                                testTag = "inspector_bg_hex_input",
                                trailingFolderClick = null
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            InspectorFieldRow(
                                title = "Text Hex",
                                leadingIcon = Icons.Default.Edit,
                                value = textHex,
                                onValueChange = {
                                    textHex = it
                                    onSaveComponent(buildUpdated())
                                },
                                placeholder = "#FFFFFF",
                                testTag = "inspector_text_hex_input",
                                trailingFolderClick = null
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            InspectorFieldRow(
                                title = "Width (dp)",
                                leadingIcon = Icons.Default.Tune,
                                value = widthDp,
                                onValueChange = {
                                    widthDp = it
                                    onSaveComponent(buildUpdated())
                                },
                                placeholder = "196",
                                testTag = "inspector_width_input",
                                trailingFolderClick = null
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            InspectorFieldRow(
                                title = "Height (dp)",
                                leadingIcon = Icons.Default.Tune,
                                value = heightDp,
                                onValueChange = {
                                    heightDp = it
                                    onSaveComponent(buildUpdated())
                                },
                                placeholder = "44",
                                testTag = "inspector_height_input",
                                trailingFolderClick = null
                            )
                        }
                    }
                }

                "Advanced" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val updated = buildUpdated()
                                onTriggerLive(updated, updated.currentValue)
                            },
                            border = BorderStroke(1.dp, DockCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("inspector_test_live_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Test Write", color = Color.White, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onDuplicateComponent(buildUpdated()) },
                            border = BorderStroke(1.dp, DockCardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Duplicate", color = Color.White, fontSize = 11.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenCodeEditor,
                            border = BorderStroke(1.dp, DockCardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Code", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onDeleteComponent(component) },
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delete", color = Color(0xFFF87171), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Full-width gradient "✓ Save Changes" Button matching screenshot
            Button(
                onClick = { onSaveComponent(buildUpdated()) },
                colors = ButtonDefaults.buttonColors(containerColor = DockIndigo),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inspector_save_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Save Changes",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InspectorFieldRow(
    title: String,
    leadingIcon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String,
    trailingFolderClick: (() -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DockInputBg)
                    .border(BorderStroke(1.dp, DockCardBorder), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = title,
                    tint = DockTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(placeholder, color = DockTextSecondary, fontSize = 11.sp)
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DockInputBg,
                    unfocusedContainerColor = DockInputBg,
                    focusedBorderColor = DockIndigo,
                    unfocusedBorderColor = DockCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag(testTag)
            )

            if (trailingFolderClick != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(DockIndigo, DockPurple))
                        )
                        .clickable { trailingFolderClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Browse",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
