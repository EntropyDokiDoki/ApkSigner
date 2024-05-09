package io.github.jd1378.otphelper.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException


class PreferenceDataStoreHelper(private val dataSource: DataStore<Preferences>) {

    /* This returns us a flow of data from DataStore.
    Basically as soon we update the value in Datastore,
    the values returned by it also changes. */
    fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataSource.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val result = preferences[key] ?: defaultValue
                result
            }

    /**
     *  This returns us a flow of data from DataStore.
    Basically as soon we update the value in Datastore,
    the values returned by it also changes.
     */
    fun <T> getPreference(key: Preferences.Key<T>): Flow<T?> =
        dataSource.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val result = preferences[key]
                result
            }

    /* This returns the last saved value of the key. If we change the value,
    it wont effect the values produced by this function */
    suspend fun <T> getFirstPreference(key: Preferences.Key<T>, defaultValue: T): T =
        dataSource.data.first()[key] ?: defaultValue

    // This Sets the value based on the value passed in value parameter.
    suspend fun <T> putPreference(key: Preferences.Key<T>, value: T?) {
        if (null == value) {
            removePreference(key)
        } else {
            dataSource.edit { preferences -> preferences[key] = value }
        }
    }

    // This Function removes the Key Value pair from the datastore, hereby removing it completely.
    suspend fun <T> removePreference(key: Preferences.Key<T>) {
        dataSource.edit { preferences -> preferences.remove(key) }
    }


    // This function clears the entire Preference Datastore.
    suspend fun addAllPreference(map: Map<Preferences.Key<Any>, Any>) {
        dataSource.edit { preferences ->
            map.forEach { (k, v) ->
                preferences[k] = v
            }
        }
    }

    // This function clears the entire Preference Datastore.
    suspend fun <T> clearAllPreference() {
        dataSource.edit { preferences -> preferences.clear() }
    }
}
