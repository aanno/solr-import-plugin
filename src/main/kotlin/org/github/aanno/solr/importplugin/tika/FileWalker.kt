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
        val it = Files.walk(base, FileVisitOption.FOLLOW_LINKS)
        launch {
            for (p in it) {
                channel.send(p)
            }
            channel.close()
        }
        // force a few coroutine workers
        repeat(4) { i ->
            launch {
                for (p in channel) {
                    action(p)
                }
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

