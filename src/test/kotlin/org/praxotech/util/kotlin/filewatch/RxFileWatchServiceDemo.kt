package org.praxotech.util.kotlin.filewatch

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import java.nio.file.*

fun main() {
  RxFileWatchService.INSTANCE.use {
    val dir = Paths.get("/home", "fyang", "swdev", "temp")
    val file1 = Paths.get(dir.toString(), "test1")
    val file2 = Paths.get(dir.toString(), "test2")
    val flowable1 = it.register(file1)
    val flowable2 = it.register(file2)

    flowable1?.subscribe {
      e -> println("Obs1: Event ${e.kind()} happened on file ${e.context()} in directory $dir")
    }

    flowable2?.subscribe {
      e -> println("Obs2: Event ${e.kind()} happened on file ${e.context()} in directory $dir")
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
  }
}