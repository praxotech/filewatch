package org.praxotech.util.kotlin.filewatch

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import java.io.File
import java.nio.file.*

fun main() {
  FileWatchService.INSTANCE.use {
    val tempDir = createTempDir(directory = File(System.getProperty("user.home"))).toPath()
    val file1 = Paths.get(tempDir.toString(), "test1")
    val file2 = Paths.get(tempDir.toString(), "test2")

    val channel1 = it.register(file1)
    val channel2 = it.register(file2)

    var job1: Job? = null
    if (channel1 != null) {
      job1 = GlobalScope.launch(Dispatchers.IO) {
        channel1.consumeEach {
          println("Watcher1: Event ${it.kind()} happened on file ${it.context()}")
        }
      }
    }

    var job2: Job? = null
    if (channel2 != null) {
      job2 = GlobalScope.launch(Dispatchers.IO) {
        channel2.consumeEach {
          println("Watcher2: Event ${it.kind()} happened on file ${it.context()}")
        }
      }
    }

    runBlocking {
      delay(1000)

      Files.createFile(file2)
      Files.createFile(file1)
      Files.newBufferedWriter(file1).use {
        it.write("test1")
        it.flush()
      }
      Files.delete(file1)
      Files.delete(file2)

      delay(1000)

      job1?.cancel()
      job2?.cancel()

      Files.deleteIfExists(tempDir)
    }
  }
}
