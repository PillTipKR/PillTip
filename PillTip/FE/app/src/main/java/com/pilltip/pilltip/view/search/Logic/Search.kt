package com.pilltip.pilltip.view.search.Logic

fun removeMarkdown(text: String): String {
    return text
        .replace(Regex("(?m)^#{1,6}\\s*"), "") // #### 제목 제거
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // **볼드체** 제거
        .replace(Regex("\\*(.*?)\\*"), "$1") // *기울임* 제거
        .replace(Regex("`(.*?)`"), "$1") // `코드` 제거
        .replace(Regex("~~(.*?)~~"), "$1") // ~~취소선~~ 제거
}