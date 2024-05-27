package com.playground.service.impl.readers

import com.playground.model.Record
import com.playground.service.RecordReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer.wrap
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.nio.charset.StandardCharsets

/**
 * Implementation of the [RecordReader] interface for reading [Record] objects from an [InputStream].
 * The implementation is not tolerant to errors, so any failures while processing a record will result in the entire
 * process being aborted.
 *
 * This class is not thread-safe and should not be shared between threads.
 *
 * @property inputStream Where to read objects from.
 * @property payloadSize The size of the payload in bytes.
 * @property sequenceNumberSize The size of the sequence number in bytes.
 *
 */
class DefaultRecordReader(
	inputStream: InputStream,
	private val payloadSize: Int,
	private val sequenceNumberSize: Int,
) : RecordReader {
	private val metadataSize = payloadSize + sequenceNumberSize
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
		val payload = wrap(metadata, 0, payloadSize).order(LITTLE_ENDIAN).int
		require(payload > 0) { "Invalid payload header: $payload" }

		// Read & validate sequence number
		val sequenceNumber = wrap(metadata, payloadSize, sequenceNumberSize).order(LITTLE_ENDIAN).int
		require(sequenceNumber >= 0) { "Invalid sequence number: $sequenceNumber" }

		// Read the actual message using the specified size
		val messageBuffer = CharArray(payload)
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
