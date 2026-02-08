package com.zaheer.upireconcilepro.data.model

data class ColumnMapping(
    val referenceIndex: Int,
    val amountIndex: Int,
    val dateIndex: Int,
    val merchantIndex: Int? = null
)
