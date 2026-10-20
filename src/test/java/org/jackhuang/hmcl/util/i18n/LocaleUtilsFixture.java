package org.jackhuang.hmcl.util.i18n;

import java.time.ZoneId;

/**
 * 模拟 HMCL LocaleUtils 结构的测试替身（仅存在于测试源集）喵~
 * <br/>
 * {@code isChinaMainland} 恒返回 true（借助非编译期常量表达式），用于验证转换后变为 false 喵~
 */
public final class LocaleUtilsFixture {
	public static final boolean IS_CHINA_MAINLAND = isChinaMainland();

	private static boolean isChinaMainland() {
		// 恒为 true，但不是编译期常量，JVM 需在 <clinit> 中实际调用
		return !"never".equals(ZoneId.systemDefault().getId());
	}

	private LocaleUtilsFixture() {
	}
}
