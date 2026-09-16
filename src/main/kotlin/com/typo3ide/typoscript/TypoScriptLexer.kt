package com.typo3ide.typoscript

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

private const val STATE_DEFAULT = 0
private const val STATE_BLOCK_COMMENT = 1

/**
 * Hand-rolled lexer, not JFlex: TypoScript's token set is small enough (comments,
 * strings, braces, conditions, operators, everything else is an identifier/value)
 * that a generated lexer would be more ceremony than the grammar needs.
 */
class TypoScriptLexer : LexerBase() {
    private lateinit var buffer: CharSequence
    private var endOffset = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null
    private var state = STATE_DEFAULT

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.endOffset = endOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        this.state = initialState
        advance()
    }

    override fun getState(): Int = state
    override fun getTokenType(): IElementType? = tokenType
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = endOffset

    override fun advance() {
        tokenStart = tokenEnd
        if (tokenStart >= endOffset) {
            tokenType = null
            return
        }

        if (state == STATE_BLOCK_COMMENT) {
            tokenType = TypoScriptTokenTypes.BLOCK_COMMENT
            val close = indexOf("*/", tokenStart)
            if (close == -1) {
                tokenEnd = endOffset
            } else {
                tokenEnd = close + 2
                state = STATE_DEFAULT
            }
            return
        }

        val c = buffer[tokenStart]
        when {
            c.isWhitespace() -> {
                tokenType = TokenType.WHITE_SPACE
                tokenEnd = tokenStart + 1
                while (tokenEnd < endOffset && buffer[tokenEnd].isWhitespace()) tokenEnd++
            }
            c == '#' -> {
                tokenType = TypoScriptTokenTypes.LINE_COMMENT
                tokenEnd = endOfLine(tokenStart)
            }
            c == '/' && charAt(tokenStart + 1) == '/' -> {
                tokenType = TypoScriptTokenTypes.LINE_COMMENT
                tokenEnd = endOfLine(tokenStart)
            }
            c == '/' && charAt(tokenStart + 1) == '*' -> {
                val close = indexOf("*/", tokenStart + 2)
                tokenType = TypoScriptTokenTypes.BLOCK_COMMENT
                if (close == -1) {
                    tokenEnd = endOffset
                    state = STATE_BLOCK_COMMENT
                } else {
                    tokenEnd = close + 2
                }
            }
            c == '"' || c == '\'' -> {
                tokenType = TypoScriptTokenTypes.STRING
                tokenEnd = endOfString(tokenStart, c)
            }
            c == '{' -> { tokenType = TypoScriptTokenTypes.LBRACE; tokenEnd = tokenStart + 1 }
            c == '}' -> { tokenType = TypoScriptTokenTypes.RBRACE; tokenEnd = tokenStart + 1 }
            c == '(' -> { tokenType = TypoScriptTokenTypes.LPAREN; tokenEnd = tokenStart + 1 }
            c == ')' -> { tokenType = TypoScriptTokenTypes.RPAREN; tokenEnd = tokenStart + 1 }
            c == '[' -> {
                tokenType = TypoScriptTokenTypes.CONDITION
                val close = indexOf("]", tokenStart)
                tokenEnd = if (close == -1) endOfLine(tokenStart) else close + 1
            }
            c == ':' && charAt(tokenStart + 1) == '=' -> {
                tokenType = TypoScriptTokenTypes.OPERATOR
                tokenEnd = tokenStart + 2
            }
            c == '=' || c == '<' || c == '>' -> {
                tokenType = TypoScriptTokenTypes.OPERATOR
                tokenEnd = tokenStart + 1
                if (c == '=' && charAt(tokenEnd) == '<') tokenEnd++
            }
            else -> {
                tokenType = TypoScriptTokenTypes.IDENTIFIER
                tokenEnd = tokenStart + 1
                while (tokenEnd < endOffset && isIdentifierPart(buffer[tokenEnd])) tokenEnd++
            }
        }
    }

    private fun charAt(offset: Int): Char? = if (offset < endOffset) buffer[offset] else null

    private fun endOfLine(from: Int): Int {
        var i = from
        while (i < endOffset && buffer[i] != '\n') i++
        return i
    }

    private fun endOfString(from: Int, quote: Char): Int {
        var i = from + 1
        while (i < endOffset && buffer[i] != quote && buffer[i] != '\n') i++
        return if (i < endOffset && buffer[i] == quote) i + 1 else i
    }

    private fun indexOf(needle: String, from: Int): Int {
        val lastStart = endOffset - needle.length
        var i = from
        while (i <= lastStart) {
            if (buffer.regionMatches(i, needle, 0, needle.length)) return i
            i++
        }
        return -1
    }

    private fun isIdentifierPart(c: Char): Boolean =
        !c.isWhitespace() && c != '{' && c != '}' && c != '(' && c != ')' && c != '[' && c != '"' && c != '\''
}
