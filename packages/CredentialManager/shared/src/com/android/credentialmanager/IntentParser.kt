/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0N
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.credentialmanager

import android.content.Intent
import android.content.pm.PackageManager
import android.credentials.ui.RequestInfo
import com.android.credentialmanager.ktx.requestInfo
import com.android.credentialmanager.mapper.toGet
import com.android.credentialmanager.mapper.toRequestCancel
import com.android.credentialmanager.mapper.toRequestClose
import com.android.credentialmanager.model.Request

fun Intent.parse(
    packageManager: PackageManager,
    previousIntent: Intent? = null,
): Request {
    this.toRequestClose(packageManager, previousIntent)?.let { closeRequest ->
        return closeRequest
    }

    this.toRequestCancel(packageManager)?.let { cancelRequest ->
        return cancelRequest
    }

    return when (requestInfo?.type) {
        RequestInfo.TYPE_CREATE -> {
            Request.Create
        }

        RequestInfo.TYPE_GET -> {
            this.toGet()
        }

        else -> {
            throw IllegalStateException("Unrecognized request type: ${requestInfo?.type}")
        }
    }
}
