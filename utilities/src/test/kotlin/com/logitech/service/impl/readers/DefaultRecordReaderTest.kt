package com.logitech.service.impl.readers

import com.logitech.service.mockRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class DefaultRecordReaderTest {
	private val payloadSize = 4
	private val sequenceSize = 4

	@Test
	fun exceptionIsThrownWhenInvalidPayloadHeaderIsReceived() {
		val record = mockRecord(-1, 1, "test", payloadSize, sequenceSize)
		val inputStream = ByteArrayInputStream(record)
		val recordReader = DefaultRecordReader(inputStream, payloadSize, sequenceSize)

		assertThatThrownBy {
			recordReader.read(record)
		}.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessage("Invalid payload header: -1")
	}

	@Test
	fun exceptionIsThrownWhenInvalidSequenceNumberIsReceived() {
		val record = mockRecord(4, -1, "test", payloadSize, sequenceSize)
		val inputStream = ByteArrayInputStream(record)
		val recordReader = DefaultRecordReader(inputStream, payloadSize, sequenceSize)

		assertThatThrownBy {
			recordReader.read(record)
		}.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessage("Invalid sequence number: -1")
	}

	@Test
	fun exceptionIsThrownWhenReadingFromTheStreamFails() {
		val failingInputStream =
			object : InputStream() {
				override fun read(): Int {
					throw IOException("Simulated read failure")
				}
			}

		val recordReader = DefaultRecordReader(failingInputStream, payloadSize, sequenceSize)

		assertThatThrownBy {
			recordReader.iterator()
		}.isInstanceOf(IOException::class.java)
			.hasMessage("Simulated read failure")
	}

	@Test
	fun iteratorWorksCorrectly() {
		val records =
			mockRecord("record1".length, 1, "record1", payloadSize, sequenceSize) +
				mockRecord("anotherRecord".length, 2, "anotherRecord", payloadSize, sequenceSize)
		val inputStream = ByteArrayInputStream(records)
		val recordReader = DefaultRecordReader(inputStream, payloadSize, sequenceSize)

		val iterator = recordReader.iterator()
		assertThat(iterator.hasNext()).isTrue()

		val record1 = iterator.next()
		assertThat(record1.sequence).isEqualTo(1)
		assertThat(record1.message).isEqualTo("record1")

		val record2 = iterator.next()
		assertThat(record2.sequence).isEqualTo(2)
		assertThat(record2.message).isEqualTo("anotherRecord")

		assertThat(iterator.hasNext()).isFalse()
		assertThatThrownBy {
			iterator.next()
		}.isInstanceOf(NoSuchElementException::class.java)
	}
}
