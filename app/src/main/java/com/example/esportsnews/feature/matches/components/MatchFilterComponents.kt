package com.example.esportsnews.feature.matches.components

import com.example.esportsnews.R
import com.example.esportsnews.feature.matches.TierFilter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.esportsnews.core.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun MatchFilterChip(label: String, isSelected: Boolean, usePurple: Boolean = false, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected, onClick = onClick,
        modifier = Modifier.heightIn(min = 48.dp),
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, when {
            !isSelected -> MaterialTheme.colorScheme.outlineVariant
            usePurple -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = if (usePurple) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = if (usePurple) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
internal fun TierFilterMenu(selectedTier: TierFilter, onSelect: (TierFilter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = HubSpacing.Small), modifier = Modifier.heightIn(min = 48.dp)) {
            Text(if (selectedTier == TierFilter.ALL) stringResource(R.string.filter_tiers) else stringResource(R.string.tier_format, selectedTier.apiValue.orEmpty()),
                style = MaterialTheme.typography.labelMedium)
            Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TierFilter.entries.forEach { tier ->
                DropdownMenuItem(
                    text = { Text(if (tier == TierFilter.ALL) stringResource(R.string.all_tiers) else stringResource(R.string.tier_format, tier.apiValue.orEmpty())) },
                    onClick = { onSelect(tier); expanded = false },
                    modifier = Modifier.semantics { selected = selectedTier == tier }
                )
            }
        }
    }
}

@Composable
internal fun MatchCalendar(dates: List<LocalDate>, selectedDate: LocalDate?, locale: Locale, onSelect: (LocalDate) -> Unit) {
    val descriptionFormatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale) }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
        items(dates, key = { it.toEpochDay() }) { date ->
            val isSelected = selectedDate == date
            val description = date.format(descriptionFormatter)
            Surface(
                onClick = { onSelect(date) }, shape = MaterialTheme.shapes.medium,
                color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.widthIn(min = 52.dp).heightIn(min = 64.dp).semantics {
                    selected = isSelected
                    contentDescription = description
                }
            ) {
                Column(Modifier.padding(horizontal = HubSpacing.Small, vertical = HubSpacing.Small),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)) {
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replace(".", ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(locale, "%02d", date.dayOfMonth), style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
