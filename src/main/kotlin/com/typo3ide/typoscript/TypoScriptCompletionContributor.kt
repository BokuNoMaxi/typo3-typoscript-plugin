package com.typo3ide.typoscript

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

private val PAGE_TS_CATEGORIES = setOf("PageTSConfig", "UserTSConfig")
private val SETUP_CATEGORIES = setOf("ContentObject", "Function", "Condition", "TopLevelObject", "DataProcessing")

class TypoScriptCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(
                parameters: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet,
            ) {
                val virtualFile = parameters.originalFile.virtualFile
                if (!isTypoScriptFile(virtualFile)) return

                val wantedCategories = if (isPageTsConfigFile(virtualFile)) PAGE_TS_CATEGORIES else SETUP_CATEGORIES
                val byName = TypoScriptDocs.entries
                    .filter { it.category in wantedCategories }
                    .groupBy { it.name }

                for ((name, matches) in byName) {
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
}
