package com.typo3ide.typoscript

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypoScriptCompletionContributorTest : BasePlatformTestCase() {
    private fun complete(text: String): List<String> {
        myFixture.configureByText("setup.txt", text)
        myFixture.completeBasic()
        return myFixture.lookupElementStrings ?: emptyList()
    }

    fun testGlobalScopeSuggestsOnlyTopLevelAndConditions() {
        val names = complete("<caret>")
        assertTrue(names.contains("bodyTag")) // TopLevelObject/Page
        assertFalse(names.contains("fieldName")) // DataProcessing-only
        assertFalse(names.contains("minItems")) // ContentObject/Hmenu-only
    }

    fun testDeclaredTextTypeFiltersOutOtherContentObjectGroups() {
        val names = complete(
            """
            page.10 = TEXT
            page.10 {
                <caret>
            }
            """.trimIndent()
        )
        assertTrue(names.contains("key")) // Function, always included inside a typed block
        assertFalse(names.contains("bodyTag")) // TopLevelObject, not global scope
        assertFalse(names.contains("minItems")) // ContentObject/Hmenu-only, wrong type
    }

    fun testDeclaredHmenuTypeIncludesHmenuOnlyProperties() {
        val names = complete(
            """
            page.20 = HMENU
            page.20 {
                <caret>
            }
            """.trimIndent()
        )
        assertTrue(names.contains("minItems")) // ContentObject/Hmenu
        assertFalse(names.contains("fieldName")) // DataProcessing-only
    }

    fun testDataProcessingBlockSuggestsOnlyDataProcessingEntries() {
        val names = complete(
            """
            page.40 = FLUIDTEMPLATE
            page.40 {
                dataProcessing {
                    10 {
                        <caret>
                    }
                }
            }
            """.trimIndent()
        )
        assertTrue(names.contains("fieldName")) // DataProcessing
        assertFalse(names.contains("minItems")) // ContentObject-only
        assertFalse(names.contains("bodyTag")) // TopLevelObject
    }

    fun testUntypedStructuralBlockFallsBackToBroadSet() {
        val names = complete(
            """
            plugin.tx_myext.settings {
                <caret>
            }
            """.trimIndent()
        )
        assertTrue(names.contains("minItems")) // ContentObject, type unknown -> unfiltered
        assertTrue(names.contains("fieldName")) // DataProcessing, type unknown -> unfiltered
        assertFalse(names.contains("bodyTag")) // TopLevelObject, still not global scope
    }
}
