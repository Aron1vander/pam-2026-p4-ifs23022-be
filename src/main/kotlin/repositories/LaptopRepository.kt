package org.delcom.repositories

import org.delcom.dao.LaptopDAO
import org.delcom.entities.Laptop
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.LaptopTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class LaptopRepository : ILaptopRepository {

    private fun daoToModel(dao: LaptopDAO) = Laptop(
        id = dao.id.value.toString(),
        namaProduk = dao.namaProduk,
        harga = dao.harga,
        pathGambar = dao.pathGambar,
        createdAt = dao.createdAt,
        updatedAt = dao.updatedAt,
    )

    override suspend fun getLaptops(search: String): List<Laptop> = suspendTransaction {
        if (search.isBlank()) {
            LaptopDAO.all()
                .orderBy(LaptopTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToModel)
        } else {
            val keyword = "%${search.lowercase()}%"
            LaptopDAO
                .find { LaptopTable.namaProduk.lowerCase() like keyword }
                .orderBy(LaptopTable.namaProduk to SortOrder.ASC)
                .limit(20)
                .map(::daoToModel)
        }
    }

    override suspend fun getLaptopById(id: String): Laptop? = suspendTransaction {
        LaptopDAO
            .find { LaptopTable.id eq UUID.fromString(id) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getLaptopByName(name: String): Laptop? = suspendTransaction {
        LaptopDAO
            .find { LaptopTable.namaProduk eq name }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addLaptop(laptop: Laptop): String = suspendTransaction {
        val dao = LaptopDAO.new {
            namaProduk = laptop.namaProduk
            harga = laptop.harga
            pathGambar = laptop.pathGambar
            createdAt = laptop.createdAt
            updatedAt = laptop.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun updateLaptop(id: String, newLaptop: Laptop): Boolean = suspendTransaction {
        val dao = LaptopDAO
            .find { LaptopTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.namaProduk = newLaptop.namaProduk
            dao.harga = newLaptop.harga
            dao.pathGambar = newLaptop.pathGambar
            dao.updatedAt = newLaptop.updatedAt
            true
        } else false
    }

    override suspend fun removeLaptop(id: String): Boolean = suspendTransaction {
        val rowsDeleted = LaptopTable.deleteWhere {
            LaptopTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }
}