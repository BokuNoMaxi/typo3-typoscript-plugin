package com.typo3ide.typoscript

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

private val WORD_KEY = Key.create<String>("TYPO3_TS_DOC_WORD")

class TypoScriptDocumentationProvider : DocumentationProvider {

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int,
    ): PsiElement? {
        if (!isTypoScriptFile(file.virtualFile)) return null
        val word = wordAt(editor, targetOffset) ?: return null
        val key = word.substringAfterLast('.').lowercase()
        if (!TypoScriptDocs.byName.containsKey(key)) return null
        val element = contextElement ?: file
        element.putUserData(WORD_KEY, key)
        return element
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        val word = element.getUserData(WORD_KEY) ?: return null
        val matches = TypoScriptDocs.byName[word] ?: return null
        return buildString {
            append("<div class='definition'><pre>").append(word).append("</pre></div>")
            append("<div class='content'>")
            for (entry in matches) {
                append("<p><b>").append(entry.category).append(" &middot; ").append(entry.group).append("</b>")
                if (entry.type.isNotBlank()) {
                    append("<br/><i>").append(entry.type).append("</i>")
                }
                if (entry.description.isNotBlank()) {
                    append("<br/>").append(entry.description.replace("\n", "<br/>"))
                }
                append("</p>")
            }
            append("</div>")
        }
    }
}

private fun isWordChar(c: Char) = c.isLetterOrDigit() || c == '_' || c == '.'

private fun wordAt(editor: Editor, offset: Int): String? {
    val text = editor.document.charsSequence
    if (offset < 0 || offset > text.length) return null
    var start = offset
    while (start > 0 && isWordChar(text[start - 1])) start--
    var end = offset
    while (end < text.length && isWordChar(text[end])) end++
    if (start == end) return null
    return text.subSequence(start, end).toString().trim('.')
}
