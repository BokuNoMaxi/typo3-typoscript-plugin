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

    fun testPropertyNestedUnderTypedPathInheritsItsColor() {
        val file = myFixture.configureByText(
            "setup.txt",
            """
            page.10 = TEXT
            page.10.value = "hi"
            """.trimIndent()
        )
        val type = file.findElementAt(file.text.indexOf("TEXT"))!!
        val property = file.findElementAt(file.text.indexOf("page.10.value"))!!

        assertEquals(TypoScriptTypeAnnotator.colorKeyFor(type), TypoScriptTypeAnnotator.colorKeyFor(property))
    }

    fun testNestedBlockSelectorInheritsEnclosingType() {
        val file = myFixture.configureByText(
            "setup.txt",
            """
            page.10 = TEXT
            page.10 {
              stdWrap.wrap = "hi"
            }
            """.trimIndent()
        )
        val type = file.findElementAt(file.text.indexOf("TEXT"))!!
        val property = file.findElementAt(file.text.indexOf("stdWrap.wrap"))!!

        assertEquals(TypoScriptTypeAnnotator.colorKeyFor(type), TypoScriptTypeAnnotator.colorKeyFor(property))
    }

    fun testUntypedSiblingPathIsNotColored() {
        val file = myFixture.configureByText(
            "setup.txt",
            """
            page.10 = TEXT
            page.20.value = "hi"
            """.trimIndent()
        )
        val property = file.findElementAt(file.text.indexOf("page.20.value"))!!

        assertNull(TypoScriptTypeAnnotator.colorKeyFor(property))
    }

    fun testDeclarationLineItselfIsNotRecoloredByInheritance() {
        val file = myFixture.configureByText("setup.txt", "page.10 = TEXT")
        val lhs = file.findElementAt(0)!!

        assertNull(TypoScriptTypeAnnotator.colorKeyFor(lhs))
    }

    fun testLaterReassignmentWinsOverEarlierOneForNestedProperties() {
        val file = myFixture.configureByText(
            "setup.txt",
            """
            page.10 = TEXT
            page.10 = HMENU
            page.10.special = 1
            """.trimIndent()
        )
        val hmenu = file.findElementAt(file.text.indexOf("HMENU"))!!
        val property = file.findElementAt(file.text.indexOf("page.10.special"))!!

        assertEquals(TypoScriptTypeAnnotator.colorKeyFor(hmenu), TypoScriptTypeAnnotator.colorKeyFor(property))
    }

    fun testRhsValueThatHappensToMatchAKeyStillIsntColoredAsAPath() {
        val file = myFixture.configureByText(
            "setup.txt",
            """
            page.10 = TEXT
            page.10.value = someWord
            """.trimIndent()
        )
        val value = file.findElementAt(file.text.indexOf("someWord"))!!

        assertNull(TypoScriptTypeAnnotator.colorKeyFor(value))
    }
}
