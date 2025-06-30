package com.pilltip.pilltip.view.questionnaire.Logic

data class DosageEntry(
    var amPm: String? = null,
    var hour: Int? = null,
    var minute: Int? = null,
    var dose: Int? = null,
    var doseCount: String = "회",
    var isDropdownExpanded: Boolean = false
)
