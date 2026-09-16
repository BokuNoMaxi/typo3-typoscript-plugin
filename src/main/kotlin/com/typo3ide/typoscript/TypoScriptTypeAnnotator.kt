package com.typo3ide.typoscript

import com.intellij.lang.ASTNode
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
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
        /** Colored when used as the value of an assignment (`page.10 = TEXT`), or when it's a
         * property path nested under one (`page.10.value`, `page.10.stdWrap {`) - see [inheritedColorFor]. */
        fun colorKeyFor(element: PsiElement): TextAttributesKey? {
            val node = element.node ?: return null
            if (node.elementType != TypoScriptTokenTypes.IDENTIFIER) return null
            val directKey = TypoScriptTypeColors.keys[element.text.uppercase()]
            if (directKey != null) {
                val prev = TypoScriptBlocks.prevSignificant(node)
                if (prev?.elementType == TypoScriptTokenTypes.OPERATOR && prev.text == "=") return directKey
            }
            return inheritedColorFor(element)
        }

        private data class TypedAssignment(val path: List<String>, val key: TextAttributesKey, val offset: Int)

        private val TYPED_ASSIGNMENTS_KEY = Key.create<Pair<Long, List<TypedAssignment>>>("typoscript.typedAssignments")

        /**
         * `page.10.value = "x"` or `page.10.stdWrap { }` inherit `page.10`'s color once
         * `page.10 = TEXT` is declared - the closest (deepest) enclosing declaration wins, and a
         * later re-declaration of the same path overrides an earlier one (closer to TypoScript's
         * runtime last-assignment-wins behavior than "first found"). Conditions (`[...]`) aren't
         * evaluated, so an environment-specific override can make this statically wrong - accepted
         * as a known limit rather than resolved.
         */
        private fun inheritedColorFor(element: PsiElement): TextAttributesKey? {
            val node = element.node ?: return null
            if (!isPathPosition(node)) return null
            val file = element.containingFile ?: return null
            val fullPath = TypoScriptBlocks.enclosingPath(element).flatMap { it.split(".") } + node.text.split(".")

            var best: TypedAssignment? = null
            for (assignment in typedAssignments(file)) {
                if (assignment.offset >= node.startOffset) break
                if (fullPath.size <= assignment.path.size) continue
                if (fullPath.subList(0, assignment.path.size) != assignment.path) continue
                if (best == null || assignment.path.size >= best!!.path.size) best = assignment
            }
            return best?.key
        }

        /** True for a key/selector position (`x =`, `x <`, `x :=`, `x {`), false for a value position. */
        private fun isPathPosition(node: ASTNode): Boolean {
            val next = TypoScriptBlocks.nextSignificant(node) ?: return false
            if (next.elementType == TypoScriptTokenTypes.LBRACE) return true
            return next.elementType == TypoScriptTokenTypes.OPERATOR && next.text in setOf("=", "<", ":=")
        }

        // ponytail: O(tokens) rescan per query on a cache miss, fine for typical setup.txt sizes;
        // switch to an incremental index if this ever shows up in profiling on huge files.
        private fun typedAssignments(file: PsiFile): List<TypedAssignment> {
            val stamp = file.modificationStamp
            file.getUserData(TYPED_ASSIGNMENTS_KEY)?.let { (cachedStamp, cached) -> if (cachedStamp == stamp) return cached }
            val computed = computeTypedAssignments(file)
            file.putUserData(TYPED_ASSIGNMENTS_KEY, stamp to computed)
            return computed
        }

        private fun computeTypedAssignments(file: PsiFile): List<TypedAssignment> {
            val result = mutableListOf<TypedAssignment>()
            val blockStack = mutableListOf<List<String>>()
            var node: ASTNode? = file.node.firstChildNode
            while (node != null) {
                when (node.elementType) {
                    TypoScriptTokenTypes.LBRACE -> blockStack += TypoScriptBlocks.selectorBefore(node)?.split(".") ?: emptyList()
                    TypoScriptTokenTypes.RBRACE -> if (blockStack.isNotEmpty()) blockStack.removeAt(blockStack.lastIndex)
                    TypoScriptTokenTypes.IDENTIFIER -> {
                        val eq = TypoScriptBlocks.nextSignificant(node)
                        if (eq?.elementType == TypoScriptTokenTypes.OPERATOR && eq.text == "=") {
                            val value = TypoScriptBlocks.nextSignificant(eq)
                            val key = if (value?.elementType == TypoScriptTokenTypes.IDENTIFIER) {
                                TypoScriptTypeColors.keys[value.text.uppercase()]
                            } else null
                            if (key != null) result += TypedAssignment(blockStack.flatten() + node.text.split("."), key, node.startOffset)
                        }
                    }
                }
                node = node.treeNext
            }
            return result
        }
    }
}
