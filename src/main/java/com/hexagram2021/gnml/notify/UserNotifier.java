package com.hexagram2021.gnml.notify;

import java.awt.AWTError;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.hexagram2021.gnml.util.GnmlConstants;
import com.hexagram2021.gnml.util.GnmlLog;

/**
 * 未找到 HMCL 时的用户引导喵~
 * <br/>
 * 行为（DR-D5）：尝试打开浏览器跳转 HMCL 发布页，并弹出警告窗口；
 * headless 或 GUI 失败时降级为向 stderr 输出下载地址喵~
 *
 * @author liudongyu
 */
public final class UserNotifier {
	private static final String DIALOG_TITLE = "Good Night Minecraft Launcher";

	/**
	 * 通知用户未找到 HMCL 并引导下载喵~
	 * <br/>
	 * 本方法只负责提示，退出码由调用方（main）决定喵~
	 *
	 * @param scannedDirs 已扫描过的目录列表，用于提示用户放置位置
	 */
	public static void notifyHmclNotFound(List<Path> scannedDirs) {
		String url = GnmlConstants.HMCL_RELEASES_URL;
		boolean headless = GraphicsEnvironment.isHeadless();

		boolean browseSucceeded = false;
		if (!headless && isBrowseSupported()) {
			browseSucceeded = browse(url);
		}

		String message = buildMessage(scannedDirs, url, browseSucceeded, headless);
		if (headless) {
			// headless：仅输出到 stderr
			printLines(message);
			return;
		}
		showDialog(message);
	}

	private static String buildMessage(List<Path> scannedDirs, String url, boolean browseSucceeded, boolean headless) {
		StringBuilder builder = new StringBuilder();
		builder.append("未找到 HMCL 启动器。\n\n");
		builder.append("已扫描的目录：\n");
		for (Path dir : scannedDirs) {
			builder.append("  - ").append(dir).append('\n');
		}
		builder.append('\n');
		if (headless) {
			builder.append("请手动打开 HMCL 官方发布页下载：\n").append(url).append('\n');
		} else if (browseSucceeded) {
			builder.append("已在浏览器中打开 HMCL 官方发布页：\n").append(url).append('\n');
		} else {
			builder.append("无法自动打开浏览器，请手动访问 HMCL 官方发布页：\n").append(url).append('\n');
		}
		builder.append("\n请将下载的 HMCL 放入上述目录后重新运行 GNML。");
		return builder.toString();
	}

	private static boolean isBrowseSupported() {
		try {
			return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
		} catch (HeadlessException e) {
			return false;
		}
	}

	private static boolean browse(String url) {
		try {
			Desktop.getDesktop().browse(URI.create(url));
			return true;
		} catch (Exception e) {
			GnmlLog.warn("打开浏览器失败，将仅通过窗口提示下载地址", e);
			return false;
		}
	}

	private static void showDialog(String message) {
		try {
			// 置顶并前置：避免浏览器打开后把警告窗口盖住（用户报告过该问题）
			JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE);
			JDialog dialog = pane.createDialog(null, DIALOG_TITLE);
			dialog.setAlwaysOnTop(true);
			dialog.toFront();
			dialog.setVisible(true);
		} catch (RuntimeException | AWTError e) {
			// GUI 不可用（无显示服务器等）：降级输出到 stderr
			GnmlLog.warn("警告窗口弹出失败，降级为控制台输出", e);
			printLines(message);
		}
	}

	private static void printLines(String message) {
		for (String line : message.split("\n")) {
			System.err.println("[GNML] " + line);
		}
	}

	private UserNotifier() {
	}
}
