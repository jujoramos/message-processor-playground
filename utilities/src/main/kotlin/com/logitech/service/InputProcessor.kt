package com.logitech.service

import java.io.InputStreamReader

/**
 * Interface for processing input data.
 */
interface InputProcessor {
	/**
	 * Processes input data from an [InputStreamReader] and transforms it using the configured [RecordTransformer].
	 *
	 * @param reader The InputStreamReader to read the input data from.
	 * @param transformer The RecordTransformer to use for transforming the input data.
	 */
	fun process(
		reader: InputStreamReader,
		transformer: RecordTransformer,
	)
}
