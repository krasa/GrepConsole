package krasa.grepconsole.gui;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Donate {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Donate.class);

	public static final @NotNull
	Icon ICON = IconLoader.getIcon("/krasa/grepconsole/icons/coins_in_hand.png", Donate.class);

	public static JButton newDonateButton() {
		JButton donate = new JButton();
		initDonateButton(donate);
		return donate;
	}

	public static void initDonateButton(JButton donate) {
		donate.setText("GitHub");
		donate.setToolTipText("GitHub Sponsors");
		donate.setIcon(ICON);
		donate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BrowserUtil.browse("https://github.com/sponsors/krasa");
			}
		});
	}

	public static void initDonateButton2(JButton donate) {
		donate.setText("PayPal");
		donate.setIcon(ICON);
		donate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BrowserUtil.browse("https://www.paypal.me/VojtechKrasa");
			}
		});
	}
}
