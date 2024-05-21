package com.logitech.app

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit = exitProcess(CommandLine(HelloWorld()).execute(*args))

@Command(
	name = "Hello World",
	mixinStandardHelpOptions = true,
	version = ["1.0"],
	description = ["Say hello"],
)
class HelloWorld : Callable<Int> {
	private companion object {
		val logger: Logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
	}

	@Option(names = ["-n", "--name"], description = ["Say your name"])
	var name = "World"

	override fun call(): Int {
		logger.info("Say hello to $name")
		println("Hello $name")

		return 0
	}
}
