package krasa.grepconsole.utils;

import java.awt.*;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

/**
 * @author Vojtech Krasa
 */
public class FocusUtils {

	public static void requestFocus(Project project) {
		JFrame frame = WindowManager.getInstance().getFrame(project);

		// the only reliable way I found to bring it to the top
		boolean aot = frame.isAlwaysOnTop();
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(aot);

		int frameState = frame.getExtendedState();
		if ((frameState & Frame.ICONIFIED) == Frame.ICONIFIED) {
			// restore the frame if it is minimized
			frame.setExtendedState(frameState ^ Frame.ICONIFIED);
		}
		frame.toFront();
		frame.requestFocus();
	}

}
