package com.dashlabs.invoicemanagement.view.products

import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.customers.OnProductSelectedListener
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
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
                        sendSelectedProducts()
                    }
                })
            }

            tableview<ProductsTable>(productsController.productsListObserver) {
                columnResizePolicy = SmartResize.POLICY
                vboxConstraints { margin = Insets(20.0) }
                column("ID", ProductsTable::productId)
                column("Product Name", ProductsTable::productName)
                column("Amount", ProductsTable::amount)
                onProductSelectedListener?.let {
                    multiSelect(enable = true)
                    this@ProductsView.selectionModel = selectionModel
                }

                this.setOnKeyPressed {
                    when (it.code) {
                        KeyCode.DELETE, KeyCode.BACK_SPACE -> {
                            selectedCell?.row?.let { position ->
                                alert(Alert.AlertType.CONFIRMATION, "Delete Product ?",
                                        "Remove ${selectedItem?.productName} ?",
                                        buttons = *arrayOf(ButtonType.OK, ButtonType.CANCEL), owner = currentWindow, title = "Hey!") {
                                    if (it == ButtonType.OK) {
                                        productsController.deleteProduct(selectedItem!!)
                                    }
                                }
                            }
                        }
                        KeyCode.ENTER -> {
                            sendSelectedProducts()
                        }
                    }
                }
            }
        }
    }

    private fun sendSelectedProducts() {
        selectionModel?.selectedItems.let {
            val productsMap = FXCollections.observableHashMap<ProductsTable, Int>()
            it?.forEach {
                productsMap[it] = 1
            }
            onProductSelectedListener?.onProductSelected(productsMap)
        }
        //currentStage?.close()
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

                        this.setOnKeyPressed {
                            when (it.code) {
                                KeyCode.ENTER -> {
                                    productsController.addProduct(productViewModel.productName, productViewModel.amountName)
                                }
                            }
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

                        productViewModel.searchName.onChange {
                            it?.let {
                                productsController.searchProduct(it)
                            } ?: run {
                                requestForProducts()
                            }
                        }
                    }

                    this.setOnKeyPressed {
                        when (it.code) {
                            KeyCode.ENTER -> {
                                productsController.searchProduct(productViewModel.searchName.value)
                            }
                        }
                    }
                }

                button("Search Product") {
                    alignment = Pos.BOTTOM_RIGHT
                    setOnMouseClicked {
                        productsController.searchProduct(productViewModel.searchName.value)
                    }
                }
            }
        }
    }

    fun requestForProducts() {
        productsController.requestForProducts()
    }
}