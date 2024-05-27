package com.playground.service.impl.readers

import com.playground.service.Defaults.PAYLOAD_SIZE
import com.playground.service.Defaults.SEQUENCE_NUMBER_SIZE
import com.playground.service.mockRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class DefaultRecordReaderTest {
	@Test
	fun exceptionIsThrownWhenInvalidPayloadHeaderIsReceived() {
		val record = mockRecord(-1, 1, "test", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE)
		val recordReader = DefaultRecordReader(ByteArrayInputStream(record), PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE)

		assertThatThrownBy {
			recordReader.read(record)
		}.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessage("Invalid payload header: -1")
	}

	@Test
	fun exceptionIsThrownWhenInvalidSequenceNumberIsReceived() {
		val record = mockRecord(PAYLOAD_SIZE, -1, "test", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE)
		val recordReader = DefaultRecordReader(ByteArrayInputStream(record), PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE)

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

		val recordReader = DefaultRecordReader(failingInputStream, PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE)

		assertThatThrownBy {
			recordReader.iterator()
		}.isInstanceOf(IOException::class.java)
			.hasMessage("Simulated read failure")
	}

	@Test
	fun iterationWorksCorrectly() {
		val records =
			mockRecord("record1".length, 1, "record1", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE) +
				mockRecord("anotherRecord".length, 2, "anotherRecord", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE)
		val recordReader = DefaultRecordReader(ByteArrayInputStream(records), PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE)

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
