package com.playground.service

import java.nio.ByteBuffer
import java.nio.ByteOrder

object Defaults {
	const val PAYLOAD_SIZE = 4
	const val SEQUENCE_NUMBER_SIZE = 4
}

fun mockRecord(
	payload: Int,
	sequence: Int,
	message: String,
	payloadSize: Int,
	sequenceSize: Int,
): ByteArray {
	val headerBytes = ByteBuffer.allocate(payloadSize).order(ByteOrder.LITTLE_ENDIAN).putInt(payload).array()
	val sequenceBytes = ByteBuffer.allocate(sequenceSize).order(ByteOrder.LITTLE_ENDIAN).putInt(sequence).array()
	val messageBytes = message.toByteArray()

	return headerBytes + sequenceBytes + messageBytes
}
