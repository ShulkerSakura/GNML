package org.jackhuang.hmcl.util;

import java.time.ZoneId;

/**
 * 模拟 HMCL RegionUtils 结构的测试替身（仅存在于测试源集）喵~
 * <br/>
 * {@code checkMachineIsChinaMainland} 恒返回 true（借助非编译期常量表达式），用于验证转换后变为 false 喵~
 */
public final class RegionUtilsFixture {
	private static final boolean IS_CHINA_MAINLAND_MACHINE = checkMachineIsChinaMainland();

	/**
	 * 模拟 HMCL 的公开读取入口喵~
	 *
	 * @return 机器是否位于中国大陆（未改造时恒为 true）
	 */
	public static boolean isIsChinaMainlandMachine() {
		return IS_CHINA_MAINLAND_MACHINE;
	}

	private static boolean checkMachineIsChinaMainland() {
		// 恒为 true，但不是编译期常量，JVM 需在类初始化中实际调用
		return !"never".equals(ZoneId.systemDefault().getId());
	}

	private RegionUtilsFixture() {
	}
}
