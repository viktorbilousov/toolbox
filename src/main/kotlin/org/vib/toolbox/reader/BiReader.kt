package org.vib.toolbox.reader

import java.io.Reader

abstract class BiReader: BiDirectionalReader, Reader {
    protected constructor() : super()
    protected constructor(lock: Any) : super(lock)
}