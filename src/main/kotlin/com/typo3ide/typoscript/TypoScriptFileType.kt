package com.typo3ide.typoscript

import com.intellij.openapi.fileTypes.LanguageFileType

object TypoScriptFileType : LanguageFileType(TypoScriptLanguage) {
    override fun getName() = "TypoScript"
    override fun getDescription() = "TYPO3 TypoScript / PageTS"
    override fun getDefaultExtension() = "typoscript"
    override fun getIcon() = null
}
