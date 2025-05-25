fun computeLpsArray(pattern: String): IntArray {
    val m = pattern.length
    val lps = IntArray(m) // Initialize LPS array with zeros (default for IntArray)
    var length = 0 // Length of the previous longest prefix suffix
    var i = 1

    // lps[0] is always 0, so we start from i = 1
    while (i < m) {
        if (pattern[i] == pattern[length]) {
            length++
            lps[i] = length
            i++
        } else {
            if (length != 0) {
                length = lps[length - 1]
                // Note: We do not increment i here
            } else {
                lps[i] = 0
                i++
            }
        }
    }
    return lps
}

fun kmpSearch(text: String, pattern: String): List<Int> {
    val n = text.length
    val m = pattern.length
    val foundIndices = mutableListOf<Int>()

    if (m == 0) return emptyList() // Edge case: empty pattern
    if (m > n) return emptyList()  // Edge case: pattern longer than text

    // Preprocess the pattern to get the LPS array
    val lps = computeLpsArray(pattern)

    var i = 0 // Index for text
    var j = 0 // Index for pattern

    while (i < n) {
        if (pattern[j] == text[i]) {
            i++
            j++
        }

        if (j == m) {
            // Pattern found at index (i - j)
            foundIndices.add(i - j)
            // Continue searching for more occurrences
            j = lps[j - 1]
        } else if (i < n && pattern[j] != text[i]) {
            // Mismatch after j matches
            if (j != 0) {
                j = lps[j - 1]
            } else {
                // If j is 0, first char of pattern didn't match, move to next char in text
                i++
            }
        }
    }
    return foundIndices
}

fun main() {
    val text = "AABAACAADAABAABA"
    val pattern = "AABA"

    println("Text: $text")
    println("Pattern: $pattern")

    val indices = kmpSearch(text, pattern)

    if (indices.isNotEmpty()) {
        // Format output as "Pattern found at index 0, 9, 12"
        val resultString = indices.joinToString(", ")
        println("Pattern found at index $resultString")
    } else {
        println("Pattern not found.")
    }

    // --- Explanation of Time Complexity ---
    // The time complexity of the KMP algorithm is O(N + M):
    // where N is the length of the text and M is the length of the pattern.
    // 1. computeLpsArray: This function iterates through the pattern of length M once.
    //    Amortized analysis shows this step is O(M).
    // 2. kmpSearch: The main search loop iterates through the text. The text index `i`
    //    is always incremented, or the pattern index `j` is reduced based on the LPS array.
    //    `i` never goes backward. Amortized analysis shows this search phase is O(N).
    // Total time complexity = O(M) (for LPS) + O(N) (for search) = O(N + M).
}
