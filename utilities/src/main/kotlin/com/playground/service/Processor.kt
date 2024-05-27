package com.playground.service

/**
 * Interface for processing records.
 */
interface Processor {
	/**
	 * Processes records using the provided [RecordReader] and [RecordWriter].
	 *
	 * @param recordReader The [RecordReader] used to read records.
	 * @param recordWriter The [RecordWriter] used to write records.
	 */
	fun process(
		recordReader: RecordReader,
		recordWriter: RecordWriter,
	)
}
