package com.logitech.service.impl.readers

import com.logitech.model.Record
import com.logitech.service.RecordReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer.wrap
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.nio.charset.StandardCharsets

/**
 * Implementation of the [RecordReader] interface for reading [Record] objects.
 *
 * This class is not thread-safe and should not be shared between threads.
 *
 * @property inputStream Where to read objects from.
 * @property headerSize The size of the header in bytes.
 * @property sequenceNumberSize The size of the sequence number in bytes.
 *
 */
class DefaultRecordReader(
	inputStream: InputStream,
	private val headerSize: Int,
	private val sequenceNumberSize: Int,
) : RecordReader {
	private val metadataSize = headerSize + sequenceNumberSize
	private val inputStreamReader = InputStreamReader(inputStream, StandardCharsets.UTF_8)

	/**
	 * Reads a [Record] from the given [metadata] and [InputStreamReader].
	 *
	 * @param metadata The metadata of the record, as a [ByteArray].
	 * @return The parsed [Record].
	 * @throws IllegalArgumentException If the payload, or sequence, number is invalid.
	 */
	fun read(metadata: ByteArray): Record {
		// TODO: handle errors more gracefully.
		val payloadHeader = wrap(metadata, 0, headerSize).order(LITTLE_ENDIAN).int
		require(payloadHeader > 0) { "Invalid payload header: $payloadHeader" }

		// Read & validate sequence number
		val sequenceNumber = wrap(metadata, headerSize, sequenceNumberSize).order(LITTLE_ENDIAN).int
		require(sequenceNumber >= 0) { "Invalid sequence number: $sequenceNumber" }

		// Read the actual message using the specified size
		val messageBuffer = CharArray(payloadHeader)
		inputStreamReader.read(messageBuffer)

		return Record(sequenceNumber, String(messageBuffer))
	}

	/**
	 * Returns an [Iterator] over the records in the internal [InputStreamReader].
	 *
	 * @return An [Iterator] over the records in internal [InputStreamReader].
	 */
	override fun iterator(): Iterator<Record> {
		return object : Iterator<Record> {
			private var nextRecord: Record? = null
			private val buffer = CharArray(metadataSize)

			init {
				loadNextRecord()
			}

			private fun loadNextRecord() {
				if (inputStreamReader.read(buffer) == metadataSize) {
					val byteArray = buffer.joinToString("").toByteArray()
					nextRecord = read(byteArray)
				} else {
					nextRecord = null
				}
			}

			override fun hasNext(): Boolean {
				return nextRecord != null
			}

			override fun next(): Record {
				val record = nextRecord ?: throw NoSuchElementException()
				loadNextRecord()

				return record
			}
		}
	}
}
