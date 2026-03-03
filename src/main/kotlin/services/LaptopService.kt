package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.LaptopRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.ILaptopRepository
import java.io.File
import java.util.UUID

class LaptopService(private val laptopRepository: ILaptopRepository) {

    suspend fun getAllLaptops(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val laptops = laptopRepository.getLaptops(search)
        call.respond(DataResponse("success", "Berhasil mengambil daftar laptop", mapOf("laptops" to laptops)))
    }

    suspend fun getLaptopById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID laptop tidak boleh kosong!")
        val laptop = laptopRepository.getLaptopById(id)
            ?: throw AppException(404, "Data laptop tidak tersedia!")
        call.respond(DataResponse("success", "Berhasil mengambil data laptop", mapOf("laptop" to laptop)))
    }

    private suspend fun getLaptopRequest(call: ApplicationCall): LaptopRequest {
        val laptopReq = LaptopRequest()
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "namaProduk" -> laptopReq.namaProduk = part.value.trim()
                        "harga" -> laptopReq.harga = part.value.trim().toLongOrNull() ?: 0L
                    }
                }
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/laptops/$fileName"
                    val file = File(filePath)
                    file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    laptopReq.pathGambar = filePath
                }
                else -> {}
            }
            part.dispose()
        }
        return laptopReq
    }

    private fun validateLaptopRequest(laptopReq: LaptopRequest) {
        val validator = ValidatorHelper(laptopReq.toMap())
        validator.required("namaProduk", "Nama produk tidak boleh kosong")
        validator.required("pathGambar", "Gambar tidak boleh kosong")
        validator.validate()

        if (laptopReq.harga <= 0) throw AppException(400, "harga: Harga harus lebih dari 0")

        val file = File(laptopReq.pathGambar)
        if (!file.exists()) throw AppException(400, "Gambar laptop gagal diupload!")
    }

    suspend fun createLaptop(call: ApplicationCall) {
        val laptopReq = getLaptopRequest(call)
        validateLaptopRequest(laptopReq)

        val exist = laptopRepository.getLaptopByName(laptopReq.namaProduk)
        if (exist != null) {
            File(laptopReq.pathGambar).takeIf { it.exists() }?.delete()
            throw AppException(409, "Laptop dengan nama produk ini sudah terdaftar!")
        }

        val laptopId = laptopRepository.addLaptop(laptopReq.toEntity())
        call.respond(DataResponse("success", "Berhasil menambahkan data laptop", mapOf("laptopId" to laptopId)))
    }

    suspend fun updateLaptop(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID laptop tidak boleh kosong!")
        val oldLaptop = laptopRepository.getLaptopById(id)
            ?: throw AppException(404, "Data laptop tidak tersedia!")

        val laptopReq = getLaptopRequest(call)
        if (laptopReq.pathGambar.isEmpty()) laptopReq.pathGambar = oldLaptop.pathGambar

        validateLaptopRequest(laptopReq)

        if (laptopReq.namaProduk != oldLaptop.namaProduk) {
            val exist = laptopRepository.getLaptopByName(laptopReq.namaProduk)
            if (exist != null) {
                File(laptopReq.pathGambar).takeIf { it.exists() }?.delete()
                throw AppException(409, "Laptop dengan nama produk ini sudah terdaftar!")
            }
        }

        if (laptopReq.pathGambar != oldLaptop.pathGambar) {
            File(oldLaptop.pathGambar).takeIf { it.exists() }?.delete()
        }

        val isUpdated = laptopRepository.updateLaptop(id, laptopReq.toEntity())
        if (!isUpdated) throw AppException(400, "Gagal memperbarui data laptop!")

        call.respond(DataResponse("success", "Berhasil mengubah data laptop", null))
    }

    suspend fun deleteLaptop(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID laptop tidak boleh kosong!")
        val oldLaptop = laptopRepository.getLaptopById(id)
            ?: throw AppException(404, "Data laptop tidak tersedia!")

        val isDeleted = laptopRepository.removeLaptop(id)
        if (!isDeleted) throw AppException(400, "Gagal menghapus data laptop!")

        File(oldLaptop.pathGambar).takeIf { it.exists() }?.delete()
        call.respond(DataResponse("success", "Berhasil menghapus data laptop", null))
    }

    suspend fun getLaptopImage(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)
        val laptop = laptopRepository.getLaptopById(id)
            ?: return call.respond(HttpStatusCode.NotFound)
        val file = File(laptop.pathGambar)
        if (!file.exists()) return call.respond(HttpStatusCode.NotFound)
        call.respondFile(file)
    }
}