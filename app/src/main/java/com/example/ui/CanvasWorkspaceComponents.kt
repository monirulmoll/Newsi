package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CanvasComponentEntity
import com.example.data.ComponentWidgetType
import com.example.data.ConfigWriteAuditEntity
import com.example.data.StudioProjectEntity
import kotlin.math.roundToInt

fun parseHexColorSafe(hex: String, fallback: Color = Color(0xFF2563EB)): Color {
    return try {
        val clean = hex.trim()
        val formatted = if (clean.startsWith("#")) clean else "#$clean"
        Color(android.graphics.Color.parseColor(formatted))
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun WidgetPaletteStrip(
    onAddWidget: (ComponentWidgetType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ComponentWidgetType.entries.forEach { widgetType ->
            Button(
                onClick = { onAddWidget(widgetType) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_widget_${widgetType.name.lowercase()}")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = widgetType.displayName,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun InteractiveOverlayCanvas(
    project: StudioProjectEntity,
    components: List<CanvasComponentEntity>,
    selectedComponentId: Long?,
    onSelectComponent: (Long?) -> Unit,
    onMoveComponent: (Long, Int, Int) -> Unit,
    onTriggerComponent: (CanvasComponentEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val canvasBg = parseHexColorSafe(project.canvasBgColorHex, Color(0xFF1E293B))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .clickable { onSelectComponent(null) }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = canvasBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .size(
                    width = project.canvasWidthDp.dp.coerceIn(200.dp, 380.dp),
                    height = project.canvasHeightDp.dp.coerceIn(220.dp, 520.dp)
                )
                .testTag("overlay_canvas_window")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Floating Window Title Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.overlayTitle.ifBlank { "${project.name} Panel" },
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${project.canvasWidthDp}x${project.canvasHeightDp}dp",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Absolute-positioned widget area
                Box(modifier = Modifier.fillMaxSize()) {
                    if (components.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap + above or use GGUF AI Builder to populate overlay controls",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    components.forEach { comp ->
                        val isSelected = comp.id == selectedComponentId
                        var offsetX by remember(comp.id, comp.posXDp) {
                            mutableFloatStateOf(with(density) { comp.posXDp.dp.toPx() })
                        }
                        var offsetY by remember(comp.id, comp.posYDp) {
                            mutableFloatStateOf(with(density) { comp.posYDp.dp.toPx() })
                        }

                        val widgetBg = parseHexColorSafe(comp.bgColorHex, Color(0xFF2563EB))
                        val widgetText = parseHexColorSafe(comp.textColorHex, Color.White)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = widgetBg,
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                                .size(comp.widthDp.dp, comp.heightDp.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSelectComponent(comp.id)
                                    val nextVal = if (comp.type == "TOGGLE") {
                                        if (comp.currentValue == "true") "false" else "true"
                                    } else {
                                        comp.currentValue
                                    }
                                    onTriggerComponent(comp, nextVal)
                                }
                                .pointerInput(comp.id) {
                                    detectDragGestures(
                                        onDragStart = { onSelectComponent(comp.id) },
                                        onDragEnd = {
                                            val newXDp = with(density) { offsetX.toDp().value.roundToInt() }.coerceAtLeast(0)
                                            val newYDp = with(density) { offsetY.toDp().value.roundToInt() }.coerceAtLeast(0)
                                            onMoveComponent(comp.id, newXDp, newYDp)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            offsetX = (offsetX + dragAmount.x).coerceAtLeast(0f)
                                            offsetY = (offsetY + dragAmount.y).coerceAtLeast(0f)
                                        }
                                    )
                                }
                                .testTag("canvas_widget_${comp.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = comp.label,
                                        color = widgetText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (comp.type == "SLIDER") {
                                        val sliderVal = (comp.currentValue.toFloatOrNull() ?: 50f)
                                            .coerceIn(0f, comp.sliderMax.toFloat().coerceAtLeast(1f))
                                        Slider(
                                            value = sliderVal,
                                            onValueChange = { v ->
                                                onTriggerComponent(comp, v.roundToInt().toString())
                                            },
                                            valueRange = 0f..comp.sliderMax.toFloat().coerceAtLeast(1f),
                                            modifier = Modifier.height(22.dp)
                                        )
                                    } else if (comp.type == "INPUT" || comp.type == "TEXT") {
                                        Text(
                                            text = comp.currentValue,
                                            color = widgetText.copy(alpha = 0.85f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1
                                        )
                                    }
                                }
                                if (comp.type == "TOGGLE") {
                                    Switch(
                                        checked = comp.currentValue.equals("true", ignoreCase = true),
                                        onCheckedChange = { checked ->
                                            onTriggerComponent(comp, checked.toString())
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditHistoryCard(
    audits: List<ConfigWriteAuditEntity>,
    onClearAudits: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Binary Offset Write Log (${audits.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                if (audits.isNotEmpty()) {
                    TextButton(onClick = onClearAudits) {
                        Text("Clear")
                    }
                }
            }
            audits.take(5).forEach { entry ->
                Text(
                    text = "${entry.parameterKey} @ ${entry.byteOffsetHex} → ${entry.newValue} (CRC32 ${entry.crc32Hex})",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF334155),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun DownloadApkReadyDialog(
    apkFileName: String,
    apkFilePath: String,
    publicDownloadPath: String,
    apkSizeBytes: Long,
    packageName: String,
    onSaveToDeviceStorage: () -> Unit,
    onShareOrInstallApk: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Floating Window APK Built",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("File: $apkFileName", fontWeight = FontWeight.SemiBold)
                Text("Package: $packageName", fontSize = 12.sp)
                Text("Size: ${apkSizeBytes / 1024} KB", fontSize = 12.sp)
                if (publicDownloadPath.isNotBlank()) {
                    Text("Downloads: $publicDownloadPath", fontSize = 11.sp, color = Color(0xFF059669))
                } else {
                    Text("Path: $apkFilePath", fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveToDeviceStorage,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.testTag("dialog_save_apk_button")
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Save APK As...")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onShareOrInstallApk,
                    modifier = Modifier.testTag("dialog_install_apk_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Install / Share")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
