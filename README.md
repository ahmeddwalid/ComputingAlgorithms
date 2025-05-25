# String Matching Algorithms in Kotlin

This repository contains Kotlin implementations of fundamental and advanced string matching algorithms. These algorithms are designed to locate all occurrences of a given pattern within a larger text, using different algorithmic strategies to optimize for time efficiency and reduced redundancy.

## Algorithms Implemented

### 1. Naïve String Matching

**File:** `Naive.kt`
**Description:**
A brute-force approach that attempts to match the pattern at every position in the text.

**Mechanism:**

* Compares the pattern to every substring of the text of the same length.
* If all characters match, the index is recorded.
* Moves one position forward and repeats.

**Time Complexity:**

* Worst-case: O(n \* m), where `n` is the length of the text and `m` is the length of the pattern.

---

### 2. Knuth-Morris-Pratt (KMP)

**File:** `KMP.kt`
**Description:**
Optimizes matching by precomputing a prefix table (`LPS` array) to avoid redundant comparisons.

**Mechanism:**

* Preprocesses the pattern to compute the LPS (Longest Prefix Suffix) array.
* During matching, avoids rechecking characters already matched.
* Skips ahead in the pattern using LPS when mismatches occur.

**Time Complexity:**

* Preprocessing: O(m)
* Searching: O(n)
* Total: O(n + m)

---

### 3. Boyer-Moore

**File:** `BoyerMoore.kt`
**Description:**
One of the most efficient practical algorithms. It compares the pattern from right to left and skips sections using two heuristics.

**Mechanism:**

* **Bad Character Rule:** Shifts the pattern to align the mismatched character with its last occurrence in the pattern.
* **Good Suffix Rule:** Uses previously matched suffixes to compute the optimal shift.
* Applies the maximum of the two shifts for efficiency.

**Time Complexity:**

* Preprocessing: O(m + |Σ|), where |Σ| is the alphabet size.
* Worst-case: O(n + m)
* Average-case: Sublinear in practice.

---

### 4. Rabin-Karp

**File:** `RabinKarp.kt`
**Description:**
Uses hashing to compare the pattern with text substrings efficiently, verifying matches only when hash values match.

**Mechanism:**

* Computes hash for the pattern and rolling hash for text windows.
* On hash match, performs character-by-character verification to handle collisions.
* Uses modulo arithmetic and polynomial rolling hash for speed and accuracy.

**Time Complexity:**

* Preprocessing: O(m)
* Average-case: O(n + m)
* Worst-case: O(n \* m) (due to hash collisions)

---

## How to Run

Each Kotlin file contains a `main()` function demonstrating the algorithm with the following test case:

* **Text:** `AABAACAADAABAABA`
* **Pattern:** `AABA`
* **Expected Output:**

  ```
  Pattern found at index 0, 9, 12
  ```

### Run via Command Line

```bash
kotlinc KMP.kt -include-runtime -d KMP.jar
java -jar KMP.jar
```

### Run via IntelliJ IDEA

1. Open the Kotlin file in IntelliJ.
2. Right-click the file and select "Run".

---

## License

This published under the MIT license.
