package com.hexagram2021.gnml.launch;

import javax.annotation.Nullable;

import java.nio.file.Path;

/**
 * 一个被确认为 HMCL 启动器的候选文件喵~
 *
 * @author liudongyu
 */
public final class HmclCandidate {
	private final Path file;
	@Nullable
	private final HmclVersion version;

	/**
	 * 构造候选喵~
	 *
	 * @param file    HMCL 文件绝对路径
	 * @param version 从文件名解析出的版本号，解析失败时为 null 喵~
	 */
	public HmclCandidate(Path file, @Nullable HmclVersion version) {
		this.file = file;
		this.version = version;
	}

	/**
	 * 获取 HMCL 文件路径喵~
	 *
	 * @return 文件绝对路径喵~
	 */
	public Path getFile() {
		return file;
	}

	/**
	 * 获取解析出的版本号喵~
	 *
	 * @return 版本号；文件名无法解析出版本时为 null 喵~
	 */
	@Nullable
	public HmclVersion getVersion() {
		return version;
	}

	/**
	 * 判断该候选是否为 jar（而非 exe）喵~，用于同版本平局裁决
	 *
	 * @return 扩展名为 jar 时返回 true 喵~
	 */
	public boolean isJar() {
		String name = file.getFileName().toString();
		int dot = name.lastIndexOf('.');
		return dot >= 0 && name.substring(dot + 1).equalsIgnoreCase("jar");
	}

	@Override
	public String toString() {
		return file.toString() + (version == null ? " (unknown version)" : " (" + version + ")");
	}
}
