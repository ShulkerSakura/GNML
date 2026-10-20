package com.hexagram2021.gnml.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link HmclLocator} 扫描、判定与择优的单元测试喵~
 */
class HmclLocatorTest {
	private static final String HMCL_MAIN = "org.jackhuang.hmcl.Main";

	@TempDir
	Path tempDir;

	@Test
	void scanFindsOnlyHmclManifestFiles() throws IOException {
		writeLauncher(tempDir.resolve("HMCL-3.16.4.jar"), HMCL_MAIN);
		writeLauncher(tempDir.resolve("other-launcher.jar"), "com.example.OtherMain");
		Files.write(tempDir.resolve("broken.jar"), "not a zip".getBytes(StandardCharsets.UTF_8));
		Files.write(tempDir.resolve("notes.txt"), "irrelevant".getBytes(StandardCharsets.UTF_8));

		List<HmclCandidate> candidates = HmclLocator.scan(tempDir);

		assertEquals(1, candidates.size());
		assertTrue(candidates.get(0).getFile().getFileName().toString().equals("HMCL-3.16.4.jar"));
	}

	@Test
	void exeWithHmclManifestIsRecognized() throws IOException {
		writeLauncher(tempDir.resolve("HMCL-3.16.4.exe"), HMCL_MAIN);

		assertTrue(HmclLocator.isHmclLauncher(tempDir.resolve("HMCL-3.16.4.exe")));
	}

	@Test
	void highestVersionWins() throws IOException {
		writeLauncher(tempDir.resolve("HMCL-3.15.0.jar"), HMCL_MAIN);
		writeLauncher(tempDir.resolve("HMCL-3.16.4.jar"), HMCL_MAIN);

		HmclCandidate best = HmclLocator.selectBest(HmclLocator.scan(tempDir));

		assertTrue(best.getFile().getFileName().toString().equals("HMCL-3.16.4.jar"));
	}

	@Test
	void jarBreaksTieAgainstExeOfSameVersion() throws IOException {
		writeLauncher(tempDir.resolve("HMCL-3.16.4.exe"), HMCL_MAIN);
		writeLauncher(tempDir.resolve("HMCL-3.16.4.jar"), HMCL_MAIN);

		HmclCandidate best = HmclLocator.selectBest(HmclLocator.scan(tempDir));

		assertTrue(best.isJar());
	}

	@Test
	void unparseableVersionLosesAgainstParsedOne() throws IOException {
		writeLauncher(tempDir.resolve("HMCL.jar"), HMCL_MAIN);
		writeLauncher(tempDir.resolve("HMCL-3.15.0.jar"), HMCL_MAIN);

		HmclCandidate best = HmclLocator.selectBest(HmclLocator.scan(tempDir));

		assertTrue(best.getFile().getFileName().toString().equals("HMCL-3.15.0.jar"));
	}

	@Test
	void locateFallsBackToSecondaryDirectory(@TempDir Path secondaryDir) throws IOException {
		List<Path> scannedDirs = new ArrayList<>();

		Optional<Path> result = HmclLocator.locate(tempDir, secondaryDir, scannedDirs);

		assertFalse(result.isPresent());
		assertEquals(Arrays.asList(tempDir, secondaryDir), scannedDirs);

		writeLauncher(secondaryDir.resolve("HMCL-3.16.4.jar"), HMCL_MAIN);

		result = HmclLocator.locate(tempDir, secondaryDir, new ArrayList<>());

		assertTrue(result.isPresent());
		assertEquals("HMCL-3.16.4.jar", result.get().getFileName().toString());
	}

	private static void writeLauncher(Path file, String mainClass) throws IOException {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
		try (JarOutputStream ignored = new JarOutputStream(Files.newOutputStream(file), manifest)) {
			// 仅构造含 Manifest 的最小 zip
		}
	}
}
