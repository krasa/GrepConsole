package krasa.grepconsole.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;


public class GrepExpressionRootFolder extends GrepExpressionFolder {
	/**
	 * Map of styles (id/style).
	 */
	private HashMap<String, GrepStyle> styles;

	/**
	 * Creates a new instance, generating a new ID.
	 */
	public GrepExpressionRootFolder() {
		super();

		styles = new HashMap<String, GrepStyle>();
	}

	public Collection<GrepStyle> getStyles() {
		return styles.values();
	}

	/**
	 * Sets a new collection of styles. Style references of all items will be updated using their ID.
	 * 
	 * @param styles
	 *            New styles.
	 */
	public void setStyles(Collection<GrepStyle> styles) {
		this.styles.clear();

		for (GrepStyle style : styles) {
			if (style == null) {
				continue;
			}

			this.styles.put(style.getId(), style);
		}

		refreshStyles();
	}

	public GrepStyle getStyle(String id) {
		return styles.get(id);
	}

	/**
	 * @see AbstractGrepModelElement#isDefaultEnabled()
	 */
	@Override
	public boolean isDefaultEnabled() {
		return true;
	}

	/**
	 * @see AbstractGrepModelElement#isDefaultFilter()
	 */
	@Override
	public boolean isDefaultFilter() {
		return true;
	}

	public void addStyle(GrepStyle style) {
		styles.put(style.getId(), style);
	}

	public void removeStyle(GrepStyle style) {
		styles.remove(style.getId());
	}

	/**
	 * Iterates through all child elements and adds all referenced styles that are not yet included in the styles map to
	 * the map.
	 * <p/>
	 * Call this after new elements with new styles have been added.
	 */
	public void addMissingStyles() {
		LinkedList<AbstractGrepModelElement> queue = new LinkedList<AbstractGrepModelElement>();
		queue.add(this);

		while (!queue.isEmpty()) {
			AbstractGrepModelElement element = queue.removeFirst();

			if (element instanceof GrepExpressionFolder) {
				queue.addAll(((GrepExpressionFolder) element).getChildren());
			} else if (element instanceof GrepExpressionItem) {
				GrepExpressionItem item = (GrepExpressionItem) element;
				GrepStyle itemStyles = item.getStyle();

				if (itemStyles == null) {
					continue;
				}

				if (!styles.containsKey(itemStyles.getId())) {
					styles.put(itemStyles.getId(), itemStyles);
				}
			}
		}
	}
}
