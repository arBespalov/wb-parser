package com.automotivecodelab.wbgoodstracker.data

import android.content.Context
import com.automotivecodelab.wbgoodstracker.R

class ResourceManagerImpl(context: Context) : ResourceManager {
    private val resources = context.resources

    override fun getAllItemsString(): String {
        return resources.getString(R.string.all_items)
    }
}
