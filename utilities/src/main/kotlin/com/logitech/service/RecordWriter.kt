package com.logitech.service

import com.logitech.model.Record

/**
 * Functional interface for writing [Record] objects.
 */
fun interface RecordWriter {
	/**
	 * Writes the given record.
	 *
	 * @param record The [Record] to write.
	 */
	fun write(record: Record)
}
