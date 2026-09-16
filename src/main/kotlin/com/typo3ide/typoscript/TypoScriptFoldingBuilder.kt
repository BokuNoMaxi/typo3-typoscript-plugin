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
 * LBRACE/RBRACE tokens with a stack instead.
 */
class TypoScriptFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        val openStack = ArrayDeque<ASTNode>()
        var node = root.node.firstChildNode
        while (node != null) {
            when (node.elementType) {
                TypoScriptTokenTypes.LBRACE -> openStack.addLast(node)
                TypoScriptTokenTypes.RBRACE -> {
                    val open = openStack.removeLastOrNull()
                    if (open != null) {
                        val range = TextRange(open.startOffset, node.startOffset + node.textLength)
                        if (document.getLineNumber(range.startOffset) != document.getLineNumber(range.endOffset)) {
                            descriptors += FoldingDescriptor(open, range)
                        }
                    }
                }
            }
            node = node.treeNext
        }
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "{...}"

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
