class RabinKarpSearch {

    // A prime number to be used for modulo operations.
    // A large prime helps in reducing collisions.
    private val Q: Long = 1000000007 // A common large prime (10^9 + 7)

    // Number of possible characters (e.g., 256 for extended ASCII)
    // This is the base for the polynomial hash.
    private val D: Int = 256


    fun search(text: String, pattern: String): List<Int> {
        val n = text.length
        val m = pattern.length
        val foundIndices = mutableListOf<Int>()

        if (m == 0 || m > n) {
            return foundIndices
        }

        var patternHash: Long = 0 // Hash value for the pattern
        var textWindowHash: Long = 0 // Hash value for the current window in the text
        var hVal: Long = 1 // D^(m-1) % Q, used for rolling hash calculation

        // Calculate D^(m-1) % Q
        // This value is used to remove the leading digit and add the trailing digit in the rolling hash.
        for (i in 0 until m - 1) {
            hVal = (hVal * D) % Q
        }

        // Calculate the hash value for the pattern and the first window of the text
        for (i in 0 until m) {
            patternHash = (D * patternHash + pattern[i].code) % Q
            textWindowHash = (D * textWindowHash + text[i].code) % Q
        }

        // Slide the pattern over the text one by one
        for (s in 0..n - m) { // s is the starting index of the current window
            // Check if the hash values of the current window of text and pattern match.
            // If they match, then only check for characters one by one (to handle collisions).
            if (patternHash == textWindowHash) {
                var match = true
                for (j in 0 until m) {
                    if (text[s + j] != pattern[j]) {
                        match = false
                        break
                    }
                }
                if (match) {
                    foundIndices.add(s)
                }
            }

            // Calculate hash value for the next window of text:
            // Remove leading digit, add trailing digit.
            if (s < n - m) { // If there is a next window
                // Formula for rolling hash:
                // textWindowHash = (D * (textWindowHash - text[s].code * hVal) + text[s + m].code) % Q

                // Subtract the hash contribution of the character leaving the window
                var termToRemove = (text[s].code * hVal) % Q
                textWindowHash = (textWindowHash - termToRemove + Q) % Q // Add Q to handle potential negative result before modulo

                // Multiply by D (the base)
                textWindowHash = (textWindowHash * D) % Q

                // Add the hash contribution of the character entering the window
                textWindowHash = (textWindowHash + text[s + m].code) % Q

                // Ensure the hash is positive (it might become negative if Q was not added before)
                // This is already handled by adding Q before the first modulo in subtraction.
                // if (textWindowHash < 0) {
                // textWindowHash += Q
                // }
            }
        }
        return foundIndices
    }
}

fun main() {
    val text = "AABAACAADAABAABA"
    val pattern = "AABA"

    val rabinKarp = RabinKarpSearch()
    println("Text: $text")
    println("Pattern: $pattern")

    val indices = rabinKarp.search(text, pattern)

    if (indices.isNotEmpty()) {
        val resultString = indices.joinToString(", ")
        println("Pattern found at index $resultString")
    } else {
        println("Pattern not found.")
    }

    // --- Explanation of Time Complexity ---
    // - Preprocessing (calculating hVal and initial hashes): O(m)
    // - Main loop iterates n-m+1 times (approximately O(n)).
    //   - Hash comparison: O(1)
    //   - Rolling hash update: O(1)
    //   - Character comparison (in case of hash match): O(m) in the worst case.
    //
    // - Average Case Time Complexity: O(n + m)
    //   If the hash function distributes values well and collisions are rare,
    //   the character comparison (O(m)) part is seldom executed fully for many windows.
    //   The number of spurious hits (hash matches but string mismatch) is expected to be small.
    //
    // - Worst Case Time Complexity: O(n * m)
    //   This occurs if every window has the same hash as the pattern (many collisions, or a pattern like "AAAAA" in "AAAAAAAAA"),
    //   forcing a character-by-character comparison of O(m) for almost all O(n) windows.
    //   A good choice of Q (large prime) and D makes this worst-case scenario very unlikely for random inputs.
}
