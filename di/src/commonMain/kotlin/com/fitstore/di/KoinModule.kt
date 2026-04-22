package com.fitstore.di

import com.fitstore.admin_panel.AdminPanelViewModel
import com.fitstore.auth.AuthViewModel
import com.fitstore.data.AdminRepositoryImpl
import com.fitstore.data.CustomerRepositoryImpl
import com.fitstore.data.ImageRepositoryImpl
import com.fitstore.data.ProductRepositoryImpl
import com.fitstore.data.com.fitstore.data.domain.ProductRepository
import com.fitstore.data.domain.AdminRepository
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.ImageRepository
import com.fitstore.home.HomeGraphViewModel
import com.fitstore.login.LoginViewModel
import com.fitstore.manage_product.ManageProductViewModel
import com.fitstore.products_overview.ProductsOverviewViewModel
import com.fitstore.profile.ProfileViewModel
import com.fitstore.register.RegisterViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    single<SupabaseClient> { configureSupabaseClient() }
    single<ImageRepository> { ImageRepositoryImpl(get()) }
    single<AdminRepository> {
        AdminRepositoryImpl(
            imageRepository = get(),
            supabase = get()
        )
    }
    single<ProductRepository> {
        ProductRepositoryImpl(
            supabase = get()
        )
    }
    single<CustomerRepository> { CustomerRepositoryImpl(get()) }
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeGraphViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::AdminPanelViewModel)
    viewModelOf(::ManageProductViewModel)
    viewModelOf(::ProductsOverviewViewModel)
}
expect val targetModule: Module
fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule,  targetModule)
    }
}