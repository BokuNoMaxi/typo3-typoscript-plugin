package com.typo3ide.typoscript

/**
 * TypoScript object keyword (`page.10 = TEXT`) -> the doc category/group of its properties.
 * Used both to narrow completion suggestions to the declared type and to give each type
 * keyword its own highlight color (see TypoScriptCompletionContributor, TypoScriptTypeAnnotator).
 */
val OBJECT_TYPES: Map<String, Pair<String, String>> = mapOf(
    "PAGE" to ("TopLevelObject" to "Page"),
    "TEXT" to ("ContentObject" to "Text"),
    "HMENU" to ("ContentObject" to "Hmenu"),
    "TMENU" to ("ContentObject" to "Hmenu.Tmenu"),
    "IMAGE" to ("ContentObject" to "Image"),
    "IMG_RESOURCE" to ("ContentObject" to "ImgResource"),
    "FILES" to ("ContentObject" to "Files"),
    "CONTENT" to ("ContentObject" to "Content"),
    "RECORDS" to ("ContentObject" to "Records"),
    "CASE" to ("ContentObject" to "Case"),
    "COA" to ("ContentObject" to "CoaAndCoaInt"),
    "COA_INT" to ("ContentObject" to "CoaAndCoaInt"),
    "USER" to ("ContentObject" to "UserAndUserInt"),
    "USER_INT" to ("ContentObject" to "UserAndUserInt"),
    "FLUIDTEMPLATE" to ("ContentObject" to "Fluidtemplate"),
    "SVG" to ("ContentObject" to "Svg"),
    "PAGEVIEW" to ("ContentObject" to "Pageview"),
    "LOAD_REGISTER" to ("ContentObject" to "LoadRegister"),
)
