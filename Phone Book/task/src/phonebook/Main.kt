package phonebook

import java.io.File
import java.lang.Exception
import java.lang.System.currentTimeMillis
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

data class Entry(val name: String, val phone: String)

val directory = mutableListOf<Entry>()
val find = mutableListOf<String>()

private fun loadDirectory(filename: String = "/Users/professional/Downloads/directory.txt") {
    File(filename).forEachLine {
        if (it.isNotBlank()) {
            val tokens = it.split(" ", limit = 2)
            directory.add(Entry(tokens[1], tokens[0]))
        }
    }
}

private fun loadFind(filename: String = "/Users/professional/Downloads/find.txt") {
    File(filename).forEachLine {
        if (it.isNotBlank()) {
            find.add(it)
        }
    }
}

private fun doLinear(): Long {
    println("Start searching (linear search)...")
    val mutable = mutableListOf<Entry>()
    mutable.addAll(directory)
    val result = linearSearch(mutable)
    printFoundTime(result.first, result.second, result.third)
    return result.third
}

private fun linearSearch(mutable: MutableList<Entry>): Triple<Int, Int, Long> {
    var found = 0
    var entries = 0
    val executionTime = measureTimeMillis {
        find.forEach {
            for (i in mutable.indices) {
                if (mutable[i].name == it) {
                    found++
                    break
                }
            }
            entries++
        }
    }
    return Triple(found, entries, executionTime)
}

private fun printFoundTime(found: Int, entries: Int, executionTime: Long) {
    println("Found $found / $entries entries. Time taken: ${getTimeString(executionTime)}")
}

private fun getTimeString(executionTime: Long): String {
    val ms = executionTime % 1000
    val sec = executionTime / 1000 % 60
    val min = executionTime / 1000 / 60
    return "$min min. $sec sec. $ms ms."
}

private fun bubbleJump(timeout: Long) {
    println("Start searching (bubble sort + jump search)...")
    val mutable = mutableListOf<Entry>()
    mutable.addAll(directory)
    val executionTime = measureTimeMillis { bubbleSort(mutable, timeout) }
    if (executionTime > timeout) {
        val result = linearSearch(mutable)
        val linearTime = result.third
        printFoundTime(result.first, result.second, result.third + executionTime)
        println("Sorting time: ${getTimeString(executionTime)} - STOPPED, moved to linear search")
        println("Searching time: ${getTimeString(linearTime)}")
    } else {
        val result = jumpSearch(mutable)
        printFoundTime(result.first, result.second, result.third + executionTime)
        println("Sorting time: ${getTimeString(executionTime)}")
        println("Searching time: ${getTimeString(result.third)}")
    }
}

private fun jumpSearch(mutable: MutableList<Entry>): Triple<Int, Int, Long> {
    var found = 0
    var entries = 0
    val executionTime = measureTimeMillis {
        val jump = sqrt(mutable.size.toDouble()).toInt()
        find.forEach {
            var index = 0
            if (mutable[index].name == it) {
                found++
            } else if (mutable[index].name < it) {
                var prev = index + 1
                index += jump
                loop@ while (index < mutable.size && mutable[prev - 1].name < it) {
                    for (i in index downTo prev) {
                        if (mutable[i].name == it) {
                            found++
                            break@loop
                        }
                    }
                    prev = index + 1
                    index += jump
                }
                if (mutable[prev - 1].name < it && index > mutable.size - 1) {
                    for (i in mutable.size - 1 downTo prev) {
                        if (mutable[i].name == it) {
                            found++
                        }
                    }
                }
            }
            entries++
        }
    }
    return Triple(found, entries, executionTime)
}

private fun bubbleSort(mutable: MutableList<Entry>, timeout: Long) {
    val start = currentTimeMillis()
    var n = mutable.size
    var swapped = true
    while (swapped) {
        swapped = false
        for (i in 1 until n) {
            if (mutable[i - 1].name > mutable[i].name) {
                val temp = mutable[i]
                mutable[i] = mutable[i - 1]
                mutable[i - 1] = temp
                swapped = true
            }
        }
        n--
        if (timeout > 0 && currentTimeMillis() - start > timeout) break
    }
}

private fun quickBinary(timeout: Long) {
    println("Start searching (quick sort + binary search)...")
    val mutable = mutableListOf<Entry>()
    mutable.addAll(directory)
    val executionTime = measureTimeMillis {
        try {
            quickSort(mutable, 0, mutable.size - 1, timeout, currentTimeMillis())
        } catch (e: Exception) {
            // Timeout triggered
        }
    }
    if (executionTime > timeout) {
        val result = linearSearch(mutable)
        val linearTime = result.third
        printFoundTime(result.first, result.second, result.third + executionTime)
        println("Sorting time: ${getTimeString(executionTime)} - STOPPED, moved to linear search")
        println("Searching time: ${getTimeString(linearTime)}")
    } else {
        val result = binarySearch(mutable)
        printFoundTime(result.first, result.second, result.third + executionTime)
        println("Sorting time: ${getTimeString(executionTime)}")
        println("Searching time: ${getTimeString(result.third)}")
    }
}

private fun binarySearch(mutable: MutableList<Entry>): Triple<Int, Int, Long> {
    var found = 0
    var entries = 0
    val executionTime = measureTimeMillis {
        find.forEach {
            var l = 0
            var r = mutable.size - 1
            while (l != r) {
                val m = ceil((l + r).toDouble() / 2).toInt()
                if (mutable[m].name > it)
                    r = m - 1
                else
                    l = m
            }
            if (mutable[l].name == it)
                found++
            entries++
        }
    }
    return Triple(found, entries, executionTime)
}

private fun quickSort(mutable: MutableList<Entry>, lo: Int, hi: Int, timeout: Long, start: Long) {
    if (lo < hi) {
        val p = partition(mutable, lo, hi)
        if (timeout > 0 && currentTimeMillis() - start > timeout) throw Exception("Timeout")
        quickSort(mutable, lo, p - 1, timeout, start)
        quickSort(mutable, p + 1, hi, timeout, start)
    }
}

private fun partition(mutable: MutableList<Entry>, lo: Int, hi: Int): Int {
    val pivot = mutable[hi].name
    var i = lo
    for (j in lo until hi) {
        if (mutable[j].name < pivot) {
            val temp = mutable[i]
            mutable[i] = mutable[j]
            mutable[j] = temp
            i++
        }
    }
    val temp = mutable[i]
    mutable[i] = mutable[hi]
    mutable[hi] = temp
    return i
}

private fun doHash() {
    println("Start searching (hash table)...")
    val table = HashTable()
    val executionTime = measureTimeMillis { table.addAll(directory) }
    val result = hashSearch(table)
    printFoundTime(result.first, result.second, result.third + executionTime)
    println("Creating time: ${getTimeString(executionTime)}")
    println("Searching time: ${getTimeString(result.third)}")
}

private fun hashSearch(table: HashTable): Triple<Int, Int, Long> {
    var found = 0
    var entries = 0
    val executionTime = measureTimeMillis {
        find.forEach {
            if (table.search(it)) found++
            entries++
        }
    }
    return Triple(found, entries, executionTime)
}

fun main() {
    loadDirectory()
    loadFind()
    val linearTime = doLinear()
    println()
    bubbleJump(linearTime * 10)
    println()
    quickBinary(linearTime * 10)
    println()
    doHash()
}

class HashTable {
    private val m = 999983
    private val bucket1 = arrayOfNulls<Entry>(m)
    private val bucket2 = arrayOfNulls<Entry>(m)

    private fun add(entry: Entry) {
        val hashKey = hash(entry.name)
        if (bucket1[hashKey] == null)
            bucket1[hashKey] = entry
        else
            bucket2[hashKey] = entry
    }

    fun addAll(list: List<Entry>) {
        list.forEach { add(it) }
    }

    private fun hash(key: String): Int {
        val r = 31
        var hash = 0
        for (element in key)
            hash = (r * hash + element.toInt()) % m
        return hash
    }

    fun search(name: String): Boolean {
        val hashKey = hash(name)
        if (bucket1[hashKey] != null || bucket2[hashKey] != null)
            return true
        return false
    }
}
