package com.typo3ide.typoscript

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import java.awt.Color
import java.awt.Font

/**
 * One color per TypoScript object-type keyword (PAGE, TEXT, HMENU, ...), so `page.10 = TEXT`
 * vs `page.20 = HMENU` is recognizable by color alone. Colors aren't yet exposed in
 * Settings > Color Scheme (would need a ColorSettingsPage) - not asked for, add if needed.
 */
object TypoScriptTypeColors {
    // ponytail: fixed mid-tone palette, legible on both light and dark themes without
    // per-scheme tuning; add bundled light/dark variants if a theme reads poorly.
    private val PALETTE = listOf(
        0xCC7832, 0x6897BB, 0x9876AA, 0x629755, 0xBBB529, 0xC77DBB,
        0x508CD7, 0xD69745, 0x67A35D, 0xB05279, 0x3EA6B5, 0xA9A15C,
        0xCF6A4C, 0x7C8FD1, 0x568D5C, 0xB48EAD, 0x4E9A9A, 0xC5866B,
    )

    val keys: Map<String, TextAttributesKey> = OBJECT_TYPES.keys.sorted().mapIndexed { i, name ->
        val attrs = TextAttributes(Color(PALETTE[i % PALETTE.size]), null, null, null, Font.BOLD)
        name to TextAttributesKey.createTextAttributesKey("TYPOSCRIPT_TYPE_$name", attrs)
    }.toMap()
}

class TypoScriptTypeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val key = colorKeyFor(element) ?: return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(key).create()
    }

    companion object {
        /** Colored only when used as the value of an assignment, e.g. `page.10 = TEXT`. */
        fun colorKeyFor(element: PsiElement): TextAttributesKey? {
            val node = element.node ?: return null
            if (node.elementType != TypoScriptTokenTypes.IDENTIFIER) return null
            val key = TypoScriptTypeColors.keys[element.text.uppercase()] ?: return null
            val prev = TypoScriptBlocks.prevSignificant(node)
            if (prev?.elementType != TypoScriptTokenTypes.OPERATOR || prev.text != "=") return null
            return key
        }
    }
}
