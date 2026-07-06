package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HalalRestaurant(
    val id: Long,
    val nameAr: String,
    val nameEn: String,
    val cuisineAr: String,
    val cuisineEn: String,
    val rating: Double,
    val distance: Double, // in km
    val addressAr: String,
    val addressEn: String,
    val phone: String,
    val isVerified: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalalFoodTool(
    viewModel: AppViewModel,
    lang: String
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "MIDDLE_EASTERN", "TURKISH", "INDIAN", "BURGER"

    val baseRestaurants = listOf(
        HalalRestaurant(
            id = 1L,
            nameAr = "مطعم ومطبخ الضيافة المكي",
            nameEn = "Al Diyafa Makkah Restaurant",
            cuisineAr = "شعبي ومندي سعودي",
            cuisineEn = "Traditional Saudi Mandi",
            rating = 4.9,
            distance = 1.2,
            addressAr = "مكة المكرمة، شارع إبراهيم الخليل",
            addressEn = "Makkah, Ibrahim Al Khalil Street",
            phone = "+966 12 558 4433"
        ),
        HalalRestaurant(
            id = 2L,
            nameAr = "مطاعم حراء الشهيرة",
            nameEn = "Hira Restaurants",
            cuisineAr = "مشويات ومأكولات شرقية",
            cuisineEn = "Middle Eastern Grills",
            rating = 4.7,
            distance = 2.5,
            addressAr = "مكة المكرمة، حي العزيزية العام",
            addressEn = "Makkah, Al Aziziyah District",
            phone = "+966 12 560 1122"
        ),
        HalalRestaurant(
            id = 3L,
            nameAr = "مطعم البيت التركي البخاري",
            nameEn = "The Turkish Bukhari House",
            cuisineAr = "تركي وبخاري أصيل",
            cuisineEn = "Turkish & Bukhari Cuisine",
            rating = 4.8,
            distance = 0.8,
            addressAr = "مكة المكرمة، طريق الهجرة بجوار الحرم",
            addressEn = "Makkah, Al Hijrah Road near Haram",
            phone = "+966 12 533 9988"
        ),
        HalalRestaurant(
            id = 4L,
            nameAr = "شاورما الفارس الشامي",
            nameEn = "Al Fares Shami Shawarma",
            cuisineAr = "شاورما ومعجنات شامية",
            cuisineEn = "Levantine Shawarma",
            rating = 4.6,
            distance = 1.9,
            addressAr = "مكة المكرمة، بطحاء قريش",
            addressEn = "Makkah, Batha Quraish",
            phone = "+966 12 540 2211"
        ),
        HalalRestaurant(
            id = 5L,
            nameAr = "بهارات الهند الكلاسيكية",
            nameEn = "Indian Spices Classic",
            cuisineAr = "هندي وبرياني متميز",
            cuisineEn = "Premium Indian Biryani",
            rating = 4.5,
            distance = 3.1,
            addressAr = "مكة المكرمة، حي الشوقية",
            addressEn = "Makkah, Al Shawqiyyah District",
            phone = "+966 12 572 6655"
        )
    )

    val filteredRestaurants = baseRestaurants.filter { rest ->
        val matchesSearch = if (lang == "ar") {
            rest.nameAr.contains(searchQuery, ignoreCase = true) || rest.cuisineAr.contains(searchQuery, ignoreCase = true)
        } else {
            rest.nameEn.contains(searchQuery, ignoreCase = true) || rest.cuisineEn.contains(searchQuery, ignoreCase = true)
        }

        val matchesFilter = when (selectedFilter) {
            "ALL" -> true
            "MIDDLE_EASTERN" -> rest.cuisineEn.contains("Saudi") || rest.cuisineEn.contains("Middle Eastern") || rest.cuisineEn.contains("Levantine")
            "TURKISH" -> rest.cuisineEn.contains("Turkish") || rest.cuisineEn.contains("Bukhari")
            "INDIAN" -> rest.cuisineEn.contains("Indian")
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Filter header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (lang == "ar") "ابحث عن مطعم أو نوع أكل حلال..." else "Search Halal Restaurant...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFFD4AF37)) },
                modifier = Modifier.fillMaxWidth().testTag("halal_search_bar"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtering chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text(if (lang == "ar") "الكل" else "All") }
                )
                FilterChip(
                    selected = selectedFilter == "MIDDLE_EASTERN",
                    onClick = { selectedFilter = "MIDDLE_EASTERN" },
                    label = { Text(if (lang == "ar") "شرقي وشعبي" else "Middle Eastern") }
                )
                FilterChip(
                    selected = selectedFilter == "TURKISH",
                    onClick = { selectedFilter = "TURKISH" },
                    label = { Text(if (lang == "ar") "تركي وبخاري" else "Turkish") }
                )
                FilterChip(
                    selected = selectedFilter == "INDIAN",
                    onClick = { selectedFilter = "INDIAN" },
                    label = { Text(if (lang == "ar") "هندي" else "Indian") }
                )
            }
        }

        // Restaurant cards list
        if (filteredRestaurants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Restaurant, contentDescription = "No Food", tint = Color(0xFFD4AF37).copy(alpha = 0.4f), modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (lang == "ar") "عذراً، لا توجد مطاعم مطابقة لبحثك" else "Sorry, no restaurants matched your search",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredRestaurants) { rest ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == "ar") rest.nameAr else rest.nameEn,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (lang == "ar") rest.cuisineAr else rest.cuisineEn,
                                        fontSize = 12.sp,
                                        color = Color(0xFFD4AF37),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${rest.rating}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (lang == "ar") rest.addressAr else rest.addressEn,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DirectionsWalk, contentDescription = "Distance", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${rest.distance} كم",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { Toast.makeText(context, "${if (lang == "ar") "الاتصال بالرقم " else "Calling "}${rest.phone}", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    ) {
                                        Icon(Icons.Filled.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = { Toast.makeText(context, if (lang == "ar") "فتح في خرائط جوجل للتوجيه..." else "Opening Google Maps for directions...", Toast.LENGTH_SHORT).show() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.Navigation, contentDescription = "Directions", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (lang == "ar") "الاتجاهات" else "Directions", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
