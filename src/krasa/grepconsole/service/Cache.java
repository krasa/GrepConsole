package krasa.grepconsole.service;

import java.util.HashMap;
import java.util.Map;

import krasa.grepconsole.model.ModifiableConsoleViewContentType;

public class Cache {
	protected static Cache instance;
	private transient Map<String, ModifiableConsoleViewContentType> map = new HashMap<String, ModifiableConsoleViewContentType>();

	public static Cache getInstance() {
		if (instance == null) {
			instance = new Cache();
		}
		return instance;
	}

	public static void reset() {
		getInstance().setMap(new HashMap<String, ModifiableConsoleViewContentType>());
	}

	public Map<String, ModifiableConsoleViewContentType> getMap() {
		return map;
	}

	public void setMap(Map<String, ModifiableConsoleViewContentType> map) {
		this.map = map;
	}

	public ModifiableConsoleViewContentType get(String id) {
		return map.get(id);
	}

	public void put(String id, ModifiableConsoleViewContentType result) {
		map.put(id, result);
	}
}
