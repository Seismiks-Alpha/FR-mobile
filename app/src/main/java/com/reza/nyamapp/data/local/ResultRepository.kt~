package com.reza.nyamapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import java.util.concurrent.Executors

class ResultRepository(private val resultDao: ResultDao) {
    fun getAllResultsByDate(date: Long): LiveData<List<ScanResult>> {
        return resultDao.getAllResultsByDate(date)
    }

//    fun getAllResults(resultSortType: ResultSortType): LiveData<PagingData<ScanResult>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = PAGE_SIZE,
//                enablePlaceholders = ENABLE_PLACEHOLDER,
//                initialLoadSize = PAGE_SIZE
//            ),
//            pagingSourceFactory = {
//                resultDao.getAllResults(
//                    QueryUtils.sortedResultQuery(
//                        resultSortType
//                    )
//                )
//            }
//        ).liveData
//    }

    fun getAllResults(): LiveData<PagingData<ScanResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = ENABLE_PLACEHOLDER,
                initialLoadSize = PAGE_SIZE
            ),
            pagingSourceFactory = {
                resultDao.getAllResults()
            }
        ).liveData
    }

    fun getCaloriesToday(date: String): LiveData<Int> {
        return resultDao.getCaloriesByDate(date)
    }


    fun insertResult(result: ScanResult) {
        executeThread{
            resultDao.insertResult(result)
        }
    }

    fun getResultById(id: Int): LiveData<ScanResult?> {
        return resultDao.getResultById(id)
    }

    fun deleteResult(result: ScanResult) {
        executeThread {
            resultDao.deleteResult(result)
        }
    }

    private fun executeThread(f: () -> Unit) {
        Executors.newSingleThreadExecutor().execute(f)
    }

    companion object {

        private const val PAGE_SIZE = 10
        private const val ENABLE_PLACEHOLDER = true

        @Volatile
        private var instance: ResultRepository? = null

        fun getInstance(context: Context): ResultRepository {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    val database = ResultDatabase.getInstance(context)
                    instance = ResultRepository(database.ResultDao())
                }
                return instance as ResultRepository
            }
        }
    }
}