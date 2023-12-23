/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.keyguard.ui.composable.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.compose.animation.scene.SceneScope
import com.android.systemui.notifications.ui.composable.NotificationStack
import com.android.systemui.statusbar.notification.stack.ui.viewmodel.NotificationsPlaceholderViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher

class NotificationSection
@Inject
constructor(
    private val viewModel: NotificationsPlaceholderViewModel,
) {
    @Composable
    fun SceneScope.Notifications(modifier: Modifier = Modifier) {
        NotificationStack(
            viewModel = viewModel,
            isScrimVisible = false,
            modifier = modifier,
        )
    }
}
