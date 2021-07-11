package com.example.thorium.model.repository

import androidx.lifecycle.LiveData
import com.example.thorium.model.entity.CellInfo
import com.example.thorium.model.source.InfoDao

class Repository(private val cellInfoDao: InfoDao) {

    val data: LiveData<List<CellInfo>> = cellInfoDao.getAlphabetizedWords()

    suspend fun insert(cellInfo: CellInfo) {
        cellInfoDao.insert(cellInfo)
    }
}