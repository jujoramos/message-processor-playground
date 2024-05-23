package com.logitech.utils

import org.slf4j.Logger

/**
 * Log a DEBUG level message produced by evaluating the given lambda, only if DEBUG logging is enabled.
 */
inline fun Logger.debug(msg: () -> String) {
	if (isDebugEnabled) debug(msg())
}
