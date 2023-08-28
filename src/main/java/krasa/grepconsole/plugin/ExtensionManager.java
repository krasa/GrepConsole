package krasa.grepconsole.plugin;

import krasa.grepconsole.utils.Notifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ExtensionManager {
	public static Map<String, Object> functions = new ConcurrentHashMap<>();

	/**
	 * Function: return null to remove a line, or return text which will be printed to console
	 */
	public static void registerFunction(@NotNull String name, Function<String, String> computable) {
		functions.put(name, computable);
		Notifier.extensions.clear();
	}

	public static void register(@NotNull String name, Object function) {
		functions.put(name, function);
		Notifier.extensions.clear();
	}

	public static void unregisterFunction(@NotNull String name) {
		functions.remove(name);
		Notifier.extensions.clear();
	}

	public static Collection<? extends String> references() {
		return functions.keySet();
	}

	public static Object getFunction(String name) {
		return functions.get(name);
	}
}
