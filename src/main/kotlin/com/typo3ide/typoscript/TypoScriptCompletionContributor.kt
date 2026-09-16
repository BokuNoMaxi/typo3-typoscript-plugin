package com.typo3ide.typoscript

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext

private val PAGE_TS_CATEGORIES = setOf("PageTSConfig", "UserTSConfig")
private val UNTYPED_BLOCK_CATEGORIES = setOf("ContentObject", "Function", "Condition", "DataProcessing")

class TypoScriptCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(
                parameters: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet,
            ) {
                val file = parameters.originalFile
                val virtualFile = file.virtualFile
                if (!isTypoScriptFile(virtualFile)) return

                val candidates = if (isPageTsConfigFile(virtualFile)) {
                    TypoScriptDocs.entries.filter { it.category in PAGE_TS_CATEGORIES }
                } else {
                    setupCandidates(file, parameters.position)
                }

                for ((name, matches) in candidates.groupBy { it.name }) {
                    val first = matches.first()
                    val typeText = if (matches.size == 1) first.type else "${matches.size}x"
                    result.addElement(
                        LookupElementBuilder.create(name)
                            .withTypeText(typeText, true)
                            .withTailText("  (${first.category})", true)
                    )
                }
            }
        })
    }

    /**
     * Narrows suggestions to what's actually valid at the caret: TopLevelObject keys at
     * global scope, DataProcessing entries inside a `.dataProcessing` block, or - when the
     * enclosing block's own object type was declared earlier in the file (`page.10 = TEXT`) -
     * just that type's ContentObject properties. Falls back to the unfiltered set for
     * purely structural blocks (e.g. `plugin.tx_myext.settings { ... }`) whose type can't
     * be determined.
     */
    private fun setupCandidates(file: PsiFile, position: PsiElement): List<TsEntry> {
        val path = TypoScriptBlocks.enclosingPath(position)
        if (path.isEmpty()) {
            return TypoScriptDocs.entries.filter { it.category == "TopLevelObject" || it.category == "Condition" }
        }
        if (path.any { it.equals("dataProcessing", ignoreCase = true) }) {
            return TypoScriptDocs.entries.filter { it.category == "DataProcessing" }
        }
        val declaredType = findAssignedType(file, path.joinToString("."))
        if (declaredType != null) {
            val (category, group) = declaredType
            return TypoScriptDocs.entries.filter { it.category == category && it.group == group } +
                TypoScriptDocs.entries.filter { it.category == "Function" }
        }
        return TypoScriptDocs.entries.filter { it.category in UNTYPED_BLOCK_CATEGORIES }
    }

    /** Scans the whole file for `<path> = <TYPE>` and resolves TYPE via OBJECT_TYPES. */
    private fun findAssignedType(file: PsiFile, path: String): Pair<String, String>? {
        var node = file.node.firstChildNode
        while (node != null) {
            if (node.elementType == TypoScriptTokenTypes.IDENTIFIER && node.text == path) {
                val eq = TypoScriptBlocks.nextSignificant(node)
                if (eq != null && eq.elementType == TypoScriptTokenTypes.OPERATOR && eq.text == "=") {
                    val value = TypoScriptBlocks.nextSignificant(eq)
                    if (value != null && value.elementType == TypoScriptTokenTypes.IDENTIFIER) {
                        OBJECT_TYPES[value.text.uppercase()]?.let { return it }
                    }
                }
            }
            node = node.treeNext
        }
        return null
    }
}
