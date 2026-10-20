package com.hexagram2021.gnml.util;

import javax.annotation.Nullable;

/**
 * GNML 日志工具：统一向 stderr 输出带 {@code [GNML]} 前缀的日志喵~
 * <br/>
 * 不引入日志框架，保持 agent 依赖面最小喵~
 *
 * @author liudongyu
 */
public final class GnmlLog {
	private static final String PREFIX = "[GNML] ";

	/**
	 * 输出 INFO 级日志喵~
	 *
	 * @param message 日志内容
	 */
	public static void info(String message) {
		System.err.println(PREFIX + message);
	}

	/**
	 * 输出 WARN 级日志喵~
	 *
	 * @param message 日志内容
	 */
	public static void warn(String message) {
		System.err.println(PREFIX + "WARN: " + message);
	}

	/**
	 * 输出 ERROR 级日志喵~
	 *
	 * @param message 日志内容
	 */
	public static void error(String message) {
		System.err.println(PREFIX + "ERROR: " + message);
	}

	/**
	 * 输出 WARN 级日志（含异常堆栈）喵~
	 *
	 * @param message   日志内容
	 * @param throwable 触发的异常，可为空
	 */
	public static void warn(String message, @Nullable Throwable throwable) {
		warn(message);
		if (throwable != null) {
			throwable.printStackTrace(System.err);
		}
	}

	/**
	 * 输出 ERROR 级日志（含异常堆栈）喵~
	 *
	 * @param message   日志内容
	 * @param throwable 触发的异常，可为空
	 */
	public static void error(String message, @Nullable Throwable throwable) {
		error(message);
		if (throwable != null) {
			throwable.printStackTrace(System.err);
		}
	}

	private GnmlLog() {
	}
}
