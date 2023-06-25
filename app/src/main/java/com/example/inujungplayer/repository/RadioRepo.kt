package com.example.inujungplayer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.inujungplayer.model.Radio
import com.example.inujungplayer.repository.dao.RoomRadioDao

class RadioRepo(private val radioDao: RoomRadioDao) {

    var _radioid = MutableLiveData<Int>()
    var radioid: LiveData<Int> = _radioid

    var radioInSetStream = MutableLiveData<Radio>()
    fun radioChanged(radio: Radio) {
        radioInSetStream.postValue(radio)
    }

    suspend fun insert(radio: Radio) {
        radioDao.insert(radio)
    }
    suspend fun insertAll(radios: List<Radio>) {
        radioDao.insertAll(radios)
    }

    suspend fun deleteAll() {
        radioDao.deleteAllRadios()
    }
    fun getRadio(id: Int) : Radio {
        return radioDao.getRadio(id)
    }

    suspend fun update(radio: Radio) {
        radioDao.update(radio)
    }
    suspend fun getAllradios(): MutableList<Radio> {
        return radioDao.getAllRadios()
    }
    companion object {
        // For Singleton instantiation
        @Volatile private var instance: RadioRepo? = null

        fun getInstance(radioDao: RoomRadioDao) = //, musicservice: NetworkService) =
            instance ?: synchronized(this) {
                instance ?: RadioRepo(radioDao).also { instance = it }
            }
    }
}