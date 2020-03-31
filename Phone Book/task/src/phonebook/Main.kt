package phonebook

import java.io.File
import java.lang.System.currentTimeMillis
import java.util.concurrent.Callable
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

data class Entry(val name: String, val phone: String)

val directory = mutableListOf<Entry>()
val find = mutableListOf<String>()
var sorted = false

class BubbleSort: Callable<Long> {
    override fun call(): Long {
        return measureTimeMillis {
            for (i in directory.indices) {
                for (j in 0 until directory.size - (i + 1)) {
                    if (directory[j].name > directory[j + 1].name) {
                        val temp = directory[j]
                        directory[j] = directory[j + 1]
                        directory[j + 1] = temp
                    }
                }
            }
        }
    }
}

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
    val result = linearSearch()
    printFoundTime(result.first, result.second, result.third)
    return result.third
}

private fun linearSearch(): Triple<Int, Int, Long> {
    var found = 0
    var entries = 0
    val executionTime = measureTimeMillis {
        find.forEach {
            for (i in directory.indices) {
                if (directory[i].name == it) {
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
    val executionTime = if (sorted) 0 else bubbleSort(timeout)
    if (executionTime > timeout) {
        val result = linearSearch()
        val linearTime = result.third
        printFoundTime(result.first, result.second, result.third + executionTime)
        println("Sorting time: ${getTimeString(executionTime)} - STOPPED, moved to linear search")
        println("Searching time: ${getTimeString(linearTime)}")
    } else {
        sorted = true
        val result = jumpSearch()
        printFoundTime(result.first, result.second, result.third + executionTime)
        println("Sorting time: ${getTimeString(executionTime)}")
        println("Searching time: ${getTimeString(result.third)}")
    }
}

private fun jumpSearch(): Triple<Int, Int, Long> {
    var found = 0
    var entries = 0
    val executionTime = measureTimeMillis {
        val jump = sqrt(directory.size.toDouble()).toInt()
        find.forEach {
            var index = 0
            if (directory[index].name == it) {
                found++
            } else if (directory[index].name < it) {
                var prev = index + 1
                index += jump
                loop@ while (index < directory.size && directory[prev - 1].name < it) {
                    for (i in index downTo prev) {
                        if (directory[i].name == it) {
                            found++
                            break@loop
                        }
                    }
                    prev = index + 1
                    index += jump
                }
                if (directory[prev - 1].name < it && index > directory.size - 1) {
                    for (i in directory.size - 1 downTo prev) {
                        if (directory[i].name == it) {
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

private fun bubbleSort(timeout: Long): Long {
    return measureTimeMillis {
        val start = currentTimeMillis()
        var n = directory.size
        var swapped = true
        while (swapped) {
            swapped = false
            for (i in 1 until n) {
                if (directory[i - 1].name > directory[i].name) {
                    val temp = directory[i]
                    directory[i] = directory[i - 1]
                    directory[i - 1] = temp
                    swapped = true
                }
            }
            n--
            if (timeout > 0 && currentTimeMillis() - start > timeout) break
        }
    }
}

fun main() {
    loadDirectory()
    loadFind()
    val linearTime = doLinear()
    println()
    bubbleJump(linearTime * 10)
}
