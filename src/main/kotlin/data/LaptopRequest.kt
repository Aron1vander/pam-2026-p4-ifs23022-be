package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Laptop

@Serializable
data class LaptopRequest(
    var namaProduk: String = "",
    var harga: Long = 0L,
    var pathGambar: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "namaProduk" to namaProduk,
        "harga" to harga,
        "pathGambar" to pathGambar,
    )

    fun toEntity(): Laptop = Laptop(
        namaProduk = namaProduk,
        harga = harga,
        pathGambar = pathGambar,
    )
}