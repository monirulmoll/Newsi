package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.StudioProjectEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(
    projects: List<StudioProjectEntity>,
    statusMessage: String,
    isGgufLoaded: Boolean,
    ggufModelName: String,
    onCreateProject: (String, String, String) -> Unit,
    onOpenProject: (StudioProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onOpenAiStudio: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    BackHandler { onBackToWelcome() }

    var newProjectName by remember { mutableStateOf("") }
    var newPackageName by remember { mutableStateOf("") }
    var newOverlayTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Floating Overlay Projects",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isGgufLoaded) "Termux Connected: $ggufModelName" else "Termux Backend: Disconnected",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isGgufLoaded) Color(0xFF34D399) else Color(0xFFFBBF24)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToWelcome,
                        modifier = Modifier.testTag("launcher_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to mode selector"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onOpenAiStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("launcher_open_ai_studio_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("AI Mode (Termux)")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Create New Floating Overlay Project",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = newProjectName,
                            onValueChange = { newProjectName = it },
                            label = { Text("Overlay App Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_project_name_input")
                        )
                        OutlinedTextField(
                            value = newPackageName,
                            onValueChange = { newPackageName = it },
                            label = { Text("Package Name (optional)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_project_package_input")
                        )
                        OutlinedTextField(
                            value = newOverlayTitle,
                            onValueChange = { newOverlayTitle = it },
                            label = { Text("Floating Panel Header Title") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_project_overlay_title_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val name = newProjectName.trim().ifEmpty { "Floating Overlay App" }
                                    val pkg = newPackageName.trim()
                                    val title = newOverlayTitle.trim().ifEmpty { "$name Panel" }
                                    onCreateProject(name, pkg, title)
                                    newProjectName = ""
                                    newPackageName = ""
                                    newOverlayTitle = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_project_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Create Canvas Project")
                            }
                            OutlinedButton(
                                onClick = onOpenAiStudio,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("generate_with_gguf_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Prompt -> GGUF")
                            }
                        }
                        if (statusMessage.isNotBlank()) {
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Saved Overlay Projects (${projects.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }

            items(projects, key = { it.id }) { project ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenProject(project) }
                        .testTag("project_card_${project.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${project.packageName} • ${project.canvasWidthDp}x${project.canvasHeightDp}dp",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Overlay Title: ${project.overlayTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2563EB)
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { onOpenProject(project) },
                                modifier = Modifier.testTag("open_project_${project.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Open project",
                                    tint = Color(0xFF2563EB)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteProject(project.id) },
                                modifier = Modifier.testTag("delete_project_${project.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete project",
                                    tint = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
