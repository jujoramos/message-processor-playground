package com.logitech.app

import com.logitech.app.commands.ProcessInput
import picocli.CommandLine
import kotlin.system.exitProcess

@CommandLine.Command(
	name = "app",
	version = ["1.0.0"],
	synopsisHeading = "",
	headerHeading = "@|bold,underline Usage|@:%n%n",
	optionListHeading = "%n@|bold,underline Options|@:%n",
	commandListHeading = "%n@|bold,underline Available Commands|@:%n",
	descriptionHeading = "%n@|bold,underline Description|@:%n%n",
	parameterListHeading = "%n@|bold,underline Parameters|@:%n",
	scope = CommandLine.ScopeType.INHERIT,
	sortOptions = false,
	usageHelpAutoWidth = true,
	mixinStandardHelpOptions = true,
	subcommands = [ProcessInput::class],
	description = ["Command Line Utilities for Performance Simulations."],
)
class CliApp : Runnable {
	@CommandLine.Spec
	var commandSpec: CommandLine.Model.CommandSpec? = null

	override fun run() {
		commandSpec!!.commandLine().usage(System.out)
	}
}

fun main(args: Array<String>) {
	exitProcess(CommandLine(CliApp()).execute(*args))
}
