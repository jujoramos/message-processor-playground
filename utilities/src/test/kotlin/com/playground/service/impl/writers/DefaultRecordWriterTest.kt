package com.playground.service.impl.writers

import com.playground.model.Record
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter

class DefaultRecordWriterTest {
	@Test
	fun shouldCorrectlyWriteRecordToOutputStream() {
		val outputStream = ByteArrayOutputStream()
		val recordWriter = DefaultRecordWriter(OutputStreamWriter(outputStream))
		val record = Record(1, "testOutput")

		recordWriter.write(record)

		val expectedOutput = "----------- 1 -----------\ntestOutput\n"
		assertThat(outputStream.toString()).isEqualTo(expectedOutput)
	}
}
