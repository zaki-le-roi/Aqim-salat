package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class MoreMenuOption(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: AppViewModel,
    lang: String,
    onNavigateToFeature: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember {
        listOf(
            MoreMenuOption("QURAN", "القرآن الكريم", "فتح المصحف", Icons.Filled.MenuBook),
            MoreMenuOption("TASBIH", "السبحة الإلكترونية", "التسبيح والعدّ", Icons.Filled.AddCircle),
            MoreMenuOption("HADITH", "كتب الأحاديث", "مطالعة الأحاديث النبوية", Icons.Filled.LibraryBooks),
            MoreMenuOption("HISN_AL_MUSLIM", "حصن المسلم", "الأدعية والأذكار", Icons.Filled.Shield),
            MoreMenuOption("QIBLA", "القبلة", "تحديد اتجاه القبلة", Icons.Filled.Explore),
            MoreMenuOption("SETTINGS", "الإعدادات", "جميع إعدادات التطبيق", Icons.Filled.Settings)
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<MoreMenuOption?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "المزيد",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "اختر خدمة أو افتح إعدادات التطبيق من القائمة المنسدلة.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().testTag("more_dropdown")
        ) {
            OutlinedTextField(
                value = selectedOption?.title ?: "اختر من القائمة",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = selectedOption?.icon ?: Icons.Filled.Apps,
                        contentDescription = null,
                        tint = Color(0xFF0D5E34)
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("more_dropdown_field"),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D5E34),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(option.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    option.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = Color(0xFF0D5E34)
                            )
                        },
                        onClick = {
                            selectedOption = option
                            expanded = false
                            onNavigateToFeature(option.key)
                        },
                        modifier = Modifier.testTag("more_dropdown_item_${option.key}")
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = Color(0xFF0D5E34),
                    modifier = Modifier.size(28.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("الإعدادات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "تجد داخلها خيارات المظهر والتنبيهات والأذان والموقع والحسابات الصوتية وإمكانية الوصول.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
