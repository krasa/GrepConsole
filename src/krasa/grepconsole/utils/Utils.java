package krasa.grepconsole.utils;

import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import krasa.grepconsole.model.Profile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public static int tryParseInteger(String text) {
		if ("".equals(text)) {
			return 0;
		}
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static boolean isSelectedText(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		CopyProvider provider = PlatformDataKeys.COPY_PROVIDER.getData(dataContext);
		return provider != null && provider.isCopyEnabled(dataContext) && provider.isCopyVisible(dataContext);
	}

	public static String getString(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		CopyProvider provider = PlatformDataKeys.COPY_PROVIDER.getData(dataContext);
		if (provider == null) {
			return null;
		}
		provider.performCopy(dataContext);
		Transferable contents = CopyPasteManager.getInstance().getContents();
		try {
			return contents == null ? null : (String) contents.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e1) {
			return null;
		}
	}

	public static String getSelectedString(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
		if (editor == null) {
			return null;
		}
		CaretModel caretModel = editor.getCaretModel();
		Caret primaryCaret = caretModel.getPrimaryCaret();
		return primaryCaret.getSelectedText();
	}

	@NotNull
	public static String toNiceLineForLog(String substring) {
		int length = substring.length();
		int endIndex = substring.length();
		int min = Math.min(endIndex, 120);

		String result = substring.substring(0, min);
		if (min < endIndex) {
			result += "...";
		}
		result += " [length=" + length + "]";
		return result;
	}

	public static String generateName(List<Profile> settingsList, String name) {
		Pattern compile = Pattern.compile("\\(\\d\\)");
		int i = 0;
		int index = indexOf(compile, name);
		if (index > 0) {
			String s = StringUtils.substring(name, index);
			try {
				i = Integer.parseInt(StringUtils.replaceChars(s, "()", ""));
				name = StringUtils.substring(name, 0, index).trim();
			} catch (Exception e) {
			}
		}

		return generateName(settingsList, i, name, name);

	}

	public static String generateName(List<Profile> settingsList, int i, String name, String resultName) {
		if (resultName == null) {
			resultName = name;
		}

		for (Profile settings : settingsList) {
			if (resultName.equals(settings.getName())) {
				resultName = name + " (" + i + ")";
				resultName = generateName(settingsList, ++i, name, resultName);
			}
		}
		return resultName;
	}

	/**
	 * @return index of pattern in s or -1, if not found
	 */
	public static int indexOf(Pattern pattern, String s) {
		Matcher matcher = pattern.matcher(s);
		return matcher.find() ? matcher.start() : -1;
	}

	public static Color nextColor() {
		return colorList.get(++index % colorList.size());
	}

	public static ArrayList<Color> colorList;
	static int index = 0;
	//TODO find a good palette
	private static final Color deepskyblue = new Color(0x006400);
	private static final Color darkturquoise = new Color(0x008000);
	private static final Color aqua = new Color(0x008b8b);
	private static final Color dodgerblue = new Color(0x00bfff);
	private static final Color seagreen = new Color(0x00ced1);
	private static final Color darkslategray = new Color(0x00fa9a);
	private static final Color mediumseagreen = new Color(0x00ffff);
	private static final Color cadetblue = new Color(0x191970);
	private static final Color slateblue = new Color(0x1e90ff);
	private static final Color olivedrab = new Color(0x20b2aa);
	private static final Color blueviolet = new Color(0x32cd32);
	private static final Color darkred = new Color(0x3cb371);
	private static final Color darkmagenta = new Color(0x4169e1);
	private static final Color saddlebrown = new Color(0x4682b4);
	private static final Color darkseagreen = new Color(0x483d8b);
	private static final Color yellowgreen = new Color(0x556b2f);
	private static final Color lightseagreen = new Color(0x5f9ea0);
	private static final Color limegreen = new Color(0x6495ed);
	private static final Color turquoise = new Color(0x663399);
	private static final Color skyblue = new Color(0x696969);
	private static final Color lightgreen = new Color(0x6a5acd);
	private static final Color palegreen = new Color(0x6b8e23);
	private static final Color darkolivegreen = new Color(0x7b68ee);
	private static final Color royalblue = new Color(0x7cfc00);
	private static final Color steelblue = new Color(0x7fffd4);
	private static final Color darkgreen = new Color(0x800000);
	private static final Color cornflowerblue = new Color(0x800080);
	private static final Color green = new Color(0x808000);
	private static final Color teal = new Color(0x808080);
	private static final Color mediumpurple = new Color(0x87ceeb);
	private static final Color darkviolet = new Color(0x8a2be2);
	private static final Color darkorchid = new Color(0x8b0000);
	private static final Color midnightblue = new Color(0x8b008b);
	private static final Color rebeccapurple = new Color(0x8b4513);
	private static final Color dimgray = new Color(0x8fbc8f);
	private static final Color slategray = new Color(0x90ee90);
	private static final Color lightslategray = new Color(0x9370d8);
	private static final Color maroon = new Color(0x9400d3);
	private static final Color purple = new Color(0x98fb98);
	private static final Color olive = new Color(0x9932cc);
	private static final Color gray = new Color(0x9acd32);
	private static final Color darkgray = new Color(0xa0522d);
	private static final Color brown = new Color(0xa52a2a);
	private static final Color sienna = new Color(0xa9a9a9);
	private static final Color lightblue = new Color(0xadd8e6);
	private static final Color greenyellow = new Color(0xadff2f);
	private static final Color paleturquoise = new Color(0xafeeee);
	private static final Color darkgoldenrod = new Color(0xb22222);
	private static final Color firebrick = new Color(0xb8860b);
	private static final Color mediumorchid = new Color(0xba55d3);
	private static final Color rosybrown = new Color(0xbc8f8f);
	private static final Color darkkhaki = new Color(0xbdb76b);
	private static final Color silver = new Color(0xc0c0c0);
	private static final Color mediumvioletred = new Color(0xc71585);
	private static final Color indianred = new Color(0xcd5c5c);
	private static final Color peru = new Color(0xcd853f);
	private static final Color tan = new Color(0xd2691e);
	private static final Color lightgray = new Color(0xd2b48c);
	private static final Color thistle = new Color(0xd3d3d3);
	private static final Color chocolate = new Color(0xd87093);
	private static final Color palevioletred = new Color(0xd8bfd8);
	private static final Color orchid = new Color(0xda70d6);
	private static final Color goldenrod = new Color(0xdaa520);
	private static final Color crimson = new Color(0xdc143c);
	private static final Color gainsboro = new Color(0xdcdcdc);
	private static final Color plum = new Color(0xdda0dd);
	private static final Color burlywood = new Color(0xdeb887);
	private static final Color darksalmon = new Color(0xe9967a);
	private static final Color violet = new Color(0xee82ee);
	private static final Color palegoldenrod = new Color(0xeee8aa);
	private static final Color sandybrown = new Color(0xf08080);
	private static final Color wheat = new Color(0xf4a460);
	private static final Color lightcoral = new Color(0xf5deb3);
	private static final Color red = new Color(0xff0000);
	private static final Color fuchsia = new Color(0xff00ff);
	private static final Color coral = new Color(0xff1493);
	private static final Color darkorange = new Color(0xff4500);
	private static final Color hotpink = new Color(0xff6347);
	private static final Color deeppink = new Color(0xff69b4);
	private static final Color tomato = new Color(0xff8c00);
	private static final Color lightsalmon = new Color(0xffa07a);
	private static final Color orange = new Color(0xffa500);
	private static final Color pink = new Color(0xffc0cb);
	private static final Color gold = new Color(0xffd700);
	private static final Color white = new Color(0xffffff);

	static {
		colorList = new ArrayList<>();
		// https://www.w3schools.com/colors/colors_names.asp
		colorList.add(deepskyblue);
		colorList.add(darkturquoise);
		colorList.add(aqua);
		colorList.add(dodgerblue);
		colorList.add(seagreen);
		colorList.add(darkslategray);
		colorList.add(mediumseagreen);
		colorList.add(cadetblue);
		colorList.add(slateblue);
		colorList.add(olivedrab);
		colorList.add(blueviolet);
		colorList.add(darkred);
		colorList.add(darkmagenta);
		colorList.add(saddlebrown);
		colorList.add(darkseagreen);
		colorList.add(yellowgreen);
		colorList.add(lightseagreen);
		colorList.add(limegreen);
		colorList.add(turquoise);
		colorList.add(skyblue);
		colorList.add(lightgreen);
		colorList.add(palegreen);
		colorList.add(darkolivegreen);
		colorList.add(royalblue);
		colorList.add(steelblue);
		colorList.add(darkgreen);
		colorList.add(cornflowerblue);
		colorList.add(green);
		colorList.add(teal);
		colorList.add(mediumpurple);
		colorList.add(darkviolet);
		colorList.add(darkorchid);
		colorList.add(midnightblue);
		colorList.add(rebeccapurple);
		colorList.add(dimgray);
		colorList.add(slategray);
		colorList.add(lightslategray);
		colorList.add(maroon);
		colorList.add(purple);
		colorList.add(olive);
		colorList.add(gray);
		colorList.add(darkgray);
		colorList.add(brown);
		colorList.add(sienna);
		colorList.add(lightblue);
		colorList.add(greenyellow);
		colorList.add(paleturquoise);
		colorList.add(darkgoldenrod);
		colorList.add(firebrick);
		colorList.add(mediumorchid);
		colorList.add(rosybrown);
		colorList.add(darkkhaki);
		colorList.add(silver);
		colorList.add(mediumvioletred);
		colorList.add(indianred);
		colorList.add(peru);
		colorList.add(tan);
		colorList.add(lightgray);
		colorList.add(thistle);
		colorList.add(chocolate);
		colorList.add(palevioletred);
		colorList.add(orchid);
		colorList.add(goldenrod);
		colorList.add(crimson);
		colorList.add(gainsboro);
		colorList.add(plum);
		colorList.add(burlywood);
		colorList.add(darksalmon);
		colorList.add(violet);
		colorList.add(palegoldenrod);
		colorList.add(sandybrown);
		colorList.add(wheat);
		colorList.add(lightcoral);
		colorList.add(red);
		colorList.add(fuchsia);
		colorList.add(coral);
		colorList.add(darkorange);
		colorList.add(hotpink);
		colorList.add(deeppink);
		colorList.add(tomato);
		colorList.add(lightsalmon);
		colorList.add(orange);
		colorList.add(pink);
		colorList.add(gold);
		colorList.add(white);
		Collections.shuffle(colorList);
	}

	public static int toInt(String s, int i) {
		try {
			int p = Integer.parseInt(s);
			if (p < 0) {
				p = i;
			}
			return p;
		} catch (NumberFormatException e) {
			return i;
		}
	}

	public static long toNano(String ms, int i) {
		return toInt(ms, i) * 1_000_000L;
	}
}
