/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.toProtoBufByteArray
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.*
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isWeb
import xyz.hyli.timeflow.utils.writeBytesToFile

data class ScheduleParams(
    val viewModel: TimeFlowViewModel,
    val navHostController: NavHostController,
    val schedule: Schedule,
    val currentWeek: Int
)

data class TableState(
    val row: Int = 0,
    val column: Int = 0,
    val isClicked: Int = 0 // 0: 未点击, 1: 点击中, 2: 点击后等待重置
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
) {
    val settings by viewModel.settings.collectAsState()
    val schedule by viewModel.selectedSchedule.collectAsState()
    val columns = if (schedule?.displayWeekends == true) 7 else 5
    val rows = schedule?.lessonTimePeriodInfo?.totalLessonsCount
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    @Composable
    fun BoxScope.ScheduleScreenContent(
        scrollState: ScrollState
    ) {
        if (schedule == null || rows == null || rows == 0) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text =
                    if (settings.isScheduleEmpty)
                        stringResource(Res.string.settings_subtitle_schedule_empty)
                    else
                        stringResource(Res.string.settings_subtitle_schedule_not_selected),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            return
        }
        var isOverviewMode by remember { mutableStateOf(false) }
        val pagerState = schedule!!.totalWeeks.let {
            rememberPagerState(
                initialPage = (schedule!!.termStartDate.weeksTill() - 1).coerceIn(0, it),
                pageCount = { it + 1 }
            )
        }
        val coroutineScope = rememberCoroutineScope()
        val showAddScheduleDialog = remember { mutableStateOf(false) }
        CustomScaffold(
            modifier = Modifier.fillMaxSize(),
            title = {
                if (isOverviewMode) {
                    Text(
                        text = stringResource(Res.string.schedule_title_overview),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            enabled = pagerState.currentPage > 0
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                                contentDescription = null
                            )
                        }
                        SubcomposeLayout { constraints ->
                            val list = listOf(
                                "part1" to Res.string.schedule_title_week_x_part_1,
                                "part2" to Res.string.schedule_title_week_x_part_2,
                                "part3" to Res.string.schedule_title_week_x_part_3,
                                "vacation" to Res.string.schedule_title_week_vacation
                            ).map {
                                subcompose(it.first) {
                                    Text(
                                        text =
                                            if (it.first == "part2") "${pagerState.currentPage + 1}"
                                            else stringResource(it.second),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }[0].measure(constraints)
                            }
                            val part2Max = subcompose("part2max") {
                                Text(
                                    text = stringResource(
                                        Res.string.schedule_title_week_x_part_2,
                                        pagerState.pageCount + 1
                                    ),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }[0].measure(constraints)
                            val width =
                                maxOf(
                                    list[0].width + part2Max.width + list[2].width,
                                    list[3].width
                                )
                            layout(width, constraints.minHeight) {
                                if (pagerState.currentPage < pagerState.pageCount - 1) {
                                    list[0].placeRelative(0, -list[0].height / 2)
                                    list[1].placeRelative(
                                        (width - list[1].width) / 2,
                                        -list[1].height / 2
                                    )
                                    list[2].placeRelative(
                                        width - list[2].width,
                                        -list[2].height / 2
                                    )
                                } else {
                                    list[3].placeRelative(
                                        (width - list[3].width) / 2,
                                        -list[3].height / 2
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            enabled = pagerState.currentPage < pagerState.pageCount - 1
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = null
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navHostController.navigate(Destination.Schedule.ScheduleList)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncAlt,
                        contentDescription = stringResource(Res.string.save)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { isOverviewMode = !isOverviewMode },
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarViewMonth,
                        contentDescription = stringResource(Res.string.schedule_title_overview)
                    )
                }
                IconButton(
                    onClick = { showAddScheduleDialog.value = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.save)
                    )
                }
            },
            topAppBarType = TopAppBarType.CenterAligned,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.fillMaxWidth(0.75f)
                )
            }
        ) {
            AnimatedContent(
                targetState = isOverviewMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                }
            ) { overview ->
                if (overview) {
                    OverviewScheduleTable(
                        schedule = schedule!!,
                        navHostController = navHostController,
                        viewModel = viewModel,
                        rows = rows,
                        columns = 7,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .bottomPadding(extra = 56.dp)
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .bottomPadding(extra = 56.dp)
                    ) { page ->
                        ScheduleTable(
                            scheduleParams = ScheduleParams(
                                viewModel = viewModel,
                                navHostController = navHostController,
                                schedule = schedule!!,
                                currentWeek = page + 1
                            ),
                            rows = rows,
                            columns = columns,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        if (showAddScheduleDialog.value) {
            AddScheduleDialog(
                state = showAddScheduleDialog,
                viewModel = viewModel
            )
        }
    }

    AnimatedContent(
        settings.initialized,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (it) {
                false -> {
                    LoadingIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(96.dp)
                    )
                }

                true -> {
                    val scrollState = rememberScrollState()
                    var fabVisible by remember { mutableStateOf(true) }
                    var lastScroll by remember { mutableStateOf(0) }
                    LaunchedEffect(scrollState.value) {
                        val delta = scrollState.value - lastScroll
                        if (delta > 0) fabVisible = false
                        else if (delta < 0) fabVisible = true
                        lastScroll = scrollState.value
                    }

                    var captureRequested by remember { mutableStateOf(false) }
                    var capturedPngBytes by remember { mutableStateOf<ByteArray?>(null) }

                    @Suppress("DEPRECATION")
                    val pngSaver = rememberFileSaverLauncher { file ->
                        if (file != null && capturedPngBytes != null) {
                            scope.launch {
                                try {
                                    writeBytesToFile(capturedPngBytes!!, file)
                                    snackbarHostState.showSnackbar(
                                        getString(Res.string.schedule_value_export_schedule_success, file.name)
                                    )
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        getString(Res.string.schedule_value_export_schedule_failed, e.message ?: "")
                                    )
                                }
                                capturedPngBytes = null
                            }
                        }
                    }

                    if (captureRequested && schedule != null) {
                        ScheduleImageCapture(
                            schedule = schedule!!,
                            onCaptured = { pngBytes ->
                                captureRequested = false
                                val polyglot = pngBytes + schedule!!.toProtoBufByteArray()
                                if (currentPlatform().isWeb()) {
                                    scope.launch {
                                        writeBytesToFile(
                                            polyglot,
                                            file = null,
                                            filename = schedule!!.name + ".png"
                                        )
                                    }
                                } else {
                                    capturedPngBytes = polyglot
                                    pngSaver.launch(schedule!!.name, "png")
                                }
                            },
                            onError = { error ->
                                captureRequested = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        getString(Res.string.schedule_value_export_schedule_failed, error.message ?: "")
                                    )
                                }
                            }
                        )
                    }

                    ScheduleScreenContent(scrollState)
                    ScheduleFAB(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        viewModel = viewModel,
                        visible = fabVisible,
                        showMessage = { message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                        onExportAsImage = { captureRequested = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSerializationApi::class)
@Composable
fun ScheduleFAB(
    modifier: Modifier = Modifier,
    viewModel: TimeFlowViewModel,
    visible: Boolean,
    showMessage: (String) -> Unit,
    onExportAsImage: () -> Unit,
) {
    val schedule by viewModel.selectedSchedule.collectAsState()
    var showContent by remember { mutableStateOf(false) }
    var showExportChoiceDialog by remember { mutableStateOf(false) }

    @Suppress("DEPRECATION")
    val saver = rememberFileSaverLauncher { file ->
        if (file != null) {
            viewModel.exportScheduleToFile(
                file = file,
                showMessage = showMessage
            )
        }
    }
    val reader = rememberFilePickerLauncher(
        mode = FileKitMode.Single
    ) { file ->
        if (file != null) {
            viewModel.importScheduleFromFile(
                file = file,
                showMessage = showMessage
            )
        }
    }

    if (showExportChoiceDialog && schedule != null) {
        val dialogState = rememberDialogState()
        LaunchedEffect(Unit) { dialogState.show() }
        if (dialogState.visible) {
            MyDialog(
                state = dialogState,
                title = { Text(stringResource(Res.string.export)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = null
                    )
                },
                buttons = DialogDefaults.buttonsDisabled(),
                onEvent = {
                    if (it is DialogEvent.Dismissed) {
                        showExportChoiceDialog = false
                    }
                }
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.export_as_image)) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null
                            )
                        },
                        colors = ListItemDefaults.colors(
                            DialogStyleDefaults.containerColor,
                            DialogStyleDefaults.contentColor,
                            DialogStyleDefaults.iconColor
                        ),
                        modifier = Modifier.clickable {
                            showExportChoiceDialog = false
                            dialogState.dismiss()
                            onExportAsImage()
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.export_as_file)) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null
                            )
                        },
                        colors = ListItemDefaults.colors(
                            DialogStyleDefaults.containerColor,
                            DialogStyleDefaults.contentColor,
                            DialogStyleDefaults.iconColor
                        ),
                        modifier = Modifier.clickable {
                            showExportChoiceDialog = false
                            dialogState.dismiss()
                            if (currentPlatform().isWeb()) {
                                viewModel.viewModelScope.launch {
                                    writeBytesToFile(
                                        schedule!!.toProtoBufByteArray(),
                                        file = null,
                                        filename = schedule!!.name + ".pb"
                                    )
                                }
                            } else {
                                saver.launch(schedule!!.name, "pb")
                            }
                        }
                    )
                }
            }
        }
    }

    FloatingActionButtonMenu(
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier.animateFloatingActionButton(
                    visible = visible,
                    alignment = Alignment.BottomCenter
                ),
                checked = showContent,
                onCheckedChange = { showContent = !showContent },
            ) {
                val imageVector by remember(checkedProgress) {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Share
                    }
                }
                Icon(
                    modifier = Modifier.animateIcon({ checkedProgress }),
                    imageVector = imageVector,
                    contentDescription = null
                )
            }
        },
        expanded = showContent
    ) {
        if (schedule != null) {
            FloatingActionButtonMenuItem(
                onClick = {
                    showContent = false
                    showExportChoiceDialog = true
                },
                text = {
                    Text(stringResource(Res.string.export))
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = null
                    )
                },
            )
        }
        FloatingActionButtonMenuItem(
            onClick = {
                showContent = false
                reader.launch()
            },
            text = {
                Text(stringResource(Res.string.import))
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null
                )
            }
        )
    }
}
