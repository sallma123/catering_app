package com.example.cateringapp.ui.screen.calendrier

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.ui.navigation.BottomNavBar
import com.example.cateringapp.ui.screen.commandes.CommandeCard
import com.example.cateringapp.viewmodel.CommandeViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

fun dateToLocalDateCompat(date: Date): LocalDate {
    val calendar = Calendar.getInstance().apply { time = date }
    return LocalDate.of(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

@Composable
fun CalendrierScreen(navController: NavController, viewModel: CommandeViewModel = viewModel()) {
    val commandes by viewModel.commandes.collectAsState()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val commandesDates = commandes.mapNotNull {
        try {
            sdf.parse(it.date)?.let { date -> dateToLocalDateCompat(date) }
        } catch (e: Exception) { null }
    }.toSet()

    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val jours = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val startOffset = (firstDayOfMonth.dayOfWeek.ordinal + 7) % 7

    val commandesDuJour = commandes.filter {
        try {
            val date = sdf.parse(it.date)
            val commandeDate = dateToLocalDateCompat(date!!)
            commandeDate == selectedDate
        } catch (e: Exception) {
            false
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White)
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.FRENCH).replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                jours.forEach {
                    Text(
                        text = it,
                        color = Color.LightGray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            val totalBoxes = startOffset + daysInMonth
            val weeks = (totalBoxes / 7) + if (totalBoxes % 7 != 0) 1 else 0
            Column {
                var dayCounter = 1
                repeat(weeks) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { dayIndex ->
                            val boxIndex = it * 7 + dayIndex
                            if (boxIndex < startOffset || dayCounter > daysInMonth) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {}
                            } else {
                                val date = currentMonth.atDay(dayCounter)
                                val isToday = date == today
                                val isSelected = date == selectedDate
                                val hasCommande = commandesDates.contains(date)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    color = when {
                                                        isSelected -> Color(0xFFFFC107)
                                                        else -> Color.Transparent
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                color = when {
                                                    isSelected -> Color.Black
                                                    isToday -> Color(0xFFFFC107)
                                                    else -> Color.White
                                                },
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(0.1.dp))
                                        if (hasCommande && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFFFFC107), shape = CircleShape)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                                dayCounter++
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Commandes du ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)} ${selectedDate.year}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(commandesDuJour) {
                    CommandeCard(it)
                }
            }
        }
    }
}