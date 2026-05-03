package com.fitstore.di


import com.fitstore.admin_panel.AdminPanelViewModel
import com.fitstore.auth.AuthViewModel
import com.fitstore.cart.CartViewModel
import com.fitstore.category_search.CategorySearchViewModel
import com.fitstore.checkout.CheckoutViewModel
import com.fitstore.checkout.PaymentLauncher
import com.fitstore.data.AdminRepositoryImpl
import com.fitstore.data.CartRepositoryImpl
import com.fitstore.data.CustomerRepositoryImpl
import com.fitstore.data.ImageRepositoryImpl
import com.fitstore.data.OrderRepositoryImpl
import com.fitstore.data.PaymentRepositoryImpl
import com.fitstore.data.ProductRepositoryImpl
import com.fitstore.data.SupplementRepositoryImpl
import com.fitstore.data.domain.CartRepository
import com.fitstore.data.domain.PaymentRepository
import com.fitstore.data.domain.AdminRepository
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.ImageRepository
import com.fitstore.data.domain.OrderRepository
import com.fitstore.data.domain.ProductRepository
import com.fitstore.data.domain.SupplementRepository
import com.fitstore.details.DetailsViewModel
import com.fitstore.edit_profile.EditProfileViewModel
import com.fitstore.home.HomeGraphViewModel
import com.fitstore.login.LoginViewModel
import com.fitstore.manage_product.ManageProductViewModel
import com.fitstore.payment_completed.PaymentCompletedViewModel
import com.fitstore.products_overview.ProductsOverviewViewModel
import com.fitstore.profile.ProfileViewModel
import com.fitstore.register.RegisterViewModel
import com.fitstore.shared.notifications.PlatformNotification
import com.fitstore.shared.notifications.createPlatformNotification
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val networkModule = module {
    single<HttpClientEngine> { platformHttpEngine() }
}

val sharedModule = module {
    single {
        val engine: HttpClientEngine = get()
        createAndConfigureSupabaseClient(
            supabaseUrl = "https://bvmamyusputqijqsokdt.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bWFteXVzcHV0cWlqcXNva2R0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY3NzM3MjEsImV4cCI6MjA5MjM0OTcyMX0.t65CvtX7sNyA_UI5Yo2ztmcMOLPCNhCgbLxc8wmtmaE",
            httpEngine = engine
        )
    }
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
            cartRepository = get()
        )
    }
    single<CartRepository> {
        CartRepositoryImpl(
            supabase = get()
        )
    }
    single<PaymentRepository> {
        PaymentRepositoryImpl(
            supabase = get()
        )
    }
    single<PlatformNotification> { createPlatformNotification() }
    single<CustomerRepository> { CustomerRepositoryImpl(
        supabase = get(),
        notifications = get()
    ) }
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
    viewModelOf(::PaymentCompletedViewModel)
    viewModel { (launcher: PaymentLauncher?) ->
        CheckoutViewModel(
            paymentRepository = get(),
            customerRepository = get(),
            cartRepository = get(),
            orderRepository = get(),
            supplementRepository = get(),
            paymentLauncher = launcher
        )
    }
}
expect val targetModule: Module
