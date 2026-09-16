package com.typo3ide.typoscript

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.logger

data class TsEntry(
    val category: String,
    val group: String,
    val name: String,
    val type: String,
    val description: String,
)

/**
 * Property reference extracted at build time from the official
 * TYPO3-Documentation/TYPO3CMS-Reference-Typoscript sources (see tools/update-docs.py).
 */
object TypoScriptDocs {
    private val LOG = logger<TypoScriptDocs>()

    val entries: List<TsEntry> by lazy { load() }

    /** name (lowercase) -> all entries sharing that property name across content objects/groups. */
    val byName: Map<String, List<TsEntry>> by lazy {
        entries.groupBy { it.name.lowercase() }
    }

    private fun load(): List<TsEntry> {
        val resource = javaClass.getResourceAsStream("/data/typoscript.json")
        if (resource == null) {
            LOG.warn("Bundled data/typoscript.json not found")
            return emptyList()
        }
        return resource.use { stream ->
            val type = object : TypeToken<List<TsEntry>>() {}.type
            Gson().fromJson(stream.reader(Charsets.UTF_8), type)
        }
    }
}
