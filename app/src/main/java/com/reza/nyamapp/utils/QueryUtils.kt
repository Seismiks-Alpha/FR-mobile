package com.reza.nyamapp.utils

import androidx.sqlite.db.SimpleSQLiteQuery

object QueryUtils {

    fun sortedResultQuery(resultSortType: ResultSortType): SimpleSQLiteQuery {
        val query = StringBuilder().append("SELECT * FROM scan_result ")
        when (resultSortType) {
            ResultSortType.DATE_ADDED -> {
                query.append("ORDER BY resultAddedInMillis DESC")
            }

            ResultSortType.CALORIE -> {
                query.append("ORDER BY calorie DESC")
            }

            ResultSortType.FOOD_NAME -> {
                query.append("ORDER BY foodName ASC")
            }
        }

        return SimpleSQLiteQuery(query.toString())
    }
}