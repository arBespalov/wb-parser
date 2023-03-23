package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.automotivecodelab.wbgoodstracker.domain.models.Ad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
): AdLocalDataSource {

    private val AD_IMG_URL = stringPreferencesKey("ad_img_url")
    private val AD_URL = stringPreferencesKey("ad_url")

    override fun observeAd(): Flow<Ad?> {
        return dataStore.data
            .map { prefs ->
                val imgUrl = prefs[AD_IMG_URL]
                val url = prefs[AD_URL]
                if (imgUrl != null && url != null) {
                    Ad(imgUrl = imgUrl, url = url)
                } else {
                    null
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun setAd(ad: Ad?) {
        dataStore.edit { prefs ->
            if (ad == null) {
                prefs.remove(AD_IMG_URL)
                prefs.remove(AD_URL)
            } else {
                prefs[AD_IMG_URL] = ad.imgUrl
                prefs[AD_URL] = ad.url
            }
        }
    }
}
