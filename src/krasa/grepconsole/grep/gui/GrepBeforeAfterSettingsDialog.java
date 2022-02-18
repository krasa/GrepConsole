package krasa.grepconsole.grep.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import krasa.grepconsole.grep.GrepContextModel;

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
	private GrepContextModel grepContextModel;

	public GrepBeforeAfterSettingsDialog(Project project, GrepContextModel grepContextModel) {
		this.project = project;
		this.grepContextModel = grepContextModel;
		setData(grepContextModel);
	}

	public GrepContextModel showAndGet(Component parent) {
		DialogBuilder builder = new DialogBuilder(parent);
		builder.setCenterPanel(this.root);
		builder.setDimensionServiceKey("GrepContextDialog");
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
			getData(this.grepContextModel);
		}
		return this.grepContextModel;
	}

	public void setData(GrepContextModel data) {
		before.setText(String.valueOf(data.getBefore()));
		after.setText(String.valueOf(data.getAfter()));
	}

	public void getData(GrepContextModel data) {
		data.setBefore(Integer.parseInt(before.getText()));
		data.setAfter(Integer.parseInt(after.getText()));
	}


}
