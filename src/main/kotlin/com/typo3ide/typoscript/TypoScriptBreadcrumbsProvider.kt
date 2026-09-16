package com.typo3ide.typoscript

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiUtilCore
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider

/**
 * Same flat token tree as TypoScriptFoldingBuilder: there are no nested block PSI
 * elements, so "nesting" for breadcrumbs/sticky lines is derived by matching
 * LBRACE/RBRACE with a backward scan over siblings instead of real PSI containment.
 */
class TypoScriptBreadcrumbsProvider : BreadcrumbsProvider {
    override fun getLanguages(): Array<Language> = arrayOf(TypoScriptLanguage)

    override fun acceptElement(e: PsiElement): Boolean =
        PsiUtilCore.getElementType(e) == TypoScriptTokenTypes.LBRACE

    override fun getElementInfo(e: PsiElement): String = selectorBefore(e.node) ?: "{...}"

    override fun getParent(e: PsiElement): PsiElement? = enclosingLBrace(e.node)?.psi

    companion object {
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

        private fun selectorBefore(lbrace: ASTNode?): String? {
            var cur = lbrace?.treePrev
            while (cur != null && cur.elementType == TokenType.WHITE_SPACE) cur = cur.treePrev
            return cur?.takeIf { it.elementType == TypoScriptTokenTypes.IDENTIFIER }?.text
        }
    }
}
