/*
 *     Copyright (C) 2024-present StellarSand
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.password.monitor.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.password.monitor.preferences.PreferenceManager
import com.password.monitor.preferences.PreferenceManager.Companion.CLEAR_CLIPBOARD_TIME
import com.password.monitor.workers.ClearClipboardWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.TimeUnit

class ClipboardUtils {
    
    companion object : KoinComponent {
        
        // Hide from revealing on copy
        // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#SensitiveContent
        fun ClipData.hideSensitiveContent() {
            description.extras = PersistableBundle().apply {
                if (Build.VERSION.SDK_INT >= 33) putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                else putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
        
        fun scheduleClipboardClear(context: Context) {
            val clearClipboardRequest =
                OneTimeWorkRequest
                    .Builder(ClearClipboardWorker::class.java)
                    .setInitialDelay(
                        get<PreferenceManager>().getLong(CLEAR_CLIPBOARD_TIME),
                        TimeUnit.SECONDS)
                    .build()
            
            // Create a new work request with a 1 min delay
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "PasswordMonitor_clear_clipboard_work",
                ExistingWorkPolicy.REPLACE,
                clearClipboardRequest
            )
        }
        
    }
}