package com.logitech.service.impl

import com.logitech.service.InputProcessor
import com.logitech.service.RecordReader
import com.logitech.service.RecordWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Implementation of the [InputProcessor] interface.
 *
 * @property recordReader The [RecordReader] used to read records from the input stream.
 * @property recordWriter The [RecordWriter] used to write records.
 */
class DefaultInputProcessor(
	private val recordReader: RecordReader,
	private val recordWriter: RecordWriter,
) : InputProcessor {
	/**
	 * Processes the given input stream.
	 *
	 * This method reads records from the input stream using the [RecordReader], and writes them using the [RecordWriter].
	 * If the input stream is empty, an [IllegalArgumentException] is thrown.
	 *
	 * @param inputStream The [InputStream] to process.
	 * @throws IllegalArgumentException If the input stream is empty.
	 */
	override fun process(inputStream: InputStream) {
		require(inputStream.available() != 0) { "Input stream cannot be empty" }

		InputStreamReader(inputStream, StandardCharsets.UTF_8).use {
			recordReader.iterator(it).forEach { record ->
				recordWriter.write(record)
			}
		}
	}
}
