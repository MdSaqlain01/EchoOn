package com.echoon.app.ui

/**
 * Optional initial text and language when navigating from Home to Write.
 * WriteRoute reads these on launch and clears them.
 */
object PendingWriteText {
    var initialText: String? = null
    var sourceLang: String? = null
    var targetLang: String? = null
}
