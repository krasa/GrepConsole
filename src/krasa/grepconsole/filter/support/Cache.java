package krasa.grepconsole.filter.support;

import com.intellij.execution.ui.ConsoleViewContentType;

import java.util.HashMap;
import java.util.Map;

public class Cache {
	protected static Cache instance;
	private Map<String, ConsoleViewContentType> map = new HashMap<>();

	public static Cache getInstance() {
		if (instance == null) {
			instance = new Cache();
		}
		return instance;
	}

	public static void reset() {
		getInstance().setMap(new HashMap<>());
	}

	public Map<String, ConsoleViewContentType> getMap() {
		return map;
	}

	public void setMap(Map<String, ConsoleViewContentType> map) {
		this.map = map;
	}

	public ConsoleViewContentType get(String id) {
		return map.get(id);
	}

	public void put(String id, ConsoleViewContentType result) {
		map.put(id, result);
	}
}
