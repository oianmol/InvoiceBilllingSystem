package com.dashlabs.invoicemanagement.view.admin

import com.dashlabs.invoicemanagement.databaseconnection.AdminTable
import com.dashlabs.invoicemanagement.databaseconnection.Database
import io.reactivex.Single
import tornadofx.*

class AdminModel : ItemViewModel<Admin>(Admin()) {

    val username = bind(Admin::nameProperty)
    val password = bind(Admin::passwordProperty)
    val newpassword = bind(Admin::passwordProperty)

    fun loginUser(): Single<AdminTable> {
        return Single.create<AdminTable> {
            try {
                commit()
                val admin = item
                println("Saving ${admin.username}")
                val adminTable = Database.checkAdminExists(admin)
                adminTable?.let { admin ->
                    it.onSuccess(admin)
                } ?: kotlin.run {
                    it.onError(Exception("User ${admin.username} doesn't exist"))
                }
            } catch (ex: Exception) {
                it.onError(ex)
            }

        }
    }

    fun changePassword(adminTable: Admin, adminModel: AdminModel): Single<AdminTable> {
        return Single.create<AdminTable> {
            try {
                val adminTable = Database.changePassword(adminTable,adminModel)
                adminTable?.let { admin ->
                    it.onSuccess(admin)
                } ?: kotlin.run {
                    it.onError(Exception("User ${adminModel.username} doesn't exist"))
                }
            } catch (ex: Exception) {
                it.onError(ex)
            }

        }
    }

    fun registerUser(): Single<AdminTable> {
        return Single.create<AdminTable> {
            commit()
            val admin = item
            println("Saving ${admin.username}")
            try {
                val adminTable = Database.createAdmin(admin)
                adminTable?.let { admin ->
                    it.onSuccess(admin)
                } ?: kotlin.run {
                    it.onError(Exception("User ${admin.username} already exist"))
                }
            } catch (ex: Exception) {
                it.onError(Exception("User ${admin.username} already exist"))
            }

        }
    }
}