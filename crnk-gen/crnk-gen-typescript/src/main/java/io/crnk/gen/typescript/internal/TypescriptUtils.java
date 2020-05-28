package io.crnk.gen.typescript.internal;

import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.gen.typescript.model.TSContainerElement;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSModule;
import io.crnk.gen.typescript.model.TSType;
import io.crnk.meta.model.MetaDataObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TypescriptUtils {

	private TypescriptUtils() {
	}

	public static void copyFile(File sourceFile, File targetFile) {
		try {
			byte[] bytes = readFully(new FileInputStream(sourceFile));
			FileOutputStream out = new FileOutputStream(targetFile);
			out.write(bytes);
			out.close();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static byte[] readFully(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	public static boolean isInstance(Class<?> clazz, String name) {
		if (clazz.getName().equals(name)) { // NOSONAR no access to String type names here due to classpath
			return true;
		}
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null && isInstance(superclass, name)) {
			return true;
		}
		for (Class<?> interfaceClass : clazz.getInterfaces()) {
			if (interfaceClass != null && isInstance(interfaceClass, name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a nested interface for the given type (using a module with the same name as the type).
	 */
	public static TSInterfaceType getNestedInterface(TSType type, String name, boolean create) {
		TSModule module = getNestedTypeContainer(type, create);
		if (module == null && !create) {
			return null;
		} else if (module == null) {
			throw new IllegalStateException("cannot setup interface as no parent container is available");
		}

		for (TSElement element : module.getElements()) {
			if (element instanceof TSInterfaceType && ((TSInterfaceType) element).getName().equals(name)) {
				return (TSInterfaceType) element;
			}
		}
		if (create) {
			TSInterfaceType interfaceType = new TSInterfaceType();
			interfaceType.setExported(true);
			interfaceType.setName(name);
			interfaceType.setParent(module);
			module.getElements().add(interfaceType);
			return interfaceType;
		} else {
			return null;
		}
	}

	/**
	 * Creates a module if the same name as the provided type used to hold nested types.
	 */
	public static TSModule getNestedTypeContainer(TSType type, boolean create) {
		TSContainerElement parent = (TSContainerElement) type.getParent();
		if (parent == null) {
			return null;
		}
		int insertionIndex = parent.getElements().indexOf(type);
		return getModule(parent, type.getName(), insertionIndex, create);
	}

	public static TSModule getModule(TSContainerElement parent, String name, int insertionIndex, boolean create) {
		for (TSElement element : parent.getElements()) {
			if (element instanceof TSModule && ((TSModule) element).getName().equals(name)) {
				return (TSModule) element;
			}
		}
		if (create) {
			TSModule module = new TSModule();
			module.setName(name);
			module.setParent(parent);
			module.setExported(true);

			// module needs to come before type definition
			parent.getElements().add(insertionIndex, module);
			return module;
		} else {
			return null;
		}
	}

	public static String firstToUpper(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * transforms "helloWorld" to "hello.world" to more closely resemble typical typescript naming.
	 */
	public static String toFileName(String name) {
		// dots are treated as directories
		int sep = name.lastIndexOf(".");
		if (sep != -1) {
			name = name.substring(sep + 1);
		}

		// move from camel case to lower case
		char[] charArray = name.toCharArray();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < charArray.length; i++) {
			if (Character.isUpperCase(charArray[i]) && i > 0 && !Character.isUpperCase(charArray[i - 1])) {
				builder.append('.');
			}
			builder.append(Character.toLowerCase(charArray[i]));
		}
		return builder.toString();
	}

	public static String toClassName(MetaDataObject metaDataObject) {
		String name = metaDataObject.getName();
		int sep = name.lastIndexOf(".");
		if (sep != -1) {
			return StringUtils.firstToUpper(name.substring(sep + 1));
		}
		return name;
	}
}
