package org.jackhuang.hmcl.util.i18n;

/**
 * 缺失 isChinaMainland 方法的测试替身，用于验证 fail-fast 喵~
 */
public final class LocaleUtilsStub {
	public static final String UNRELATED = "stub";

	public String unrelatedMethod() {
		return UNRELATED;
	}

	private LocaleUtilsStub() {
	}
}
