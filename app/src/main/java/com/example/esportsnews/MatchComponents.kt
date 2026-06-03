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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.esportsnews.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatMatchDate(dateString: String?): String {
    if (dateString == null) return "Data a definir"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val parsedDate = parser.parse(dateString)

        val formatter = SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR"))
        formatter.timeZone = TimeZone.getDefault()
        parsedDate?.let { formatter.format(it) } ?: "Data a definir"
    } catch (e: Exception) {
        "Horário indefinido"
    }
}

@Composable
fun ModernMatchCard(match: Match,
                    onClick: () -> Unit) {

    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.league?.image_url != null) {
                        AsyncImage(
                            model = match.league.image_url,
                            contentDescription = "Logo da liga",
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    val displayGame = if (match.game.equals("CSGO", ignoreCase = true)) "CS2" else match.game.uppercase()

                    Text(
                        text = displayGame,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        letterSpacing = 1.sp
                    )
                }
                StatusBadge(status = match.status, streamUrl = match.stream_url)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TeamProfile(name = match.team_a.name, imageUrl = match.team_a.image_url)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (match.status.lowercase() == "not_started") {
                        Text(
                            text = "VS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = "${match.team_a_score} - ${match.team_b_score}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatMatchDate(match.begin_at),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TeamProfile(name = match.team_b.name, imageUrl = match.team_b.image_url)
                }
            }
        }
    }
}

@Composable
fun TeamProfile(name: String, imageUrl: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(DarkBackground, CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Logo $name",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
        "running" -> "AO VIVO"
        "finished" -> "FINALIZADO"
        "not_started" -> "EM BREVE"
        else -> status.uppercase()
    }
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .then(
                if (isLive && !streamUrl.isNullOrBlank()) {
                    Modifier.clickable {
                        try {
                            uriHandler.openUri(streamUrl)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
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
                fontWeight = FontWeight.ExtraBold,
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
            .size(8.dp)
            .alpha(alpha)
            .background(color = LiveRed, shape = CircleShape)
    )
}