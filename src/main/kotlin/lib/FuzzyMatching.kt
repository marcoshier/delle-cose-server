package com.marcoshier.lib

fun levenshteinDistanceOptimized(s1: String, s2: String, maxDistance: Int = 5): Int? {
    if (kotlin.math.abs(s1.length - s2.length) > maxDistance) return null

    val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

    for (i in 0..s1.length) dp[i][0] = i
    for (j in 0..s2.length) dp[0][j] = j

    for (i in 1..s1.length) {
        var minInRow = Int.MAX_VALUE
        for (j in 1..s2.length) {
            val cost = if (s1[i-1].lowercaseChar() == s2[j-1].lowercaseChar()) 0 else 1
            dp[i][j] = minOf(
                dp[i-1][j] + 1,
                dp[i][j-1] + 1,
                dp[i-1][j-1] + cost
            )
            minInRow = minOf(minInRow, dp[i][j])
        }

        if (minInRow > maxDistance) return null
    }

    return if (dp[s1.length][s2.length] <= maxDistance) dp[s1.length][s2.length] else null
}


fun findMatch(dictionary: List<String>, query: String): String? {
    val queryLower = query.lowercase()

    return dictionary.find { it.lowercase() == queryLower }
        ?: dictionary.find { it.lowercase().startsWith(queryLower) }
        ?: dictionary.find { it.lowercase().contains(queryLower) }
        ?: dictionary.mapNotNull { word ->
            levenshteinDistanceOptimized(word, query)?.let { distance -> word to distance }
        }.filter { it.second < 5 }
            .minByOrNull { it.second }
            ?.first
}
