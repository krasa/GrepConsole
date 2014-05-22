package krasa.grepconsole.integration;

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * @author Vojtech Krasa
 */
public class WindowsRegistryChange {
	private static final Logger log = Logger.getInstance(WindowsRegistryChange.class);

	public static final String HTTP_CLIENT_JAR = "GrepConsole-http-client.jar";

	public static final String CLASSES = "Software\\Classes\\*\\shell\\";
	public static final String CLASSES_GREPCONSOLE = CLASSES + getGrepConsoleRegistryName();
	public static final String CLASSES_GREPCONSOLE_COMMAND = CLASSES_GREPCONSOLE + "\\command";
	public static final WinReg.HKEY CURRENT_USER = WinReg.HKEY_CURRENT_USER;

	public static void main(String[] args) {
		final String jarPath = "F:\\workspace\\_projekty\\Github\\" + getGrepConsoleRegistryName() + "\\lib\\"
				+ HTTP_CLIENT_JAR;
		setup(jarPath, 8093);
	}

	public static String getGrepConsoleRegistryName() {
		return "GrepConsole" + ApplicationNamesInfo.getInstance().getProductName();
	}

	public static void setup(String jarPath, final int port) {
		log.info("registering " + jarPath + ",  port=" + port);

		Advapi32Util.registryCreateKey(CURRENT_USER, "Software\\Classes\\*\\shell");
		Advapi32Util.registryCreateKey(CURRENT_USER, CLASSES_GREPCONSOLE);
		Advapi32Util.registrySetStringValue(CURRENT_USER, CLASSES_GREPCONSOLE, null, "Open in " + getProductName()
				+ " console");
		Advapi32Util.registryCreateKey(CURRENT_USER, CLASSES_GREPCONSOLE_COMMAND);
		Advapi32Util.registrySetStringValue(CURRENT_USER, CLASSES_GREPCONSOLE_COMMAND, null, getCommand(jarPath, port));
	}

	private static String getProductName() {
		return ApplicationNamesInfo.getInstance().getFullProductName();
	}

	public static String getCommand(String jarPath, int port) {
		return "javaw -jar \"" + jarPath + "\" " + port + " %1";
	}

	public static boolean isSetupped(String jarPath, final int port) {
		if (!Advapi32Util.registryKeyExists(CURRENT_USER, CLASSES_GREPCONSOLE)) {
			return false;
		}

		String s = Advapi32Util.registryGetStringValue(CURRENT_USER, CLASSES_GREPCONSOLE_COMMAND, null);

		return s.equals(getCommand(jarPath, port));
	}

	public static void remove() {
		Advapi32Util.registryDeleteKey(CURRENT_USER, CLASSES_GREPCONSOLE_COMMAND);
		Advapi32Util.registryDeleteKey(CURRENT_USER, CLASSES_GREPCONSOLE);
	}
}
