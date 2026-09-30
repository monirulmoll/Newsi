package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Token
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CanvasComponentEntity
import java.io.File

private val InspectorDarkBg = Color(0xFF050B18)
private val InspectorFieldBg = Color(0xFF081329)
private val InspectorFieldBorder = Color(0xFF1B325F)
private val InspectorTabInactiveBg = Color(0xFF081226)
private val InspectorTabInactiveBorder = Color(0xFF1A2C4E)
private val InspectorPurpleButton = Color(0xFF5B46F6)
private val InspectorPurpleAccent = Color(0xFF4F46E5)
private val InspectorSubtitleColor = Color(0xFF7B93B8)
private val InspectorPlaceholderColor = Color(0xFF6482AD)

private fun resolveDocumentUriToStoragePath(uri: Uri, fallback: String): String {
    val rawPath = uri.path ?: return fallback
    val primaryIndex = rawPath.indexOf("primary:")
    if (primaryIndex >= 0) {
        val relative = rawPath.substring(primaryIndex + "primary:".length).trimStart('/')
        return "/storage/emulated/0/$relative"
    }
    val colonIndex = rawPath.lastIndexOf(':')
    if (colonIndex >= 0 && colonIndex < rawPath.length - 1) {
        val relative = rawPath.substring(colonIndex + 1).trimStart('/')
        if (relative.isNotEmpty()) {
            return "/storage/emulated/0/$relative"
        }
    }
    return rawPath.ifBlank { fallback }
}

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
    var label by remember(component.id) { mutableStateOf(component.label) }
    var customSourceFilePath by remember(component.id, component.customImagePath) {
        mutableStateOf(component.customImagePath)
    }
    var posX by remember(component.id) { mutableStateOf(component.posXDp.toString()) }
    var posY by remember(component.id) { mutableStateOf(component.posYDp.toString()) }
    var widthDp by remember(component.id) { mutableStateOf(component.widthDp.toString()) }
    var heightDp by remember(component.id) { mutableStateOf(component.heightDp.toString()) }
    var byteOffset by remember(component.id) { mutableStateOf(component.byteOffsetHex) }
    var onPayload by remember(component.id) { mutableStateOf(component.onPayloadHex) }
    var offPayload by remember(component.id) { mutableStateOf(component.offPayloadHex) }
    var bgHex by remember(component.id) { mutableStateOf(component.bgColorHex) }
    var textHex by remember(component.id) { mutableStateOf(component.textColorHex) }
    var currentValue by remember(component.id) { mutableStateOf(component.currentValue) }
    var targetFile by remember(component.id) { mutableStateOf(component.targetFilePath) }

    // Directory icon on "Select your main file" selects ANY source file to replace/merge onto Target Path
    val sourceFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onPickImageUri(uri)
        }
    }

    // Directory icon on "Target Path" selects the destination target file path
    val targetDocumentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val resolvedPath = resolveDocumentUriToStoragePath(uri, targetFile)
            targetFile = resolvedPath
            val updated = component.copy(
                label = label.trim().ifEmpty { component.label },
                customImagePath = customSourceFilePath.trim(),
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
                targetFilePath = resolvedPath.trim()
            )
            onSaveComponent(updated)
        }
    }

    fun buildUpdated(): CanvasComponentEntity {
        return component.copy(
            label = label.trim().ifEmpty { component.label },
            customImagePath = customSourceFilePath.trim(),
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
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        color = InspectorDarkBg,
        border = BorderStroke(1.dp, Color(0xFF152342)),
        tonalElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row matching screenshot: Back arrow + Purple Cube Icon + "Toggle Widget / Configure widget properties & target path" + "Button #1" pill + '✕'
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
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(InspectorPurpleAccent, InspectorPurpleButton))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Token,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        val readableType = component.type.lowercase().replaceFirstChar { it.uppercase() }
                        Text(
                            text = "$readableType Widget",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Configure widget properties & target path",
                            color = InspectorSubtitleColor,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF091328),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E335C))
                    ) {
                        Text(
                            text = label.ifBlank { component.label },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("inspector_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close inspector",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3 Tabs: General | Style | Advanced
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("General", "Style", "Advanced").forEach { tab ->
                    val isSelected = activeTab == tab
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) InspectorPurpleButton else InspectorTabInactiveBg,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF60A5FA) else InspectorTabInactiveBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clickable { activeTab = tab }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            when (activeTab) {
                "General" -> {
                    val selectedFileName = remember(customSourceFilePath) {
                        if (customSourceFilePath.isNotBlank()) {
                            File(customSourceFilePath).name.ifBlank { customSourceFilePath }
                        } else {
                            ""
                        }
                    }

                    // 1. Widget Name (Full-width clean input without leading/trailing icon boxes)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "Widget Name",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = label,
                            onValueChange = {
                                label = it
                                onSaveComponent(buildUpdated())
                            },
                            placeholder = {
                                Text("Button #1", color = InspectorPlaceholderColor, fontSize = 13.sp)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InspectorFieldBg,
                                unfocusedContainerColor = InspectorFieldBg,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = InspectorFieldBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inspector_label_input")
                        )
                    }

                    // 2. Select your main file ("Select file" box + purple folder button on right)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "Select your main file",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = InspectorFieldBg,
                                border = BorderStroke(1.dp, InspectorFieldBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clickable {
                                        sourceFilePickerLauncher.launch(arrayOf("*/*"))
                                    }
                                    .testTag("inspector_main_file_box")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = if (selectedFileName.isNotBlank()) selectedFileName else "Select file",
                                        color = if (selectedFileName.isNotBlank()) Color.White else InspectorPlaceholderColor,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedFileName.isNotBlank()) FontWeight.SemiBold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(InspectorPurpleButton)
                                    .clickable {
                                        sourceFilePickerLauncher.launch(arrayOf("*/*"))
                                    }
                                    .testTag("inspector_select_source_file_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Select Main File",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 3. Target Path (Full-width dark input + purple folder button on right)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "Target Path",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = targetFile,
                                onValueChange = {
                                    targetFile = it
                                    onSaveComponent(buildUpdated())
                                },
                                placeholder = {
                                    Text(
                                        "/storage/emulated/0/app.apk",
                                        color = InspectorPlaceholderColor,
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InspectorFieldBg,
                                    unfocusedContainerColor = InspectorFieldBg,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = InspectorFieldBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("inspector_target_path_input")
                            )

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(InspectorPurpleButton)
                                    .clickable {
                                        targetDocumentPickerLauncher.launch(arrayOf("*/*"))
                                    }
                                    .testTag("inspector_select_target_path_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Select Target Path",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                "Style" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("BG Hex", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = bgHex,
                                onValueChange = {
                                    bgHex = it
                                    onSaveComponent(buildUpdated())
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InspectorFieldBg,
                                    unfocusedContainerColor = InspectorFieldBg,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = InspectorFieldBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inspector_bg_hex_input")
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("Text Hex", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = textHex,
                                onValueChange = {
                                    textHex = it
                                    onSaveComponent(buildUpdated())
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InspectorFieldBg,
                                    unfocusedContainerColor = InspectorFieldBg,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = InspectorFieldBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inspector_text_hex_input")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("Width (dp)", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = widthDp,
                                onValueChange = {
                                    widthDp = it
                                    onSaveComponent(buildUpdated())
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InspectorFieldBg,
                                    unfocusedContainerColor = InspectorFieldBg,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = InspectorFieldBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inspector_width_input")
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("Height (dp)", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = heightDp,
                                onValueChange = {
                                    heightDp = it
                                    onSaveComponent(buildUpdated())
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InspectorFieldBg,
                                    unfocusedContainerColor = InspectorFieldBg,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = InspectorFieldBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inspector_height_input")
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
                            border = BorderStroke(1.dp, InspectorFieldBorder),
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
                            border = BorderStroke(1.dp, InspectorFieldBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Duplicate", color = Color.White, fontSize = 11.sp)
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
        }
    }
}
