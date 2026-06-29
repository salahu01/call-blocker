package com.salah.callblocker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salah.callblocker.CallBlockerApp
import com.salah.callblocker.data.RuleRepository
import com.salah.callblocker.data.SettingsStore
import com.salah.callblocker.data.ThemeMode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val settings: SettingsStore = CallBlockerApp.settings(app)
    private val repo: RuleRepository = CallBlockerApp.repository(app)

    val allowContacts: StateFlow<Boolean> = settings.allowContacts
    val notifyOnBlock: StateFlow<Boolean> = settings.notifyOnBlock
    val blockUnknown: StateFlow<Boolean> = settings.blockUnknown
    val themeMode: StateFlow<ThemeMode> = settings.themeMode

    fun setAllowContacts(v: Boolean) = settings.setAllowContacts(v)
    fun setNotifyOnBlock(v: Boolean) = settings.setNotifyOnBlock(v)
    fun setBlockUnknown(v: Boolean) = settings.setBlockUnknown(v)
    fun setThemeMode(v: ThemeMode) = settings.setThemeMode(v)

    suspend fun exportJson(): String = repo.exportJson()

    fun importJson(json: String, replace: Boolean) {
        viewModelScope.launch {
            repo.importJson(json, replace)
        }
    }
}
