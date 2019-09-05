package org.praxotech.util.kotlin.filewatch

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Closeable
import java.nio.file.*

class RxFileWatchService private constructor(): Closeable {
  companion object {
    var INSTANCE = RxFileWatchService()
  }

  private val watchService = FileSystems.getDefault().newWatchService()
  private val emitterMap = mutableMapOf<WatchKey, MutableList<FileEventEmitter>>()
  private val watchJob: Job

  init {
    watchJob = GlobalScope.launch(Dispatchers.IO) {
      while (true) {
        val eventKey = watchService.take()
        val emitters = emitterMap.get(eventKey)
        if (emitters != null) {
          eventKey.pollEvents().forEach {
            emitters.filter {
              e -> e.file.fileName.toString() == it.context().toString()
            }.forEach { e -> e.onNext(it as WatchEvent<Path>) }
          }
        }

        if (!eventKey.reset()) {
          eventKey.cancel()
          emitterMap.remove(eventKey)
        }
      }
    }
  }

  fun register(file: Path): Flowable<WatchEvent<Path>>? {
    if (Files.isDirectory(file.parent)) {
      val watchKey = file.parent.register(watchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_DELETE)
      val emitters = emitterMap.getOrPut(watchKey, { mutableListOf() })
      return Flowable.create<WatchEvent<Path>>(
          {
            emitter ->  emitters.add(FileEventEmitter(file, FileEventEmitter(file, emitter)))
          },
          BackpressureStrategy.LATEST
      )
    } else {
      TODO("Log exception of no registration")
      return null
    }
  }

  override fun close() {
    watchJob.cancel(null)
    emitterMap.keys.forEach { it.cancel() }
    watchService.close()
  }

}