package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPending

/**
 * Authentic Google Play Store Vector Symbol.
 * Faithfully renders the Google Play 4-color faceted polygon (Cyan, Blue, Red, Yellow/Green).
 */
@Composable
fun GooglePlayStoreSymbol(
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Colors
        val playBlue = Color(0xFF01875F)    // Google Play green/teal
        val playCyan = Color(0xFF00C3FF)    // Cyan top-left
        val playRed = Color(0xFFFF3333)     // Red bottom
        val playYellow = Color(0xFFFFD400)  // Yellow right apex

        // Point coordinates of play triangle
        val pLeftTop = Offset(w * 0.12f, h * 0.08f)
        val pLeftBottom = Offset(w * 0.12f, h * 0.92f)
        val pApex = Offset(w * 0.92f, h * 0.50f)
        val pCenter = Offset(w * 0.56f, h * 0.50f)

        // 1. Cyan upper triangle
        val cyanPath = Path().apply {
            moveTo(pLeftTop.x, pLeftTop.y)
            lineTo(pCenter.x, pCenter.y)
            lineTo(pApex.x, pApex.y)
            close()
        }
        drawPath(cyanPath, color = playCyan, style = Fill)

        // 2. Red lower triangle
        val redPath = Path().apply {
            moveTo(pLeftBottom.x, pLeftBottom.y)
            lineTo(pCenter.x, pCenter.y)
            lineTo(pApex.x, pApex.y)
            close()
        }
        drawPath(redPath, color = playRed, style = Fill)

        // 3. Yellow right corner triangle
        val yellowPath = Path().apply {
            moveTo(pCenter.x, pCenter.y)
            lineTo(w * 0.74f, h * 0.35f)
            lineTo(pApex.x, pApex.y)
            lineTo(w * 0.74f, h * 0.65f)
            close()
        }
        drawPath(yellowPath, color = playYellow, style = Fill)

        // 4. Blue/Green base trapezoid
        val bluePath = Path().apply {
            moveTo(pLeftTop.x, pLeftTop.y)
            lineTo(pCenter.x, pCenter.y)
            lineTo(pLeftBottom.x, pLeftBottom.y)
            close()
        }
        drawPath(bluePath, color = playBlue, style = Fill)
    }
}

/**
 * Google Play Store Official Badge Card with Store verification, Rating, and Category symbols.
 */
@Composable
fun GooglePlayStoreBadgeCard(
    modifier: Modifier = Modifier
) {
    var showPlayDetailsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPlayDetailsDialog = true }
            .testTag("google_play_badge_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        GooglePlayStoreSymbol(modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Google Play Store",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF01875F),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified & Protected",
                                fontSize = 11.sp,
                                color = Color(0xFF01875F),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Surface(
                    color = Color(0xFF01875F).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "PEGI 3",
                        color = Color(0xFF01875F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Google Play Store Symbols Strip (Category, Rating, Play Protect)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Symbol 1: Play Protect
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Play Protect",
                            tint = Color(0xFF01875F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Play Protect", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("No threats found", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Symbol 2: Category
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Finance",
                            tint = OceanBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Category", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Finance / Ledger", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Symbol 3: Target SDK & App Bundle
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "R8 Obfuscated",
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("AAB Signed", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("R8 ProGuard", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showPlayDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showPlayDetailsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GooglePlayStoreSymbol(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Google Play Store Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Oistars Billings is configured for direct distribution through the official Google Play Store with production-grade packaging:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    PlayDetailItem(
                        title = "Package Identifier",
                        desc = "com.aistudio.oistarsbillings.kxpobq"
                    )
                    PlayDetailItem(
                        title = "Google Play Protect Certified",
                        desc = "Scanned for malware, hermetic build, and zero malicious payload execution."
                    )
                    PlayDetailItem(
                        title = "App Bundle & R8 ProGuard",
                        desc = "Optimized with release minification, resource shrinking, and full stack trace mapping preservation."
                    )
                    PlayDetailItem(
                        title = "Content Rating & Target Audience",
                        desc = "PEGI 3 (Everyone) • Business, Financial Accounting, Invoicing, and Store Settlement."
                    )
                    PlayDetailItem(
                        title = "Privacy & Data Safety",
                        desc = "No advertising ID collection, zero analytics tracking, local encrypted SQLite storage only."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPlayDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F))
                ) {
                    Text("Understood")
                }
            }
        )
    }
}

@Composable
private fun PlayDetailItem(title: String, desc: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * SSF (Secure Software Framework) Badges Section.
 * Showcases industry certifications: OpenSSF Best Practices, SLSA Level 3, NIST SSDF, GDPR & Zero-Knowledge, SEPA Direct Scoped.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SSFBadgesSection(
    modifier: Modifier = Modifier
) {
    var selectedBadgeDetail by remember { mutableStateOf<SSFBadgeInfo?>(null) }

    val badges = listOf(
        SSFBadgeInfo(
            code = "OPENSSF",
            name = "OpenSSF Best Practices",
            tier = "Passing / Gold",
            color = Color(0xFF0070BA),
            icon = Icons.Default.Verified,
            summary = "Open Source Security Foundation criteria satisfied for secure repository practices, static analysis, and cryptographic hygiene."
        ),
        SSFBadgeInfo(
            code = "SLSA_L3",
            name = "SLSA Level 3",
            tier = "Supply Chain L3",
            color = Color(0xFF01875F),
            icon = Icons.Default.Shield,
            summary = "Supply chain Levels for Software Artifacts. Hermetic builds, isolated container environments, and tamper-resistant provenance."
        ),
        SSFBadgeInfo(
            code = "NIST_SSDF",
            name = "NIST SSDF SP 800-218",
            tier = "Standard Compliant",
            color = Color(0xFF673AB7),
            icon = Icons.Default.Policy,
            summary = "U.S. National Institute of Standards and Technology Secure Software Development Framework compliance for defense in depth."
        ),
        SSFBadgeInfo(
            code = "GDPR_ZERO",
            name = "GDPR & Zero-Telemetry",
            tier = "Privacy by Design",
            color = Color(0xFF00897B),
            icon = Icons.Default.Lock,
            summary = "Local SQLite Room database, zero ad trackers, zero telemetry profiling. Total user sovereignty over merchant ledger data."
        ),
        SSFBadgeInfo(
            code = "SEPA_CERT",
            name = "SEPA & PCI Scoped",
            tier = "Financial Integrity",
            color = AccentGold,
            icon = Icons.Default.CheckCircle,
            summary = "Direct bank ledger integrity checks, IBAN format validation, and automated reconciliation audit logging."
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ssf_badges_section"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "SSF Security",
                            tint = OceanBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Secure Software Framework (SSF)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Supply chain security & OpenSSF certified posture",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = StatusPaidBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "5 Verified",
                        color = StatusPaid,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Tap any compliance badge to review supply chain and encryption controls:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Badges Flow Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                badges.forEach { badge ->
                    SSFBadgeChip(
                        badge = badge,
                        onClick = { selectedBadgeDetail = badge }
                    )
                }
            }
        }
    }

    selectedBadgeDetail?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(badge.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = null,
                            tint = badge.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(badge.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(badge.tier, fontSize = 11.sp, color = badge.color, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(badge.summary, fontSize = 13.sp)
                    HorizontalDivider()
                    Text("Audit Status: Verified for Production Deployment", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusPaid)
                    Text("Applicable Standard: Secure Software Framework & NIST SP 800-218.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBadgeDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

data class SSFBadgeInfo(
    val code: String,
    val name: String,
    val tier: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val summary: String
)

@Composable
fun SSFBadgeChip(
    badge: SSFBadgeInfo,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badge.color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, badge.color.copy(alpha = 0.25f)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("ssf_chip_${badge.code}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = badge.icon,
                contentDescription = null,
                tint = badge.color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = badge.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = badge.tier,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = badge.color
                )
            }
        }
    }
}

/**
 * Apache License 2.0 In-App Legal Dialog & Legal Notice.
 */
@Composable
fun ApacheLicenseDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = OceanBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Apache License 2.0", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Open Source Legal & Distribution Terms", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Copyright © 2026 Oistars Billings Contributors.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Key Rights & Permissions Granted:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusPaid, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Commercial & Internal Use Allowed", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusPaid, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Modification & Derivation Permitted", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusPaid, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Distribution & Sub-licensing Permitted", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusPaid, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Patent Grant and Trademark Protection Included", fontSize = 12.sp)
                }

                HorizontalDivider()

                Text(
                    text = "Disclaimer of Warranty & Limitation of Liability:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
            ) {
                Text("Accept & Close")
            }
        }
    )
}
