package com.afi.record.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afi.record.presentation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQueueScreen(navController: NavController) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStatusOptions by remember { mutableStateOf(false) }
    var showProductOrder by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) } // Added confirmation dialog state

    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedStatus by remember { mutableStateOf("In queue") }
    var grandTotal by remember { mutableStateOf(0.0) }
    var totalDiscount by remember { mutableStateOf(0.0) }

    // Product order state variables
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf(0.0) }

    // List to store product orders
    var productOrders by remember { mutableStateOf(listOf<ProductOrder>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Create queue",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Modified Customer Section with click
        CustomerSection(
            date = selectedDate,
            status = selectedStatus,
            onDateClick = { showDatePicker = true },
            onStatusClick = { showStatusOptions = true },
            onCustomerClick = { navController.navigate(Screen.SelectCostumer.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Product Orders Section
        ProductOrdersSection(
            onAddClick = {
                productName = ""
                quantity = ""
                discount = ""
                totalPrice = 0.0
                showProductOrder = true
            },
            productOrders = productOrders,
            grandTotal = grandTotal,
            totalDiscount = totalDiscount
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Note Section
        NoteSection()

        // Add Save Button that shows confirmation dialog
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = { showConfirmationDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Save Queue")
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                            selectedDate = formatDate(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Select date") }
            )
        }
    }

    // Status Options Dialog
    if (showStatusOptions) {
        AlertDialog(
            onDismissRequest = { showStatusOptions = false },
            title = { Text("Select status") },
            text = {
                Column {
                    Text("Selecting the \"Unpaid\" status will add more debt to the customer.")

                    Spacer(modifier = Modifier.height(16.dp))

                    StatusOptionItem(
                        text = "In queue",
                        selected = selectedStatus == "In queue",
                        onSelect = {
                            selectedStatus = "In queue"
                            showStatusOptions = false
                        }
                    )

                    StatusOptionItem(
                        text = "In process",
                        selected = selectedStatus == "In process",
                        onSelect = {
                            selectedStatus = "In process"
                            showStatusOptions = false
                        }
                    )

                    StatusOptionItem(
                        text = "Unpaid",
                        selected = selectedStatus == "Unpaid",
                        onSelect = {
                            selectedStatus = "Unpaid"
                            showStatusOptions = false
                        }
                    )

                    StatusOptionItem(
                        text = "Completed",
                        selected = selectedStatus == "Completed",
                        onSelect = {
                            selectedStatus = "Completed"
                            showStatusOptions = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showStatusOptions = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Product Order Dialog
    if (showProductOrder) {
        AlertDialog(
            onDismissRequest = { showProductOrder = false },
            title = { Text("Make product orders") },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.SelectProduct.route)
                                showProductOrder = false
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (productName.isEmpty()) "Pilih Produk" else productName,
                            color = if (productName.isEmpty()) Color.Gray else Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = quantity,
                        onValueChange = {
                            quantity = it
                            try {
                                val qty = quantity.toIntOrNull() ?: 0
                                val price = 10.0
                                totalPrice = qty * price
                            } catch (e: NumberFormatException) {
                                totalPrice = 0.0
                            }
                        },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = discount,
                        onValueChange = {
                            discount = it
                            try {
                                val qty = quantity.toIntOrNull() ?: 0
                                val price = 10.0
                                val disc = discount.toDoubleOrNull() ?: 0.0
                                totalPrice = (qty * price) - disc
                            } catch (e: NumberFormatException) {
                            }
                        },
                        label = { Text("Discount") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total price", fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", totalPrice)}")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (productName.isNotBlank() && quantity.isNotBlank()) {
                            val newOrder = ProductOrder(
                                product = productName,
                                quantity = quantity.toIntOrNull() ?: 0,
                                discount = discount.toDoubleOrNull() ?: 0.0,
                                totalPrice = totalPrice
                            )

                            productOrders = productOrders + newOrder
                            grandTotal += totalPrice
                            totalDiscount += discount.toDoubleOrNull() ?: 0.0
                            showProductOrder = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showProductOrder = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Added Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Queue") },
            text = {
                Text("Are you sure you want to save this queue?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        // Here you would typically save the data and navigate back
                        navController.popBackStack()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class ProductOrder(
    val product: String,
    val quantity: Int,
    val discount: Double,
    val totalPrice: Double
)

@Composable
fun CustomerSection(
    date: String,
    status: String,
    onDateClick: () -> Unit,
    onStatusClick: () -> Unit,
    onCustomerClick: () -> Unit
) {
    Column {
        Text(
            text = "Customer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onCustomerClick)
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Date")
            TextButton(onClick = onDateClick) {
                Text(date)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Status")
            TextButton(onClick = onStatusClick) {
                Text(status)
            }
        }
    }
}


@Composable
fun ProductOrdersSection(
    onAddClick: () -> Unit,
    productOrders: List<ProductOrder>,
    grandTotal: Double,
    totalDiscount: Double
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Product orders",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onAddClick) {
                Text("+ Add")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (productOrders.isNotEmpty()) {
            Column {
                productOrders.forEach { order ->
                    ProductOrderItem(order = order)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Grand total price", fontWeight = FontWeight.Bold)
            Text("$${"%.2f".format(grandTotal)}")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total discount")
            Text("$${"%.2f".format(totalDiscount)}")
        }
    }
}

@Composable
fun ProductOrderItem(order: ProductOrder) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.product, fontWeight = FontWeight.Bold)
                Text("${order.quantity} x $10.00")
            }

            if (order.discount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Discount")
                    Text("-$${String.format("%.2f", order.discount)}")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text("$${String.format("%.2f", order.totalPrice)}")
            }
        }
    }
}

@Composable
fun NoteSection() {
    var noteText by remember { mutableStateOf("") }

    Column {
        Text(
            text = "Note",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Add note here...") }
        )
    }
}

@Composable
fun StatusOptionItem(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

