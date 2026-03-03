package org.delcom.module

import org.delcom.repositories.ILaptopRepository
import org.delcom.repositories.IPlantRepository
import org.delcom.repositories.LaptopRepository
import org.delcom.repositories.PlantRepository
import org.delcom.services.LaptopService
import org.delcom.services.PlantService
import org.delcom.services.ProfileService
import org.koin.dsl.module

val appModule = module {
    // Plant
    single<IPlantRepository> { PlantRepository() }
    single { PlantService(get()) }

    // Laptop
    single<ILaptopRepository> { LaptopRepository() }
    single { LaptopService(get()) }

    // Profile
    single { ProfileService() }
}