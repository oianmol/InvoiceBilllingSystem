package com.dashlabs.invoicemanagement.databaseconnection

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "admin")
class AdminTable {

    @DatabaseField(id = true)
    var name: String = ""

    @DatabaseField(canBeNull = false)
    var password: String = ""
}