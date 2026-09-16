package com.typo3ide.typoscript

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

/**
 * The parser only produces a flat token stream (see TypoScriptParserDefinition), so
 * block nesting isn't real PSI containment. It's derived here by matching LBRACE/RBRACE
 * with a backward scan over siblings, and reused by both TypoScriptBreadcrumbsProvider
 * (sticky lines) and TypoScriptCompletionContributor (context-aware suggestions).
 */
object TypoScriptBlocks {
    fun enclosingLBrace(node: ASTNode?): ASTNode? {
        var depth = 0
        var cur = node?.treePrev
        while (cur != null) {
            when (cur.elementType) {
                TypoScriptTokenTypes.RBRACE -> depth++
                TypoScriptTokenTypes.LBRACE -> {
                    if (depth == 0) return cur
                    depth--
                }
            }
            cur = cur.treePrev
        }
        return null
    }

    fun selectorBefore(lbrace: ASTNode?): String? =
        prevSignificant(lbrace)?.takeIf { it.elementType == TypoScriptTokenTypes.IDENTIFIER }?.text

    fun prevSignificant(node: ASTNode?): ASTNode? {
        var cur = node?.treePrev
        while (cur != null && cur.elementType == TokenType.WHITE_SPACE) cur = cur.treePrev
        return cur
    }

    fun nextSignificant(node: ASTNode?): ASTNode? {
        var cur = node?.treeNext
        while (cur != null && cur.elementType == TokenType.WHITE_SPACE) cur = cur.treeNext
        return cur
    }

    /** Dotted path of the block enclosing [element], outermost segment first. Empty at global scope. */
    fun enclosingPath(element: PsiElement): List<String> {
        val segments = mutableListOf<String>()
        var brace = enclosingLBrace(element.node)
        while (brace != null) {
            segments += selectorBefore(brace) ?: return segments.asReversed()
            brace = enclosingLBrace(brace)
        }
        return segments.asReversed()
    }
}
