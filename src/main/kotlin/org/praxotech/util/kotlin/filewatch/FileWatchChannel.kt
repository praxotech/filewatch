package org.praxotech.util.kotlin.filewatch

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.nio.file.*

@ExperimentalCoroutinesApi
class FileWatchChannel (
    val file: Path,
    private var channel: Channel<WatchEvent<Path>> = Channel()
) : Channel<WatchEvent<Path>> by channel