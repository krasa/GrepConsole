package krasa.grepconsole.gui.table;

import com.intellij.ui.CheckedTreeNode;

/**
 * @author Vojtech Krasa
 */
public class ModelUtils {
	public static <Item> Item unWrap(Item item) {
		if (item instanceof CheckedTreeNode) {
			CheckedTreeNode item1 = (CheckedTreeNode) item;
			item = (Item) item1.getUserObject();
		}
		return item;
	}
}
