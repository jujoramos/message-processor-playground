package com.logitech.service.impl.writers

import com.logitech.model.Record
import com.logitech.service.RecordWriter
import java.io.Writer

/**
 * Implementation of the [RecordWriter] interface for writing [Record] objects through the configured [Writer].
 */
class DefaultRecordWriter(
	private val writer: Writer,
) : RecordWriter {
	/**
	 * Writes the given record to the output stream.
	 *
	 * @param record The [Record] to write.
	 */
	override fun write(record: Record) {
		writer.write("----------- ${record.sequence} -----------\n")
		writer.write("${record.message}\n")
		writer.flush()
	}
}
