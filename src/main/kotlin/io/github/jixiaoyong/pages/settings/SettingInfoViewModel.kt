package io.github.jixiaoyong.pages.settings

import ApkSigner
import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.data.SettingPreferencesRepository
import io.github.jixiaoyong.utils.showToast
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * @author : jixiaoyong
 * @description ：setting info view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SettingInfoViewModel(private val repository: SettingPreferencesRepository) :
    BaseViewModel() {

    private val uiStateFlow: MutableStateFlow<SettingInfoUiState> = MutableStateFlow(SettingInfoUiState())
    val uiState = uiStateFlow.asStateFlow()

    override fun onInit() {

        combine(
            repository.apkSigner,
            repository.zipAlign,
            repository.aapt,
            repository.isAutoMatchSignature
        ) { apkSigner, zipAlign, aapt, isAutoMatchSignature ->
            uiStateFlow.value.copy(
                apkSign = apkSigner,
                zipAlign = zipAlign,
                aapt = aapt,
                isAutoMatchSignature = isAutoMatchSignature
            )
        }.onEach {
            uiStateFlow.emit(it)
        }
            .launchIn(viewModelScope)

    }

    fun toggleResetDialog() {
        uiStateFlow.value = uiStateFlow.value.copy(
            resetInfo = uiStateFlow.value.resetInfo.copy(showResetDialog = !uiStateFlow.value.resetInfo.showResetDialog)
        )
    }

    fun toggleLanguageDialog() {
        uiStateFlow.value = uiStateFlow.value.copy(
            resetInfo = uiStateFlow.value.resetInfo.copy(showChangeLanguageDialog = !uiStateFlow.value.resetInfo.showChangeLanguageDialog)
        )
    }

    /**
     *  Updates the reset configuration values based on the provided parameters.
     *  null 表示不修改现有值
     */
    fun updateResetConfig(
        resetSignInfo: Boolean? = null,
        resetApkTools: Boolean? = null,
        resetSignTypes: Boolean? = null,
        resetSignedDirectory: Boolean? = null,
    ) {
        val oldResetInfo = uiStateFlow.value.resetInfo
        uiStateFlow.value = uiStateFlow.value.copy(
            resetInfo = oldResetInfo.copy(
                resetSignInfo = resetSignInfo ?: oldResetInfo.resetSignInfo,
                resetApkTools = resetApkTools ?: oldResetInfo.resetApkTools,
                resetSignTypes = resetSignTypes ?: oldResetInfo.resetSignTypes,
                resetSignedDirectory = resetSignedDirectory ?: oldResetInfo.resetSignedDirectory
            )
        )
    }

    fun runRestConfig() {
        viewModelScope.launch {
            val resetConfig = uiStateFlow.value.resetInfo

            if (resetConfig.resetSignInfo) {
                repository.saveSelectedSignInfo(null)
                repository.saveSignInfoList(null)
            }
            if (resetConfig.resetApkTools) {
                repository.saveApkSignPath(null)
                repository.saveZipAlignPath(null)
                repository.saveAaptPath(null)
            }
            if (resetConfig.resetSignTypes) {
                repository.setSignTypeList(emptySet())
            }
            if (resetConfig.resetSignedDirectory) {
                repository.saveSignedDirectory(null)
            }
        }
    }

    fun setupBuildToolsConfig(buildToolDir: String) {
        val result = ApkSigner.init(buildToolDir)
        saveApkSigner(ApkSigner.apkSignerPath)
        saveZipAlign(ApkSigner.zipAlignPath)
        saveAapt(ApkSigner.aaptPath)
        showToast(result ?: "修改成功")
    }

    fun saveApkSigner(apkSigner: String?) {
        viewModelScope.launch { repository.saveApkSignPath(apkSigner) }
    }

    fun saveZipAlign(zipAlign: String?) {
        viewModelScope.launch { repository.saveZipAlignPath(zipAlign) }
    }

    fun saveAapt(aapt: String?) {
        viewModelScope.launch { repository.saveAaptPath(aapt) }
    }

    fun saveAutoMatchSignature(autoMatch: Boolean) {
        viewModelScope.launch { repository.saveAutoMatchSignature(autoMatch) }
    }

    fun changeLanguage(currentLanguage: String) {
        viewModelScope.launch { repository.setLanguage(currentLanguage) }
    }
}

data class SettingInfoUiState(
    val apkSign: String? = null,
    val zipAlign: String? = null,
    val aapt: String? = null,
    val isAutoMatchSignature: Boolean = false,
    val resetInfo: SettingInfoResetState = SettingInfoResetState()
)

data class SettingInfoResetState(
    val showResetDialog: Boolean = false,
    val resetSignInfo: Boolean = false,
    val resetApkTools: Boolean = false,
    val resetSignTypes: Boolean = false,
    val resetSignedDirectory: Boolean = false,
    val showChangeLanguageDialog: Boolean = false
)