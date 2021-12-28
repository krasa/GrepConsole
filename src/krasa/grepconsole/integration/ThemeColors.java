package krasa.grepconsole.integration;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.ui.JBColor;

import java.awt.*;

import static com.intellij.openapi.editor.colors.ColorKey.createColorKey;

public class ThemeColors {
	public static final ColorKey CHESSBOARD_WHITE_COLOR = createColorKey("GrepConsole.chessboard.white", new JBColor(Color.WHITE, Color.GRAY));
	public static final ColorKey CHESSBOARD_BLACK_COLOR = createColorKey("GrepConsole.chessboard.black", new JBColor(Color.LIGHT_GRAY, Color.BLACK));

	public static final ColorKey FATAL_BACKGROUND = createColorKey("GrepConsole.fatal.background", new JBColor(JBColor.RED, new Color(0, 0, 0, 255)));
	public static final ColorKey FATAL_FOREGROUND = createColorKey("GrepConsole.fatal.foreground", null);
	public static final ColorKey ERROR_BACKGROUND = createColorKey("GrepConsole.error.background", new JBColor(JBColor.ORANGE, new Color(55, 0, 0, 200)));
	public static final ColorKey ERROR_FOREGROUND = createColorKey("GrepConsole.error.foreground", null);
	public static final ColorKey WARN_BACKGROUND = createColorKey("GrepConsole.warn.background", new JBColor(JBColor.YELLOW, new Color(26, 0, 55, 200)));
	public static final ColorKey WARN_FOREGROUND = createColorKey("GrepConsole.warn.foreground", null);
	public static final ColorKey INFO_BACKGROUND = createColorKey("GrepConsole.info.background", null);
	public static final ColorKey INFO_FOREGROUND = createColorKey("GrepConsole.info.foreground", null);
	public static final ColorKey DEBUG_FOREGROUND = createColorKey("GrepConsole.debug.foreground", new JBColor(JBColor.GRAY, JBColor.GRAY));
	public static final ColorKey DEBUG_BACKGROUND = createColorKey("GrepConsole.debug.background", null);
	public static final ColorKey TRACE_FOREGROUND = createColorKey("GrepConsole.trace.foreground", new JBColor(JBColor.LIGHT_GRAY, JBColor.BLACK));
	public static final ColorKey TRACE_BACKGROUND = createColorKey("GrepConsole.trace.background", null);
}
