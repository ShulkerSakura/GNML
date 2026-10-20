package com.hexagram2021.gnml.util;

/**
 * GNML 全局常量喵~
 *
 * @author liudongyu
 */
public final class GnmlConstants {
	/** HMCL 启动器主类（Manifest Main-Class 判定值）喵~ */
	public static final String HMCL_MAIN_CLASS = "org.jackhuang.hmcl.Main";

	/** HMCL 官方发布页喵~ */
	public static final String HMCL_RELEASES_URL = "https://github.com/HMCL-dev/HMCL/releases";

	/** 需要改造的目标类内部名喵~ */
	public static final String TARGET_LOCALE_UTILS_INTERNAL_NAME = "org/jackhuang/hmcl/util/i18n/LocaleUtils";

	/** 需要改造的目标方法名喵~ */
	public static final String TARGET_IS_CHINA_MAINLAND_METHOD = "isChinaMainland";

	/** 需要改造的目标方法描述符（无参、返回 boolean）喵~ */
	public static final String TARGET_IS_CHINA_MAINLAND_DESC = "()Z";

	/** 退出码：成功喵~ */
	public static final int EXIT_CODE_SUCCESS = 0;

	/** 退出码：一般性错误或用法错误喵~ */
	public static final int EXIT_CODE_ERROR = 1;

	/** 退出码：未找到 HMCL 启动器喵~ */
	public static final int EXIT_CODE_HMCL_NOT_FOUND = 2;

	/** 退出码：无法解析 java 可执行文件喵~ */
	public static final int EXIT_CODE_JAVA_NOT_FOUND = 3;

	private GnmlConstants() {
	}
}
