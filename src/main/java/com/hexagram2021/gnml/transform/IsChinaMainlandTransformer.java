package com.hexagram2021.gnml.transform;

import javax.annotation.Nullable;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import com.hexagram2021.gnml.util.GnmlConstants;
import com.hexagram2021.gnml.util.GnmlLog;

/**
 * 将 HMCL 的中国大陆判定方法整体替换为 {@code return false} 的转换器喵~
 * <br/>
 * 当前目标（均为 {@code private static}、描述符 {@code ()Z}）：
 * <ul>
 *     <li>{@code org.jackhuang.hmcl.util.i18n.LocaleUtils#isChinaMainland()}</li>
 *     <li>{@code org.jackhuang.hmcl.util.RegionUtils#checkMachineIsChinaMainland()}（HMCL 较新版本引入）</li>
 * </ul>
 * 原理：这些方法的返回值被赋给非编译期静态常量（如 {@code IS_CHINA_MAINLAND}），
 * 方法恒返回 false 后，HMCL 任何位置读取这些字段都会拿到 false 喵~
 * <br/>
 * 失败策略（DR-D8）：命中目标类但未命中目标方法时抛出异常，让 JVM 中止该类的加载，
 * 从而阻止未经改造的 HMCL 被放行喵~
 *
 * @author liudongyu
 */
public final class IsChinaMainlandTransformer implements ClassFileTransformer {
	private static final int MAX_REPORTED_METHODS = 30;

	/** 目标类内部名 -> 目标方法名（描述符统一为 ()Z）喵~ */
	private static final Map<String, String> TARGET_METHODS = buildTargetMethods();

	private static Map<String, String> buildTargetMethods() {
		Map<String, String> targets = new HashMap<>();
		targets.put(GnmlConstants.TARGET_LOCALE_UTILS_INTERNAL_NAME,
				GnmlConstants.TARGET_IS_CHINA_MAINLAND_METHOD);
		targets.put(GnmlConstants.TARGET_REGION_UTILS_INTERNAL_NAME,
				GnmlConstants.TARGET_CHECK_MACHINE_IS_CHINA_MAINLAND_METHOD);
		return Collections.unmodifiableMap(targets);
	}

	/**
	 * 转换类文件喵~。仅当类名精确匹配目标表中的 HMCL 类时才会产生变更
	 *
	 * @param loader             定义该类的类加载器
	 * @param className          目标类内部名（以 {@code /} 分隔）
	 * @param classBeingRedefined 被重定义的类（初始加载时为 null）
	 * @param protectionDomain   保护域
	 * @param classFileBuffer    原始类文件字节
	 * @return 转换后的字节码；不匹配时返回 null 表示不转换
	 * @throws IllegalClassFormatException 命中目标类但转换失败或目标方法缺失（fail-fast，阻止 HMCL 启动）
	 */
	@Override
	@Nullable
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
							ProtectionDomain protectionDomain, byte[] classFileBuffer)
			throws IllegalClassFormatException {
		if (className == null) {
			return null;
		}
		String targetMethod = TARGET_METHODS.get(className);
		if (targetMethod == null) {
			return null;
		}
		String targetMethodId = targetMethod + GnmlConstants.TARGET_METHOD_DESC;
		GnmlLog.info("命中目标类 " + className + "，开始强制 " + targetMethodId + " -> false");
		try {
			ClassReader reader = new ClassReader(classFileBuffer);
			// 拷贝式写入：未命中的方法原样保留，目标方法整体替换为极简方法体
			ClassWriter writer = new ClassWriter(reader, 0);
			MethodCollector collector = new MethodCollector(targetMethodId);
			reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String descriptor,
												 String signature, String[] exceptions) {
					collector.record(name + descriptor);
					MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
					if (targetMethod.equals(name) && GnmlConstants.TARGET_METHOD_DESC.equals(descriptor)) {
						return new AlwaysFalseMethodVisitor(delegate, name + descriptor);
					}
					return delegate;
				}
			}, 0);
			if (!collector.foundTarget) {
				throw new IllegalClassFormatException("目标方法缺失，当前 HMCL 版本不兼容: "
						+ className.replace('/', '.') + "#" + targetMethod
						+ "，实际方法: " + collector.summarize());
			}
			return writer.toByteArray();
		} catch (IllegalClassFormatException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalClassFormatException("转换 " + className + " 失败: " + e.getMessage());
		}
	}

	/**
	 * 记录被访问到的方法，并标记是否命中目标方法喵~
	 */
	private static final class MethodCollector {
		private final String targetMethodId;
		private final StringBuilder visited = new StringBuilder();
		private boolean foundTarget;
		private int reported;

		MethodCollector(String targetMethodId) {
			this.targetMethodId = targetMethodId;
		}

		void record(String methodSignature) {
			if (targetMethodId.equals(methodSignature)) {
				foundTarget = true;
			}
			if (reported < MAX_REPORTED_METHODS) {
				if (reported > 0) {
					visited.append(", ");
				}
				visited.append(methodSignature);
				reported++;
			}
		}

		String summarize() {
			return reported == 0 ? "(无)" : visited.toString();
		}
	}

	/**
	 * 将目标方法整体替换为 {@code return false} 方法体的访问器喵~
	 * <br/>
	 * 不能只在入口插入 {@code ICONST_0; IRETURN}：HotSpot 栈图验证器要求无条件跳转之后的指令
	 * 必须有 StackMapTable 帧，残留的原始代码会触发 VerifyError 喵~
	 * 因此原始指令、标签、栈图、行号全部丢弃，只写出两字节的新方法体喵~
	 */
	private static final class AlwaysFalseMethodVisitor extends MethodVisitor {
		private final String methodId;

		AlwaysFalseMethodVisitor(MethodVisitor delegate, String methodId) {
			super(Opcodes.ASM9, delegate);
			this.methodId = methodId;
		}

		@Override
		public void visitCode() {
			// 仅写入 iconst_0; ireturn
			this.mv.visitCode();
			this.mv.visitInsn(Opcodes.ICONST_0);
			this.mv.visitInsn(Opcodes.IRETURN);
			GnmlLog.info("已将方法体替换为 return false: " + methodId);
		}

		@Override
		public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
			// 原始栈图随方法体一起作废
		}

		@Override
		public void visitInsn(int opcode) {
		}

		@Override
		public void visitIntInsn(int opcode, int operand) {
		}

		@Override
		public void visitVarInsn(int opcode, int varIndex) {
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
		}

		@Override
		@Deprecated
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
			// 兼容旧版调用，同样丢弃
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
										   Object... bootstrapMethodArguments) {
		}

		@Override
		public void visitJumpInsn(int opcode, Label label) {
		}

		@Override
		public void visitLabel(Label label) {
		}

		@Override
		public void visitLdcInsn(Object value) {
		}

		@Override
		public void visitIincInsn(int varIndex, int increment) {
		}

		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		}

		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		}

		@Override
		public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			// 异常表随原始方法体一起丢弃
		}

		@Override
		public void visitLocalVariable(String name, String descriptor, String signature,
									   Label start, Label end, int index) {
		}

		@Override
		@Nullable
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, @Nullable TypePath typePath,
															  Label[] start, Label[] end, int[] index,
															  String descriptor, boolean visible) {
			return null;
		}

		@Override
		public void visitLineNumber(int line, Label start) {
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			// 固定为静态无参布尔方法所需的最小栈帧：深度 1，无局部变量
			this.mv.visitMaxs(1, 0);
		}
	}
}
