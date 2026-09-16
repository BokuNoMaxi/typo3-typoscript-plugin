package com.typo3ide.typoscript

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

object TypoScriptHighlighterColors {
    val LINE_COMMENT = createTextAttributesKey("TYPOSCRIPT_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val BLOCK_COMMENT = createTextAttributesKey("TYPOSCRIPT_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    val STRING = createTextAttributesKey("TYPOSCRIPT_STRING", DefaultLanguageHighlighterColors.STRING)
    val BRACES = createTextAttributesKey("TYPOSCRIPT_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val PARENTHESES = createTextAttributesKey("TYPOSCRIPT_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val CONDITION = createTextAttributesKey("TYPOSCRIPT_CONDITION", DefaultLanguageHighlighterColors.METADATA)
    val OPERATOR = createTextAttributesKey("TYPOSCRIPT_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val IDENTIFIER = createTextAttributesKey("TYPOSCRIPT_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
}

class TypoScriptSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = TypoScriptLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        val key = when (tokenType) {
            TypoScriptTokenTypes.LINE_COMMENT -> TypoScriptHighlighterColors.LINE_COMMENT
            TypoScriptTokenTypes.BLOCK_COMMENT -> TypoScriptHighlighterColors.BLOCK_COMMENT
            TypoScriptTokenTypes.STRING -> TypoScriptHighlighterColors.STRING
            TypoScriptTokenTypes.LBRACE, TypoScriptTokenTypes.RBRACE -> TypoScriptHighlighterColors.BRACES
            TypoScriptTokenTypes.LPAREN, TypoScriptTokenTypes.RPAREN -> TypoScriptHighlighterColors.PARENTHESES
            TypoScriptTokenTypes.CONDITION -> TypoScriptHighlighterColors.CONDITION
            TypoScriptTokenTypes.OPERATOR -> TypoScriptHighlighterColors.OPERATOR
            TypoScriptTokenTypes.IDENTIFIER -> TypoScriptHighlighterColors.IDENTIFIER
            else -> null
        }
        return if (key != null) arrayOf(key) else emptyArray()
    }
}

class TypoScriptSyntaxHighlighterFactory : com.intellij.openapi.fileTypes.SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(
        project: com.intellij.openapi.project.Project?,
        virtualFile: com.intellij.openapi.vfs.VirtualFile?,
    ): com.intellij.openapi.fileTypes.SyntaxHighlighter = TypoScriptSyntaxHighlighter()
}
