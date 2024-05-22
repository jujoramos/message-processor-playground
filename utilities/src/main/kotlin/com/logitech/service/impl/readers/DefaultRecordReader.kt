package com.logitech.service.impl.readers

import com.logitech.model.Record
import com.logitech.service.RecordReader
import java.io.InputStreamReader
import java.nio.ByteBuffer.wrap
import java.nio.ByteOrder.LITTLE_ENDIAN

/**
 * Implementation of the [RecordReader] interface for reading [Record] objects.
 *
 * This class is not thread safe (Iterators generally aren't thread safe), so it should not be shared between threads.
 *
 * @property headerSize The size of the header in bytes.
 * @property sequenceNumberSize The size of the sequence number in bytes.
 */
class DefaultRecordReader(
	private val headerSize: Int,
	private val sequenceNumberSize: Int,
) : RecordReader {
	private val metadataSize = headerSize + sequenceNumberSize

	/**
	 * Reads a [Record] from the given [metadata] and [InputStreamReader].
	 *
	 * @param metadata The metadata of the record, as a [ByteArray].
	 * @param streamReader The [InputStreamReader] to read the record from.
	 * @return The parsed [Record].
	 * @throws IllegalArgumentException If the payload, or sequence, number is invalid.
	 */
	fun read(
		metadata: ByteArray,
		streamReader: InputStreamReader,
	): Record {
		// TODO: handle errors more gracefully.
		val payloadHeader = wrap(metadata, 0, headerSize).order(LITTLE_ENDIAN).int
		require(payloadHeader > 0) { "Invalid payload header: $payloadHeader" }

		// Read & validate sequence number
		val sequenceNumber = wrap(metadata, headerSize, sequenceNumberSize).order(LITTLE_ENDIAN).int
		require(sequenceNumber >= 0) { "Invalid sequence number: $sequenceNumber" }

		// Read the actual message using the specified size
		val messageBuffer = CharArray(payloadHeader)
		streamReader.read(messageBuffer)

		return Record(sequenceNumber, String(messageBuffer))
	}

	/**
	 * Returns an [Iterator] over the records in the given [InputStreamReader].
	 *
	 * @param streamReader The [InputStreamReader] to read the records from.
	 * @return An [Iterator] over the records in the [InputStreamReader].
	 */
	override fun iterator(streamReader: InputStreamReader): Iterator<Record> {
		return object : Iterator<Record> {
			private var nextRecord: Record? = null
			private val buffer = CharArray(metadataSize)

			init {
				loadNextRecord()
			}

			private fun loadNextRecord() {
				if (streamReader.read(buffer) == metadataSize) {
					val byteArray = buffer.joinToString("").toByteArray()
					nextRecord = read(byteArray, streamReader)
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
