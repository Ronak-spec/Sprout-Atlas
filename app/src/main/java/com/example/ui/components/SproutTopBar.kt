package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.SproutUserProfile
import com.example.ui.theme.*

@Composable
fun SproutTopBar(
    onLogoClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAccountClick: () -> Unit,
    onProClick: (() -> Unit)? = null,
    isPro: Boolean = false,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    quizStreak: Int = 0,
    isAuthenticated: Boolean = false,
    userProfile: SproutUserProfile? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sprout_top_bar"),
        color = SproutPaper.copy(alpha = 0.94f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = SproutInk.copy(alpha = 0.12f)
                )
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button (if active) + Sprout Vector Logo + Brand Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable { onLogoClick() }
                ) {
                    if (showBack && onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SproutInk.copy(alpha = 0.06f))
                                .testTag("top_bar_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = SproutInk,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Green sprout vector logo (extra enlarged for prominent brand presence)
                    ProduceDoodle(
                        symbolId = "d-sprout",
                        archetype = "leafy",
                        name = "Sprout",
                        size = 42.dp
                    )

                    // Brand Name in bold serif font (more enlarged & prominent)
                    Text(
                        text = buildAnnotatedString {
                            append("Sprout ")
                            withStyle(
                                SpanStyle(
                                    color = SproutLeaf,
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append("Atlas")
                            }
                        },
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp,
                            color = SproutInk
                        ),
                        maxLines = 1
                    )
                }

                // Right: [PRO Button] -> [Daily Quiz Button] -> [Tour Option Button] -> [Profile Picture (Right End)]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // PRO Button / Status Indicator
                    if (onProClick != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isPro) Color(0xFFEAF2E6) else Color(0xFFA5182F))
                                .border(
                                    width = 1.dp,
                                    color = if (isPro) SproutLeaf else Color(0xFF711021),
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .clickable { onProClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("top_bar_pro_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPro) Icons.Filled.Check else Icons.Filled.Star,
                                    contentDescription = "Sprout Atlas Pro",
                                    tint = if (isPro) SproutLeafDark else Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isPro) "PRO" else "UPGRADE",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp,
                                        color = if (isPro) SproutLeafDark else Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 1. Daily Quiz status indicator icon
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(SproutLeaf.copy(alpha = 0.12f))
                            .border(width = 1.dp, color = SproutLeaf.copy(alpha = 0.25f), shape = RoundedCornerShape(100.dp))
                            .clickable { onNotificationClick() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("top_bar_status_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SproutLeaf)
                            )
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Daily Quiz & Alerts",
                                tint = SproutInk,
                                modifier = Modifier.size(16.dp)
                            )
                            if (quizStreak > 0) {
                                Text(
                                    text = "${quizStreak}d",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Default,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SproutInk
                                    )
                                )
                            }
                        }
                    }

                    // Profile picture at the right end of the topbar
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isAuthenticated) SproutLeaf else SproutPaperCard)
                            .border(
                                width = 1.2.dp,
                                color = if (isAuthenticated) SproutLeafDark else SproutInk.copy(alpha = 0.22f),
                                shape = CircleShape
                            )
                            .clickable { onAccountClick() }
                            .testTag("top_bar_profile_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfile?.photoUrl != null && userProfile.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = userProfile.photoUrl,
                                contentDescription = "Google Account Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (isAuthenticated) {
                            val initial = (userProfile?.displayName?.take(1)
                                ?: userProfile?.email?.take(1)
                                ?: "B").uppercase()
                            Text(
                                text = initial,
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Account & Sign In",
                                tint = SproutInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
