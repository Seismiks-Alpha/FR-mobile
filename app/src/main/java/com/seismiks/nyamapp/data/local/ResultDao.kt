package com.seismiks.nyamapp.data.local

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ResultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertResult(result: ScanResult) : Long

//    @Transaction
//    @RawQuery(observedEntities = [ScanResult::class])
//    fun getAllResults(query: SupportSQLiteQuery): PagingSource<Int, ScanResult>

    @Query("SELECT * FROM scan_result ORDER BY resultAddedInMillis DESC")
    fun getAllResults(): PagingSource<Int, ScanResult>

    @Query("SELECT * FROM scan_result WHERE resultAddedInMillis >= :date")
    fun getAllResultsByDate(date: Long): LiveData<List<ScanResult>>

    @Query("SELECT * FROM scan_result WHERE id = :id")
    fun getResultById(id: Int): LiveData<ScanResult?>

    @Query("SELECT SUM(kalori) FROM scan_result WHERE resultAddedInMillis = :date")
    fun getCaloriesByDate(date: String): LiveData<Int>

    @Delete
    fun deleteResult(result: ScanResult)

}