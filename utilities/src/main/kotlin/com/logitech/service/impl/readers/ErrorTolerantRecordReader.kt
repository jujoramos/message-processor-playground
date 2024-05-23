package com.logitech.service.impl.readers

import com.logitech.model.Record
import com.logitech.service.RecordReader
import com.logitech.utils.debug
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.EOFException
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * Implementation of the [RecordReader] interface that reads [Record] objects from a file and tolerates errors.
 *
 * This implementation reads records from a file using a [RandomAccessFile].
 * If an error occurs while reading a record, and if and only if the payload was correctly read, the implementation logs
 * the error and skips to the next record. This allows the implementation to continue reading records even if some records
 * are corrupted, but works only if the payload size is always correct and there are no incomplete messages.
 *
 * This class is not thread-safe and should not be shared between threads.
 *
 * @property file The [File] to read records from.
 * @property payloadSize The size of the payload header in bytes.
 * @property sequenceNumberSize The size of the sequence number in bytes.
 */
class ErrorTolerantRecordReader(
	file: File,
	private val payloadSize: Int,
	private val sequenceNumberSize: Int,
) : RecordReader {
	private companion object {
		val logger: Logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
	}

	private var next: Record? = null
	private val file = RandomAccessFile(file, "r")

	/**
	 * Loads the next record from the file.
	 *
	 * This method reads the payload size, sequence number, and message from the file to create a new [Record] object.
	 * If an error occurs while reading the record, the method logs the error, skips to the next record, and calls
	 * itself recursively to try to load the next record.
	 */
	private fun loadNextRecord() {
		logger.debug { "Loading next record" }

		// No more records to read
		if (file.length() - file.filePointer < payloadSize) {
			next = null
			logger.debug { "End of file reached at offset ${file.filePointer}" }
			return
		}

		// Outside the try-catch block as the implementation cannot move forward if the payLoadSize cannot be read.
		val payloadSizeBuffer = ByteArray(payloadSize)
		file.readFully(payloadSizeBuffer)
		val payload = ByteBuffer.wrap(payloadSizeBuffer).order(ByteOrder.LITTLE_ENDIAN).int
		require(payload > 0) { "Invalid payload header: $payload" }
		val lastSuccessPosition = file.filePointer
		logger.debug { "Payload successfully retrieved at offset ${file.filePointer}: $payload" }

		// Within the try-catch block so failures upon reading the sequence number and message can be handled gracefully.
		try {
			val sequenceNumberBuffer = ByteArray(sequenceNumberSize)
			file.readFully(sequenceNumberBuffer)
			val sequenceNumber = ByteBuffer.wrap(sequenceNumberBuffer).order(ByteOrder.LITTLE_ENDIAN).int
			require(sequenceNumber >= 0) { "Invalid sequence number: $sequenceNumber" }
			logger.debug { "Sequence number successfully retrieved at offset ${file.filePointer}: $sequenceNumber" }

			val messageBuffer = ByteArray(payload)
			file.readFully(messageBuffer)
			next = Record(sequenceNumber, String(messageBuffer, StandardCharsets.UTF_8))
			logger.debug { "Record successfully retrieved at offset ${file.filePointer}: $next" }
		} catch (e: EOFException) {
			// Ignore, no more records to read
			logger.debug { "End of file reached at offset ${file.filePointer}" }
			next = null
		} catch (e: Exception) {
			// If an error occurs, skip to the next record
			val currentPosition = file.filePointer
			file.seek(lastSuccessPosition + sequenceNumberSize + payload)
			logger.warn("Error occurred while reading record at offset $currentPosition", e)
			loadNextRecord()
		}
	}

	/**
	 * Returns an [Iterator] over the records read from the internal [File].
	 *
	 * @return An [Iterator] over the records read from the internal [File].
	 */
	override fun iterator(): Iterator<Record> {
		return object : Iterator<Record> {
			override fun hasNext(): Boolean {
				if (next == null) {
					loadNextRecord()
				}

				return next != null
			}

			override fun next(): Record {
				val record = next ?: throw NoSuchElementException()
				next = null

				return record
			}
		}
	}
}
