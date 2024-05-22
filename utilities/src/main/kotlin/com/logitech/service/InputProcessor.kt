package com.logitech.service

import java.io.InputStream

/**
 * Interface for processing input streams.
 */
interface InputProcessor {
	/**
	 * Processes the given input stream, reading and writing records one by one.
	 *
	 * @param inputStream The [InputStream] to process.
	 */
	fun process(inputStream: InputStream)
}
