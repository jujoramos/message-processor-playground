package com.logitech.service

import com.logitech.model.Record

/**
 * Functional interface for transforming records.
 */
fun interface RecordTransformer {
	/**
	 * Transforms the given record.
	 *
	 * @param record The [Record] object to be transformed.
	 */
	fun transform(record: Record)
}
