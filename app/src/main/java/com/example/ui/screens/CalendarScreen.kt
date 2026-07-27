package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class EventType(val label: String) {
    VENTA("Venta / Entrega"),
    INVENTARIO("Control Stock"),
    REUNION("Cita Cliente"),
    RECORDATORIO("Recordatorio")
}

data class CalendarEvent(
    val id: String,
    val title: String,
    val time: String,
    val type: EventType,
    val clientOrItem: String,
    val isGCalSynced: Boolean = true,
    val hasReminder: Boolean = true
)

data class DayOption(
    val dayName: String,
    val dayNumber: Int,
    val fullDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val coroutineScope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    var syncSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Selected day state
    val days = remember {
        listOf(
            DayOption("Lun", 27, "27 de Julio"),
            DayOption("Mar", 28, "28 de Julio"),
            DayOption("Mié", 29, "29 de Julio"),
            DayOption("Jue", 30, "30 de Julio"),
            DayOption("Vie", 31, "31 de Julio"),
            DayOption("Sáb", 1, "1 de Agosto"),
            DayOption("Dom", 2, "2 de Agosto")
        )
    }
    var selectedDay by remember { mutableStateOf(days[0]) }

    // Selected filter state
    var selectedFilter by remember { mutableStateOf<EventType?>(null) }

    // Event list state
    var events by remember {
        mutableStateOf(
            listOf(
                CalendarEvent(
                    id = "1",
                    title = "Entrega Pedido #402 - Colección Verano",
                    time = "09:30 AM - 10:30 AM",
                    type = EventType.VENTA,
                    clientOrItem = "Cliente: María González",
                    isGCalSynced = true,
                    hasReminder = true
                ),
                CalendarEvent(
                    id = "2",
                    title = "Revisión de Stock - Almacén Central",
                    time = "11:30 AM - 12:30 PM",
                    type = EventType.INVENTARIO,
                    clientOrItem = "Inventario: 342 artículos en revisión",
                    isGCalSynced = true,
                    hasReminder = false
                ),
                CalendarEvent(
                    id = "3",
                    title = "Cita Comercial y Cotización Mayorista",
                    time = "03:00 PM - 04:00 PM",
                    type = EventType.REUNION,
                    clientOrItem = "Cliente: Carlos Mendoza & Co.",
                    isGCalSynced = true,
                    hasReminder = true
                ),
                CalendarEvent(
                    id = "4",
                    title = "Enviar Recordatorio de Pago Factura #189",
                    time = "05:00 PM - 05:15 PM",
                    type = EventType.RECORDATORIO,
                    clientOrItem = "Cliente: Lucía Fernández",
                    isGCalSynced = false,
                    hasReminder = true
                )
            )
        )
    }

    val filteredEvents = remember(selectedDay, selectedFilter, events) {
        events.filter { event ->
            selectedFilter == null || event.type == selectedFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo Evento o Recordatorio")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header & GCal Sync status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calendario & Eventos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Interfaz limpia en colores sólidos suaves",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Soft Solid Sync Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSystemInDarkTheme()) Color(0xFF143326) else Color(0xFFE8F8F2),
                    modifier = Modifier.clickable {
                        if (!isSyncing) {
                            coroutineScope.launch {
                                isSyncing = true
                                syncSuccessMessage = null
                                delay(1200)
                                isSyncing = false
                                syncSuccessMessage = "¡Google Calendar Sincronizado!"
                                // Mark all as synced
                                events = events.map { it.copy(isGCalSynced = true) }
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = if (isSystemInDarkTheme()) Color(0xFFA8E6CF) else Color(0xFF0F5132)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = "Sync",
                                tint = if (isSystemInDarkTheme()) Color(0xFFA8E6CF) else Color(0xFF0F5132),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSyncing) "Sincronizando..." else "GCal Activo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSystemInDarkTheme()) Color(0xFFA8E6CF) else Color(0xFF0F5132)
                        )
                    }
                }
            }

            // Sync success message banner
            AnimatedVisibility(visible = syncSuccessMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSystemInDarkTheme()) Color(0xFF1B3B2B) else Color(0xFFD1E7DD)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Éxito",
                            tint = if (isSystemInDarkTheme()) Color(0xFF75B798) else Color(0xFF0F5132),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSystemInDarkTheme()) Color(0xFFE2F3EB) else Color(0xFF0F5132)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Horizontal Date Selector (Solid Soft Pills)
            Text(
                text = "Julio / Agosto 2026",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    val isSelected = selectedDay == day
                    val isDark = isSystemInDarkTheme()
                    val bgColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isDark -> Color(0xFF1E293B) // Solid soft dark slate
                        else -> Color(0xFFF1F5F9) // Solid soft light slate
                    }
                    val textColor = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isDark -> Color(0xFFCBD5E1)
                        else -> Color(0xFF475569)
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .clickable { selectedDay = day },
                        shape = RoundedCornerShape(14.dp),
                        color = bgColor
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.dayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${day.dayNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Filter Chips (Solid Soft Colors)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isDark = isSystemInDarkTheme()
                FilterPill(
                    label = "Todos",
                    isSelected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    selectedBg = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                    unselectedBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                )
                EventType.values().forEach { type ->
                    val (softBg, textColor, _) = getSoftEventColors(type)
                    FilterPill(
                        label = type.label.split(" ")[0],
                        isSelected = selectedFilter == type,
                        onClick = { selectedFilter = if (selectedFilter == type) null else type },
                        selectedBg = softBg,
                        unselectedBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                        customTextColor = if (selectedFilter == type) textColor else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Agenda para el ${selectedDay.fullDate}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Event Cards List
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos programados para este filtro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredEvents) { event ->
                        EventCardSolidSoft(
                            event = event,
                            onToggleReminder = {
                                events = events.map {
                                    if (it.id == event.id) it.copy(hasReminder = !it.hasReminder) else it
                                }
                            }
                        )
                    }
                }
            }
        }

        // Add Event / Reminder Dialog
        if (showAddDialog) {
            AddEventDialog(
                onDismiss = { showAddDialog = false },
                onAddEvent = { title, time, type, client ->
                    val newEvent = CalendarEvent(
                        id = "${System.currentTimeMillis()}",
                        title = title,
                        time = time,
                        type = type,
                        clientOrItem = client,
                        isGCalSynced = true,
                        hasReminder = true
                    )
                    events = listOf(newEvent) + events
                    showAddDialog = false
                    syncSuccessMessage = "¡Nuevo evento guardado y sincronizado con Google Calendar!"
                }
            )
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedBg: Color,
    unselectedBg: Color,
    customTextColor: Color? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) selectedBg else unselectedBg,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = customTextColor ?: if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun EventCardSolidSoft(
    event: CalendarEvent,
    onToggleReminder: () -> Unit
) {
    val (softBg, textColor, iconBg) = getSoftEventColors(event.type)
    val icon: ImageVector = when (event.type) {
        EventType.VENTA -> Icons.Outlined.ShoppingBag
        EventType.INVENTARIO -> Icons.Outlined.Inventory2
        EventType.REUNION -> Icons.Filled.Person
        EventType.RECORDATORIO -> Icons.Filled.Event
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = softBg
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Icon container in solid soft color
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = event.type.label,
                        tint = textColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.time,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.85f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (event.isGCalSynced) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = iconBg.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "GCal",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Recordatorio",
                            tint = if (event.hasReminder) textColor else textColor.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onToggleReminder() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.clientOrItem,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun getSoftEventColors(type: EventType): Triple<Color, Color, Color> {
    val isDark = isSystemInDarkTheme()
    return when (type) {
        EventType.VENTA -> if (isDark) {
            Triple(Color(0xFF143326), Color(0xFFA8E6CF), Color(0xFF1E4D3B))
        } else {
            Triple(Color(0xFFE8F8F2), Color(0xFF0F5132), Color(0xFFC2EEDD))
        }
        EventType.INVENTARIO -> if (isDark) {
            Triple(Color(0xFF162E45), Color(0xFFB3E5FC), Color(0xFF1F4466))
        } else {
            Triple(Color(0xFFE6F4FF), Color(0xFF004085), Color(0xFFC4E2FF))
        }
        EventType.REUNION -> if (isDark) {
            Triple(Color(0xFF382C14), Color(0xFFFFE082), Color(0xFF54421E))
        } else {
            Triple(Color(0xFFFEF9E7), Color(0xFF7D5A00), Color(0xFFFDECB6))
        }
        EventType.RECORDATORIO -> if (isDark) {
            Triple(Color(0xFF2D1B36), Color(0xFFE1BEE7), Color(0xFF432952))
        } else {
            Triple(Color(0xFFF5EEF8), Color(0xFF4A154B), Color(0xFFE3D0EB))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (title: String, time: String, type: EventType, client: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("10:00 AM - 11:00 AM") }
    var client by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.VENTA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nuevo Evento / Recordatorio", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del evento o entrega") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Cliente o Detalle del inventario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Horario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EventType.values().forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            label = { Text(type.label.split(" ")[0], fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddEvent(title, time, selectedType, if (client.isNotBlank()) "Detalle: $client" else "Sin notas adicionales")
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Guardar y Sincronizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

