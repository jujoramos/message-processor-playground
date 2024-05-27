package com.playground.app.commands

import com.playground.app.ExceptionHandler
import com.playground.app.commands.ProcessInputCommand.Defaults
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.io.StringWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path

class ProcessInputCommandTest {
	private val errorWriter = StringWriter()
	private val outputWriter = StringWriter()
	private val command = ProcessInputCommand()
	private val commandLine =
		CommandLine(command).apply {
			out = PrintWriter(outputWriter)
			err = PrintWriter(errorWriter)
			executionExceptionHandler = ExceptionHandler()
		}

	private fun mockRecord(
		payload: Int,
		sequence: Int,
		message: String,
	): ByteArray {
		val headerBytes = ByteBuffer.allocate(Defaults.PAYLOAD_SIZE).order(ByteOrder.LITTLE_ENDIAN).putInt(payload).array()
		val sequenceBytes = ByteBuffer.allocate(Defaults.SEQUENCE_NUMBER_SIZE).order(ByteOrder.LITTLE_ENDIAN).putInt(sequence).array()
		val messageBytes = message.toByteArray()

		return headerBytes + sequenceBytes + messageBytes
	}

	@Test
	fun commandFailsWhenNoFileIsProvided() {
		val exitCode = commandLine.execute()

		assertThat(exitCode).isEqualTo(ExitCode.USAGE)
		assertThat(outputWriter.toString()).isEmpty()
		assertThat(errorWriter.toString()).contains("Missing required option")
	}

	@Test
	fun commandFailsWhenFileDoesNotExist() {
		val exitCode = commandLine.execute("--file", "non-existent-file")

		assertThat(exitCode).isEqualTo(ExitCode.SOFTWARE)
		assertThat(outputWriter.toString()).isEmpty()
		assertThat(errorWriter.toString()).contains("java.io.FileNotFoundException: non-existent-file (No such file or directory)")
	}

	@Test
	fun commandFailsWhenReaderTypeIsInvalid(
		@TempDir tempDir: Path,
	) {
		val file = Files.createTempFile(tempDir, "", "").toFile()
		val exitCode = commandLine.execute("--file", file.absolutePath, "--reader", "InvalidReader")

		assertThat(exitCode).isEqualTo(ExitCode.SOFTWARE)
		assertThat(outputWriter.toString()).isEmpty()
		assertThat(errorWriter.toString()).contains("java.lang.IllegalArgumentException: 'InvalidReader' is not a valid reader type")
	}

	@Test
	fun commandFailsWhenWriterTypeIsInvalid(
		@TempDir tempDir: Path,
	) {
		val file = Files.createTempFile(tempDir, "", "").toFile()
		val exitCode = commandLine.execute("--file", file.absolutePath, "--writer", "InvalidWriter")

		assertThat(exitCode).isEqualTo(ExitCode.SOFTWARE)
		assertThat(outputWriter.toString()).isEmpty()
		assertThat(errorWriter.toString()).contains("java.lang.IllegalArgumentException: 'InvalidWriter' is not a valid writer type")
	}

	@Test
	fun commandFailsWhenProcessorTypeIsInvalid(
		@TempDir tempDir: Path,
	) {
		val file = Files.createTempFile(tempDir, "", "").toFile()
		val exitCode = commandLine.execute("--file", file.absolutePath, "--processor", "InvalidProcessor")

		assertThat(exitCode).isEqualTo(ExitCode.SOFTWARE)
		assertThat(outputWriter.toString()).isEmpty()
		assertThat(errorWriter.toString()).contains("java.lang.IllegalArgumentException: 'InvalidProcessor' is not a valid processor type")
	}

	@Test
	fun commandSucceedsWhenUsingDefaultOptions(
		@TempDir tempDir: Path,
	) {
		val file = Files.createTempFile(tempDir, "", "").toFile()
		RandomAccessFile(file, "rw").use { raf ->
			mockRecord("validRecord".length, 1, "validRecord").let { raf.write(it) }
			mockRecord("invalidRecord".length, -2, "invalidRecord").let { raf.write(it) }
			mockRecord("anotherValidRecord".length, 2, "anotherValidRecord").let { raf.write(it) }
		}

		val exitCode = commandLine.execute("--file", file.absolutePath)

		assertThat(exitCode).isEqualTo(ExitCode.OK)
		assertThat(outputWriter.toString()).contains("validRecord").contains("anotherValidRecord")
		assertThat(errorWriter.toString()).isEmpty()
	}

	@Test
	fun commandSucceedsWhenUsingNonDefaultOptions(
		@TempDir tempDir: Path,
	) {
		val file = Files.createTempFile(tempDir, "", "").toFile()
		RandomAccessFile(file, "rw").use { raf ->
			mockRecord("validRecord".length, 1, "validRecord").let { raf.write(it) }
			mockRecord("anotherValidRecord".length, 2, "anotherValidRecord").let { raf.write(it) }
		}

		val exitCode =
			commandLine.execute(
				"-f",
				file.absolutePath,
				"-p",
				"StreamProcessor",
				"-r",
				"DefaultRecordReader",
				"-w",
				"DefaultRecordWriter",
			)

		assertThat(exitCode).isEqualTo(ExitCode.OK)
		assertThat(outputWriter.toString()).contains("validRecord").contains("anotherValidRecord")
		assertThat(errorWriter.toString()).isEmpty()
	}
}
