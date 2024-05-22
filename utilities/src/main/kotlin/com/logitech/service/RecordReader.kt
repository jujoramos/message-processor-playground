package com.logitech.service

import com.logitech.model.Record
import java.io.InputStreamReader

/**
 * Interface for reading records, one at a time.
 */
interface RecordReader {
	/**
	 * Returns an iterator over the records in the input stream.
	 *
	 * @param streamReader The [InputStreamReader] to read objects from.
	 * @return An [Iterator] over the records in the input stream.
	 */
	fun iterator(streamReader: InputStreamReader): Iterator<Record>
}
