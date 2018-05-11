package krasa.grepconsole.model;

import java.util.UUID;

public abstract class AbstractGrepModelElement extends DomainObject {
	private String id;
	private String name;
	private boolean defaultEnabled;
	private boolean defaultFilter;

	public AbstractGrepModelElement(String id) {
		if (id == null) {
			this.id = generateId();
		} else {
			this.id = id;
		}

		defaultEnabled = true;
		defaultFilter = true;
	}

	public AbstractGrepModelElement generateNewId() {
		this.id = generateId();
		return this;
	}

	private static String generateId() {
		return UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDefaultEnabled() {
		return defaultEnabled;
	}

	public void setDefaultEnabled(boolean defaultEnabled) {
		this.defaultEnabled = defaultEnabled;
	}

	public boolean isDefaultFilter() {
		return defaultFilter;
	}

	public void setDefaultFilter(boolean defaultFilter) {
		this.defaultFilter = defaultFilter;
	}


}
