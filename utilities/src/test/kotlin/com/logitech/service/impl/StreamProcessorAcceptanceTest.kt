package com.logitech.service.impl

import com.logitech.model.Record
import com.logitech.service.Defaults.PAYLOAD_SIZE
import com.logitech.service.Defaults.SEQUENCE_NUMBER_SIZE
import com.logitech.service.RecordWriter
import com.logitech.service.impl.readers.DefaultRecordReader
import com.logitech.service.impl.readers.ErrorTolerantRecordReader
import com.logitech.service.impl.writers.DefaultRecordWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.io.OutputStreamWriter

class StreamProcessorAcceptanceTest {
	private val delegatingRecordingRecordWriter =
		object : RecordWriter {
			val records = mutableMapOf<Int, String>()
			val defaultWriter = DefaultRecordWriter(OutputStreamWriter(System.out))

			override fun write(record: Record) {
				records[record.sequence] = record.message
				defaultWriter.write(record)
			}
		}

	private val inputFile = "src/test/resources/logi.bin"
	private val record0 = Record(0, "Hello, welcome to Logitech!")
	private val record1 =
		Record(
			1,
			"""
			Open and Ourselves
			Hungry but Humble
			Collaborate but Challenge
			Decide and Do
			Equality and Environment
			""".trimIndent(),
		)
	private val record2 = Record(2, "This is the final message, Goodbye")

	@Test
	fun recordsFromSampleFileAreCorrectlyProcessedByTheDefaultRecordReader() {
		File(inputFile).also {
			StreamProcessor().process(
				DefaultRecordReader(it.inputStream(), PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE),
				delegatingRecordingRecordWriter,
			)
		}

		assertThat(delegatingRecordingRecordWriter.records).hasSize(3)
		assertThat(delegatingRecordingRecordWriter.records[0]).isEqualTo(record0.message)
		assertThat(delegatingRecordingRecordWriter.records[1]).isEqualTo(record1.message)
		assertThat(delegatingRecordingRecordWriter.records[2]).isEqualTo(record2.message)
	}

	@Test
	fun recordsFromSampleFileAreCorrectlyProcessedByTheErrorTolerantRecordReader() {
		File(inputFile).also {
			StreamProcessor().process(
				ErrorTolerantRecordReader(it, PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE),
				delegatingRecordingRecordWriter,
			)
		}

		assertThat(delegatingRecordingRecordWriter.records).hasSize(3)
		assertThat(delegatingRecordingRecordWriter.records[0]).isEqualTo(record0.message)
		assertThat(delegatingRecordingRecordWriter.records[1]).isEqualTo(record1.message)
		assertThat(delegatingRecordingRecordWriter.records[2]).isEqualTo(record2.message)
	}
}
