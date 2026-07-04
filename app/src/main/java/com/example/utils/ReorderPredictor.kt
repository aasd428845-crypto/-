package com.example.utils

import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PharmaProduct
import kotlin.math.roundToInt

data class ReorderSuggestion(
    val product: PharmaProduct,
    val avgIntervalDays: Int,
    val daysSinceLastOrder: Int,
    val suggestedQuantity: Int,
    val urgency: ReorderUrgency
)

enum class ReorderUrgency { DUE_NOW, UPCOMING }

fun calculateReorderSuggestions(
    pastOrders: List<Order>,
    catalog: List<PharmaProduct>
): List<ReorderSuggestion> {
    // Filter out draft orders as they don't represent completed purchases/consumption
    val activeOrders = pastOrders.filter { 
        it.orderStatus !is OrderStatus.Draft && it.status != "draft"
    }
    
    if (activeOrders.isEmpty() || catalog.isEmpty()) return emptyList()

    val suggestions = mutableListOf<ReorderSuggestion>()

    for (product in catalog) {
        // Find all active orders containing this product, along with the corresponding order line
        val ordersWithProduct = activeOrders.mapNotNull { order ->
            val matchingLine = order.orderLines.find { line ->
                (line.product.productId == product.productId && product.productId.isNotBlank()) ||
                (line.product.sku == product.sku && product.sku.isNotBlank())
            }
            if (matchingLine != null) {
                Pair(order, matchingLine)
            } else {
                null
            }
        }.sortedBy { it.first.createdAt }

        // Require at least 2 orders for a reliable pattern
        if (ordersWithProduct.size < 2) continue

        val firstOrderTime = ordersWithProduct.first().first.createdAt
        val lastOrderTime = ordersWithProduct.last().first.createdAt
        val numOrders = ordersWithProduct.size

        // If all orders are at the exact same millisecond or time flows backwards, skip
        if (lastOrderTime <= firstOrderTime) continue

        val totalDurationMs = lastOrderTime - firstOrderTime
        val avgIntervalMs = totalDurationMs.toDouble() / (numOrders - 1)
        val avgIntervalDays = (avgIntervalMs / (24.0 * 60.0 * 60.0 * 1000.0)).roundToInt().coerceAtLeast(1)

        val currentTimeMs = System.currentTimeMillis()
        val daysSinceLastOrder = ((currentTimeMs - lastOrderTime).toDouble() / (24.0 * 60.0 * 60.0 * 1000.0)).toInt().coerceAtLeast(0)

        val urgency = when {
            daysSinceLastOrder >= avgIntervalDays -> ReorderUrgency.DUE_NOW
            daysSinceLastOrder >= (avgIntervalDays * 0.8) -> ReorderUrgency.UPCOMING
            else -> null
        }

        if (urgency != null) {
            val avgQty = ordersWithProduct.map { it.second.requestedQty }.average().roundToInt().coerceAtLeast(1)
            suggestions.add(
                ReorderSuggestion(
                    product = product,
                    avgIntervalDays = avgIntervalDays,
                    daysSinceLastOrder = daysSinceLastOrder,
                    suggestedQuantity = avgQty,
                    urgency = urgency
                )
            )
        }
    }

    return suggestions
}
