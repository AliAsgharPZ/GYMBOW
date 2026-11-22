package com.ali.asghar.gymbow

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val defaultWeek = listOf(
    "Saturday|Push · Chest, shoulders, triceps",
    "Sunday|Pull · Back, biceps, rear delts",
    "Monday|Legs · Quads, hamstrings, calves",
    "Tuesday|Push · Chest, shoulders, triceps",
    "Wednesday|Pull · Back, biceps, rear delts",
    "Thursday|Legs · Quads, hamstrings, calves"
)
private val splitOptions = listOf(
    "Push · Chest, shoulders, triceps",
    "Pull · Back, biceps, rear delts",
    "Legs · Quads, hamstrings, calves",
    "Upper · Chest, back, shoulders, arms",
    "Lower · Quads, hamstrings, glutes, calves",
    "Full body · All major muscle groups",
    "Rest · Mobility and recovery"
)

@Composable
fun WorkoutSchedulePanel(context: Context) {
    val store = remember { context.getSharedPreferences("gymbow-native", Context.MODE_PRIVATE) }
    var plan by remember {
        mutableStateOf(store.getString("weekly_plan", null)?.split("\n")?.filter { it.contains("|") } ?: defaultWeek)
    }
    var editing by remember { mutableIntStateOf(-1) }
    fun save(next: List<String>) {
        plan = next
        store.edit().putString("weekly_plan", next.joinToString("\n")).apply()
    }
    Panel {
        Eyebrow("YOUR WEEK · EDITABLE PROGRAM")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("TRAINING SPLIT", fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("${plan.size} DAYS", fontSize = 10.sp, color = muted)
        }
        plan.forEachIndexed { index, item ->
            val parts = item.split("|", limit = 2)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(parts[0], fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(parts.getOrElse(1) { "Unassigned" }, fontSize = 10.sp, color = muted)
                }
                IconButton(onClick = { editing = index }) { Icon(Icons.Outlined.Edit, "Edit day", tint = ink) }
                IconButton(onClick = { save(plan.filterIndexed { i, _ -> i != index }) }) { Icon(Icons.Outlined.Delete, "Remove day", tint = muted) }
            }
            if (index != plan.lastIndex) HorizontalDivider(color = line)
        }
        OutlinedButton(
            onClick = { save(plan + "New day|Full body · All major muscle groups") },
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, line)
        ) {
            Icon(Icons.Outlined.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("ADD TRAINING DAY", color = ink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text("Edit the split to match your own weekly volume. The schedule is stored locally on this device.", fontSize = 9.sp, color = muted)
    }
    if (editing in plan.indices) {
        val index = editing
        val day = plan[index].substringBefore("|")
        AlertDialog(
            onDismissRequest = { editing = -1 },
            title = { Text("EDIT $day", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    splitOptions.forEach { option ->
                        TextButton(onClick = {
                            save(plan.toMutableList().also { it[index] = "$day|$option" })
                            editing = -1
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(option, color = ink, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { editing = -1 }) { Text("CLOSE", color = ink) } }
        )
    }
}

@Composable
fun BodyMetricsPanel(context: Context, weightKg: Float) {
    val store = remember { context.getSharedPreferences("gymbow-native", Context.MODE_PRIVATE) }
    var heightCm by remember { mutableFloatStateOf(store.getFloat("height_cm", 175f)) }
    val bmi = weightKg / ((heightCm / 100f) * (heightCm / 100f))
    val label = when {
        bmi < 18.5f -> "Below standard range"
        bmi < 25f -> "Standard range"
        bmi < 30f -> "Above standard range"
        else -> "High BMI range"
    }
    Panel {
        Eyebrow("BODY COMPOSITION · BMI")
        Text(String.format(Locale.US, "%.1f", bmi), fontSize = 38.sp, fontWeight = FontWeight.Black)
        Text(label.uppercase(Locale.US), fontSize = 10.sp, color = muted, fontWeight = FontWeight.Bold)
        HorizontalDivider(color = line)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("HEIGHT", fontSize = 10.sp, color = muted)
            Text("${heightCm.toInt()} cm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = heightCm,
            onValueChange = { heightCm = it; store.edit().putFloat("height_cm", it).apply() },
            valueRange = 140f..210f,
            colors = SliderDefaults.colors(thumbColor = ink, activeTrackColor = ink)
        )
        Text("BMI is a screening measure, not a body-fat or muscle measurement. Interpret it cautiously, especially for teenagers and athletes.", fontSize = 9.sp, color = muted, lineHeight = 14.sp)
    }
}

@Composable
fun TrainingHistoryPanel(context: Context) {
    val store = remember { context.getSharedPreferences("gymbow-native", Context.MODE_PRIVATE) }
    var history by remember { mutableStateOf(store.getString("training_history", "").orEmpty().split("\n").filter { it.isNotBlank() }) }
    Panel {
        Eyebrow("LOCAL TRAINING LOG")
        Text("SESSION HISTORY", fontSize = 19.sp, fontWeight = FontWeight.Black)
        if (history.isEmpty()) {
            Text("Completed workouts will appear here.", fontSize = 10.sp, color = muted)
        } else {
            history.takeLast(12).reversed().forEach { entry ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(entry.substringAfter("|", entry), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(entry.substringBefore("|"), fontSize = 9.sp, color = muted)
                }
            }
            TextButton(onClick = { history = emptyList(); store.edit().remove("training_history").apply() }) {
                Text("CLEAR HISTORY", color = muted, fontSize = 9.sp)
            }
        }
    }
}

fun recordWorkoutSession(context: Context, name: String) {
    val store = context.getSharedPreferences("gymbow-native", Context.MODE_PRIVATE)
    val existing = store.getString("training_history", "").orEmpty().split("\n").filter { it.isNotBlank() }
    val stamp = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
    store.edit().putString("training_history", (existing + "$stamp|$name").takeLast(100).joinToString("\n")).apply()
}
