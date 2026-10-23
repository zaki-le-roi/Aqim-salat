package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.example.data.MishkatAnswerRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private data class MishkatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
)

private val MishkatGreen = Color(0xFF115E3F)
private val MishkatGold = Color(0xFFD4AF37)

private fun newMishkatMessageId(): Long = System.nanoTime()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MishkatScreen(onBack: () -> Unit) {
    val messages = remember { mutableStateListOf<MishkatMessage>() }
    var question by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var isAnswering by remember { mutableStateOf(false) }
    var historyLoaded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val historyPrefs = remember(context) {
        context.getSharedPreferences("mishkat_conversation", android.content.Context.MODE_PRIVATE)
    }

    val sendQuestion: () -> Unit = {
        val submitted = question.trim()
        if (submitted.isNotEmpty() && !isAnswering) {
            messages.add(MishkatMessage(newMishkatMessageId(), submitted, true))
            question = ""
            keyboard?.hide()
            isAnswering = true
            val assistantId = newMishkatMessageId()
            messages.add(MishkatMessage(id = assistantId, text = "يتصل مِشكاة...", isUser = false))
            scope.launch {
                try {
                    val answer = MishkatAnswerRepository.answer(context, submitted) { partial ->
                        val index = messages.indexOfFirst { it.id == assistantId }
                        if (index >= 0) messages[index] = messages[index].copy(text = partial)
                    }
                    val index = messages.indexOfFirst { it.id == assistantId }
                    if (index >= 0) messages[index] = messages[index].copy(text = answer)
                } catch (_: Exception) {
                    val index = messages.indexOfFirst { it.id == assistantId }
                    if (index >= 0) messages[index] = messages[index].copy(
                        text = "تعذّر إكمال الإجابة بسبب خطأ غير متوقع. تحقق من اتصال الإنترنت ثم أعد المحاولة."
                    )
                } finally {
                    isAnswering = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val saved = historyPrefs.getString("messages", null)
        if (!saved.isNullOrBlank()) {
            runCatching {
                val array = JSONArray(saved)
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    messages.add(
                        MishkatMessage(
                            id = item.optLong("id", System.currentTimeMillis() + index),
                            text = item.optString("text"),
                            isUser = item.optBoolean("isUser"),
                        )
                    )
                }
            }
        }
        historyLoaded = true
    }

    LaunchedEffect(messages.size, historyLoaded) {
        if (historyLoaded) {
            val array = JSONArray()
            messages.forEach { message ->
                array.put(JSONObject().apply {
                    put("id", message.id)
                    put("text", message.text)
                    put("isUser", message.isUser)
                })
            }
            historyPrefs.edit().putString("messages", array.toString()).apply()
        }
    }

    BackHandler(onBack = onBack)

    suspend fun scrollToLatestMessage() {
        val expectedCount = messages.size + if (isAnswering) 1 else 0
        if (expectedCount <= 0) return

        // Wait until Compose has measured the newly added message/loading row.
        // This prevents scrolling using a stale item count immediately after sending.
        if (listState.layoutInfo.totalItemsCount < expectedCount) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { itemCount -> itemCount >= expectedCount }
        }

        val lastIndex = listState.layoutInfo.totalItemsCount - 1
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
            // Also reach the bottom of a very long final answer, not just its first line.
            listState.animateScrollBy(1_000_000f)
        }
    }

    LaunchedEffect(messages.size, isAnswering, historyLoaded) {
        if (historyLoaded && messages.isNotEmpty()) {
            scrollToLatestMessage()
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("حذف المحادثة؟") },
            text = { Text("سيتم حذف جميع الرسائل المحفوظة في مِشكاة من هذا الجهاز. لا يمكن التراجع عن الحذف.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        messages.clear()
                        question = ""
                        historyPrefs.edit().remove("messages").apply()
                        showDeleteConfirmation = false
                    }
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("إلغاء") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("مِشكاة", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "مساعد إسلامي بالذكاء الاصطناعي",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { scope.launch { scrollToLatestMessage() } },
                        enabled = messages.isNotEmpty(),
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "الانتقال إلى آخر المحادثة",
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        enabled = messages.isNotEmpty() && !isAnswering,
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "حذف المحادثة",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .padding(start = 16.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = question,
                            onValueChange = { question = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("اكتب سؤالك...") },
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendQuestion() }),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MishkatGreen
                            )
                        )
                        IconButton(
                            onClick = { sendQuestion() },
                            enabled = question.isNotBlank() && !isAnswering,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (question.isNotBlank()) MishkatGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "إرسال",
                                tint = if (question.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MishkatGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MishkatGold,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Text(
                    "مِشكاة",
                    modifier = Modifier.padding(top = 18.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "ما الذي تريد أن تعرفه؟",
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 14.dp, end = 14.dp, top = 18.dp, bottom = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isUser) Arrangement.Start else Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomEnd = if (message.isUser) 20.dp else 6.dp,
                                bottomStart = if (message.isUser) 6.dp else 20.dp
                            ),
                            color = if (message.isUser) MishkatGreen else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth(if (message.isUser) 0.88f else 1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!message.isUser && message.text.contains("https://")) {
                                    val linkedText = remember(message.text) {
                                        buildAnnotatedString {
                                            append(message.text)
                                            Regex("https://\\S+").findAll(message.text).forEach { match ->
                                                val cleanUrl = match.value.trimEnd('.', ',', '،', ')', ']', '»')
                                                if (cleanUrl.isNotBlank()) {
                                                    addStringAnnotation(
                                                        tag = "MISHKAT_SOURCE_URL",
                                                        annotation = cleanUrl,
                                                        start = match.range.first,
                                                        end = match.range.last + 1
                                                    )
                                                    addStyle(
                                                        SpanStyle(
                                                            color = Color(0xFF1565C0),
                                                            textDecoration = TextDecoration.Underline
                                                        ),
                                                        start = match.range.first,
                                                        end = match.range.last + 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    ClickableText(
                                        text = linkedText,
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp,
                                            lineHeight = 24.sp
                                        ),
                                        onClick = { offset ->
                                            linkedText.getStringAnnotations(
                                                tag = "MISHKAT_SOURCE_URL",
                                                start = offset,
                                                end = offset
                                            ).firstOrNull()?.let { annotation ->
                                                runCatching { uriHandler.openUri(annotation.item) }
                                            }
                                        }
                                    )
                                } else {
                                    Text(
                                        message.text,
                                        color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
                if (isAnswering) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MishkatGreen
                                    )
                                    Text("يُعدّ الذكاء الاصطناعي الإجابة…", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
