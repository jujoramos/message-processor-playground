package com.playground.service.impl.readers

import com.playground.service.Defaults.PAYLOAD_SIZE
import com.playground.service.Defaults.SEQUENCE_NUMBER_SIZE
import com.playground.service.mockRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path

class ErrorTolerantRecordReaderTest {
	@Test
	fun exceptionIsThrownWhenInvalidPayloadHeaderIsReceived(
		@TempDir tempDir: Path,
	) {
		val file = Files.createTempFile(tempDir, "", "").toFile()
		RandomAccessFile(file, "rw").use { raf ->
			mockRecord(-1, 10, "invalidRecord", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).let { raf.write(it) }
		}
		val iterator = ErrorTolerantRecordReader(file, PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).iterator()

		assertThatThrownBy {
			iterator.hasNext()
		}.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessage("Invalid payload header: -1")
	}

	@Test
	fun iterationWorksCorrectly(
		@TempDir tempDir: Path,
	) {
		val file = Files.createTempFile(tempDir, "", "").toFile()
		RandomAccessFile(file, "rw").use { raf ->
			mockRecord("validRecord".length, 1, "validRecord", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).let { raf.write(it) }
			mockRecord("anotherValidRecord".length, 2, "anotherValidRecord", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).let { raf.write(it) }
		}

		val iterator = ErrorTolerantRecordReader(file, PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).iterator()
		assertThat(iterator.hasNext()).isTrue()

		iterator.next().also {
			assertThat(it.sequence).isEqualTo(1)
			assertThat(it.message).isEqualTo("validRecord")
		}
		assertThat(iterator.hasNext()).isTrue()

		iterator.next().also {
			assertThat(it.sequence).isEqualTo(2)
			assertThat(it.message).isEqualTo("anotherValidRecord")
		}
		assertThat(iterator.hasNext()).isFalse()
		assertThatThrownBy {
			iterator.next()
		}.isInstanceOf(NoSuchElementException::class.java)
	}

	@Test
	fun iterationIgnoresInvalidRecords(
		@TempDir tempDir: Path,
	) {
		val file = Files.createTempFile(tempDir, "", "").toFile()
		RandomAccessFile(file, "rw").use { raf ->
			mockRecord("validRecord".length, 1, "validRecord", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).let { raf.write(it) }
			mockRecord("invalidRecord".length, -1, "invalidRecord", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).let { raf.write(it) }
			mockRecord("anotherValidRecord".length, 2, "anotherValidRecord", PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).let { raf.write(it) }
		}

		val iterator = ErrorTolerantRecordReader(file, PAYLOAD_SIZE, SEQUENCE_NUMBER_SIZE).iterator()
		assertThat(iterator.hasNext()).isTrue()

		iterator.next().also {
			assertThat(it.sequence).isEqualTo(1)
			assertThat(it.message).isEqualTo("validRecord")
		}
		assertThat(iterator.hasNext()).isTrue()

		iterator.next().also {
			assertThat(it.sequence).isEqualTo(2)
			assertThat(it.message).isEqualTo("anotherValidRecord")
		}
		assertThat(iterator.hasNext()).isFalse()
		assertThatThrownBy {
			iterator.next()
		}.isInstanceOf(NoSuchElementException::class.java)
	}
}
