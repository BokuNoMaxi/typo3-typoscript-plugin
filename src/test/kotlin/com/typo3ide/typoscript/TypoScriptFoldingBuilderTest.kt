package com.typo3ide.typoscript

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypoScriptFoldingBuilderTest : BasePlatformTestCase() {
    fun testNestedBracesFoldSeparately() {
        val psiFile = myFixture.configureByText(
            "setup.txt",
            """
            plugin.tx_myext.settings {
                foo = bar
                inner {
                    baz = qux
                }
            }
            single = { not multi-line }
            """.trimIndent()
        )

        val descriptors = TypoScriptFoldingBuilder().buildFoldRegions(psiFile, myFixture.editor.document, false)

        // two multi-line brace blocks (outer + inner); the single-line brace pair is not folded
        assertEquals(2, descriptors.size)
        assertTrue(descriptors.all { it.getElement().elementType == TypoScriptTokenTypes.LBRACE })
    }

    fun testMultilineParensFold() {
        val psiFile = myFixture.configureByText(
            "setup.txt",
            """
            page.10.value (
                line one
                line two
            )
            single = (not multi-line)
            """.trimIndent()
        )

        val descriptors = TypoScriptFoldingBuilder().buildFoldRegions(psiFile, myFixture.editor.document, false)

        assertEquals(1, descriptors.size)
        assertEquals(TypoScriptTokenTypes.LPAREN, descriptors[0].getElement().elementType)
        assertEquals("(...)", TypoScriptFoldingBuilder().getPlaceholderText(descriptors[0].getElement()))
    }
}
