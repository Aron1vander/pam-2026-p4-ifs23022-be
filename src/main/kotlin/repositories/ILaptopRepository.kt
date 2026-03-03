package org.delcom.repositories

import org.delcom.entities.Laptop

interface ILaptopRepository {
    suspend fun getLaptops(search: String): List<Laptop>
    suspend fun getLaptopById(id: String): Laptop?
    suspend fun getLaptopByName(name: String): Laptop?
    suspend fun addLaptop(laptop: Laptop): String
    suspend fun updateLaptop(id: String, newLaptop: Laptop): Boolean
    suspend fun removeLaptop(id: String): Boolean
}