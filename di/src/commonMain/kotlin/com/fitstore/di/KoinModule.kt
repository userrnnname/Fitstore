package com.fitstore.di

import com.fitstore.admin_panel.AdminPanelViewModel
import com.fitstore.auth.AuthViewModel
import com.fitstore.cart.CartViewModel
import com.fitstore.category_search.CategorySearchViewModel
import com.fitstore.checkout.CheckoutViewModel
import com.fitstore.data.AdminRepositoryImpl
import com.fitstore.data.CustomerRepositoryImpl
import com.fitstore.data.ImageRepositoryImpl
import com.fitstore.data.OrderRepositoryImpl
import com.fitstore.data.ProductRepositoryImpl
import com.fitstore.data.SupplementRepositoryImpl
import com.fitstore.data.domain.OrderRepository
import com.fitstore.data.domain.AdminRepository
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.ImageRepository
import com.fitstore.data.domain.ProductRepository
import com.fitstore.data.domain.SupplementRepository
import com.fitstore.details.DetailsViewModel
import com.fitstore.edit_profile.EditProfileViewModel
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
    single<SupplementRepository> {
        SupplementRepositoryImpl(
            supabase = get()
        )
    }
    single<OrderRepository> {
        OrderRepositoryImpl(
            supabase = get(),
            customerRepository = get()
        )
    }
    single<CustomerRepository> { CustomerRepositoryImpl(get()) }
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeGraphViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::EditProfileViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::AdminPanelViewModel)
    viewModelOf(::ManageProductViewModel)
    viewModelOf(::ProductsOverviewViewModel)
    viewModelOf(::DetailsViewModel)
    viewModelOf(::CartViewModel)
    viewModelOf(::CategorySearchViewModel)
    viewModelOf(::CheckoutViewModel)
    //viewModelOf(::PaymentCompletedViewModel)
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