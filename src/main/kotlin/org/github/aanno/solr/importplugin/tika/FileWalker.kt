package org.github.aanno.solr.importplugin.tika

import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.nio.file.Paths

class FileWalker(val base: Path, val pattern: Pattern) {

    suspend fun walk(action: (path: Path) -> Unit) {
        withContext(Dispatchers.Default) {
            val channel = Channel<Path>()
            Files.walk(base, FileVisitOption.FOLLOW_LINKS).use { it ->
                try {
                    it.forEach { p ->
                        launch {
                            channel.send(p)
                        }
                    }
                } finally {
                    channel.close()
                }
            }
            launch {
                for (p in channel) {
                    action(p)
                }
            }
        }
    }
}

fun main(args: Array<String>) = runBlocking {
    val fw = FileWalker(Paths.get("/home2/tpasch/java/solr-8.3.1/example/exampledocs/"), Pattern.compile("."))
    withContext(Dispatchers.Default) {
        fw.walk(::println)
    }
}

