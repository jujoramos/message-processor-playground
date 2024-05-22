package com.logitech.service.impl.readers

import com.logitech.service.mockRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class DefaultRecordReaderTest {
	private val payloadSize = 4
	private val sequenceSize = 4
	private lateinit var recordReader: DefaultRecordReader

	@BeforeEach
	fun setup() {
		recordReader = DefaultRecordReader(payloadSize, sequenceSize)
	}

	@Test
	fun exceptionIsThrownWhenInvalidPayloadHeaderIsReceived() {
		val record = mockRecord(-1, 1, "test", payloadSize, sequenceSize)
		val stream = ByteArrayInputStream(record)

		assertThatThrownBy {
			recordReader.read(record, InputStreamReader(stream))
		}.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessage("Invalid payload header: -1")
	}

	@Test
	fun exceptionIsThrownWhenInvalidSequenceNumberIsReceived() {
		val record = mockRecord(4, -1, "test", payloadSize, sequenceSize)
		val stream = ByteArrayInputStream(record)

		assertThatThrownBy {
			recordReader.read(record, InputStreamReader(stream))
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

		assertThatThrownBy {
			recordReader.iterator(InputStreamReader(failingInputStream))
		}.isInstanceOf(IOException::class.java)
			.hasMessage("Simulated read failure")
	}

	@Test
	fun iteratorWorksCorrectly() {
		val records =
			mockRecord("record1".length, 1, "record1", payloadSize, sequenceSize) +
				mockRecord("anotherRecord".length, 2, "anotherRecord", payloadSize, sequenceSize)
		val stream = ByteArrayInputStream(records)

		val iterator = recordReader.iterator(InputStreamReader(stream))
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
