package com.typo3ide.typoscript

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypoScriptTypeAnnotatorTest : BasePlatformTestCase() {
    fun testKnownTypeKeywordAfterEqualsIsColored() {
        val file = myFixture.configureByText("setup.txt", "page.10 = TEXT")
        val value = file.findElementAt(file.text.indexOf("TEXT"))!!

        assertNotNull(TypoScriptTypeAnnotator.colorKeyFor(value))
    }

    fun testDifferentTypeKeywordsGetDifferentColors() {
        val file = myFixture.configureByText(
            "setup.txt",
            """
            page.10 = TEXT
            page.20 = HMENU
            """.trimIndent()
        )
        val text = file.findElementAt(file.text.indexOf("TEXT"))!!
        val hmenu = file.findElementAt(file.text.indexOf("HMENU"))!!

        assertNotSame(TypoScriptTypeAnnotator.colorKeyFor(text), TypoScriptTypeAnnotator.colorKeyFor(hmenu))
    }

    fun testPropertyNameIsNotColoredEvenIfItMatchesATypeKeyword() {
        // TEXT here is the key being assigned to, not a value - shouldn't be colored as a type.
        val file = myFixture.configureByText("setup.txt", "TEXT = foo")
        val key = file.findElementAt(0)!!

        assertNull(TypoScriptTypeAnnotator.colorKeyFor(key))
    }

    fun testUnknownValueIsNotColored() {
        val file = myFixture.configureByText("setup.txt", "page.10 = bar")
        val value = file.findElementAt(file.text.indexOf("bar"))!!

        assertNull(TypoScriptTypeAnnotator.colorKeyFor(value))
    }
}
