package krasa.grepconsole.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GrepExpressionFolder extends AbstractGrepModelElement {
	protected List<AbstractGrepModelElement> children;

	public GrepExpressionFolder() {
		this(null);
	}

	public GrepExpressionFolder(String id) {
		super(id);

		children = new LinkedList<AbstractGrepModelElement>();
	}

	@Override
	public String toString() {
		return getName();
	}

	public void add(AbstractGrepModelElement element) {
		add(element, -1);
	}

	/**
	 * Adds a new element at the specified index in the child list.
	 *
	 * @param element New element.
	 * @param index   Index. Values smaller than 0 refer to the end of the list.
	 */
	public void add(AbstractGrepModelElement element, int index) {
		if (element.getParent() != null) {
			element.getParent().remove(element);
		}

		if (index < 0) {
			index = children.size();
		} else if (index >= 0) {
			index = Math.min(index, children.size());
		}

		children.add(index, element);
		element.setParent(this);
	}

	/**
	 * Removes the specified child.
	 *
	 * @param element Element to remove.
	 * @return <code>true</code> if the element was removed, or <code>false</code> if it was not a child of this group.
	 */
	public boolean remove(AbstractGrepModelElement element) {
		if (children.remove(element)) {
			element.setParent(null);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns an unmodifiable list of all children.
	 *
	 * @return Children.
	 */
	public List<AbstractGrepModelElement> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * @see AbstractGrepModelElement#getAllIds(java.util.Set)
	 */
	@Override
	public Set<String> getAllIds(Set<String> ids) {
		super.getAllIds(ids);

		for (AbstractGrepModelElement child : children) {
			child.getAllIds(ids);
		}

		return ids;
	}

	/**
	 * @see AbstractGrepModelElement#rewriteDuplicateIds(java.util.Set)
	 */
	@Override
	public void rewriteDuplicateIds(Set<String> excludeIds) {
		super.rewriteDuplicateIds(excludeIds);

		for (AbstractGrepModelElement child : children) {
			child.rewriteDuplicateIds(excludeIds);
		}
	}

	/**
	 * Recursively finds the element with the specified ID among the children of this group.
	 * <p/>
	 * Note: This group itself is not found by the search.
	 *
	 * @param id Search ID.
	 * @return Element with a matching ID. <code>null</code> if no such element is found.
	 */
	public AbstractGrepModelElement findById(String id) {
		for (AbstractGrepModelElement child : children) {
			if (id.equals(child.getId())) {
				return child;
			} else if (child instanceof GrepExpressionFolder) {
				AbstractGrepModelElement found = ((GrepExpressionFolder) child).findById(id);

				if (found != null) {
					return found;
				}
			}
		}

		return null;
	}

	@Override
	public void findStyleUses(GrepStyle style, Set<GrepExpressionItem> items) {
		for (AbstractGrepModelElement child : getChildren()) {
			child.findStyleUses(style, items);
		}
	}

	@Override
	protected void refreshStyles() {
		for (AbstractGrepModelElement child : children) {
			child.refreshStyles();
		}
	}
}
