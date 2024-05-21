package com.logitech.service.impl

import com.logitech.model.Record
import com.logitech.service.InputProcessor
import com.logitech.service.RecordTransformer
import java.io.InputStreamReader
import java.nio.ByteBuffer.wrap
import java.nio.ByteOrder.LITTLE_ENDIAN

/**
 * Implementation of [InputProcessor] that processes binary data of fixed size.
 *
 * This class reads binary data from an [InputStreamReader], validates the header and sequence number,
 * and then transforms the data using the configured [RecordTransformer].
 *
 * @property headerSize The size of the header in the binary data.
 * @property sequenceNumberSize The size of the sequence number in the binary data.
 */
class FixedBinarySizeProcessor(
	private val headerSize: Int,
	private val sequenceNumberSize: Int,
) : InputProcessor {
	private val metadataSize = headerSize + sequenceNumberSize
	private val buffer = CharArray(metadataSize)

	override fun process(
		reader: InputStreamReader,
		transformer: RecordTransformer,
	) {
		reader.use {
			while (it.read(buffer) == metadataSize) {
				// Convert CharArray to ByteArray
				val byteArray = buffer.joinToString("").toByteArray()

				// Read & validate header
				val payloadHeader = wrap(byteArray, 0, headerSize).order(LITTLE_ENDIAN).int
				require(payloadHeader > 0) { "Invalid payload header: $payloadHeader" }

				// Read & validate sequence number
				val sequenceNumber = wrap(byteArray, headerSize, sequenceNumberSize).order(LITTLE_ENDIAN).int
				require(sequenceNumber >= 0) { "Invalid sequence number: $sequenceNumber" }

				// Read the actual message using the specified size
				val messageBuffer = CharArray(payloadHeader)
				it.read(messageBuffer)
				val message = String(messageBuffer)

				transformer.transform(Record(sequenceNumber, message))
			}
		}
	}
}
