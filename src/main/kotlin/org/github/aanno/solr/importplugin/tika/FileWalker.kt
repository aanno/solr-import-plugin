package org.github.aanno.solr.importplugin.tika

import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.nio.file.Paths

class FileWalker(val base: Path, val pattern: Pattern) {

    fun walk() {
        val channel = Channel<Path>()
        Files.walk(base, FileVisitOption.FOLLOW_LINKS).use { it ->
            it.forEach { p ->
                GlobalScope.launch {
                    channel.send(p)
                }
            }
        }
        GlobalScope.launch {
            for (p in channel) {
                println(p)
            }
        }
    }
}

fun main(args: Array<String>) {
    val fw = FileWalker(Paths.get("/home2/tpasch/java/solr-8.3.1/example/exampledocs/"), Pattern.compile("."))
    fw.walk()
    runBlocking {     // but this expression blocks the main thread
        delay(20000L)  // ... while we delay for 2 seconds to keep JVM alive
    }
}

