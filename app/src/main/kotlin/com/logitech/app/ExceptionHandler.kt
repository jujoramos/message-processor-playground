package com.logitech.app

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Help.Ansi
import picocli.CommandLine.IExecutionExceptionHandler
import picocli.CommandLine.ParseResult
import java.lang.Exception

/**
 * Custom implementation of [IExecutionExceptionHandler] to avoid showing the stack trace when an exception occurs.
 */
class ExceptionHandler : IExecutionExceptionHandler {
	private companion object {
		val logger: Logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
	}

	override fun handleExecutionException(
		exception: Exception,
		commandLine: CommandLine,
		parseResult: ParseResult,
	): Int {
		logger.error("Error Executing Command '${commandLine.commandSpec.name()}': ${exception.message}", exception)
		val errorMessage = Ansi.AUTO.string("@|bold,red ${exception.javaClass.name}: ${exception.message}|@")
		commandLine.getErr().println(errorMessage)

		return commandLine.exitCodeExceptionMapper?.getExitCode(exception)
			?: commandLine.commandSpec.exitCodeOnExecutionException()
	}
}
