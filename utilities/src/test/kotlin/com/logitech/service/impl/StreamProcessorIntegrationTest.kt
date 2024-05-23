package com.logitech.service.impl

import com.logitech.model.Record
import com.logitech.service.RecordWriter
import com.logitech.service.impl.readers.DefaultRecordReader
import com.logitech.service.impl.writers.DefaultRecordWriter
import com.logitech.service.mockRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class StreamProcessorIntegrationTest {
	private val recordWriter =
		object : RecordWriter {
			val records = mutableMapOf<Int, String>()

			override fun write(record: Record) {
				records[record.sequence] = record.message
			}
		}

	@Test
	fun recordsFromSampleFileAreCorrectlyProcessed() {
		File("src/test/resources/logi.bin").inputStream().use {
			val recordReader = DefaultRecordReader(it, 4, 4)
			StreamProcessor().process(recordReader, DefaultRecordWriter(System.out))
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
			val recordReader = DefaultRecordReader(it, 4, 4)
			StreamProcessor().process(recordReader, recordWriter)
		}

		assertThat(recordWriter.records).hasSize(3)
		assertThat(recordWriter.records[1]).isEqualTo("aRecord")
		assertThat(recordWriter.records[2]).isEqualTo("anotherRecord")
		assertThat(recordWriter.records[3]).isEqualTo("yetAnotherRecord")
	}
}
