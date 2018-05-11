package krasa.grepconsole.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ExtensionManager {
	public static Map<String, Function<String, String>> functions = new HashMap<>();

	public static void registerFunction(@NotNull String name, Function<String, String> computable) {
		functions.put(name, computable);
	}

	public static void unregisterFunction(@NotNull String name) {
		functions.remove(name);
	}

	public static Collection<? extends String> references() {
		return functions.keySet();
	}

	public static Function<String, String> getFunction(String name) {
		return functions.get(name);
	}
}
