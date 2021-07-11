package com.example.thorium.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.thorium.model.entity.CellInfo
import com.example.thorium.model.repository.Repository
import com.example.thorium.model.source.InfoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository
    val info: LiveData<List<CellInfo>>

    init {
        val infoDao = InfoDatabase.getDatabase(application, viewModelScope).cellInfoDao()
        repository = Repository(infoDao)
        info = repository.data


    }

    fun insert(cellInfo: CellInfo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(cellInfo)
    }
}
