package com.hexagram2021.gnml.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link JavaRuntimeLocator} 解析顺序的单元测试喵~
 */
class JavaRuntimeLocatorTest {
	private final String originalJavaHome = System.getProperty("java.home");

	@AfterEach
	void restoreJavaHome() {
		System.setProperty("java.home", originalJavaHome);
	}

	@Test
	void findsJavaUnderGivenHome(@TempDir Path home) throws IOException {
		Path executable = createJavaExecutable(home);

		assertEquals(executable.toFile().getAbsolutePath(), JavaRuntimeLocator.findJavaExecutable(home.toString()));
	}

	@Test
	void returnsNullForMissingHomeAndExecutable(@TempDir Path home) {
		assertNull(JavaRuntimeLocator.findJavaExecutable(null));
		assertNull(JavaRuntimeLocator.findJavaExecutable(""));
		assertNull(JavaRuntimeLocator.findJavaExecutable(home.toString()));
	}

	@Test
	void currentJvmWinsOverEnvironment(@TempDir Path home) throws IOException {
		createJavaExecutable(home);
		System.setProperty("java.home", home.toString());

		JavaRuntimeLocator.Resolution resolution = JavaRuntimeLocator.locate(name -> null);

		assertEquals(JavaRuntimeLocator.Source.CURRENT_JVM, resolution.getSource());
	}

	@Test
	void hmclJavaHomeWinsOverJavaHome(@TempDir Path hmclHome, @TempDir Path javaHome) throws IOException {
		Path expected = createJavaExecutable(hmclHome);
		createJavaExecutable(javaHome);
		System.setProperty("java.home", "does-not-exist");

		Map<String, String> environment = new HashMap<>();
		environment.put("HMCL_JAVA_HOME", hmclHome.toString());
		environment.put("JAVA_HOME", javaHome.toString());
		JavaRuntimeLocator.Resolution resolution = JavaRuntimeLocator.locate(environment::get);

		assertEquals(JavaRuntimeLocator.Source.HMCL_JAVA_HOME, resolution.getSource());
		assertEquals(expected.toFile().getAbsolutePath(), resolution.getCommand());
	}

	@Test
	void fallsBackToPathWhenEverythingMissing(@TempDir Path javaHome) {
		System.setProperty("java.home", "does-not-exist");

		JavaRuntimeLocator.Resolution resolution = JavaRuntimeLocator.locate(name -> null);

		assertEquals(JavaRuntimeLocator.Source.PATH, resolution.getSource());
		assertEquals("java", resolution.getCommand());
	}

	/**
	 * 同时创建 java 与 java.exe，保证测试在 Windows / POSIX 上均成立喵~
	 */
	private static Path createJavaExecutable(Path home) throws IOException {
		Path bin = Files.createDirectories(home.resolve("bin"));
		Path posix = bin.resolve("java");
		Files.createFile(posix);
		Path windows = bin.resolve("java.exe");
		Files.createFile(windows);
		return System.getProperty("os.name", "").toLowerCase().contains("win") ? windows : posix;
	}
}
