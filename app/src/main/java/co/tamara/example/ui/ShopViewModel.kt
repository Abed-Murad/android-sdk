package co.tamara.example.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import co.tamara.example.data.DataSource
import co.tamara.example.model.EItem

class ShopViewModel(private val dataSource: DataSource) : ViewModel() {
    val items: LiveData<List<EItem>> = liveData {
        val items = loadDataFromAsset()
        emit(items)
    }

    private fun loadDataFromAsset(): List<EItem> {
        return dataSource.loadListItem()
    }
}
