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
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, SproutInk, RoundedCornerShape(24.dp)),
            color = SproutInk
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0x1FFAF6E9))
                            .border(1.dp, Color(0x3DFAF6E9), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentStreak}-day streak",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutCitrus
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SproutPaper)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mystery Icon
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(if (isSubmitted && isCorrect) SproutLeafLight else Color(0x1AFAF6E9))
                        .border(1.5.dp, SproutCitrus, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitted) {
                        ProduceDoodle(
                            symbolId = question.symbolId,
                            archetype = question.archetype,
                            name = question.specimenName,
                            size = 68.dp
                        )
                    } else {
                        Text(
                            text = "?",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutCitrus
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                EyebrowHeader(
                    text = "Daily Specimen · ${question.dateString}",
                    color = SproutCitrus
                )

                Text(
                    text = if (isSubmitted && isCorrect) "You nailed it!" else if (isSubmitted) "Good try!" else "Guess it before we name it.",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutPaper,
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
                        .background(Color(0x14FAF6E9))
                        .border(1.dp, Color(0x28FAF6E9), RoundedCornerShape(14.dp))
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
                                    color = SproutCitrus
                                )
                            )
                            Text(
                                text = clue,
                                style = TextStyle(
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFCDD6C4),
                                    lineHeight = 16.sp
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
                                    .background(if (isSelected) SproutCitrus else SproutPaperCard)
                                    .border(1.2.dp, if (isSelected) SproutCitrus else SproutPaperCard, RoundedCornerShape(12.dp))
                                    .clickable { selectedIndex = idx }
                                    .padding(vertical = 11.dp, horizontal = 16.dp)
                                    .testTag("quiz_option_$idx"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = opt,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SproutInk
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
                            containerColor = SproutCitrus,
                            contentColor = SproutInk
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
                            text = if (isCorrect) "It is indeed the ${question.specimenName}!" else "Not quite! It was the ${question.specimenName}.",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) Color(0xFF90C27A) else SproutTomato
                            )
                        )

                        Text(
                            text = question.funFact,
                            style = TextStyle(
                                fontSize = 11.5.sp,
                                color = Color(0xFFCDD6C4),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
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
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutCitrus),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SproutCitrus)
                                ) {
                                    Text("Open Guide", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                }
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(44.dp).testTag("continue_quiz_btn"),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCorrect) SproutLeaf else SproutTomato,
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
