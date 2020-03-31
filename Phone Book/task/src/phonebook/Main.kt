package phonebook

import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.measureTimeMillis

data class Entry(val name: String, val phone: String)

val directory = mutableListOf<Entry>()
val find = mutableListOf<String>()

class BubbleSort: Callable<Long> {
    override fun call(): Long {
        return measureTimeMillis {
            bubbleSort()
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
    val executor: ExecutorService = Executors.newSingleThreadExecutor()
    val future: Future<Long> = executor.submit(BubbleSort())
    try {
        val executionTime = future.get(timeout, TimeUnit.MILLISECONDS)
        println(executionTime)
    } catch (e: TimeoutException) {
        println("STOPPED, moved to linear search")
        future.cancel(true)
    }
    executor.shutdownNow()
}

private fun bubbleSort() {
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

fun main() {
    // loadDirectory("/Users/professional/Downloads/directory copy.txt")
    loadDirectory()
    loadFind()
    println("Start searching (linear search)...")
    val result = linearSearch()
    printFoundTime(result.first, result.second, result.third)
    println()
    println("Start searching (bubble sort + jump search)...")
    bubbleJump(result.third * 10)
    println(directory)
}
