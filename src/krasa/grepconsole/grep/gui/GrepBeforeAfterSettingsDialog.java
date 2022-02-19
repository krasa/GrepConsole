package krasa.grepconsole.grep.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import krasa.grepconsole.grep.GrepBeforeAfterModel;
import krasa.grepconsole.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GrepBeforeAfterSettingsDialog {
	private JPanel root;

	private JLabel beforeLabel;
	private JLabel afterLabel;

	private JTextField before;
	private JTextField after;

	private Project project;
	private GrepBeforeAfterModel beforeAfterModel;

	public GrepBeforeAfterSettingsDialog(Project project, GrepBeforeAfterModel beforeAfterModel) {
		this.project = project;
		this.beforeAfterModel = beforeAfterModel;
		setData(beforeAfterModel);
	}

	public GrepBeforeAfterModel showAndGet(Component parent) {
		DialogBuilder builder = new DialogBuilder(parent);
		builder.setCenterPanel(this.root);
		builder.setDimensionServiceKey("GrepBeforeAfterSettingsDialog");
		builder.setTitle("Print Before/After Lines in Addition to Matched Lines");
		builder.removeAllActions();
		builder.addAction(new AbstractAction("Reset") {
			@Override
			public void actionPerformed(ActionEvent e) {
				before.setText("0");
				after.setText("0");
				builder.getDialogWrapper().close(0, true);
			}
		});
		builder.addOkAction();
		builder.addCancelAction();
		boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
		if (isOk) {
			getData(this.beforeAfterModel);
		}
		return this.beforeAfterModel;
	}

	public void setData(GrepBeforeAfterModel data) {
		before.setText(String.valueOf(data.getBefore()));
		after.setText(String.valueOf(data.getAfter()));
	}

	public void getData(GrepBeforeAfterModel data) {
		data.setBefore(Utils.safeParseInt(before.getText()));
		data.setAfter(Utils.safeParseInt(after.getText()));
	}


}
