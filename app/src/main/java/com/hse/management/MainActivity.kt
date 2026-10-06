package com.hse.management

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HseViewModel(
    private val dao: HseDao
) : ViewModel() {

    val records =
        dao.records()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val observations =
        dao.observationCount()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

    val incidents =
        dao.incidentCount()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

    val audits =
        dao.auditCount()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

    val capa =
        dao.capaCount()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

    fun add(
        record: HseRecord
    ) = viewModelScope.launch {

        val id = dao.insert(record)

        dao.log(
            AuditLog(
                username = record.createdBy,
                action = "Created",
                module = record.module,
                recordId = id
            )
        )
    }

    fun delete(
        record: HseRecord
    ) = viewModelScope.launch {

        dao.delete(record)

        dao.log(
            AuditLog(
                username = record.createdBy,
                action = "Deleted",
                module = record.module,
                recordId = record.id
            )
        )
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        val dao =
            HseDatabase
                .get(this)
                .dao()

        setContent {

            HseApp(
                HseViewModel(dao)
            )
        }
    }
}

@Composable
fun HseApp(
    vm: HseViewModel
) {

    var tab by remember {
        mutableIntStateOf(0)
    }

    var formModule by remember {
        mutableStateOf<String?>(null)
    }

    val titles =
        listOf(
            "Dashboard",
            "Observations",
            "Incidents",
            "Audits",
            "CAPA",
            "More"
        )

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        "HSE Management System",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },

        bottomBar = {

            NavigationBar {

                listOf(

                    Icons.Default.Dashboard,

                    Icons.Default.Visibility,

                    Icons.Default.Warning,

                    Icons.Default.AssignmentTurnedIn,

                    Icons.Default.Assignment,

                    Icons.Default.MoreHoriz

                ).forEachIndexed { index, icon ->

                    NavigationBarItem(

                        selected = tab == index,

                        onClick = {
                            tab = index
                        },

                        icon = {
                            Icon(
                                icon,
                                contentDescription = null
                            )
                        },

                        label = {
                            Text(titles[index])
                        }
                    )
                }
            }
        }

    ) { padding ->

        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            when (tab) {

                0 ->
                    Dashboard(vm)

                1 ->
                    Register(
                        vm = vm,
                        module = "Observation",
                        title = "HSE Observations"
                    ) {
                        formModule = "Observation"
                    }

                2 ->
                    Register(
                        vm = vm,
                        module = "Incident",
                        title = "Incident Investigations"
                    ) {
                        formModule = "Incident"
                    }

                3 ->
                    Register(
                        vm = vm,
                        module = "Audit",
                        title = "Audit Register"
                    ) {
                        formModule = "Audit"
                    }

                4 ->
                    Register(
                        vm = vm,
                        module = "CAPA",
                        title = "CAPA Register"
                    ) {
                        formModule = "CAPA"
                    }

                else ->
                    More(vm)
            }

            formModule?.let { module ->

                RecordDialog(

                    module = module,

                    onDismiss = {
                        formModule = null
                    },

                    onSave = {

                        vm.add(it)

                        formModule = null
                    }
                )
            }
        }
    }
}

@Composable
fun Dashboard(
    vm: HseViewModel
) {

    val observations by
        vm.observations.collectAsState()

    val incidents by
        vm.incidents.collectAsState()

    val audits by
        vm.audits.collectAsState()

    val capa by
        vm.capa.collectAsState()

    LazyColumn(

        modifier = Modifier.padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)

    ) {

        item {

            Text(
                "HSE Performance Dashboard",
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall
            )
        }

        item {

            Row(
                Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Metric(
                    name = "Observations",
                    value = observations
                )

                Metric(
                    name = "Incidents",
                    value = incidents
                )
            }
        }

        item {

            Row(
                Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Metric(
                    name = "Audits",
                    value = audits
                )

                Metric(
                    name = "CAPA",
                    value = capa
                )
            }
        }

        item {

            Card(
                Modifier.fillMaxWidth()
            ) {

                Column(
                    Modifier.padding(16.dp)
                ) {

                    Text(
                        "HSE Workflow",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(6.dp)
                    )

                    Text(
                        "Observation / Incident / Audit " +
                        "→ Root Cause → CAPA → " +
                        "Corrective Action → " +
                        "Verification → Closeout"
                    )
                }
            }
        }
    }
}

@Composable
fun Metric(
    name: String,
    value: Int
) {

    Card(
        Modifier.weight(1f)
    ) {

        Column(
            Modifier.padding(16.dp)
        ) {

            Text(name)

            Text(
                value.toString(),

                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
fun Register(
    vm: HseViewModel,
    module: String,
    title: String,
    onAdd: () -> Unit
) {

    val list by
        vm.records
            .collectAsState()
            .let { state ->
                mutableStateOf(
                    state.value.filter {
                        it.module == module
                    }
                )
            }

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Row(
            Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                title,

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall
            )

            Button(
                onClick = onAdd
            ) {

                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(4.dp)
                )

                Text("New")
            }
        }

        Spacer(
            Modifier.height(10.dp)
        )

        if (list.isEmpty()) {

            Text(
                "No records yet. " +
                "Tap New to create one.",
                Modifier.padding(12.dp)
            )

        } else {

            LazyColumn(

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(
                    list,
                    key = {
                        it.id
                    }
                ) { record ->

                    RecordCard(
                        record
                    ) {
                        vm.delete(record)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordCard(
    record: HseRecord,
    onDelete: () -> Unit
) {

    Card(
        Modifier.fillMaxWidth()
    ) {

        Column(
            Modifier.padding(14.dp),

            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {

            Row(
                Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    record.number,
                    fontWeight =
                        FontWeight.Bold
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(record.status)
                    }
                )
            }

            Text(
                record.title,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Text(
                "${record.type} • " +
                record.category
            )

            if (
                record.responsible.isNotBlank()
            ) {

                Text(
                    "Responsible: " +
                    record.responsible
                )
            }

            if (
                record.targetDate.isNotBlank()
            ) {

                Text(
                    "Target: " +
                    record.targetDate
                )
            }

            if (
                record.description.isNotBlank()
            ) {

                Text(
                    record.description
                )
            }

            TextButton(
                onClick = onDelete
            ) {

                Text("Delete")
            }
        }
    }
}

@Composable
fun RecordDialog(
    module: String,
    onDismiss: () -> Unit,
    onSave: (HseRecord) -> Unit
) {

    var title by
        remember {
            mutableStateOf("")
        }

    var type by
        remember {

            mutableStateOf(

                when (module) {

                    "Observation" ->
                        "Unsafe Condition"

                    "Incident" ->
                        "Near Miss"

                    "Audit" ->
                        "Internal Audit"

                    else ->
                        "Corrective Action"
                }
            )
        }

    var category by
        remember {
            mutableStateOf("")
        }

    var description by
        remember {
            mutableStateOf("")
        }

    var responsible by
        remember {
            mutableStateOf("")
        }

    var priority by
        remember {
            mutableStateOf("Medium")
        }

    var targetDate by
        remember {
            mutableStateOf("")
        }

    var correctiveAction by
        remember {
            mutableStateOf("")
        }

    var rootCause by
        remember {
            mutableStateOf("")
        }

    var rcaMethod by
        remember {
            mutableStateOf("")
        }

    var standard by
        remember {
            mutableStateOf("")
        }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(
                "New $module"
            )
        },

        text = {

            Column(

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Field(
                    label = "Title",
                    value = title
                ) {
                    title = it
                }

                Field(
                    label = "Type",
                    value = type
                ) {
                    type = it
                }

                Field(
                    label = "Category / Finding Type",
                    value = category
                ) {
                    category = it
                }

                Field(
                    label = "Description / Finding",
                    value = description
                ) {
                    description = it
                }

                Field(
                    label = "Responsible",
                    value = responsible
                ) {
                    responsible = it
                }

                Field(
                    label = "Priority",
                    value = priority
                ) {
                    priority = it
                }

                Field(
                    label = "Target Date",
                    value = targetDate
                ) {
                    targetDate = it
                }

                Field(
                    label = "Corrective Action",
                    value = correctiveAction
                ) {
                    correctiveAction = it
                }

                Field(
                    label = "Root Cause",
                    value = rootCause
                ) {
                    rootCause = it
                }

                if (module == "Incident") {

                    Field(
                        label = "RCA Method",
                        value = rcaMethod
                    ) {
                        rcaMethod = it
                    }
                }

                if (module == "Audit") {

                    Field(
                        label = "Standard / Clause",
                        value = standard
                    ) {
                        standard = it
                    }
                }
            }
        },

        confirmButton = {

            Button(

                enabled =
                    title.isNotBlank(),

                onClick = {

                    val record =
                        HseRecord(

                            number =
                                "HSE-" +
                                System.currentTimeMillis(),

                            module =
                                module,

                            title =
                                title,

                            type =
                                type,

                            category =
                                category,

                            description =
                                description,

                            responsible =
                                responsible,

                            priority =
                                priority,

                            targetDate =
                                targetDate,

                            correctiveAction =
                                correctiveAction,

                            rootCause =
                                rootCause,

                            investigationMethod =
                                rcaMethod,

                            standard =
                                standard
                        )

                    onSave(record)
                }
            ) {

                Text("Save")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        }
    )
}

@Composable
fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {

    OutlinedTextField(

        value = value,

        onValueChange = onChange,

        label = {
            Text(label)
        },

        modifier =
            Modifier.fillMaxWidth(),

        singleLine = false
    )
}

@Composable
fun More(
    vm: HseViewModel
) {

    Column(

        Modifier.padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Administration",

            style =
                MaterialTheme
                    .typography
                    .headlineSmall
        )

        Text(
            "HSE Management System"
        )

        Text(
            "Current foundation includes:"
        )

        Text(
            "• Offline Room database\n" +
            "• Dashboard\n" +
            "• HSE Observations\n" +
            "• Incident register\n" +
            "• RCA fields\n" +
            "• Audit register\n" +
            "• CAPA register\n" +
            "• Corrective actions\n" +
            "• Root cause\n" +
            "• Responsible person\n" +
            "• Priority and target date\n" +
            "• Audit logging"
        )

        Text(
            "Next production modules:"
        )

        Text(
            "• STOP Card / Observation detail\n" +
            "• Fishbone RCA\n" +
            "• 5 Why\n" +
            "• ICAM\n" +
            "• Evidence/photos\n" +
            "• Audit findings\n" +
            "• CAPA source linking\n" +
            "• Employees\n" +
            "• Projects\n" +
            "• Companies\n" +
            "• Reports\n" +
            "• Backup / restore\n" +
            "• User roles\n" +
            "• Notifications"
        )
    }
}
