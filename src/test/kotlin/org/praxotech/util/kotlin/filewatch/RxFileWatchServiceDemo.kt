package org.praxotech.util.kotlin.filewatch

import kotlinx.coroutines.*
import java.io.File
import java.nio.file.*

fun main() {
  RxFileWatchService.INSTANCE.use {
    val tempDir = createTempDir(directory = File(System.getProperty("user.home"))).toPath()
    val file1 = Paths.get(tempDir.toString(), "test1")
    val file2 = Paths.get(tempDir.toString(), "test2")
    val flowable1 = it.register(file1)
    val flowable2 = it.register(file2)

    flowable1?.subscribe {
      e -> println("Obs1: Event ${e.kind()} happened on file ${e.context()} in directory $tempDir")
    }

    flowable2?.subscribe {
      e -> println("Obs2: Event ${e.kind()} happened on file ${e.context()} in directory $tempDir")
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
    }

    Files.deleteIfExists(tempDir)
  }
}
