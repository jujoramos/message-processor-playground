package com.logitech.service

import com.logitech.model.Record

/**
 * Interface for reading records.
 */
interface RecordReader {
	/**
	 * Returns an iterator over the available [Record] objects.
	 *
	 * @return An [Iterator] over the available [Record] objects.
	 */
	fun iterator(): Iterator<Record>
}
