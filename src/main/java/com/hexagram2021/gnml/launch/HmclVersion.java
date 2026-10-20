package com.hexagram2021.gnml.launch;

import javax.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从文件名解析出的 HMCL 版本号，用于多候选时选择最高版本喵~
 * <br/>
 * 解析规则：取文件名中第一个匹配 {@code 数字(.数字)*[-+预发布后缀]} 的片段喵~
 *
 * @author liudongyu
 */
public final class HmclVersion implements Comparable<HmclVersion> {
	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)*)([-+][A-Za-z0-9.\\-+]+)?");

	private final int[] numbers;
	@Nullable
	private final String preRelease;

	private HmclVersion(int[] numbers, @Nullable String preRelease) {
		this.numbers = numbers;
		this.preRelease = preRelease;
	}

	/**
	 * 尝试从文件名解析版本号喵~
	 *
	 * @param fileName 文件名（不含路径）
	 * @return 解析成功返回版本号；文件名中不含版本片段时返回 null 喵~
	 */
	@Nullable
	public static HmclVersion tryParse(String fileName) {
		// 先剥掉启动器扩展名，避免预发布后缀把 .jar / .exe 吞进去
		String name = stripLauncherExtension(fileName);
		Matcher matcher = VERSION_PATTERN.matcher(name);
		if (!matcher.find()) {
			return null;
		}
		String numberPart = matcher.group(1);
		String[] segments = numberPart.split("\\.");
		int[] numbers = new int[segments.length];
		for (int i = 0; i < segments.length; i++) {
			numbers[i] = Integer.parseInt(segments[i]);
		}
		return new HmclVersion(numbers, matcher.group(2));
	}

	private static String stripLauncherExtension(String fileName) {
		int dot = fileName.lastIndexOf('.');
		if (dot >= 0) {
			String extension = fileName.substring(dot + 1);
			if (extension.equalsIgnoreCase("jar") || extension.equalsIgnoreCase("exe")) {
				return fileName.substring(0, dot);
			}
		}
		return fileName;
	}

	/**
	 * 版本比较：逐段比较数字（缺失段补 0）；数字段相同时无预发布后缀者更大喵~
	 *
	 * @param other 目标版本
	 * @return 负数表示当前版本更旧，正数表示更新，0 表示等价喵~
	 */
	@Override
	public int compareTo(HmclVersion other) {
		int length = Math.max(this.numbers.length, other.numbers.length);
		for (int i = 0; i < length; i++) {
			int mine = i < this.numbers.length ? this.numbers[i] : 0;
			int theirs = i < other.numbers.length ? other.numbers[i] : 0;
			if (mine != theirs) {
				return Integer.compare(mine, theirs);
			}
		}
		// 数字段相同时，稳定版（无后缀）大于预发布版
		if (this.preRelease == null && other.preRelease != null) {
			return 1;
		}
		if (this.preRelease != null && other.preRelease == null) {
			return -1;
		}
		if (this.preRelease != null && other.preRelease != null) {
			return this.preRelease.compareTo(other.preRelease);
		}
		return 0;
	}

	/**
	 * 判断两个版本是否等价（数字段补零后一致且预发布状态一致）喵~
	 *
	 * @param other 目标版本
	 * @return 等价返回 true 喵~
	 */
	public boolean isEquivalentTo(HmclVersion other) {
		return this.compareTo(other) == 0;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return obj instanceof HmclVersion && this.compareTo((HmclVersion) obj) == 0;
	}

	@Override
	public int hashCode() {
		// 与 isEquivalentTo 保持一致：3.16 与 3.16.0 视为同一版本，先裁剪尾部的 0 再计算
		int end = numbers.length;
		while (end > 0 && numbers[end - 1] == 0) {
			end--;
		}
		int result = 1;
		for (int i = 0; i < end; i++) {
			result = 31 * result + numbers[i];
		}
		return 31 * result + (preRelease == null ? 0 : preRelease.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < numbers.length; i++) {
			if (i > 0) {
				builder.append('.');
			}
			builder.append(numbers[i]);
		}
		if (preRelease != null) {
			builder.append(preRelease);
		}
		return builder.toString();
	}
}
