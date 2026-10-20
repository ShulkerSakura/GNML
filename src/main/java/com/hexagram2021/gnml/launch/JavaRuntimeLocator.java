package com.hexagram2021.gnml.launch;

import javax.annotation.Nullable;

import java.io.File;
import java.util.Locale;

import com.hexagram2021.gnml.util.GnmlLog;

/**
 * java 可执行文件定位器喵~
 * <br/>
 * 按 DR-D9 顺序解析：当前 JVM 同款 → {@code HMCL_JAVA_HOME} → {@code JAVA_HOME} → 系统 PATH 喵~
 *
 * @author liudongyu
 */
public final class JavaRuntimeLocator {
	/** java 解析来源喵~ */
	public enum Source {
		/** 当前运行 GNML 的 JVM 同款 java 喵~ */
		CURRENT_JVM,
		/** 环境变量 HMCL_JAVA_HOME 喵~ */
		HMCL_JAVA_HOME,
		/** 环境变量 JAVA_HOME 喵~ */
		JAVA_HOME,
		/** 系统 PATH 中的 java 喵~ */
		PATH
	}

	/**
	 * java 解析结果喵~
	 */
	public static final class Resolution {
		private final String command;
		private final Source source;

		Resolution(String command, Source source) {
			this.command = command;
			this.source = source;
		}

		/**
		 * 获取可直接传给 {@link ProcessBuilder} 的 java 命令喵~
		 *
		 * @return java 可执行文件绝对路径，或 PATH 回退时的 {@code java} 喵~
		 */
		public String getCommand() {
			return command;
		}

		/**
		 * 获取解析来源喵~
		 *
		 * @return 来源枚举喵~
		 */
		public Source getSource() {
			return source;
		}

		@Override
		public String toString() {
			return command + " (来源: " + source + ")";
		}
	}

	/**
	 * 环境变量读取器，抽象出来便于单元测试喵~
	 */
	interface EnvironmentProvider {
		/**
		 * 读取环境变量喵~
		 *
		 * @param name 变量名
		 * @return 变量值；未设置时返回 null 喵~
		 */
		@Nullable
		String getEnv(String name);
	}

	/**
	 * 使用真实环境解析 java 可执行文件喵~
	 *
	 * @return 解析结果，永不为 null（最终回退到 PATH 的 {@code java}）喵~
	 */
	public static Resolution locate() {
		return locate(System::getenv);
	}

	static Resolution locate(EnvironmentProvider environment) {
		// 1. 当前 JVM 同款 java
		String resolution = findJavaExecutable(System.getProperty("java.home"));
		if (resolution != null) {
			return new Resolution(resolution, Source.CURRENT_JVM);
		}
		// 2. HMCL_JAVA_HOME
		resolution = findJavaExecutable(environment.getEnv("HMCL_JAVA_HOME"));
		if (resolution != null) {
			return new Resolution(resolution, Source.HMCL_JAVA_HOME);
		}
		// 3. JAVA_HOME
		resolution = findJavaExecutable(environment.getEnv("JAVA_HOME"));
		if (resolution != null) {
			return new Resolution(resolution, Source.JAVA_HOME);
		}
		// 4. PATH 回退
		GnmlLog.warn("未能定位具体 java 可执行文件，回退到系统 PATH 中的 java");
		return new Resolution("java", Source.PATH);
	}

	/**
	 * 在给定 JDK/JRE 根目录下查找 bin 中的 java 可执行文件喵~
	 *
	 * @param javaHome JDK/JRE 根目录，可为 null
	 * @return java 可执行文件绝对路径；目录为空或文件不存在时返回 null 喵~
	 */
	@Nullable
	static String findJavaExecutable(@Nullable String javaHome) {
		if (javaHome == null || javaHome.isEmpty()) {
			return null;
		}
		File executable = new File(new File(javaHome, "bin"), executableName());
		return executable.isFile() ? executable.getAbsolutePath() : null;
	}

	private static String executableName() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win") ? "java.exe" : "java";
	}

	private JavaRuntimeLocator() {
	}
}
