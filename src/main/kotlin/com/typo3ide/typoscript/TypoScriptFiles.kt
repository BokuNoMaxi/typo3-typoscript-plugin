package com.typo3ide.typoscript

import com.intellij.openapi.vfs.VirtualFile

private val KNOWN_EXTENSIONS = setOf("typoscript", "tsconfig")
private val KNOWN_FILENAMES = setOf("setup.txt", "constants.txt", "page.tsconfig", "user.tsconfig")

/** Heuristic file match, kept in sync with the fileType registration in plugin.xml. */
fun isTypoScriptFile(file: VirtualFile?): Boolean {
    if (file == null) return false
    val extension = file.extension?.lowercase()
    if (extension != null && extension in KNOWN_EXTENSIONS) return true
    return file.name.lowercase() in KNOWN_FILENAMES
}

fun isPageTsConfigFile(file: VirtualFile?): Boolean {
    if (file == null) return false
    val name = file.name.lowercase()
    return name.endsWith(".tsconfig") || name == "page.tsconfig" || name == "user.tsconfig"
}
