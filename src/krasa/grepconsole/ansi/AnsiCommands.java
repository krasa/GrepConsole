package krasa.grepconsole.ansi;

// From Wikipedia, http://en.wikipedia.org/wiki/ANSI_escape_code
public class AnsiCommands {

	public static final int COMMAND_ATTR_RESET = 0; // Reset / Normal all attributes off
	public static final int COMMAND_ATTR_INTENSITY_BRIGHT = 1; // Bright (increased intensity) or Bold
	public static final int COMMAND_ATTR_INTENSITY_FAINT = 2; // Faint (decreased intensity) not widely supported
	public static final int COMMAND_ATTR_ITALIC = 3; // Italic: on not widely supported. Sometimes treated as inverse.
	public static final int COMMAND_ATTR_UNDERLINE = 4; // Underline: Single
	public static final int COMMAND_ATTR_BLINK_SLOW = 5; // Blink: Slow less than 150 per minute
	public static final int COMMAND_ATTR_BLINK_FAST = 6; // Blink: Rapid MS-DOS ANSI.SYS; 150 per minute or more; not
	// widely supported
	public static final int COMMAND_ATTR_NEGATIVE_ON = 7; // Image: Negative inverse or reverse; swap foreground and
	// background
	public static final int COMMAND_ATTR_CONCEAL_ON = 8; // Conceal not widely supported
	public static final int COMMAND_ATTR_UNDERLINE_DOUBLE = 21; // Bright/Bold: off or Underline: Double bold off not
	// widely supported, double underline hardly ever
	public static final int COMMAND_ATTR_INTENSITY_NORMAL = 22; // Normal color or intensity neither bright, bold nor
	// faint
	public static final int COMMAND_ATTR_ITALIC_OFF = 23; // Not italic, not Fraktur
	public static final int COMMAND_ATTR_UNDERLINE_OFF = 24; // Underline: None not singly or doubly underlined
	public static final int COMMAND_ATTR_BLINK_OFF = 25; // Blink: off
	public static final int COMMAND_ATTR_NEGATIVE_Off = 27; // Image: Positive
	public static final int COMMAND_ATTR_CONCEAL_OFF = 28; // Reveal conceal off

	public static final int COMMAND_COLOR_FOREGROUND_RESET = 39; // Default text color
	public static final int COMMAND_COLOR_BACKGROUND_RESET = 49; // Default background color

	public static final int COMMAND_COLOR_FOREGROUND_FIRST = 30; // First text color
	public static final int COMMAND_COLOR_FOREGROUND_LAST = 37; // Last text color
	public static final int COMMAND_COLOR_BACKGROUND_FIRST = 40; // First background text color
	public static final int COMMAND_COLOR_BACKGROUND_LAST = 47; // Last background text color
	public static final int COMMAND_COLOR_INTENSITY_DELTA = 8; // Last background text color
}
