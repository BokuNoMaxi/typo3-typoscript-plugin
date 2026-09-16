package com.typo3ide.typoscript

import com.intellij.psi.tree.IElementType

class TypoScriptTokenType(debugName: String) : IElementType(debugName, TypoScriptLanguage)

object TypoScriptTokenTypes {
    val LINE_COMMENT = TypoScriptTokenType("LINE_COMMENT")
    val BLOCK_COMMENT = TypoScriptTokenType("BLOCK_COMMENT")
    val STRING = TypoScriptTokenType("STRING")
    val LBRACE = TypoScriptTokenType("LBRACE")
    val RBRACE = TypoScriptTokenType("RBRACE")
    val LPAREN = TypoScriptTokenType("LPAREN")
    val RPAREN = TypoScriptTokenType("RPAREN")
    val CONDITION = TypoScriptTokenType("CONDITION")
    val OPERATOR = TypoScriptTokenType("OPERATOR")
    val IDENTIFIER = TypoScriptTokenType("IDENTIFIER")
}
