package com.example.esportsnews

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.esportsnews.model.Match
import com.example.esportsnews.model.Team
import com.example.esportsnews.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatMatchDate(dateString: String?, tbdString: String, undefinedString: String): String {
    if (dateString == null) return tbdString
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val parsedDate = parser.parse(dateString)
        val formatter = SimpleDateFormat("dd/MM • HH:mm", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        parsedDate?.let { formatter.format(it) } ?: tbdString
    } catch (_: Exception) {
        undefinedString
    }
}

@Composable
fun ModernMatchCard(match: Match, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardHeader)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val leagueLogo = match.league?.dark_mode_image_url ?: match.league?.image_url
                    if (leagueLogo != null) {
                        AsyncImage(
                            model = leagueLogo,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    val displayGame = if (match.game.equals("CSGO", ignoreCase = true)) "CS2" else match.game.uppercase()
                    Text(
                        text = displayGame,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    if (match.tier != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val tierColor = when (match.tier.lowercase()) {
                            "s" -> Color(0xFFFFD700)
                            "a" -> Color(0xFFC0C0C0)
                            "b" -> Color(0xFFCD7F32)
                            else -> TextSecondary
                        }
                        Box(
                            modifier = Modifier
                                .background(tierColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.tier_format, match.tier.uppercase()),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = tierColor
                            )
                        }
                    }
                }
                StatusBadge(status = match.status, streamUrl = match.stream_url)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TeamProfile(team = match.team_a)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    if (match.status.lowercase() == "not_started") {
                        Text(
                            text = formatMatchDate(
                                dateString = match.begin_at,
                                tbdString = stringResource(R.string.to_be_defined),
                                undefinedString = stringResource(R.string.undefined)
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.best_of_format, match.number_of_games ?: 3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = "${match.team_a_score} - ${match.team_b_score}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TeamProfile(team = match.team_b)
                }
            }
        }
    }
}

@Composable
fun TeamProfile(team: Team?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(DarkBackground, CircleShape)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (team != null) {
                AsyncImage(
                    model = team.dark_mode_image_url ?: team.image_url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (team?.location != null) {
                AsyncImage(
                    model = "https://flagcdn.com/w40/${team.location.lowercase()}.png",
                    contentDescription = null,
                    modifier = Modifier
                        .height(12.dp)
                        .width(16.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = team?.name ?: stringResource(R.string.to_be_defined),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusBadge(status: String, streamUrl: String?) {
    val isLive = status.lowercase() == "running"
    val isFinished = status.lowercase() == "finished"
    val badgeColor = when {
        isLive -> LiveRed
        isFinished -> TextSecondary
        else -> AccentBlue
    }
    val displayText = when (status.lowercase()) {
        "running" -> stringResource(R.string.status_live).uppercase()
        "finished" -> stringResource(R.string.status_finished).uppercase()
        "not_started" -> stringResource(R.string.status_upcoming).uppercase()
        else -> status.uppercase()
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .background(if (isLive) badgeColor.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
            .then(
                if (isLive && !streamUrl.isNullOrBlank()) {
                    Modifier.clickable {
                        openExternalWebLink(context, streamUrl)
                    }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLive) {
                BlinkingDot()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = displayText,
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun BlinkingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkingDot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaAnimation"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .alpha(alpha)
            .background(color = LiveRed, shape = CircleShape)
    )
}

@Composable
fun SkeletonMatchCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardHeader)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).shimmerEffect())
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.height(14.dp).width(40.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
                Box(modifier = Modifier.height(16.dp).width(60.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).shimmerEffect())
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.height(14.dp).width(60.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.height(24.dp).width(40.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect())
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).shimmerEffect())
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.height(14.dp).width(60.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                }
            }
        }
    }
}
