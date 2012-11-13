package krasa.grepconsole.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Set;
import java.util.UUID;


public abstract class AbstractGrepModelElement extends DomainObject{
	private String id;
	private String name;
	private boolean defaultEnabled;
	private boolean defaultFilter;
	private GrepExpressionFolder parent;

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

	public GrepExpressionRootFolder getRoot() {
		AbstractGrepModelElement root = this;

		while (root != null && !(root instanceof GrepExpressionRootFolder)) {
			root = root.getParent();
		}

		return (GrepExpressionRootFolder) root;
	}

	/**
	 * Fills the specified set with all items in the tree, starting with this element, that use the specified style.
	 *
	 * @param style Style.
	 * @param items Set to be filled with items using the style.
	 */
	public abstract void findStyleUses(GrepStyle style, Set<GrepExpressionItem> items);

	/**
	 * Re-sets all style references based on their IDs. Call this if style instances have changed in the root element.
	 */
	protected abstract void refreshStyles();

	/**
	 * Fills a set of all IDs in the element tree starting with this element.
	 *
	 * @param ids Set to which the gathered IDs are added.
	 * @return A reference to the specified <code>ids</code> set, for convenience.
	 */
	public Set<String> getAllIds(Set<String> ids) {
		ids.add(id);
		return ids;
	}

	/**
	 * Starting at this element in the tree, regenerates the IDs for all elements that have IDs contained in the
	 * specified exclusion set.
	 *
	 * @param excludeIds Set of IDs which should not be used. During execution, this method may add new IDs to this set.
	 */
	public void rewriteDuplicateIds(Set<String> excludeIds) {
		if (excludeIds.contains(id)) {
			id = generateId();
			excludeIds.add(id);
		}
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

	public GrepExpressionFolder getParent() {
		return parent;
	}

	public void setParent(GrepExpressionFolder parent) {
		this.parent = parent;
	}
	
}
