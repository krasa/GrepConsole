package krasa.grepconsole;

import java.util.HashMap;
import java.util.Map;

import com.intellij.execution.ui.ConsoleViewContentType;

public class Cache {
	protected static Cache instance;
	private Map<String, ConsoleViewContentType> map = new HashMap<String, ConsoleViewContentType>();

	public static Cache getInstance() {
		if (instance == null) {
			instance = new Cache();
		}
		return instance;
	}

	public static void reset() {
		getInstance().setMap(new HashMap<String, ConsoleViewContentType>());
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
