package me.ash.reader.ui.page.home.feeds.drawer.group

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.domain.model.group.Group
import me.ash.reader.ui.component.RenameDialog
import me.ash.reader.ui.component.base.BottomDrawer
import me.ash.reader.ui.component.base.RYSelectionChip
import me.ash.reader.ui.component.base.RYSortPriority
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.ext.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GroupOptionDrawer(
    viewModel: GroupOptionViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val groupOptionUiState = viewModel.groupOptionUiState.collectAsStateValue()
    val group = groupOptionUiState.group
    val toastString = stringResource(R.string.rename_toast, groupOptionUiState.newName)

    BackHandler(groupOptionUiState.drawerState.isVisible) {
        scope.launch {
            groupOptionUiState.drawerState.hide()
        }
    }

    BottomDrawer(
        drawerState = groupOptionUiState.drawerState,
        sheetContent = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = group?.name ?: stringResource(R.string.unknown),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.roundClick {
                            if (viewModel.rssService.get().update) {
                                viewModel.showRenameDialog()
                            }
                        },
                        text = group?.name ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.group_option_tips),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))
                    Subtitle(text = stringResource(R.string.preset))

                    Spacer(modifier = Modifier.height(10.dp))
                    Preset(viewModel, group, context)

                    Spacer(modifier = Modifier.height(26.dp))
                    Subtitle(text = stringResource(R.string.priority))

                    Spacer(modifier = Modifier.height(10.dp))
                    RYSortPriority(
                        priority = groupOptionUiState.group?.priority ?: 0,
                        setPriority = {
                            viewModel.setPriority(it)
                            viewModel.hideDrawer(scope)
                        })

                    if (viewModel.rssService.get().move && groupOptionUiState.groups.size != 1) {
                        Spacer(modifier = Modifier.height(26.dp))
                        Subtitle(text = stringResource(R.string.move_to_group))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (groupOptionUiState.groups.size > 6) {
                            LazyRowGroups(groupOptionUiState, group, viewModel)
                        } else {
                            FlowRowGroups(groupOptionUiState, group, viewModel)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    ) {
        content()
    }

    ClearGroupDialog(groupName = group?.name ?: "")
    DeleteGroupDialog(groupName = group?.name ?: "")
    AllAllowNotificationDialog(groupName = group?.name ?: "")
    AllParseFullContentDialog(groupName = group?.name ?: "")
    AllMoveToGroupDialog(groupName = group?.name ?: "")
    RenameDialog(
        visible = groupOptionUiState.renameDialogVisible,
        value = groupOptionUiState.newName,
        onValueChange = {
            viewModel.inputNewName(it)
        },
        onDismissRequest = {
            viewModel.hideRenameDialog()
        },
        onConfirm = {
            viewModel.rename()
            viewModel.hideDrawer(scope)
            context.showToast(toastString)
        }
    )
}

@Composable
private fun Preset(
    viewModel: GroupOptionViewModel,
    group: Group?,
    context: Context,
) {
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        crossAxisSpacing = 10.dp,
        mainAxisSpacing = 10.dp,
    ) {
        RYSelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(R.string.allow_notification),
            selected = false,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.allow_notification),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            viewModel.showAllAllowNotificationDialog()
        }
        RYSelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(R.string.parse_full_content),
            selected = false,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = stringResource(R.string.parse_full_content),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            viewModel.showAllParseFullContentDialog()
        }
        RYSelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(R.string.clear_articles),
            selected = false,
        ) {
            viewModel.showClearDialog()
        }
        if (viewModel.rssService.get().delete && group?.id != context.currentAccountId.getDefaultGroupId()) {
            RYSelectionChip(
                modifier = Modifier.animateContentSize(),
                content = stringResource(R.string.delete_group),
                selected = false,
            ) {
                viewModel.showDeleteDialog()
            }
        }
    }
}

@Composable
private fun FlowRowGroups(
    groupOptionUiState: GroupOptionUiState,
    group: Group?,
    groupOptionViewModel: GroupOptionViewModel,
) {
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        crossAxisSpacing = 10.dp,
        mainAxisSpacing = 10.dp,
    ) {
        groupOptionUiState.groups.forEach {
            if (it.id != group?.id) {
                RYSelectionChip(
                    modifier = Modifier.animateContentSize(),
                    content = it.name,
                    selected = false,
                ) {
                    groupOptionViewModel.showAllMoveToGroupDialog(it)
                }
            }
        }
    }
}

@Composable
private fun LazyRowGroups(
    groupOptionUiState: GroupOptionUiState,
    group: Group?,
    groupOptionViewModel: GroupOptionViewModel,
) {
    LazyRow {
        items(groupOptionUiState.groups) {
            if (it.id != group?.id) {
                RYSelectionChip(
                    modifier = Modifier.animateContentSize(),
                    content = it.name,
                    selected = false,
                ) {
                    groupOptionViewModel.showAllMoveToGroupDialog(it)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}
