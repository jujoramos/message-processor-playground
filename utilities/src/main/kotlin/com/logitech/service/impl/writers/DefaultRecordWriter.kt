package com.logitech.service.impl.writers

import com.logitech.model.Record
import com.logitech.service.RecordWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Implementation of the [RecordWriter] interface for writing [Record] objects to an output stream.
 */
class DefaultRecordWriter(
	outputStream: OutputStream,
) : RecordWriter {
	private val writer = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)

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
