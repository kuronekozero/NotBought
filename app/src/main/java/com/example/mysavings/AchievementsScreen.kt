package com.example.mysavings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel) {
    val achievements by viewModel.achievements.collectAsState()

    val groups = remember(achievements) { achievements.groupBy { it.definition.category }.mapValues { (_, list) -> list.sortedBy { it.definition.target } } }

    var expandedCategories by remember { mutableStateOf<Set<AchievementCategory>>(emptySet()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AchievementCategory.values().forEach { category ->
            val group = groups[category] ?: emptyList()
            if (group.isNotEmpty()) {
                val isExpanded = expandedCategories.contains(category)
                item {
                    Text(
                        text = when (category) {
                            AchievementCategory.SAVED -> stringResource(R.string.achievements_category_saved)
                            AchievementCategory.WASTED -> stringResource(R.string.achievements_category_wasted)
                            AchievementCategory.STREAK -> stringResource(R.string.achievements_category_streak)
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                val visibleItems = if (isExpanded) group else listOf(group.first())
                items(visibleItems) { progress ->
                    AchievementRow(progress)
                }

                // Expand/collapse button
                item {
                    val buttonLabel = if (isExpanded) stringResource(R.string.achievements_collapse) else stringResource(R.string.achievements_expand_more)
                    TextButton(onClick = {
                        expandedCategories = if (isExpanded) {
                            expandedCategories - category
                        } else {
                            expandedCategories + category
                        }
                    }) {
                        Text(buttonLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementRow(progress: AchievementProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (progress.achieved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (progress.definition.category) {
                    AchievementCategory.SAVED -> Icons.Outlined.MonetizationOn
                    AchievementCategory.WASTED -> Icons.Outlined.MoneyOff
                    AchievementCategory.STREAK -> Icons.Outlined.Whatshot
                }
                Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(progress.definition.nameRes), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(progress.definition.descriptionRes), style = MaterialTheme.typography.bodySmall)
                }
                if (progress.achieved) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = stringResource(R.string.achievements_completed_desc))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress.progressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            val currentDisplay = if (progress.definition.category == AchievementCategory.STREAK) {
                progress.current.toInt().toString()
            } else {
                String.format("%.0f", progress.current)
            }
            val targetDisplay = if (progress.definition.category == AchievementCategory.STREAK) {
                progress.definition.target.toInt().toString()
            } else {
                String.format("%.0f", progress.definition.target)
            }
            Text(
                text = "$currentDisplay / $targetDisplay",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 