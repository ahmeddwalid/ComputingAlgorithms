private const val ALPHABET_SIZE = 256 // Assuming ASCII or extended ASCII characters

private fun computeKmpLps(pattern: String): IntArray {
    val m = pattern.length
    val lps = IntArray(m)
    var length = 0
    var i = 1
    lps[0] = 0 // lps[0] is always 0

    while (i < m) {
        if (pattern[i] == pattern[length]) {
            length++
            lps[i] = length
            i++
        } else {
            if (length != 0) {
                length = lps[length - 1]
            } else {
                lps[i] = 0
                i++
            }
        }
    }
    return lps
}


// --- Preprocessing for the Bad Character Rule ---
private fun preprocessBadCharacter(pattern: String, m: Int): IntArray {
    val badCharTable = IntArray(ALPHABET_SIZE) { -1 } // Initialize all to -1 (char not in pattern)
    for (k in 0 until m) {
        badCharTable[pattern[k].code] = k // Record rightmost occurrence
    }
    return badCharTable
}

// --- Preprocessing for the Strong Good Suffix Rule ---
private fun preprocessStrongGoodSuffix(pattern: String, m: Int): IntArray {
    val goodSuffixShift = IntArray(m) { m } // Initialize with max shift (pattern length)

    // Use KMP's LPS array for pattern and reversed pattern to implement good suffix cases.
    // lps = KMP LPS for P
    // lpsRev = KMP LPS for P.reversed()

    val lps = computeKmpLps(pattern)
    val reversedPattern = pattern.reversed()
    val lpsRev = computeKmpLps(reversedPattern)

    // Case 2 of Strong Good Suffix Rule:
    // A prefix of P, say P[0...k-1], matches a suffix of the good suffix P[i+1...m-1].
    // We want to shift P to align P[0...k-1] with the corresponding part of the text.
    // The shift amount is m - k.
    // This rule is applied for each possible border of P that could form such a k.
    // Iterate through all possible lengths k of a prefix of P that is also a suffix of P.
    // k = lps[m-1] is the length of the longest prefix of P that is also a suffix of P.
    // If goodSuffix has length k, shift by m-k. Mismatch pos i = m-1-k.
    // goodSuffixShift[m-1-k] = m-k
    var k = lps[m - 1] // k is the length of the border of the whole pattern
    while (k > 0) {
        // If a mismatch occurs at position m-1-k (so good suffix has length k),
        // and this good suffix P[m-k...m-1] is also a prefix P[0...k-1],
        // then we can shift by m-k.
        if (goodSuffixShift[m - 1 - k] == m) { // Update only if not set by a more specific rule (Case 1)
            goodSuffixShift[m - 1 - k] = m - k
        }
        k = lps[k - 1] // Check next shorter border of P
    }
    // If the whole pattern is periodic, this sets some shifts.
    // A more general application for Case 2:
    // If P[0...j-1] is a suffix of good suffix P[i+1...m-1], shift by m-j.
    // goodSuffixShift[i] should be m-j.
    // This means if mismatch at i, and P[0...lpsRev[m-1-(i+1)]-1] is suffix of P[i+1...m-1], shift.
    // The length of the prefix of P that is a suffix of P[i+1...m-1] is lpsRev[m-1-(i+1)].
    // Let this length be `len_pref_match = lpsRev[m-1-(i+1)]`.
    // If `len_pref_match > 0`, then `goodSuffixShift[i] = min(goodSuffixShift[i], m - len_pref_match)`.
    // This needs to be applied carefully.

    // Case 1 of Strong Good Suffix Rule:
    // The good suffix P[i+1...m-1] occurs again at P[j...j + (m-1-(i+1)) -1]
    // and P[j-1] != P[i] (if j>0). Shift is (i+1) - j.
    // We iterate from right to left of P (index `s_pat` from `m-1` down to `0`).
    // `lpsRev[s_pat]` gives the length of the longest suffix of P (which is `P[m-1-s_pat ... m-1]`)
    // that is also a prefix of `P[0...m-1-s_pat]`.
    // Let `len_match = lpsRev[s_pat]`.
    // This means `P[m-len_match ... m-1]` matches `P[m-1-s_pat ... m-1-s_pat+len_match-1]`.
    // The good suffix is `P[m-1-s_pat ... m-1]`. Its length is `s_pat+1`.
    // The mismatch position `i` for this good suffix is `m-1-(s_pat+1) = m-2-s_pat`.
    // The shift is `(s_pat+1) - len_match`.
    // `goodSuffixShift[m-2-s_pat] = min(goodSuffixShift[m-2-s_pat], (s_pat+1) - len_match)`.

    // Standard implementation using `lpsRev` (LPS of reversed pattern):
    // `lpsRev[k]` = length of longest suffix of P that is prefix of `P[0...m-1-k]`.
    // Iterate `k` from `0` to `m-1` (index in `lpsRev`).
    // `j = lpsRev[k]`. This `j` is a length.
    // The suffix `P[m-j ... m-1]` matches `P[m-1-k ... m-1-k+j-1]`.
    // The good suffix is `P[m-1-k ... m-1]`. Mismatch is at `m-2-k`.
    // Shift is `(k+1) - j`.
    // `goodSuffixShift[m-1-k-1]` (mismatch pos) = `k+1 - j` (shift).
    for (idxRevLps in 0 until m) { // idxRevLps is k in the description above
        val lenBorderRev = lpsRev[idxRevLps] // This is j, length of border of P_rev[0...idxRevLps]
        // which means P[m-lenBorderRev...m-1] matches P[m-1-idxRevLps ... m-1-idxRevLps+lenBorderRev-1]
        if (lenBorderRev > 0) {
            // Good Suffix is P[m-1-idxRevLps ... m-1]. Length is idxRevLps + 1.
            // Mismatch position `i` in pattern is `m - 1 - (idxRevLps + 1) = m - 2 - idxRevLps`.
            // Shift amount is `(idxRevLps + 1) - lenBorderRev`.
            val mismatchPos = m - 1 - (idxRevLps + 1)
            if (mismatchPos >=0) { // Ensure mismatchPos is a valid index
                goodSuffixShift[mismatchPos] = Math.min(goodSuffixShift[mismatchPos], (idxRevLps + 1) - lenBorderRev)
            }
        }
    }
    // This covers Case 1. For Case 2, we need to ensure that if Case 1 doesn't give a better shift,
    // we use the shift based on the longest prefix of P that is a suffix of the good suffix.
    // The initialization `goodSuffixShift[i] = m` and the previous loop for borders of P handle this.
    // A simpler way to combine:
    // Initialize `goodSuffixShift` to `m`.
    // First, apply Case 2 shifts (prefix of P is suffix of good suffix):
    // `idx = lps[m-1]`. While `idx > 0`: `goodSuffixShift[m-1-idx] = Math.min(goodSuffixShift[m-1-idx], m-idx)`. `idx = lps[idx-1]`.
    // Then, apply Case 1 shifts (good suffix appears elsewhere):
    // For `i` from `0` to `m-1`: `j = lpsRev[i]`. `shift_val = (i+1)-j`. `mismatch_pos = m-1-(i+1)`.
    // If `mismatch_pos >= 0`: `goodSuffixShift[mismatch_pos] = Math.min(goodSuffixShift[mismatch_pos], shift_val)`.

    // A common way to fill goodSuffixShift (often called bmGs):
    // bmGs[i] = shift if mismatch at P[i-1] (good suffix P[i..m-1])
    // Fill with m.
    // 1. Suffix of P is prefix of P
    //    j = lps[m-1]. While j > 0: bmGs[j] = m-j. j = lps[j-1]. (This sets bmGs for suffix lengths that are borders of P)
    //    This means goodSuffixShift[m-j] (mismatch pos) = m-j (shift).
    // 2. Good suffix occurs elsewhere
    //    For i = 1 to m-1: bmGs[m - lpsRev[i-1]] = min(bmGs[m-lpsRev[i-1]], i)
    // This is still tricky to map directly.

    // Let's use the GeeksForGeeks variant for `goodSuffixShift` (called `s` there, but `h` in some texts)
    // `h[i]` stores the starting position of the widest border of the suffix `P[i..m-1]`
    // `shift[i]` is the good suffix shift value if mismatch occurs at `i`.
    // This is also complex.

    // Using a simpler, understandable, and correct construction for `goodSuffixShift[i]`
    // where `i` is the length of the good suffix.
    val shiftsByLength = IntArray(m + 1) { m } // shiftsByLength[len] = shift for good suffix of length len

    // Case 2: Longest prefix of P that is a suffix of P
    var borderLen = lps[m - 1]
    while (borderLen > 0) {
        shiftsByLength[borderLen] = Math.min(shiftsByLength[borderLen], m - borderLen)
        borderLen = lps[borderLen - 1]
    }
    if (lps[m-1] == 0 && m > 0) { // If no border for whole pattern, but pattern not empty
        shiftsByLength[0] = Math.min(shiftsByLength[0], m) // Shift by m if empty good suffix (mismatch at last char)
        // Or for any length if no prefix rule applies.
    }


    // Case 1: Good suffix appears elsewhere
    // Iterate `j` from `m-1` down to `0`. `j` is end index of a prefix of P_reversed.
    // `len_match = lpsRev[j]`.
    // This `len_match` is the length of a suffix of P that matches a prefix of `P[0...m-1-j]`.
    // The suffix is `P[m-len_match ... m-1]`.
    // It matches `P[m-1-j ... m-1-j+len_match-1]`.
    // The good suffix found is `P[m-len_match ... m-1]`. Its length is `len_match`.
    // The shift is `(m-1-j) - (m-len_match) + len_match = len_match - 1 - j`. No.
    // Shift is `(m-len_match) - (m-1-j) = j+1-len_match`.
    // `shiftsByLength[len_match] = min(shiftsByLength[len_match], j + 1 - len_match)`.
    for (jRev in 0 until m) { // jRev is index in reversed pattern
        val lenMatch = lpsRev[jRev] // length of suffix of P that is prefix of P[0...m-1-jRev]
        if (lenMatch > 0) {
            // Good suffix has length `lenMatch`.
            // It occurs starting at `m-1-jRev - lenMatch +1`. No.
            // It occurs ending at `m-1-jRev`.
            // The shift is related to `m - (m-1-jRev) = jRev+1` (length of P_rev prefix)
            // and `lenMatch`. Shift is `(jRev+1) - lenMatch`.
            shiftsByLength[lenMatch] = Math.min(shiftsByLength[lenMatch], (jRev + 1) - lenMatch)
        }
    }

    // Convert shiftsByLength (indexed by length of good suffix) to goodSuffixShift
    // (indexed by mismatch position `i`, where good suffix is `P[i+1...m-1]`)
    // If good suffix has length `len`, then `i = m - 1 - len`.
    for (len in 1 until m) { // len is length of good suffix
        val mismatchPos = m - 1 - len
        if (mismatchPos >= 0) {
            goodSuffixShift[mismatchPos] = Math.min(goodSuffixShift[mismatchPos], shiftsByLength[len])
        }
    }
    // For mismatch at last char of pattern (i=m-1), good suffix length is 0.
    if (m > 0) {
        goodSuffixShift[m-1] = Math.min(goodSuffixShift[m-1], shiftsByLength[0])
        if (shiftsByLength[0] == m && m > 1) { // If no rule for empty suffix, use rule for suffix of length 1
            goodSuffixShift[m-1] = Math.min(goodSuffixShift[m-1], shiftsByLength[1] +1) // Heuristic
        } else if (shiftsByLength[0] == m && m==1) {
            goodSuffixShift[m-1] = 1 // Single char pattern, mismatch, shift by 1
        }
    }


    return goodSuffixShift
}


fun boyerMooreSearch(text: String, pattern: String): List<Int> {
    val n = text.length
    val m = pattern.length
    val foundIndices = mutableListOf<Int>()

    if (m == 0 || m > n) {
        return foundIndices // Empty pattern or pattern longer than text
    }

    val badCharTable = preprocessBadCharacter(pattern, m)
    val goodSuffixShift = preprocessStrongGoodSuffix(pattern, m)

    var s = 0 // s is the alignment shift of the pattern with respect to the text

    while (s <= n - m) {
        var j = m - 1 // Index for pattern (from right to left)

        // Keep reducing index j of pattern while characters of
        // pattern and text are matching at this shift s
        while (j >= 0 && pattern[j] == text[s + j]) {
            j--
        }

        // If the pattern is present at the current shift,
        // then j will become -1 after the above loop
        if (j < 0) {
            foundIndices.add(s)

            // Shift the pattern so that the next character in text
            // aligns with the last occurrence of it in pattern.
            // The condition s+m < n is necessary for the case when
            // pattern occurs at the end of text.
            // Good Suffix shift for a full match:
            // If pattern P = uP', where P' is the longest border of P, shift by |u|.
            // This is m - lps[m-1] if lps is KMP LPS for P.
            // Or, use goodSuffixShift[0] if it's defined for mismatch before P[0].
            // A common shift after a full match is goodSuffixShift[0] (if it means shift for suffix P[1..m-1])
            // or more simply, if pattern is PxP (P is prefix), shift by length of x.
            // Smallest shift to find next match: use goodSuffixShift table.
            // If goodSuffixShift[0] is shift for suffix P[1..m-1]
            // This is complex. A simple rule: shift by goodSuffixShift for suffix of length m-1 (mismatch at P[0])
            // or if pattern is "AAAAA", shift by 1.
            // If using `goodSuffixShift[m-1-len_gs]`, then for full match, `len_gs` is effectively `m`.
            // The `goodSuffixShift` array is indexed by mismatch position.
            // After a match, the "mismatch" is conceptually before the pattern starts.
            // Use the shift for the longest border of P.
            // `shift = m - computeKmpLps(pattern)[m-1]` if `computeKmpLps(pattern)[m-1] > 0` else `m`.
            // Or, simply, if `s + m < n`, use `m - badCharTable[text[s + m].code]` (Horspool-like for next window)
            // but this is not standard BM after match.
            // Standard: shift by `goodSuffixShift[0]` (if `goodSuffixShift` is indexed by `i` where `P[i+1..m-1]` is good suffix).
            // Our goodSuffixShift is indexed by `i` (mismatch at `P[i]`).
            // For a full match, the "mismatch" is before P[0].
            // Let's use a safe shift: if m > 0, `goodSuffixShift[0]` (if it's populated for suffix P[1..m-1]) or m.
            // The `goodSuffixShift` as computed might not be ideal for this.
            // Smallest shift is `m - lps[m-1]` if `lps = computeKmpLps(pattern)`.
            val lps = computeKmpLps(pattern)
            val shiftAfterMatch = if (m > 1 && lps[m-1] > 0) m - lps[m-1] else 1 // Shift by period or 1
            s += shiftAfterMatch


        } else {
            // Mismatch occurred at pattern[j] and text[s+j]
            val badCharShiftValue = j - badCharTable[text[s + j].code]

            // goodSuffixShift[j] is the shift if mismatch at pattern[j]
            // (good suffix is pattern[j+1...m-1])
            val goodSuffixShiftValue = if (j < m -1) goodSuffixShift[j] else m
            // If j == m-1 (mismatch at last char), good suffix is empty.
            // Use a default shift or a specific rule for empty good suffix.
            // The `goodSuffixShift[m-1]` should handle this.

            s += Math.max(1, Math.max(badCharShiftValue, goodSuffixShiftValue))
            // Ensure shift is at least 1 to avoid infinite loops.
            // The goodSuffixShiftValue should already be >=1.
            // badCharShiftValue can be negative if badCharTable[text[s+j].code] > j.
            // So, `max(1, badCharRuleShift)` is important.
            // `shift = max(goodSuffixRule(j), badCharRule(text[s+j], j))`
            // `badCharRule(char, j_mismatch) = max(1, j_mismatch - badCharTable[char])`
            // `goodSuffixRule(j_mismatch) = goodSuffixShift[j_mismatch]` (where good suffix is P[j_mismatch+1...])

            val bcShift = if (badCharTable[text[s + j].code] != -1) {
                Math.max(1, j - badCharTable[text[s + j].code])
            } else {
                j + 1 // Character not in pattern, shift past it
            }

            val gsShift = goodSuffixShift[j] // goodSuffixShift[j] is for mismatch at P[j]

            s += Math.max(bcShift, gsShift)
        }
    }
    return foundIndices
}


fun main() {
    val text = "AABAACAADAABAABA"
    val pattern = "AABA"

    println("Text: $text")
    println("Pattern: $pattern")

    val indices = boyerMooreSearch(text, pattern)

    if (indices.isNotEmpty()) {
        val resultString = indices.joinToString(", ")
        println("Pattern found at index $resultString")
    } else {
        println("Pattern not found.")
    }
}
