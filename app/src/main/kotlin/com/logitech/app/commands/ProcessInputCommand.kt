package com.logitech.app.commands

import com.logitech.service.impl.StreamProcessor
import com.logitech.service.impl.readers.DefaultRecordReader
import com.logitech.service.impl.readers.ErrorTolerantRecordReader
import com.logitech.service.impl.writers.DefaultRecordWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import java.util.concurrent.Callable

@Command(
	name = "process",
	version = ["1.0"],
	sortOptions = false,
	usageHelpAutoWidth = true,
	mixinStandardHelpOptions = true,
	description = [
		"Process the given input <file> through the specified <processorName>.",
		"Records are read and written using <readerName> and <writerName> respectively.",
	],
)
class ProcessInputCommand : Callable<Int> {
	private companion object {
		val logger: Logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
	}

	// This could be make configurable as command line arguments as well
	object Defaults {
		const val PAYLOAD_SIZE = 4
		const val SEQUENCE_NUMBER_SIZE = 4
	}

	@CommandLine.Spec
	var commandSpec: CommandLine.Model.CommandSpec? = null

	@Option(
		required = true,
		names = ["-f", "--file"],
		description = ["Input File"],
	)
	var file: File? = null

	@Option(
		required = false,
		names = ["-p", "--processor"],
		defaultValue = "StreamProcessor",
		description = [
			"Name of the Processor instance to use.",
			"Default: 'StreamProcessor'.",
			"Available: 'StreamProcessor'.",
		],
	)
	var processorName: String? = null

	@Option(
		required = false,
		names = ["-r", "--reader"],
		defaultValue = "ErrorTolerantRecordReader",
		description = [
			"Name of the RecordReader instance to use.",
			"Default: 'ErrorTolerantRecordReader'.",
			"Available: 'DefaultRecordReader', 'ErrorTolerantRecordReader'.",
		],
	)
	var readerName: String? = null

	@Option(
		required = false,
		names = ["-w", "--writer"],
		defaultValue = "DefaultRecordWriter",
		description = [
			"Name of the RecordWriter instance to use.",
			"Default: 'DefaultRecordWriter'.",
			"Available: 'DefaultRecordWriter'.",
		],
	)
	var writerName: String? = null

	override fun call(): Int {
		file!!.also {
			logger.info("Processing '${it.absolutePath}' through '$processorName' with '$readerName' and '$writerName'...")

			val recordWriter =
				when (writerName) {
					DefaultRecordWriter::class.java.simpleName -> DefaultRecordWriter(commandSpec!!.commandLine().out)
					else -> throw IllegalArgumentException("'$writerName' is not a valid writer type")
				}

			val recordReader =
				when (readerName) {
					DefaultRecordReader::class.java.simpleName ->
						DefaultRecordReader(
							it.inputStream(),
							Defaults.PAYLOAD_SIZE,
							Defaults.SEQUENCE_NUMBER_SIZE,
						)

					ErrorTolerantRecordReader::class.java.simpleName ->
						ErrorTolerantRecordReader(
							it,
							Defaults.PAYLOAD_SIZE,
							Defaults.SEQUENCE_NUMBER_SIZE,
						)

					else -> throw IllegalArgumentException("'$readerName' is not a valid reader type")
				}

			val processor =
				when (processorName) {
					StreamProcessor::class.java.simpleName -> StreamProcessor()
					else -> throw IllegalArgumentException("'$processorName' is not a valid processor type")
				}

			processor.process(recordReader, recordWriter)
			logger.info("Processing '${it.absolutePath}' through '$processorName' with '$readerName' and '$writerName'... Done!")
		}

		return 0
	}
}
