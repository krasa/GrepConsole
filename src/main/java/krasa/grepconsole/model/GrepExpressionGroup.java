package krasa.grepconsole.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class GrepExpressionGroup extends DomainObject {
	private String name = "default";
	private List<GrepExpressionItem> grepExpressionItems = new ArrayList<>();

	public GrepExpressionGroup() {
	}

	public GrepExpressionGroup(String name, List<GrepExpressionItem> grepExpressionItems) {
		this.name = name;
		this.grepExpressionItems = grepExpressionItems;
	}

	public GrepExpressionGroup(String name) {
		this.name = name;
	}

	public String getName() {
		if (name == null) {
			return "default";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<GrepExpressionItem> getGrepExpressionItems() {
		return grepExpressionItems;
	}

	public void setGrepExpressionItems(List<GrepExpressionItem> grepExpressionItems) {
		this.grepExpressionItems = grepExpressionItems;
	}

	public void add(GrepExpressionItem newItem) {
		getGrepExpressionItems().add(newItem);
	}
}
