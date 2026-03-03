package org.delcom.dao

import org.delcom.tables.LaptopTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class LaptopDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, LaptopDAO>(LaptopTable)

    var namaProduk by LaptopTable.namaProduk
    var harga by LaptopTable.harga
    var pathGambar by LaptopTable.pathGambar
    var createdAt by LaptopTable.createdAt
    var updatedAt by LaptopTable.updatedAt
}