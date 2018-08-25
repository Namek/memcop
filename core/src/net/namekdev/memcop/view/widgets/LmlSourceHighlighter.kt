package net.namekdev.memcop.view.widgets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.util.highlight.BaseHighlighter
import com.kotcrab.vis.ui.util.highlight.Highlight
import com.kotcrab.vis.ui.util.highlight.HighlightRule
import com.kotcrab.vis.ui.widget.HighlightTextArea

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Highlights LML's XML-like syntax when used by a [HighlightTextArea].
 */
class LmlSourceHighlighter : BaseHighlighter() {
    init {
        // instruction name
        addPattern(Color(0.8f, 0.6f, 0.6f, 1f), "^[a-z]+")

        // register name
        addPattern(Color(0.8f, 0.6f, 1f, 1f), "\\$[a-z][a-z0-9]?")

        // comments
        addPattern(Color(0.75f, 0.75f, 0.75f, 1f), "//.*$")
        addPattern(Color(0.75f, 0.75f, 0.75f, 1f), "#.*$")
    }

    /**
     * @param color will be used to color values detected by the pattern.
     * @param pattern will be compiled to a [Pattern]. Has to be a valid regular expression.
     */
    fun addPattern(color: Color, pattern: String) {
        addRule(RegexRule(color, pattern))
    }

    class RegexRule(private val color: Color, regex: String) : HighlightRule {
        private val pattern: Pattern

        init {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
        }

        override fun process(textArea: HighlightTextArea, highlights: Array<Highlight>) {
            val matcher = pattern.matcher(textArea.text)
            while (matcher.find()) {
                highlights.add(Highlight(color, matcher.start(), matcher.end()))
            }
        }
    }
}
