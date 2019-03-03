package com.dashlabs.invoicemanagement.view.products

import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.customers.OnProductSelectedListener
import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import tornadofx.*

class ProductsView(private val onProductSelectedListener: OnProductSelectedListener? = null) : View("Products View") {

    private val productViewModel = ProductViewModel()
    private val productsController: ProductsController by inject()

    override val root = hbox {
        this.add(getProductsView())

        vbox {
            onProductSelectedListener?.let {
                this.add(button("Select Products") {
                    vboxConstraints {
                        margin = Insets(20.0)
                    }
                    setOnMouseClicked {
                        selectionModel?.selectedItems.let {
                            onProductSelectedListener.onProductSelected(it)
                        }
                        currentStage?.close()
                    }
                })
            }

            tableview<ProductsTable>(productsController.productsListObserver) {
                columnResizePolicy = SmartResize.POLICY
                maxHeight = 300.0
                vboxConstraints { margin = Insets(20.0) }
                column("ID", ProductsTable::productId)
                column("Product Name", ProductsTable::productName)
                column("Amount", ProductsTable::amount)
                onProductSelectedListener?.let {
                    multiSelect(enable = true)
                    this@ProductsView.selectionModel = selectionModel
                }
            }
        }
    }

    private var selectionModel: TableView.TableViewSelectionModel<ProductsTable>? = null

    private fun getProductsView(): VBox {
        return vbox {
            hboxConstraints { margin = Insets(20.0) }
            vbox {
                this.add(getSearchProductForm())
                this.add(getAddProductView())
            }
        }
    }

    private fun getAddProductView(): VBox {
        return vbox {
            form {
                fieldset {
                    field("Product Name") {
                        textfield(productViewModel.productName).validator {
                            if (it.isNullOrBlank()) error("Please enter product name!") else null
                        }
                    }

                    field("Amount") {
                        textfield(productViewModel.amountName) {
                            this.filterInput { it.controlNewText.isDouble() }
                        }.validator {
                            if (it.isNullOrBlank()) error("Please specify amount!") else null
                        }
                    }
                }



                button("Add Product") {
                    setOnMouseClicked {
                        productsController.addProduct(productViewModel.productName, productViewModel.amountName)
                    }
                }
            }

        }
    }

    private fun getSearchProductForm(): VBox {
        return vbox {
            form {
                fieldset {
                    field("Search Products") {
                        textfield(productViewModel.searchName).validator {
                            if (it.isNullOrBlank()) error("Please enter search Query!") else null
                        }
                    }
                }

                button("Search Product") {
                    alignment = Pos.BOTTOM_RIGHT
                    setOnMouseClicked {
                        productsController.searchProduct(productViewModel.searchName)
                    }
                }
            }
        }
    }

    fun requestForProducts() {
        productsController.requestForProducts()
    }
}