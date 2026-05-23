package com.example.data

import kotlinx.coroutines.flow.Flow

class RouteRepository(private val routeDao: RouteDao) {
    val allRoutes: Flow<List<ServerRoute>> = routeDao.getAllRoutes()

    suspend fun insert(route: ServerRoute) {
        routeDao.insertRoute(route)
    }

    suspend fun delete(route: ServerRoute) {
        routeDao.deleteRoute(route)
    }

    suspend fun deleteById(id: Int) {
        routeDao.deleteRouteById(id)
    }

    suspend fun getCount(): Int {
        return routeDao.getCount()
    }
}
