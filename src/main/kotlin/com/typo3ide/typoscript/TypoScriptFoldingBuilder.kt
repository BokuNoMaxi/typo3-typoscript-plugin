package com.typo3ide.typoscript

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * The parser only produces a flat token stream (see TypoScriptParserDefinition), so
 * there are no nested block PSI elements to fold. Fold regions are found by matching
 * LBRACE/RBRACE and LPAREN/RPAREN tokens with a stack instead. Parens fold too since
 * TypoScript uses `key (` ... `)` for multiline values.
 */
class TypoScriptFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        val braceStack = ArrayDeque<ASTNode>()
        val parenStack = ArrayDeque<ASTNode>()
        var node = root.node.firstChildNode
        while (node != null) {
            when (node.elementType) {
                TypoScriptTokenTypes.LBRACE -> braceStack.addLast(node)
                TypoScriptTokenTypes.RBRACE -> closeRegion(descriptors, braceStack, node, document)
                TypoScriptTokenTypes.LPAREN -> parenStack.addLast(node)
                TypoScriptTokenTypes.RPAREN -> closeRegion(descriptors, parenStack, node, document)
            }
            node = node.treeNext
        }
        return descriptors.toTypedArray()
    }

    private fun closeRegion(descriptors: MutableList<FoldingDescriptor>, stack: ArrayDeque<ASTNode>, close: ASTNode, document: Document) {
        val open = stack.removeLastOrNull() ?: return
        val range = TextRange(open.startOffset, close.startOffset + close.textLength)
        if (document.getLineNumber(range.startOffset) != document.getLineNumber(range.endOffset)) {
            descriptors += FoldingDescriptor(open, range)
        }
    }

    override fun getPlaceholderText(node: ASTNode): String =
        if (node.elementType == TypoScriptTokenTypes.LPAREN) "(...)" else "{...}"

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
