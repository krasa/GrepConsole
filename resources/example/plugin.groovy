import java.util.function.Function
import java.util.regex.Matcher
import java.util.regex.Pattern

import static support.limitAndCutNewline
import static support.registerFunction

/**
 https://github.com/dkandalov/live-plugin/blob/master/src/plugin-util-groovy/liveplugin/PluginUtil.groovy
 */
//REMOVE_COMMENT import static liveplugin.PluginUtil.*

registerFunction("myScript", new Function<String, String>() {
    Pattern pattern = Pattern.compile(".*ugly slow regexp.*");

    /** - The text will never be empty, it may or may not end with a newline - \n
     *  - It is possible that the stream will flush prematurely and the text will be incomplete: IDEA-70016
     *  - Return null to remove the line
     *  - Processing blocks application output stream, make sure to limit the length and processing time when needed using #limitAndCutNewline
     **/
    @Override
    String apply(String text) {
        try {
            CharSequence textForMatching = limitAndCutNewline(text, 150, 1000)

            if (textForMatching.contains("remove this line")) {
                return null
            }
            if (textForMatching.contains("notify me")) {
                show("Alert: " + text)
                return text
            }

            Matcher matcher = pattern.matcher(textForMatching)
            if (matcher.matches()) {
                return text.toUpperCase()
            }

        } catch (com.intellij.openapi.progress.ProcessCanceledException ex) {
            show("Processing took too long for: " + text)
        }
        return text
    }
})

