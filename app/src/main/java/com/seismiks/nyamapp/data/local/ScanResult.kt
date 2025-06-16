package com.seismiks.nyamapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_result")
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "image")
    val image: String,

    @ColumnInfo(name = "foodName")
    val foodName: String?,

    @ColumnInfo(name = "kalori")
    val kalori: Double,

    @ColumnInfo(name = "lemak")
    val lemak: Double?,

    @ColumnInfo(name = "karbo")
    val karbo: Double?,

    @ColumnInfo(name = "protein")
    val protein: Double?,

    @ColumnInfo(name = "berat")
    val weight: Double?,

    @ColumnInfo(name = "resultAddedInMillis")
    val resultAddedInMillis: Long,

    )