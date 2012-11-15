package krasa.grepconsole;

import java.util.HashMap;
import java.util.Map;

import com.intellij.openapi.editor.markup.TextAttributes;

public class Cache {
	protected static Cache instance;
	private Map<String, TextAttributes> map = new HashMap<String, TextAttributes>();

	public static Cache getInstance() {
		if (instance == null) {
			instance = new Cache();
		}
		return instance;
	}

	public static void reset() {
		getInstance().setMap(new HashMap<String, TextAttributes>());
	}

	public Map<String, TextAttributes> getMap() {
		return map;
	}

	public void setMap(Map<String, TextAttributes> map) {
		this.map = map;
	}

	public TextAttributes get(String id) {
		return map.get(id);
	}

	public void put(String id, TextAttributes result) {
		map.put(id, result);
	}

}
