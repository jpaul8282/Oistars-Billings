package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlyRevenueTrend
import com.example.ui.theme.AccentGold
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending
import kotlin.math.max
import kotlin.math.roundToInt

enum class ChartType {
    AREA_TREND,
    BAR_COLUMNS
}

@Composable
fun MonthlyRevenueTrendsChart(
    trends: List<MonthlyRevenueTrend>,
    selectedPeriodMonths: Int,
    onPeriodChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf(ChartType.AREA_TREND) }
    // Default selected index to the latest month so tooltip is immediately previewed
    var selectedMonthIndex by remember(trends) {
        mutableStateOf<Int?>(if (trends.isNotEmpty()) trends.lastIndex else null)
    }

    // Filter toggles for the series (Invoiced vs Collected vs Outstanding)
    var showInvoicedSeries by remember { mutableStateOf(true) }
    var showCollectedSeries by remember { mutableStateOf(true) }

    // MoM (Month-over-Month) calculation between last month and previous month
    val momGrowth = remember(trends) {
        if (trends.size >= 2) {
            val current = trends[trends.lastIndex].billedAmount
            val previous = trends[trends.lastIndex - 1].billedAmount
            if (previous > 0) {
                ((current - previous) / previous) * 100.0
            } else if (current > 0) {
                100.0
            } else 0.0
        } else 0.0
    }

    val totalPeriodBilled = remember(trends) { trends.sumOf { it.billedAmount } }
    val totalPeriodCollected = remember(trends) { trends.sumOf { it.collectedAmount } }
    val avgMonthlyRevenue = remember(trends) {
        if (trends.isNotEmpty()) totalPeriodBilled / trends.size else 0.0
    }
    val peakMonth = remember(trends) {
        trends.maxByOrNull { it.billedAmount }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_revenue_trends_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title, MoM growth badge, and period/chart-type controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = OceanBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = OceanBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Revenue Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Monthly billing analytics from Room ledger",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // MoM Growth Indicator Badge
                Surface(
                    color = if (momGrowth >= 0) StatusPaid.copy(alpha = 0.12f) else StatusOverdue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (momGrowth >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (momGrowth >= 0) StatusPaid else StatusOverdue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${if (momGrowth >= 0) "+" else ""}${String.format("%.1f", momGrowth)}% MoM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (momGrowth >= 0) StatusPaid else StatusOverdue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Control Bar: Chart View Type toggle (Area vs Bar) & Period selector (6M vs 12M)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chart Type Segmented Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (chartType == ChartType.AREA_TREND) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .clickable { chartType = ChartType.AREA_TREND }
                                .testTag("chart_type_area_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                    contentDescription = "Area Trend",
                                    modifier = Modifier.size(15.dp),
                                    tint = if (chartType == ChartType.AREA_TREND) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Area",
                                    fontSize = 11.sp,
                                    fontWeight = if (chartType == ChartType.AREA_TREND) FontWeight.Bold else FontWeight.Normal,
                                    color = if (chartType == ChartType.AREA_TREND) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (chartType == ChartType.BAR_COLUMNS) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .clickable { chartType = ChartType.BAR_COLUMNS }
                                .testTag("chart_type_bar_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Bar Chart",
                                    modifier = Modifier.size(15.dp),
                                    tint = if (chartType == ChartType.BAR_COLUMNS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Bars",
                                    fontSize = 11.sp,
                                    fontWeight = if (chartType == ChartType.BAR_COLUMNS) FontWeight.Bold else FontWeight.Normal,
                                    color = if (chartType == ChartType.BAR_COLUMNS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Time Range Selector Pill (6M / 12M)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        listOf(6 to "6M", 12 to "12M").forEach { (months, label) ->
                            val isSelected = selectedPeriodMonths == months
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clickable { onPeriodChange(months) }
                                    .testTag("period_selector_${months}m")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recharts-style Interactive Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Invoiced Legend Item
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showInvoicedSeries = !showInvoicedSeries }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (showInvoicedSeries) OceanBlue else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Total Invoiced",
                        fontSize = 12.sp,
                        fontWeight = if (showInvoicedSeries) FontWeight.Bold else FontWeight.Normal,
                        color = if (showInvoicedSeries) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Collected Legend Item
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showCollectedSeries = !showCollectedSeries }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (showCollectedSeries) StatusPaid else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Collected (Paid)",
                        fontSize = 12.sp,
                        fontWeight = if (showCollectedSeries) FontWeight.Bold else FontWeight.Normal,
                        color = if (showCollectedSeries) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Tooltip Card Preview (Recharts <Tooltip />)
            val activeTrend = selectedMonthIndex?.let { idx -> trends.getOrNull(idx) }
            if (activeTrend != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .testTag("chart_tooltip_preview")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeTrend.monthFullLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${activeTrend.invoiceCount} invoice${if (activeTrend.invoiceCount == 1) "" else "s"} issued (${activeTrend.paidInvoiceCount} settled)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Billed",
                                    fontSize = 10.sp,
                                    color = OceanBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = Formatters.formatCurrency(activeTrend.billedAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Paid (${activeTrend.collectionRatePercent}%)",
                                    fontSize = 10.sp,
                                    color = StatusPaid,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = Formatters.formatCurrency(activeTrend.collectedAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPaid
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Chart Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .padding(vertical = 4.dp)
            ) {
                if (trends.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No invoice data available for this period",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    RechartsCanvasView(
                        trends = trends,
                        chartType = chartType,
                        showInvoiced = showInvoicedSeries,
                        showCollected = showCollectedSeries,
                        selectedIndex = selectedMonthIndex,
                        onSelectIndex = { selectedMonthIndex = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary KPIs Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Avg Monthly Revenue
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Avg Monthly",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatCurrency(avgMonthlyRevenue),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Metric 2: Peak Month
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Highest Month",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = peakMonth?.monthLabel ?: "-",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }
                }

                // Metric 3: Period Total Collected
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Total Collected",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatCurrency(totalPeriodCollected),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusPaid
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RechartsCanvasView(
    trends: List<MonthlyRevenueTrend>,
    chartType: ChartType,
    showInvoiced: Boolean,
    showCollected: Boolean,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val invoicedColor = OceanBlue
    val collectedColor = StatusPaid
    val outstandingColor = StatusPending

    val maxAmount = remember(trends) {
        val maxVal = trends.maxOfOrNull { max(it.billedAmount, it.collectedAmount) } ?: 1000.0
        // Round max up to nearest convenient ceiling
        val ceiling = if (maxVal <= 0.0) 1000.0 else maxVal * 1.15
        ceiling
    }

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(trends) {
                detectTapGestures { offset ->
                    val pointCount = trends.size
                    if (pointCount > 0) {
                        val leftPadding = 80f
                        val rightPadding = 40f
                        val availableWidth = size.width - leftPadding - rightPadding
                        val stepX = availableWidth / (pointCount - 1).coerceAtLeast(1)

                        val closestIdx = ((offset.x - leftPadding + (stepX / 2f)) / stepX)
                            .toInt()
                            .coerceIn(0, pointCount - 1)
                        onSelectIndex(closestIdx)
                    }
                }
            }
            .pointerInput(trends) {
                detectDragGestures { change, _ ->
                    val pointCount = trends.size
                    if (pointCount > 0) {
                        val leftPadding = 80f
                        val rightPadding = 40f
                        val availableWidth = size.width - leftPadding - rightPadding
                        val stepX = availableWidth / (pointCount - 1).coerceAtLeast(1)

                        val closestIdx = ((change.position.x - leftPadding + (stepX / 2f)) / stepX)
                            .toInt()
                            .coerceIn(0, pointCount - 1)
                        onSelectIndex(closestIdx)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val leftPadding = 80f
        val rightPadding = 40f
        val topPadding = 25f
        val bottomPadding = 45f

        val plotWidth = width - leftPadding - rightPadding
        val plotHeight = height - topPadding - bottomPadding

        // 1. Draw Cartesian Horizontal Grid Lines & Y-Axis Labels (Recharts <CartesianGrid />)
        val gridStepCount = 4
        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

        for (i in 0..gridStepCount) {
            val fraction = i.toFloat() / gridStepCount
            val y = topPadding + plotHeight * (1f - fraction)
            val gridValue = maxAmount * fraction

            // Grid Line
            drawLine(
                color = gridLineColor,
                start = Offset(leftPadding, y),
                end = Offset(width - rightPadding, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (i > 0) dashedEffect else null
            )

            // Y-Axis Currency Label (e.g. "€0", "€2.5k", "€5k")
            val formattedValue = if (gridValue >= 1000) {
                "€${(gridValue / 1000).toInt()}k"
            } else {
                "€${gridValue.toInt()}"
            }

            drawText(
                textMeasurer = textMeasurer,
                text = formattedValue,
                topLeft = Offset(8f, y - 10.sp.toPx()),
                style = TextStyle(
                    fontSize = 10.sp,
                    color = onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            )
        }

        val pointCount = trends.size
        if (pointCount == 0) return@Canvas

        val stepX = if (pointCount > 1) plotWidth / (pointCount - 1) else plotWidth / 2f

        // 2. Draw Active Selection Cursor Line (Recharts <Tooltip cursor />)
        selectedIndex?.let { selIdx ->
            if (selIdx in 0 until pointCount) {
                val cursorX = leftPadding + (selIdx * stepX)
                drawLine(
                    color = OceanBlue.copy(alpha = 0.4f),
                    start = Offset(cursorX, topPadding),
                    end = Offset(cursorX, topPadding + plotHeight),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Highlight background column for selected month
                val colWidth = stepX.coerceAtMost(50.dp.toPx())
                drawRoundRect(
                    color = OceanBlue.copy(alpha = 0.05f),
                    topLeft = Offset(cursorX - (colWidth / 2f), topPadding),
                    size = Size(colWidth, plotHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }

        // 3. Draw Chart Content: Area Trend or Bar Columns
        if (chartType == ChartType.AREA_TREND) {
            // Compute Coordinates
            val invoicedPoints = mutableListOf<Offset>()
            val collectedPoints = mutableListOf<Offset>()

            for (i in 0 until pointCount) {
                val x = leftPadding + (i * stepX)
                val trend = trends[i]

                val invoicedY = topPadding + plotHeight * (1f - (trend.billedAmount / maxAmount).toFloat().coerceIn(0f, 1f))
                val collectedY = topPadding + plotHeight * (1f - (trend.collectedAmount / maxAmount).toFloat().coerceIn(0f, 1f))

                invoicedPoints.add(Offset(x, invoicedY))
                collectedPoints.add(Offset(x, collectedY))
            }

            // Draw Invoiced Area Gradient & Smooth Cubic Curve
            if (showInvoiced && invoicedPoints.size >= 2) {
                val areaPath = Path().apply {
                    moveTo(invoicedPoints.first().x, topPadding + plotHeight)
                    lineTo(invoicedPoints.first().x, invoicedPoints.first().y)

                    for (i in 0 until invoicedPoints.size - 1) {
                        val p0 = invoicedPoints[i]
                        val p1 = invoicedPoints[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }

                    lineTo(invoicedPoints.last().x, topPadding + plotHeight)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            invoicedColor.copy(alpha = 0.30f),
                            invoicedColor.copy(alpha = 0.02f)
                        ),
                        startY = topPadding,
                        endY = topPadding + plotHeight
                    )
                )

                // Stroke Line
                val linePath = Path().apply {
                    moveTo(invoicedPoints.first().x, invoicedPoints.first().y)
                    for (i in 0 until invoicedPoints.size - 1) {
                        val p0 = invoicedPoints[i]
                        val p1 = invoicedPoints[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = invoicedColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw Collected Area Gradient & Smooth Curve
            if (showCollected && collectedPoints.size >= 2) {
                val collectedAreaPath = Path().apply {
                    moveTo(collectedPoints.first().x, topPadding + plotHeight)
                    lineTo(collectedPoints.first().x, collectedPoints.first().y)

                    for (i in 0 until collectedPoints.size - 1) {
                        val p0 = collectedPoints[i]
                        val p1 = collectedPoints[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }

                    lineTo(collectedPoints.last().x, topPadding + plotHeight)
                    close()
                }

                drawPath(
                    path = collectedAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            collectedColor.copy(alpha = 0.22f),
                            collectedColor.copy(alpha = 0.01f)
                        ),
                        startY = topPadding,
                        endY = topPadding + plotHeight
                    )
                )

                val linePath = Path().apply {
                    moveTo(collectedPoints.first().x, collectedPoints.first().y)
                    for (i in 0 until collectedPoints.size - 1) {
                        val p0 = collectedPoints[i]
                        val p1 = collectedPoints[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = collectedColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw Data Nodes & Active Glow Rings
            for (i in 0 until pointCount) {
                val isSelected = selectedIndex == i

                if (showInvoiced) {
                    val p = invoicedPoints[i]
                    if (isSelected) {
                        drawCircle(
                            color = invoicedColor.copy(alpha = 0.25f),
                            radius = 10.dp.toPx(),
                            center = p
                        )
                    }
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = p)
                    drawCircle(color = invoicedColor, radius = 4.dp.toPx(), center = p)
                }

                if (showCollected) {
                    val p = collectedPoints[i]
                    if (isSelected) {
                        drawCircle(
                            color = collectedColor.copy(alpha = 0.25f),
                            radius = 9.dp.toPx(),
                            center = p
                        )
                    }
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = p)
                    drawCircle(color = collectedColor, radius = 3.dp.toPx(), center = p)
                }
            }
        } else {
            // Bar Columns View (Grouped Dual Bars per Month)
            val barGroupWidth = (stepX * 0.65f).coerceAtMost(36.dp.toPx())
            val singleBarWidth = barGroupWidth / 2f - 2.dp.toPx()

            for (i in 0 until pointCount) {
                val centerX = leftPadding + (i * stepX)
                val trend = trends[i]
                val isSelected = selectedIndex == i

                // Billed Bar (Left bar in group)
                if (showInvoiced) {
                    val invoicedH = (trend.billedAmount / maxAmount).toFloat().coerceIn(0f, 1f) * plotHeight
                    val barX = centerX - singleBarWidth - 1.dp.toPx()
                    val barY = topPadding + plotHeight - invoicedH

                    drawRoundRect(
                        color = if (isSelected) invoicedColor else invoicedColor.copy(alpha = 0.85f),
                        topLeft = Offset(barX, barY),
                        size = Size(singleBarWidth, invoicedH),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                // Collected Bar (Right bar in group)
                if (showCollected) {
                    val collectedH = (trend.collectedAmount / maxAmount).toFloat().coerceIn(0f, 1f) * plotHeight
                    val barX = centerX + 1.dp.toPx()
                    val barY = topPadding + plotHeight - collectedH

                    drawRoundRect(
                        color = if (isSelected) collectedColor else collectedColor.copy(alpha = 0.85f),
                        topLeft = Offset(barX, barY),
                        size = Size(singleBarWidth, collectedH),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // 4. Draw X-Axis Baseline & Month Labels
        drawLine(
            color = gridLineColor,
            start = Offset(leftPadding, topPadding + plotHeight),
            end = Offset(width - rightPadding, topPadding + plotHeight),
            strokeWidth = 1.5.dp.toPx()
        )

        for (i in 0 until pointCount) {
            val x = leftPadding + (i * stepX)
            val label = trends[i].monthLabel
            val isSelected = selectedIndex == i

            val textLayoutResult = textMeasurer.measure(
                text = label,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) primaryColor else onSurfaceVariant
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x - (textLayoutResult.size.width / 2f), topPadding + plotHeight + 10f)
            )
        }
    }
}
