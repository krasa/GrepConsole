package krasa.grepconsole.service;

import krasa.grepconsole.model.ModifiableConsoleViewContentType;

import java.util.HashMap;
import java.util.Map;

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

	public ModifiableConsoleViewContentType get(String s) {
		return map.get(s);
	}

	public void put(String s, ModifiableConsoleViewContentType result) {
		 map.put(s,result);
	}
}
