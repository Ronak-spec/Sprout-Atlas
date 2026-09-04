package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.QuizQuestion
import com.example.ui.components.EyebrowHeader
import com.example.ui.components.ProduceDoodle
import com.example.ui.theme.*

@Composable
fun QuizDialog(
    question: QuizQuestion,
    currentStreak: Int,
    onDismiss: () -> Unit,
    onCorrectAnswer: () -> Unit,
    onNavigateToGuide: ((Int) -> Unit)? = null
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = SproutInk.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, SproutLine.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
            color = SproutPaperCard
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Whatshot,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${currentStreak}-day streak",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SproutInkMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mystery Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isSubmitted && isCorrect) Color(0xFFDCFCE7) else Color(0xFFF0FDF4))
                        .border(1.5.dp, if (isSubmitted && isCorrect) GenZElectricEmerald else GenZCyberGreen.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitted) {
                        ProduceDoodle(
                            symbolId = question.symbolId,
                            archetype = question.archetype,
                            name = question.specimenName,
                            size = 56.dp
                        )
                    } else {
                        Text(
                            text = "?",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = GenZDarkForest
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                EyebrowHeader(
                    text = "Daily Specimen · ${question.dateString}",
                    color = GenZDarkForest
                )

                Text(
                    text = if (isSubmitted && isCorrect) "You nailed it!" else if (isSubmitted) "Good try!" else "Guess the mystery produce",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutInk,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Clues box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF4FBF6))
                        .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.clues.forEachIndexed { i, clue ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Clue ${i + 1}:",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GenZDarkForest
                                )
                            )
                            Text(
                                text = clue,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = SproutInkSoft,
                                    lineHeight = 16.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isSubmitted) {
                    // Option buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        question.options.forEachIndexed { idx, opt ->
                            val isSelected = selectedIndex == idx
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) GenZDarkForest else Color.White
                                    )
                                    .border(
                                        1.2.dp,
                                        if (isSelected) GenZDarkForest else SproutLine.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedIndex = idx }
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                                    .testTag("quiz_option_$idx"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = opt,
                                    style = TextStyle(
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SproutInk
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (selectedIndex != null) {
                                isSubmitted = true
                                isCorrect = selectedIndex == question.correctIndex
                                if (isCorrect) {
                                    onCorrectAnswer()
                                }
                            }
                        },
                        enabled = selectedIndex != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("submit_guess_btn"),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF047857),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Color(0xFF9CA3AF)
                        )
                    ) {
                        Text(
                            text = "Submit Guess",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        )
                    }
                } else {
                    // Result feedback
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isCorrect) "🎉 Correct! It's ${question.specimenName}!" else "Not quite! It was ${question.specimenName}.",
                            style = TextStyle(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) Color(0xFF047857) else Color(0xFFDC2626)
                            )
                        )

                        Text(
                            text = question.funFact,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = SproutInkSoft,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.5.sp
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onNavigateToGuide != null) {
                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onNavigateToGuide(question.produceId)
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(100.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF047857)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF047857))
                                ) {
                                    Text("Open Guide", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                }
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(44.dp).testTag("continue_quiz_btn"),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCorrect) Color(0xFF047857) else Color(0xFFDC2626),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Continue", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
