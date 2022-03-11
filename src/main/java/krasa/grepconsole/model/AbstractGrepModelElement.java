package krasa.grepconsole.model;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractGrepModelElement extends DomainObject {

	public static AtomicInteger atomicInteger = new AtomicInteger();

	@Transient
	private String id;
	private String name;
	private boolean defaultEnabled;
	private boolean defaultFilter;

	public AbstractGrepModelElement() {
		this.id = generateId();

		defaultEnabled = true;
		defaultFilter = true;
	}

	public AbstractGrepModelElement generateNewId() {
		this.id = generateId();
		return this;
	}

	private static String generateId() {
		return String.valueOf(atomicInteger.incrementAndGet());
	}

	@Transient
	public String getId() {
		return id;
	}

	@Transient
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
