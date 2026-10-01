package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HalalFoodTool(
    viewModel: AppViewModel,
    lang: String
) {
    val context = LocalContext.current
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val locationName by viewModel.locationName.collectAsState()

    fun openHalalMaps() {
        val query = Uri.encode("halal restaurants near " + latitude + "," + longitude)
        val geoUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + query)
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
        }.onFailure {
            runCatching {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + query)
                    )
                )
            }.onFailure {
                Toast.makeText(
                    context,
                    if (lang == "ar") "تعذر فتح الخرائط على هذا الجهاز." else "Could not open maps on this device.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Color(0xFFD4AF37))
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (lang == "ar") "المطاعم الحلال" else "Halal Restaurants",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (lang == "ar") "الموقع المستخدم للبحث" else "Location used for search",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = locationName,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = if (lang == "ar")
                        "بدلاً من عرض أسماء وتقييمات وهمية، يفتح التطبيق بحثاً حقيقياً عن المطاعم الحلال القريبة من موقعك."
                    else
                        "Instead of showing fabricated restaurant names or ratings, the app opens a real search for halal restaurants near your location.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = ::openHalalMaps,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (lang == "ar") "ابحث عن المطاعم الحلال القريبة" else "Find nearby halal restaurants")
                }
            }
        }
    }
}
