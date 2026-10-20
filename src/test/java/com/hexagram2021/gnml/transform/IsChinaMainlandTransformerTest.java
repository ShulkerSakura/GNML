package com.hexagram2021.gnml.transform;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;

import org.junit.jupiter.api.Test;

/**
 * {@link IsChinaMainlandTransformer} 的字节码级单元测试喵~
 * <br/>
 * 以测试替身类的字节码冒充 HMCL 的 LocaleUtils，验证注入后字段值由 true 变 false 喵~
 */
class IsChinaMainlandTransformerTest {
	private static final String TARGET_INTERNAL_NAME = "org/jackhuang/hmcl/util/i18n/LocaleUtils";
	private static final String FIXTURE_CLASS_NAME = "org.jackhuang.hmcl.util.i18n.LocaleUtilsFixture";
	private static final String STUB_CLASS_NAME = "org.jackhuang.hmcl.util.i18n.LocaleUtilsStub";
	private static final String REGION_TARGET_INTERNAL_NAME = "org/jackhuang/hmcl/util/RegionUtils";
	private static final String REGION_FIXTURE_CLASS_NAME = "org.jackhuang.hmcl.util.RegionUtilsFixture";

	@Test
	void injectionForcesIsChinaMainlandToFalse() throws Exception {
		byte[] originalBytes = loadClassBytes(FIXTURE_CLASS_NAME);
		byte[] transformedBytes = transform(originalBytes, TARGET_INTERNAL_NAME);

		// 未改造：字段恒为 true（defineClass 不允许改名，两个加载器各自加载同名类）
		Class<?> originalClass = new DefiningClassLoader().define(FIXTURE_CLASS_NAME, originalBytes);
		assertTrue((Boolean) originalClass.getField("IS_CHINA_MAINLAND").get(null));

		// 改造后：字段恒为 false
		Class<?> transformedClass = new DefiningClassLoader().define(FIXTURE_CLASS_NAME, transformedBytes);
		assertFalse((Boolean) transformedClass.getField("IS_CHINA_MAINLAND").get(null));
	}

	@Test
	void injectionForcesCheckMachineIsChinaMainlandToFalse() throws Exception {
		byte[] originalBytes = loadClassBytes(REGION_FIXTURE_CLASS_NAME);
		byte[] transformedBytes = transform(originalBytes, REGION_TARGET_INTERNAL_NAME);

		// 未改造：公开读取入口返回 true
		Class<?> originalClass = new DefiningClassLoader().define(REGION_FIXTURE_CLASS_NAME, originalBytes);
		assertTrue((Boolean) originalClass.getMethod("isIsChinaMainlandMachine").invoke(null));

		// 改造后：公开读取入口返回 false
		Class<?> transformedClass = new DefiningClassLoader().define(REGION_FIXTURE_CLASS_NAME, transformedBytes);
		assertFalse((Boolean) transformedClass.getMethod("isIsChinaMainlandMachine").invoke(null));
	}

	@Test
	void unrelatedClassesAreNotTouched() throws IOException, IllegalClassFormatException {
		byte[] originalBytes = loadClassBytes(FIXTURE_CLASS_NAME);

		assertNull(new IsChinaMainlandTransformer().transform(
				getClass().getClassLoader(), "com/example/Other", null, null, originalBytes));
	}

	@Test
	void missingTargetMethodFailsFast() throws IOException {
		byte[] stubBytes = loadClassBytes(STUB_CLASS_NAME);

		IllegalClassFormatException exception = assertThrows(IllegalClassFormatException.class,
				() -> transform(stubBytes, TARGET_INTERNAL_NAME));

		assertTrue(exception.getMessage().contains("isChinaMainland"));
	}

	private static byte[] transform(byte[] classBytes, String targetInternalName)
			throws IllegalClassFormatException {
		byte[] result = new IsChinaMainlandTransformer().transform(
				IsChinaMainlandTransformerTest.class.getClassLoader(), targetInternalName, null, null, classBytes);
		if (result == null) {
			throw new IllegalStateException("目标类不应被跳过");
		}
		return result;
	}

	private static byte[] loadClassBytes(String className) throws IOException {
		String resource = className.replace('.', '/') + ".class";
		try (InputStream stream = IsChinaMainlandTransformerTest.class.getClassLoader().getResourceAsStream(resource)) {
			if (stream == null) {
				throw new IOException("测试资源缺失: " + resource);
			}
			return stream.readAllBytes();
		}
	}

	/**
	 * 一次性定义类的类加载器喵~
	 */
	private static final class DefiningClassLoader extends ClassLoader {
		DefiningClassLoader() {
			super(IsChinaMainlandTransformerTest.class.getClassLoader());
		}

		Class<?> define(String name, byte[] bytes) {
			return defineClass(name, bytes, 0, bytes.length);
		}
	}
}
