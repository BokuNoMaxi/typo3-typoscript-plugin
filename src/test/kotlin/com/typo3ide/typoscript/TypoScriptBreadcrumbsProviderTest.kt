package com.typo3ide.typoscript

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypoScriptBreadcrumbsProviderTest : BasePlatformTestCase() {
    fun testCrumbTrailReflectsBlockNesting() {
        val psiFile = myFixture.configureByText(
            "setup.txt",
            """
            plugin.tx_myext.settings {
                foo = bar
                subsettings {
                    nested = value
                }
            }
            """.trimIndent()
        )
        val provider = TypoScriptBreadcrumbsProvider()

        val nestedValue = psiFile.findElementAt(psiFile.text.indexOf("value"))!!
        val innerBrace = generateSequence(nestedValue as com.intellij.psi.PsiElement) { provider.getParent(it) }
            .first { provider.acceptElement(it) }
        val outerBrace = provider.getParent(innerBrace)!!

        assertEquals("subsettings", provider.getElementInfo(innerBrace))
        assertEquals("plugin.tx_myext.settings", provider.getElementInfo(outerBrace))
        assertNull(provider.getParent(outerBrace))
    }
}
