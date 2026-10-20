package com.hexagram2021.gnml.launch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.hexagram2021.gnml.util.GnmlLog;

/**
 * HMCL 子进程启动器喵~
 * <br/>
 * 以 {@code java -javaagent:<GNML.jar> -jar <HMCL>} 拉起 HMCL，等待其退出并返回退出码喵~
 *
 * @author liudongyu
 */
public final class HmclProcessLauncher {
	/**
	 * 启动 HMCL 子进程并等待其退出喵~
	 *
	 * @param javaCommand java 可执行文件（绝对路径或 PATH 中的 {@code java}）
	 * @param hmclFile    HMCL 启动器文件绝对路径
	 * @param agentJar    GNML 制品自身 jar 的绝对路径（作为 -javaagent 参数）
	 * @param workDir     子进程工作目录（继承自 GNML 进程）
	 * @return HMCL 子进程的退出码
	 * @throws IOException          子进程创建失败
	 * @throws InterruptedException 等待期间被中断（中断位已被调用方恢复）
	 */
	public static int launch(String javaCommand, Path hmclFile, Path agentJar, Path workDir)
			throws IOException, InterruptedException {
		List<String> command = new ArrayList<>();
		command.add(javaCommand);
		command.add("-javaagent:" + agentJar.toAbsolutePath());
		command.add("-jar");
		command.add(hmclFile.toAbsolutePath().toString());
		GnmlLog.info("启动 HMCL: " + command);

		// inheritIO：HMCL 控制台输出直接透传给用户，避免缓冲与死锁
		Process process = new ProcessBuilder(command)
				.directory(workDir.toFile())
				.inheritIO()
				.start();
		return process.waitFor();
	}

	private HmclProcessLauncher() {
	}
}
