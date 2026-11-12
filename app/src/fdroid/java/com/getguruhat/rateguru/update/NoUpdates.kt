package com.getguruhat.rateguru.update

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// F-Droid build: F-Droid delivers updates, so the app never checks GitHub or installs APKs.

class AppRelease

sealed interface UpdateState {
    data object None : UpdateState
}

class UpdateViewModel : ViewModel() {
    val state: StateFlow<UpdateState> = MutableStateFlow(UpdateState.None)
    fun update(release: AppRelease) = Unit
    fun dismiss() = Unit
    fun onResume() = Unit
}

@Composable
fun UpdateDialog(state: UpdateState, onUpdate: (AppRelease) -> Unit, onDismiss: () -> Unit) = Unit
