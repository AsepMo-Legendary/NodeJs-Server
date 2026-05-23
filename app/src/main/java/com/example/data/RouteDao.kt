package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY timestamp ASC")
    fun getAllRoutes(): Flow<List<ServerRoute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: ServerRoute)

    @Delete
    suspend fun deleteRoute(route: ServerRoute)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRouteById(id: Int)

    @Query("SELECT COUNT(*) FROM routes")
    suspend fun getCount(): Int
}
