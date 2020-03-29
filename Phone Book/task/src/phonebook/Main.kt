package phonebook

import java.io.File
import kotlin.system.measureTimeMillis

val directory = mutableMapOf<String, Int>()
val find = mutableListOf<String>()

private fun loadDirectory(filename: String = "/Users/professional/Downloads/directory.txt") {
    File(filename).forEachLine {
        if (it.isNotBlank()) {
            val tokens = it.split(" ", limit = 2)
            directory[tokens[1]] = tokens[0].toInt()
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

private fun linearSearch() {
    var found = 0
    var entries = 0
    println("Start searching...")
    val names = directory.keys.toList()
    val executionTime = measureTimeMillis {
        find.forEach {
            for (i in names.indices) {
                if (names[i] == it) {
                    found++
                    break
                }
            }
            entries++
        }
    }
    val ms = executionTime % 1000
    val sec = executionTime / 1000 % 60
    val min = executionTime / 1000 / 60
    println("Found $found / $entries entries. Time taken: $min min. $sec sec. $ms ms.")
}

fun main() {
    loadDirectory()
    loadFind()
    linearSearch()
}
