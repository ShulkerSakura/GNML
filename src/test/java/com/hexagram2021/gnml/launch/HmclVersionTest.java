package com.hexagram2021.gnml.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@link HmclVersion} 解析与比较的单元测试喵~
 */
class HmclVersionTest {
	@Test
	void parsesSimpleVersionFromFileName() {
		HmclVersion version = HmclVersion.tryParse("HMCL-3.16.4.jar");
		assertNotNull(version);
		assertEquals("3.16.4", version.toString());
	}

	@Test
	void parsesPrereleaseSuffix() {
		HmclVersion version = HmclVersion.tryParse("HMCL-3.17.0-beta.1.jar");
		assertNotNull(version);
		assertEquals("3.17.0-beta.1", version.toString());
	}

	@Test
	void returnsNullWhenNoVersionPresent() {
		assertNull(HmclVersion.tryParse("HMCL.jar"));
	}

	@Test
	void higherMinorVersionIsNewer() {
		HmclVersion older = HmclVersion.tryParse("HMCL-3.15.0.jar");
		HmclVersion newer = HmclVersion.tryParse("HMCL-3.16.4.jar");
		assertTrue(older.compareTo(newer) < 0);
		assertTrue(newer.compareTo(older) > 0);
	}

	@Test
	void paddedZeroSegmentsAreEquivalent() {
		HmclVersion shortForm = HmclVersion.tryParse("HMCL-3.16.jar");
		HmclVersion longForm = HmclVersion.tryParse("HMCL-3.16.0.jar");
		assertEquals(0, shortForm.compareTo(longForm));
		assertTrue(shortForm.isEquivalentTo(longForm));
		assertEquals(shortForm.hashCode(), longForm.hashCode());
	}

	@Test
	void stableIsGreaterThanPrerelease() {
		HmclVersion stable = HmclVersion.tryParse("HMCL-3.17.0.jar");
		HmclVersion beta = HmclVersion.tryParse("HMCL-3.17.0-beta.1.jar");
		assertTrue(stable.compareTo(beta) > 0);
		assertTrue(beta.compareTo(stable) < 0);
	}
}
