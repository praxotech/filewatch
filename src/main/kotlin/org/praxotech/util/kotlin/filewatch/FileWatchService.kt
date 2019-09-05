package org.praxotech.util.kotlin.filewatch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Closeable
import java.nio.file.*

class FileWatchService private constructor(): Closeable {
  companion object {
    var INSTANCE = FileWatchService()
  }

  private val watchService = FileSystems.getDefault().newWatchService()
  private val channelMap = mutableMapOf<WatchKey, MutableList<FileWatchChannel>>()
  private val watchJob: Job

  init {
    watchJob = GlobalScope.launch(Dispatchers.IO) {
      while (true) {
        val eventKey = watchService.take()
        val channels = channelMap.get(eventKey)
        if (channels != null) {
          eventKey.pollEvents().forEach {
            channels.filter {
              c -> c.file.fileName.toString() == it.context().toString()
            }.forEach { c -> c.send(it as WatchEvent<Path>) }
          }
        }

        if (!eventKey.reset()) {
          eventKey.cancel()
          channelMap.remove(eventKey)
        }
      }
    }
  }

  /**
   * This method register the input parameter <i>file</i> to the
   * service. It returns the channel through which the service sends
   * the change events of the file.
   *
   * @param the file to be watched.
   * @return a channel through which the change events of the file is
   * sent. A null is returned if the input parameter is not a valid
   * file path
   */
  fun register(file: Path): FileWatchChannel? {
    if (Files.isDirectory(file.parent)) {
      val watchKey = file.parent.register(watchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_DELETE)
      val channels = channelMap.getOrPut(watchKey, { mutableListOf() })
      var channel = FileWatchChannel(file)
      channels.add(channel)
      return channel
    } else {
      TODO("Log exception of no registration")
      return null
    }
  }

  override fun close() {
    watchJob.cancel(null)
    channelMap.keys.forEach { it.cancel() }
    watchService.close()
  }

}