package com.typo3ide.typoscript

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class TypoScriptPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TypoScriptLanguage) {
    override fun getFileType(): FileType = TypoScriptFileType
}
