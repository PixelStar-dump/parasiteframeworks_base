package com.android.systemui.qs.tiles.viewmodel

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents tiles behaviour logic. This ViewModel is a connection between tile view and data
 * layers. All direct inheritors must be added to the [QSTileViewModelInterfaceComplianceTest] class
 * to pass compliance tests.
 *
 * All methods of this view model should be considered running on the main thread. This means no
 * synchronous long running operations are permitted in any method.
 */
interface QSTileViewModel {

    /**
     * State of the tile to be shown by the view. It's guaranteed that it's only accessed between
     * [QSTileLifecycle.ALIVE] and [QSTileLifecycle.DEAD].
     */
    val state: SharedFlow<QSTileState>

    val config: QSTileConfig

    /**
     * Specifies whether this device currently supports this tile. This might be called outside of
     * [QSTileLifecycle.ALIVE] and [QSTileLifecycle.DEAD] bounds (for example in Edit Mode).
     */
    val isAvailable: StateFlow<Boolean>

    /**
     * Handles ViewModel lifecycle. Implementations should be inactive outside of
     * [QSTileLifecycle.ALIVE] and [QSTileLifecycle.DEAD] bounds.
     */
    fun onLifecycle(lifecycle: QSTileLifecycle)

    /**
     * Notifies about the user change. Implementations should avoid using 3rd party userId sources
     * and use this value instead. This is to maintain consistent and concurrency-free behaviour
     * across different parts of QS.
     */
    fun onUserIdChanged(userId: Int)

    /** Triggers the emission of the new [QSTileState] in a [state]. */
    fun forceUpdate()

    /** Notifies underlying logic about user input. */
    fun onActionPerformed(userAction: QSTileUserAction)
}

/**
 * Returns the immediate state of the tile or null if the state haven't been collected yet. Favor
 * reactive consumption over the [currentState], because there is no guarantee that current value
 * would be available at any time.
 */
val QSTileViewModel.currentState: QSTileState?
    get() = state.replayCache.lastOrNull()
