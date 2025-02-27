package me.ash.reader.ui.page.settings.accounts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.KeepArchivedPreference
import me.ash.reader.infrastructure.preference.SyncBlockListPreference
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.preference.not
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.ext.showToastLong
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.page.settings.accounts.connection.AccountConnection
import me.ash.reader.ui.theme.palette.onLight

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AccountDetailsPage(
    navController: NavHostController = rememberAnimatedNavController(),
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val uiState = viewModel.accountUiState.collectAsStateValue()
    val context = LocalContext.current

    val selectedAccount = uiState.selectedAccount.collectAsStateValue(initial = null)

    var nameValue by remember { mutableStateOf(selectedAccount?.name) }
    var nameDialogVisible by remember { mutableStateOf(false) }
    var blockListValue by remember {
        mutableStateOf(SyncBlockListPreference.toString(selectedAccount?.syncBlockList
            ?: SyncBlockListPreference.default))
    }
    var blockListDialogVisible by remember { mutableStateOf(false) }
    var syncIntervalDialogVisible by remember { mutableStateOf(false) }
    var keepArchivedDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            it.arguments?.getString("accountId")?.let {
                viewModel.initData(it.toInt())
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { result ->
        viewModel.exportAsOPML(selectedAccount!!.id!!) { string ->
            result?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(string.toByteArray())
                }
            }
        }
    }

    val exportStarredLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { result ->
        viewModel.exportStarred(selectedAccount!!.id!!) { string ->
            result?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(string.toByteArray())
                }
            }
        }
    }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                navController.popBackStack()
            }
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = selectedAccount?.type?.toDesc(context) ?: "", desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.display),
                    )
                    SettingItem(
                        title = stringResource(R.string.name),
                        desc = selectedAccount?.name ?: "",
                        onClick = {
                            nameValue = selectedAccount?.name
                            nameDialogVisible = true
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                if (selectedAccount != null) {
                    item {
                        AccountConnection(account = selectedAccount)
                    }
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.synchronous),
                    )
                    SettingItem(
                        title = stringResource(R.string.sync_interval),
                        desc = selectedAccount?.syncInterval?.toDesc(context),
                        onClick = { syncIntervalDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.sync_once_on_start),
                        onClick = {
                            selectedAccount?.id?.let {
                                (!selectedAccount.syncOnStart).put(it, viewModel)
                            }
                        },
                    ) {
                        RYSwitch(activated = selectedAccount?.syncOnStart?.value == true) {
                            selectedAccount?.id?.let {
                                (!selectedAccount.syncOnStart).put(it, viewModel)
                            }
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.only_on_wifi),
                        onClick = {
                            selectedAccount?.id?.let {
                                (!selectedAccount.syncOnlyOnWiFi).put(it, viewModel)
                            }
                        },
                    ) {
                        RYSwitch(activated = selectedAccount?.syncOnlyOnWiFi?.value == true) {
                            selectedAccount?.id?.let {
                                (!selectedAccount.syncOnlyOnWiFi).put(it, viewModel)
                            }
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.only_when_charging),
                        onClick = {
                            selectedAccount?.id?.let {
                                (!selectedAccount.syncOnlyWhenCharging).put(it, viewModel)
                            }
                        },
                    ) {
                        RYSwitch(activated = selectedAccount?.syncOnlyWhenCharging?.value == true) {
                            selectedAccount?.id?.let {
                                (!selectedAccount.syncOnlyWhenCharging).put(it, viewModel)
                            }
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.keep_archived_articles),
                        desc = selectedAccount?.keepArchived?.toDesc(context),
                        onClick = { keepArchivedDialogVisible = true },
                    ) {}
                    // SettingItem(
                    //     title = stringResource(R.string.block_list),
                    //     onClick = { blockListDialogVisible = true },
                    // ) {}
                    Tips(text = stringResource(R.string.synchronous_tips))
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.advanced),
                    )
                    SettingItem(
                        title = stringResource(R.string.export_as_opml),
                        onClick = {
                            launcher.launch("ReadYou.opml")
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.export_starred),
                        onClick = {
                            exportStarredLauncher.launch("ReadYouStarred.json")
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.clear_all_articles),
                        onClick = { viewModel.showClearDialog() },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.delete_account),
                        onClick = { viewModel.showDeleteDialog() },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    TextFieldDialog(
        visible = nameDialogVisible,
        title = stringResource(R.string.name),
        value = nameValue ?: "",
        placeholder = stringResource(R.string.value),
        onValueChange = {
            nameValue = it
        },
        onDismissRequest = {
            nameDialogVisible = false
        },
        onConfirm = {
            if (nameValue?.isNotBlank() == true) {
                selectedAccount?.id?.let {
                    viewModel.update(it) {
                        name = nameValue ?: ""
                    }
                }
                nameDialogVisible = false
            }
        }
    )

    RadioDialog(
        visible = syncIntervalDialogVisible,
        title = stringResource(R.string.sync_interval),
        options = SyncIntervalPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == selectedAccount?.syncInterval,
            ) {
                selectedAccount?.id?.let { accountId ->
                    it.put(accountId, viewModel)
                }
            }
        }
    ) {
        syncIntervalDialogVisible = false
    }

    RadioDialog(
        visible = keepArchivedDialogVisible,
        title = stringResource(R.string.keep_archived_articles),
        options = KeepArchivedPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == selectedAccount?.keepArchived,
            ) {
                selectedAccount?.id?.let { accountId ->
                    it.put(accountId, viewModel)
                }
            }
        }
    ) {
        keepArchivedDialogVisible = false
    }

    TextFieldDialog(
        visible = blockListDialogVisible,
        title = stringResource(R.string.block_list),
        value = blockListValue,
        singleLine = false,
        placeholder = stringResource(R.string.value),
        onValueChange = {
            blockListValue = it
        },
        onDismissRequest = {
            blockListDialogVisible = false
        },
        onConfirm = {
            selectedAccount?.id?.let {
                SyncBlockListPreference.put(it, viewModel, selectedAccount.syncBlockList)
                blockListDialogVisible = false
                context.showToast(selectedAccount.syncBlockList.toString())
            }
        }
    )

    RYDialog(
        visible = uiState.clearDialogVisible,
        onDismissRequest = {
            viewModel.hideClearDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = stringResource(R.string.clear_all_articles),
            )
        },
        title = {
            Text(text = stringResource(R.string.clear_all_articles))
        },
        text = {
            Text(text = stringResource(R.string.clear_all_articles_tips))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedAccount?.let {
                        viewModel.clear(it) {
                            viewModel.hideClearDialog()
                            context.showToastLong(context.getString(R.string.clear_all_articles_toast))
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.clear),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.hideClearDialog()
                }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
    )

    RYDialog(
        visible = uiState.deleteDialogVisible,
        onDismissRequest = {
            viewModel.hideDeleteDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.PersonOff,
                contentDescription = stringResource(R.string.delete_account),
            )
        },
        title = {
            Text(text = stringResource(R.string.delete_account))
        },
        text = {
            Text(text = stringResource(R.string.delete_account_tips))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedAccount?.id?.let {
                        viewModel.delete(it) {
                            navController.popBackStack()
                            context.showToastLong(context.getString(R.string.delete_account_toast))
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.delete),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.hideDeleteDialog()
                }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
    )
}
