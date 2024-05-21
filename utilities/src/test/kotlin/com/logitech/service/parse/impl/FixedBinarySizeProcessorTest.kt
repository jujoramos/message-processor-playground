package com.logitech.service.parse.impl

import com.logitech.model.Record
import com.logitech.service.RecordTransformer
import com.logitech.service.impl.FixedBinarySizeProcessor
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class FixedBinarySizeProcessorTest {
	private val transformer = mock<RecordTransformer>()
	private val inputProcessor = FixedBinarySizeProcessor(4, 4)

	@Test
	fun `process should correctly parse sample data stream`() {
		File("src/test/resources/logi.bin").inputStream().use {
			val reader = InputStreamReader(it)
			inputProcessor.process(reader) { record ->
				println(record)
			}
		}
	}

	@Test
	fun `process should correctly parse binary stream`() {
		val expectedRecord = Record(1, "test")
		val payloadHeader = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(4).array()
		val sequenceNumber = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array()
		val input = payloadHeader + sequenceNumber + "test".toByteArray(StandardCharsets.UTF_8)
		val stream = ByteArrayInputStream(input)

		stream.use {
			val reader = InputStreamReader(it, StandardCharsets.UTF_8)
			inputProcessor.process(reader, transformer)
		}

		verify(transformer).transform(expectedRecord)
	}

	@Test
	fun `process should throw exception for invalid payload`() {
		val payloadHeader = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(-4).array()
		val sequenceNumber = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array()
		val input = payloadHeader + sequenceNumber + "test".toByteArray(StandardCharsets.UTF_8)
		val stream = ByteArrayInputStream(input)

		stream.use {
			val reader = InputStreamReader(it, StandardCharsets.UTF_8)
			assertThatThrownBy {
				inputProcessor.process(reader, transformer)
			}.isInstanceOf(IllegalArgumentException::class.java)
		}
	}

	@Test
	fun `process should throw exception for invalid sequence number`() {
		val payloadHeader = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(4).array()
		val sequenceNumber = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(-1).array()
		val input = payloadHeader + sequenceNumber + "test".toByteArray(StandardCharsets.UTF_8)
		val stream = ByteArrayInputStream(input)

		stream.use {
			val reader = InputStreamReader(it, StandardCharsets.UTF_8)
			assertThatThrownBy {
				inputProcessor.process(reader, transformer)
			}.isInstanceOf(IllegalArgumentException::class.java)
		}
	}

	@Test
	fun `process should throw exception when reading from stream fails`() {
		val reader: InputStreamReader =
			mock {
				on { read(any<CharArray>()) } doAnswer {
					throw IOException("Test exception")
				}
			}

		assertThatThrownBy {
			inputProcessor.process(reader, transformer)
		}.isInstanceOf(IOException::class.java)
	}
}
