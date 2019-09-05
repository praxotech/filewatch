package org.praxotech.util.kotlin.filewatch

import io.reactivex.FlowableEmitter
import java.nio.file.Path
import java.nio.file.WatchEvent

class FileEventEmitter(
    val file: Path,
    private val emitter: FlowableEmitter<WatchEvent<Path>>
) : FlowableEmitter<WatchEvent<Path>> by emitter