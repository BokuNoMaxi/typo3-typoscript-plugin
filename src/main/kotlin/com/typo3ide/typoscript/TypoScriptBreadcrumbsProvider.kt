package com.typo3ide.typoscript

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider

class TypoScriptBreadcrumbsProvider : BreadcrumbsProvider {
    override fun getLanguages(): Array<Language> = arrayOf(TypoScriptLanguage)

    override fun acceptElement(e: PsiElement): Boolean =
        PsiUtilCore.getElementType(e) == TypoScriptTokenTypes.LBRACE

    override fun getElementInfo(e: PsiElement): String = TypoScriptBlocks.selectorBefore(e.node) ?: "{...}"

    override fun getParent(e: PsiElement): PsiElement? = TypoScriptBlocks.enclosingLBrace(e.node)?.psi
}
