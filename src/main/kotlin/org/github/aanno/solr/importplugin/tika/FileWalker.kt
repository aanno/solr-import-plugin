package org.github.aanno.solr.importplugin.tika

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

class FileWalker(val base: Path, val pattern: Pattern) {

    suspend fun walk(channel: Channel<Path>, action: (path: Path) -> Unit) = coroutineScope {
        Files.walk(base, FileVisitOption.FOLLOW_LINKS).use { it ->
            it.forEach { p ->
                launch {
                    channel.send(p)
                }
            }
            launch {
                channel.close()
            }
        }
        for (p in channel) {
            launch {
                action(p)
            }
        }
    }
}

fun main(args: Array<String>) = runBlocking<Unit> {
    // DebugProbes.install()
    val fw = FileWalker(Paths.get("/home2/tpasch/java/solr-8.3.1/example/exampledocs/"), Pattern.compile("."))
    val channel = Channel<Path>()
    val job = withContext(Dispatchers.Default) {
        // DebugProbes.dumpCoroutines()
        val innerJob = fw.walk(channel, ::println)
    }
    // DebugProbes.dumpCoroutines()
    // DebugProbes.dumpCoroutines()
}

