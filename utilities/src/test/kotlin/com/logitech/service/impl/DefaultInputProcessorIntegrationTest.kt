package com.logitech.service.impl

import com.logitech.model.Record
import com.logitech.service.RecordWriter
import com.logitech.service.impl.readers.DefaultRecordReader
import com.logitech.service.impl.writers.DefaultRecordWriter
import com.logitech.service.mockRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class DefaultInputProcessorIntegrationTest {
	private val recordReader = DefaultRecordReader(4, 4)

	private val recordWriter =
		object : RecordWriter {
			val records = mutableMapOf<Int, String>()

			override fun write(record: Record) {
				records[record.sequence] = record.message
			}
		}

	private val inputProcessor = DefaultInputProcessor(recordReader, recordWriter)

	@Test
	fun exceptionShouldBeThrownForEmptyInputStream(
		@TempDir tempDir: Path,
	) {
		val tempFile = Files.createTempFile(tempDir, "", "").toFile()

		tempFile.inputStream().use {
			assertThatThrownBy {
				inputProcessor.process(it)
			}.isInstanceOf(IllegalArgumentException::class.java)
		}
	}

	@Test
	fun recordsFromSampleFileAreCorrectlyProcessed() {
		File("src/test/resources/logi.bin").inputStream().use {
			DefaultInputProcessor(recordReader, DefaultRecordWriter(System.out)).process(it)
		}
	}

	@Test
	fun recordsAreCorrectlyProcessed(
		@TempDir tempDir: Path,
	) {
		val mockRecord1 = mockRecord("aRecord".length, 1, "aRecord", 4, 4)
		val mockRecord2 = mockRecord("anotherRecord".length, 2, "anotherRecord", 4, 4)
		val mockRecord3 = mockRecord("yetAnotherRecord".length, 3, "yetAnotherRecord", 4, 4)
		val records = mockRecord1 + mockRecord2 + mockRecord3

		val tempFile = Files.createTempFile(tempDir, "", "").toFile()
		tempFile.writeBytes(records)
		tempFile.inputStream().use {
			inputProcessor.process(it)
		}

		assertThat(recordWriter.records).hasSize(3)
		assertThat(recordWriter.records[1]).isEqualTo("aRecord")
		assertThat(recordWriter.records[2]).isEqualTo("anotherRecord")
		assertThat(recordWriter.records[3]).isEqualTo("yetAnotherRecord")
	}
}
