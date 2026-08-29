package com.example.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ProduceDoodle
import com.example.ui.theme.SproutCitrus
import com.example.ui.theme.SproutInk
import com.example.ui.theme.SproutInkMuted
import com.example.ui.theme.SproutInkSoft
import com.example.ui.theme.SproutLeaf
import com.example.ui.theme.SproutLeafDark
import com.example.ui.theme.SproutLine
import com.example.ui.theme.SproutPaper
import com.example.ui.theme.SproutPaperCard
import com.example.ui.theme.SproutTomato

private data class TourStep(
    val title: String,
    val subtitle: String,
    val badge: String,
    val doodles: List<Triple<String, String, String>>, // symbolId, archetype, name
    val colorAccent: Color
)

@Composable
fun QuickTourDialog(
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    val steps = remember {
        listOf(
            TourStep(
                title = "500+ Botanical Guides",
                subtitle = "Discover field-grade nutrition facts, optimal refrigerator storage, ripeness cues, and washing advice for every fruit and vegetable.",
                badge = "ENCYCLOPEDIA",
                doodles = listOf(
                    Triple("d-apple", "pome", "Apple"),
                    Triple("d-carrot", "root", "Carrot"),
                    Triple("d-strawberry", "berry", "Strawberry")
                ),
                colorAccent = SproutLeaf
            ),
            TourStep(
                title = "The 6-Color Rainbow Plate",
                subtitle = "Log your daily botanical diversity across Red, Orange, Yellow, Green, Blue/Purple, and White phytonutrient categories.",
                badge = "DAILY NUTRITION",
                doodles = listOf(
                    Triple("d-tomato", "nightshade", "Tomato"),
                    Triple("d-lemon", "citrus", "Lemon"),
                    Triple("d-blueberry", "berry", "Blueberry")
                ),
                colorAccent = SproutCitrus
            ),
            TourStep(
                title = "Daily Mystery Specimen Quiz",
                subtitle = "Test your produce knowledge each day with 3 progressive botanical clues. Guess correctly to build your explorer streak!",
                badge = "DAILY CHALLENGE",
                doodles = listOf(
                    Triple("d-avocado", "tropical", "Avocado"),
                    Triple("d-mushroom", "mushroom", "Mushroom"),
                    Triple("d-garlic", "allium", "Garlic")
                ),
                colorAccent = SproutTomato
            ),
            TourStep(
                title = "Camera Vision & AI Meal Labs",
                subtitle = "Scan real produce with your camera for instant freshness analysis, and use Gemini AI to generate customized recipes from your pantry.",
                badge = "AI & CAMERA LABS",
                doodles = listOf(
                    Triple("d-broccoli", "crucifer", "Broccoli"),
                    Triple("d-banana", "tropical", "Banana"),
                    Triple("d-cucumber", "gourd", "Cucumber")
                ),
                colorAccent = SproutLeafDark
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, SproutLine.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .testTag("dialog_quick_tour"),
            color = SproutPaper
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with Skip button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(SproutPaperCard)
                            .border(1.dp, SproutLine.copy(alpha = 0.25f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STEP ${currentStep + 1} OF ${steps.size}",
                            style = TextStyle(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutInkSoft
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tour",
                            tint = SproutInkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slide Content with animated transitions
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "TourSlideTransition"
                ) { stepIdx ->
                    val step = steps[stepIdx]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Large Doodles Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            step.doodles.forEach { (sym, arch, name) ->
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(SproutPaperCard)
                                        .border(1.2.dp, SproutLine.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ProduceDoodle(
                                        symbolId = sym,
                                        archetype = arch,
                                        name = name,
                                        size = 58.dp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Category Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(step.colorAccent.copy(alpha = 0.15f))
                                .border(1.dp, step.colorAccent.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = step.badge,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = step.colorAccent
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = step.title,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutInk,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = step.subtitle,
                            style = TextStyle(
                                fontSize = 13.5.sp,
                                color = SproutInkSoft,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Step indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (idx == currentStep) 24.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == currentStep) SproutInk else SproutInk.copy(alpha = 0.2f)
                                )
                                .clickable { currentStep = idx }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Bottom Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep -= 1 },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SproutInk
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Back",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = "Skip Tour",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SproutInkMuted
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < steps.size - 1) {
                                currentStep += 1
                            } else {
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SproutInk,
                            contentColor = SproutPaper
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("btn_tour_next")
                    ) {
                        Text(
                            text = if (currentStep == steps.size - 1) "Start Exploring!" else "Next",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (currentStep == steps.size - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
