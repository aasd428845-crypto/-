package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Branch
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.User
import com.example.service.FirebaseService
import com.example.service.SupabaseClientProvider
import com.example.ui.theme.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialItemRequestScreen(
    currentUser: User,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val supabase = SupabaseClientProvider.client

    // Form states
    var itemDescription by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var dbError by remember { mutableStateOf<String?>(null) }

    // Voice recording states
    var isRecording by remember { mutableStateOf(false) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }
    var isPlayingPreview by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var recordingDuration by remember { mutableStateOf(0) }

    // Branch selection states
    var branchSelectionMode by remember { mutableStateOf("auto") } // "auto" or "manual"
    var allBranches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var nearestBranch by remember { mutableStateOf<Branch?>(null) }
    var nearestDistance by remember { mutableStateOf<Double?>(null) }
    var isLoadingBranches by remember { mutableStateOf(true) }

    // Client location
    var clientLat by remember { mutableStateOf(0.0) }
    var clientLng by remember { mutableStateOf(0.0) }
    var clientGovernorate by remember { mutableStateOf("") }

    // Load branches and client profile
    LaunchedEffect(Unit) {
        FirebaseService.getBranches { branches ->
            allBranches = branches.filter { it.isActive }
            isLoadingBranches = false
        }
        FirebaseService.getClientProfile(currentUser.userId) { profile ->
            if (profile != null) {
                clientLat = profile.latitude
                clientLng = profile.longitude
                clientGovernorate = profile.governorate
            }
        }
    }

    // Compute nearest branch when mode is "auto"
    LaunchedEffect(allBranches, clientLat, clientLng, branchSelectionMode) {
        if (branchSelectionMode == "auto" && allBranches.isNotEmpty() && clientLat != 0.0 && clientLng != 0.0) {
            val withDist = allBranches.map { branch ->
                val d = FirebaseService.calculateDistanceKm(clientLat, clientLng, branch.latitude, branch.longitude)
                branch to d
            }
            val closest = withDist.minByOrNull { it.second }
            nearestBranch = closest?.first
            nearestDistance = closest?.second
            if (closest != null) {
                selectedBranchId = closest.first.branchId
            }
        }
    }

    // Permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "يجب منح صلاحية التسجيل الصوتي", Toast.LENGTH_SHORT).show()
        }
    }

    fun startRecording() {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        try {
            val audioFile = File(context.cacheDir, "special_request_${System.currentTimeMillis()}.m4a")
            val recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            audioFilePath = audioFile.absolutePath
            isRecording = true
            recordingDuration = 0
        } catch (e: Exception) {
            Toast.makeText(context, "فشل بدء التسجيل: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (_: Exception) {}
        isRecording = false
    }

    fun deleteRecording() {
        mediaPlayer?.release()
        mediaPlayer = null
        audioFilePath?.let { File(it).delete() }
        audioFilePath = null
        isPlayingPreview = false
    }

    fun playPreview() {
        val path = audioFilePath ?: return
        try {
            mediaPlayer?.release()
            val player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
            }
            mediaPlayer = player
            isPlayingPreview = true
            player.setOnCompletionListener {
                isPlayingPreview = false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "فشل تشغيل التسجيل", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopPreview() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlayingPreview = false
    }

    fun submitRequest() {
        // Validation
        if (itemDescription.isBlank() && audioFilePath == null) {
            Toast.makeText(context, "الرجاء وصف الصنف نصياً أو تسجيل ملاحظة صوتية", Toast.LENGTH_SHORT).show()
            return
        }
        if (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() <= 0) {
            Toast.makeText(context, "الرجاء إدخال كمية صحيحة", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedBranchId == null) {
            Toast.makeText(context, "الرجاء اختيار الفرع", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true
        dbError = null

        MainScope().launch {
            try {
                var audioUrl: String? = null

                // Upload audio if exists
                if (audioFilePath != null) {
                    val audioFile = File(audioFilePath!!)
                    if (audioFile.exists()) {
                        val fileName = "audio_${currentUser.userId}_${System.currentTimeMillis()}.m4a"
                        try {
                            val bucket = supabase.storage["special-request-audio"]
                            bucket.upload(fileName, audioFile.readBytes(), upsert = false)
                            audioUrl = bucket.publicUrl(fileName)
                        } catch (e: Exception) {
                            dbError = "فشل رفع الملف الصوتي: ${e.message}"
                            isSubmitting = false
                            return@launch
                        }
                    }
                }

                // Build order content
                val orderContent = if (itemDescription.isNotBlank()) {
                    itemDescription
                } else {
                    "طلب بملاحظة صوتية مرفقة"
                }

                // Create order
                val order = Order(
                    orderId = "",
                    clientId = currentUser.userId,
                    orderStatus = OrderStatus.Submitted,
                    createdAt = System.currentTimeMillis(),
                    clientName = currentUser.name.ifEmpty { currentUser.orgName },
                    clientType = currentUser.clientType,
                    clientGovernorate = clientGovernorate,
                    orderContent = orderContent,
                    urgencyLevel = "normal",
                    broadcastType = "selected",
                    targetBranches = listOf(selectedBranchId!!),
                    status = "broadcast",
                    quantity = quantity.toIntOrNull() ?: 1,
                    isSpecialRequest = true,
                    audioNoteUrl = audioUrl
                )

                withContext(Dispatchers.IO) {
                    supabase.postgrest["orders"].upsert(order)
                }

                withContext(Dispatchers.Main) {
                    isSubmitting = false
                    Toast.makeText(context, "تم إرسال طلبك الخاص بنجاح!", Toast.LENGTH_LONG).show()
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSubmitting = false
                    dbError = "خطأ في إرسال الطلب: ${e.message}"
                }
            }
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            try { mediaRecorder?.release() } catch (_: Exception) {}
            try { mediaPlayer?.release() } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("special_request_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                stopRecording()
                stopPreview()
                onBackClick()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = OnSurfaceDark)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "طلب صنف غير متوفر بالكتالوج",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = OnSurfaceDark
                )
                Text(
                    "استخدم هذا فقط إذا لم تجد الصنف المطلوب في الكتالوج الرئيسي",
                    fontSize = 11.sp,
                    color = TextSecondaryGray
                )
            }
        }

        // Error banner
        if (dbError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dbError!!, color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
            }
        }

        // Section 1: Item Description
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📝 وصف الصنف المطلوب", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceDark)

                OutlinedTextField(
                    value = itemDescription,
                    onValueChange = { itemDescription = it },
                    label = { Text("اسم الصنف والتفاصيل") },
                    placeholder = { Text("أدخل اسم الصنف، المادة الفعالة، التركيز، الشركة المصنعة...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("special_item_desc"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        focusedLabelColor = BrandPrimary,
                        focusedTextColor = OnSurfaceDark,
                        unfocusedTextColor = OnSurfaceDark
                    ),
                    maxLines = 5
                )

                // Voice Recording Section
                Text("🎤 تسجيل ملاحظة صوتية (اختياري)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextSecondaryGray)

                if (!isRecording && audioFilePath == null) {
                    // Record button
                    OutlinedButton(
                        onClick = { startRecording() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("record_audio_btn"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🎤 تسجيل ملاحظة صوتية", color = OnSurfaceDark, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                if (isRecording) {
                    // Recording in progress
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🔴 جارِ التسجيل...", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFDC2626))
                            Button(
                                onClick = { stopRecording() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("stop_recording_btn")
                            ) {
                                Text("⏹️ إيقاف التسجيل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                if (!isRecording && audioFilePath != null) {
                    // Recording saved — preview options
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("✅ تم حفظ التسجيل الصوتي", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF166534))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { if (isPlayingPreview) stopPreview() else playPreview() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("play_preview_btn"),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                                ) {
                                    Text(
                                        if (isPlayingPreview) "⏸️ إيقاف" else "▶️ معاينة",
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = { deleteRecording() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("delete_recording_btn"),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                                ) {
                                    Text("🗑️ حذف وإعادة", color = ErrorRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Quantity
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📦 الكمية المطلوبة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceDark)
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { if (it.all { c -> c.isDigit() }) quantity = it },
                    label = { Text("الكمية") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("special_quantity"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        focusedLabelColor = BrandPrimary,
                        focusedTextColor = OnSurfaceDark,
                        unfocusedTextColor = OnSurfaceDark
                    ),
                    singleLine = true
                )
            }
        }

        // Section 3: Additional Notes
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📋 ملاحظات إضافية (اختياري)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceDark)
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = { Text("ملاحظات إضافية") },
                    placeholder = { Text("أي تفاصيل إضافية تساعد في تأمين الصنف...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("special_notes"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        focusedLabelColor = BrandPrimary,
                        focusedTextColor = OnSurfaceDark,
                        unfocusedTextColor = OnSurfaceDark
                    ),
                    maxLines = 3
                )
            }
        }

        // Section 4: Branch Selection
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🏢 اختيار الفرع المستلم", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceDark)

                // Auto option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            2.dp,
                            if (branchSelectionMode == "auto") BrandPrimary else Color(0xFFCBD5E1),
                            RoundedCornerShape(10.dp)
                        )
                        .background(
                            if (branchSelectionMode == "auto") BrandPrimary.copy(alpha = 0.08f) else Color.White
                        )
                        .clickable { branchSelectionMode = "auto" }
                        .padding(14.dp)
                        .testTag("branch_auto_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("أرسل لأقرب فرع تلقائياً", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurfaceDark)
                            if (branchSelectionMode == "auto" && nearestBranch != null) {
                                Text(
                                    "الفرع الأقرب: ${nearestBranch!!.branchName} (${String.format("%.1f", nearestDistance ?: 0.0)} كم)",
                                    fontSize = 11.sp,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Manual option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            2.dp,
                            if (branchSelectionMode == "manual") BrandPrimary else Color(0xFFCBD5E1),
                            RoundedCornerShape(10.dp)
                        )
                        .background(
                            if (branchSelectionMode == "manual") BrandPrimary.copy(alpha = 0.08f) else Color.White
                        )
                        .clickable { branchSelectionMode = "manual" }
                        .padding(14.dp)
                        .testTag("branch_manual_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏢", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("اختر الفرع بنفسي", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurfaceDark)
                    }
                }

                // Manual dropdown
                if (branchSelectionMode == "manual") {
                    if (isLoadingBranches) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = allBranches.find { it.branchId == selectedBranchId }?.branchName ?: "اختر الفرع",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الفرع") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("branch_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPrimary,
                                    focusedLabelColor = BrandPrimary,
                                    focusedTextColor = OnSurfaceDark,
                                    unfocusedTextColor = OnSurfaceDark
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                allBranches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text("${branch.branchName} — ${branch.city}") },
                                        onClick = {
                                            selectedBranchId = branch.branchId
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Button
        Button(
            onClick = { submitRequest() },
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_special_request_btn")
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = OnBrandPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("📨 إرسال الطلب الخاص", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
