package com.logitech.service.impl

import com.logitech.service.Processor
import com.logitech.service.RecordReader
import com.logitech.service.RecordWriter

/**
 * Default implementation of the [Processor] interface.
 * It reads records from a provided [RecordReader] and writes them using a provided [RecordWriter], one by one.
 */
class StreamProcessor : Processor {
	/**
	 * Processes records from a provided [RecordReader] and writes them using a provided [RecordWriter].
	 *
	 * @param recordReader The [RecordReader] used to read records.
	 * @param recordWriter The [RecordWriter] used to write records.
	 */
	override fun process(
		recordReader: RecordReader,
		recordWriter: RecordWriter,
	) {
		recordReader.iterator().forEach { record ->
			recordWriter.write(record)
		}
	}
}
