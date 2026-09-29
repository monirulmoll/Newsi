package com.example.engine

import com.example.data.CanvasComponentEntity
import com.example.data.ComponentWidgetType

enum class StudioGenerationMode {
    OFFLINE_MANUAL,
    AI_GGUF_MODE
}

fun interface TokenStreamCallback {
    fun onToken(piece: String, tokenId: Int)
}

data class AiBuildStepStatus(
    val stepNumber: Int,
    val totalSteps: Int,
    val title: String,
    val detail: String,
    val isCompleted: Boolean = false,
    val hasError: Boolean = false
)

data class GeneratedFileArtifact(
    val name: String,
    val relativePath: String,
    val fullPath: String,
    val role: String
)

enum class AiRequestClassification {
    CLASS_A_SPECIFIC_APP,
    CLASS_B_AUTONOMOUS_CHOICE,
    CLASS_C_NEEDS_CLARIFICATION,
    CONVERSATIONAL_CHAT
}

data class AiChatTurn(
    val id: Long = System.currentTimeMillis(),
    val userPrompt: String,
    val aiResponseText: String,
    val steps: List<AiBuildStepStatus> = emptyList(),
    val isAppReady: Boolean = false,
    val isConversationalReply: Boolean = false,
    val generatedCodePreview: String = "",
    val generatedScratchFiles: Map<String, String> = emptyMap(),
    val appName: String = "",
    val packageName: String = "",
    val apkFileName: String = "",
    val apkFilePath: String = "",
    val publicDownloadApkPath: String = "",
    val projectRootPath: String = "",
    val targetDataFileName: String = "",
    val targetDataFilePath: String = "",
    val isFloatingOverlayApp: Boolean = true,
    val appCategory: String = "FLOATING_OVERLAY_APP",
    val fileArtifacts: List<GeneratedFileArtifact> = emptyList(),
    val requestClass: AiRequestClassification = AiRequestClassification.CLASS_A_SPECIFIC_APP,
    val decisionAnnouncement: String = "",
    val appPurpose: String = "",
    val buildPlanSummary: String = "",
    val expectedBehavior: String = ""
)

enum class AiPromptIntent {
    CONVERSATIONAL_CHAT,
    BUILD_OR_MODIFY_APP
}

data class AiPromptEvaluation(
    val intent: AiPromptIntent,
    val shouldBuildOrUpdateApp: Boolean,
    val conversationalReply: String = "",
    val requestClass: AiRequestClassification = AiRequestClassification.CLASS_A_SPECIFIC_APP,
    val selectedConceptName: String = "",
    val decisionAnnouncement: String = ""
)

data class GgufModelState(
    val mode: StudioGenerationMode = StudioGenerationMode.OFFLINE_MANUAL,
    val isValidGgufLoaded: Boolean = false,
    val modelHandle: Long = 0L,
    val contextHandle: Long = 0L,
    val modelFileName: String = "",
    val modelFilePath: String = "",
    val modelArchitecture: String = "",
    val modelDescription: String = "",
    val quantizationTag: String = "",
    val modelSizeBytes: Long = 0L,
    val tensorCount: Long = 0L,
    val kvCount: Long = 0L,
    val vocabSize: Int = 0,
    val embeddingDim: Int = 0,
    val numLayers: Int = 0,
    val attentionHeads: Int = 0,
    val contextLength: Int = 0,
    val parameterCount: Long = 0L,
    val importErrorMessage: String? = null,
    val statusMessage: String = "No GGUF model selected.",
    val lastGeneratedBlueprintSummary: String = ""
)

data class NeuralInferenceTrace(
    val usedNativeJni: Boolean,
    val ggufVersion: Int,
    val tensorCount: Long,
    val kvCount: Long,
    val vocabSize: Int,
    val inputTokenIds: List<Int>,
    val generatedTokenIds: List<Int>,
    val decodedTokenStream: String,
    val topLogitConfidence: Float,
    val embeddingDimension: Int,
    val attentionHeads: Int,
    val layersEvaluated: Int
)

data class OverlayPanelSpec(
    val width: Int = 280,
    val height: Int = 360,
    val x: Int = 48,
    val y: Int = 140,
    val draggable: Boolean = true
)

data class OverlayThemeSpec(
    val backgroundColor: String = "#0F172A",
    val panelColor: String = "#1E293B",
    val accentColor: String = "#38BDF8",
    val textColor: String = "#F8FAFC"
)

data class OverlayServiceSpec(
    val className: String = "AiDynamicOverlayService",
    val windowType: String = "TYPE_APPLICATION_OVERLAY",
    val flags: String = "FLAG_NOT_FOCUSABLE"
)

data class OverlayActionSpec(
    val id: String,
    val type: String,
    val expression: String
)

data class StructuredWidgetSpec(
    val widgetType: ComponentWidgetType,
    val label: String,
    val fieldSlug: String,
    val byteOffsetHex: String,
    val offPayload: String,
    val onPayload: String,
    val initialValue: String,
    val sliderMax: Int,
    val bgColorHex: String,
    val textColorHex: String,
    val soundTrigger: String,
    val customLogicExpression: String
)

data class StructuredAppSpecification(
    val appType: String = "floating_overlay",
    val appName: String,
    val packageName: String,
    val overlayTitle: String,
    val targetFilePath: String,
    val isFloatingOverlay: Boolean = true,
    val appCategory: String = "FLOATING_OVERLAY_APP",
    val requestClass: AiRequestClassification = AiRequestClassification.CLASS_A_SPECIFIC_APP,
    val decisionAnnouncement: String,
    val appPurpose: String,
    val expectedBehavior: String,
    val panel: OverlayPanelSpec = OverlayPanelSpec(),
    val theme: OverlayThemeSpec = OverlayThemeSpec(),
    val service: OverlayServiceSpec = OverlayServiceSpec(),
    val permissions: List<String> = listOf(
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.FOREGROUND_SERVICE"
    ),
    val actions: List<OverlayActionSpec> = emptyList(),
    val widgets: List<StructuredWidgetSpec>,
    val rawLlmJson: String = "",
    val inferenceTrace: NeuralInferenceTrace
)

data class GeneratedBlueprintSpec(
    val suggestedAppName: String,
    val suggestedPackageName: String,
    val suggestedOverlayTitle: String,
    val suggestedTargetFilePath: String,
    val components: List<CanvasComponentEntity>,
    val kotlinJavaSummary: String,
    val generatedScratchFiles: Map<String, String> = emptyMap(),
    val compilerDiagnostics: List<String> = emptyList(),
    val autoPatchedFixes: List<String> = emptyList(),
    val finalErrorCount: Int = 0,
    val isFloatingOverlayApp: Boolean = true,
    val appCategory: String = "FLOATING_OVERLAY_APP",
    val apkFileName: String = "",
    val apkOutputPath: String = "",
    val publicDownloadApkPath: String = "",
    val projectRootPath: String = "",
    val structuredBuildOutput: String = "",
    val fileArtifacts: List<GeneratedFileArtifact> = emptyList(),
    val requestClass: AiRequestClassification = AiRequestClassification.CLASS_A_SPECIFIC_APP,
    val decisionAnnouncement: String = "",
    val appPurpose: String = "",
    val buildPlanSummary: String = "",
    val expectedBehavior: String = ""
)
