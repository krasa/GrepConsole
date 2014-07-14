package krasa.grepconsole.gui.table;

import static com.intellij.ui.CheckboxTreeBase.NodeState;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.util.ui.UIUtil;

public class CheckboxTreeCellRendererBase extends JPanel implements TreeCellRenderer {
	public final JCheckBox myCheckbox;
	private final ColoredTreeCellRenderer myTextRenderer;
	private final boolean myUsePartialStatusForParentNodes;

	public CheckboxTreeCellRendererBase(boolean opaque) {
		this(opaque, true);
	}

	public CheckboxTreeCellRendererBase(boolean opaque, final boolean usePartialStatusForParentNodes) {
		super(new BorderLayout());
		myUsePartialStatusForParentNodes = usePartialStatusForParentNodes;
		myCheckbox = new JCheckBox();
		myTextRenderer = new ColoredTreeCellRenderer() {
			public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
			}
		};
		myTextRenderer.setOpaque(opaque);
		myTextRenderer.setMyBorder(null);
		add(myCheckbox, BorderLayout.WEST);
		add(myTextRenderer, BorderLayout.CENTER);
	}

	public CheckboxTreeCellRendererBase() {
		this(true);
	}

	public final Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		invalidate();
		if (value instanceof CheckedTreeNode) {
			CheckedTreeNode node = (CheckedTreeNode) value;

			NodeState state = getNodeStatus(node);
			myCheckbox.setVisible(true);
			myCheckbox.setSelected(state != NodeState.CLEAR);
			myCheckbox.setEnabled(node.isEnabled() && state != NodeState.PARTIAL);
			myCheckbox.setOpaque(false);
			myCheckbox.setBackground(null);
			setBackground(null);
		} else {
			myCheckbox.setVisible(false);
		}
		myTextRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if (UIUtil.isUnderGTKLookAndFeel()) {
			final Color background = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
			UIUtil.changeBackGround(this, background);
		} else if (UIUtil.isUnderNimbusLookAndFeel()) {
			UIUtil.changeBackGround(this, UIUtil.TRANSPARENT_COLOR);
		}
		customizeRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
		revalidate();

		return this;
	}

	private NodeState getNodeStatus(final CheckedTreeNode node) {
		final boolean checked = node.isChecked();
		if (node.getChildCount() == 0 || !myUsePartialStatusForParentNodes)
			return checked ? NodeState.FULL : NodeState.CLEAR;

		NodeState result = null;

		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			NodeState childStatus = child instanceof CheckedTreeNode ? getNodeStatus((CheckedTreeNode) child)
					: checked ? NodeState.FULL : NodeState.CLEAR;
			if (childStatus == NodeState.PARTIAL)
				return NodeState.PARTIAL;
			if (result == null) {
				result = childStatus;
			} else if (result != childStatus) {
				return NodeState.PARTIAL;
			}
		}

		return result == null ? NodeState.CLEAR : result;
	}

	/**
	 * Should be implemented by concrete implementations. This method is invoked only for customization of component.
	 * All component attributes are cleared when this method is being invoked. Note that in general case
	 * <code>value</code> is not an instance of CheckedTreeNode.
	 */
	public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value instanceof CheckedTreeNode) {
			customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
		}
	}

	/**
	 * @deprecated
	 * @see CheckboxTreeCellRendererBase#customizeRenderer(javax.swing.JTree, Object, boolean, boolean, boolean, int,
	 *      boolean)
	 */
	@Deprecated
	public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
	}

	public ColoredTreeCellRenderer getTextRenderer() {
		return myTextRenderer;
	}

	public JCheckBox getCheckbox() {
		return myCheckbox;
	}
}
