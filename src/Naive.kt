fun naiveSearch(text: String, pattern: String): List<Int> {
    val n = text.length
    val m = pattern.length
    val foundIndices = mutableListOf<Int>()

    // Edge case: If the pattern is empty, technically it could be considered to match everywhere
    // or nowhere. For consistency with typical string search, we'll return an empty list.
    // If the pattern is longer than the text, it cannot be found.
    if (m == 0 || m > n) {
        return foundIndices
    }

    // Iterate through all possible starting positions for the pattern in the text.
    // The last possible starting position is n - m.
    for (i in 0..n - m) {
        var match = true // Assume a match until a mismatch is found
        // Check for pattern match starting at text index i
        for (j in 0 until m) {
            if (text[i + j] != pattern[j]) {
                match = false // Mismatch found
                break         // No need to check further for this starting position
            }
        }
        if (match) {
            foundIndices.add(i) // Pattern found at index i
        }
    }

    return foundIndices
}

fun main() {
    val text = "AABAACAADAABAABA"
    val pattern = "AABA"

    println("Text: $text")
    println("Pattern: $pattern")

    val indices = naiveSearch(text, pattern)

    if (indices.isNotEmpty()) {
        // Format output as "Pattern found at index 0, 9, 12"
        val resultString = indices.joinToString(", ")
        println("Pattern found at index $resultString")
    } else {
        println("Pattern not found.")
    }

    // --- Explanation of Time Complexity ---
    // The Naïve String Matching algorithm has a time complexity that can be analyzed as follows:
    //
    // - The outer loop runs `n - m + 1` times, where `n` is the length of the text
    //   and `m` is the length of the pattern. In the worst case, this is approximately `n` times.
    // - The inner loop (for character comparison) runs `m` times in the worst case for each
    //   starting position (e.g., when the pattern almost matches or when it does match).
    //
    // Therefore, the worst-case time complexity is O((n - m + 1) * m), which is often
    // simplified to O(n * m).
    //
    // Example of worst-case:
    // Text: "AAAAAAAAAB"
    // Pattern: "AAAB"
    // For each of the first several starting positions, the inner loop will run almost `m` times.
    //
    // Best-case time complexity:
    // If the first character of the pattern rarely matches characters in the text,
    // the inner loop might often break after the first comparison.
    // In an ideal scenario (e.g., pattern found immediately and text is short), it could be O(m).
    // However, if we consider iterating through the text, it's closer to O(n) if mismatches
    // occur quickly.
    //
    // For the purpose of your coursework, O(n*m) is the important complexity to note for the Naïve algorithm.
}
