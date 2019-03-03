package com.dashlabs.invoicemanagement

data class StateDistrict(
    val states: List<State>
)

data class State(
    val districts: List<String>,
    val state: String
)