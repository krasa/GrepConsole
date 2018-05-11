package krasa.grepconsole.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ExtensionManager {
	public static Map<String, Function<String, String>> actions = new HashMap<>();

	public static void registerAction(@NotNull String name, Function<String, String> computable) {
		actions.put(name, computable);
	}

	public static void unregisterAction(@NotNull String name) {
		actions.remove(name);
	}

	public static Collection<? extends String> references() {
		return actions.keySet();
	}

	public static Function<String, String> getAction(String name) {
		return actions.get(name);
	}
}
