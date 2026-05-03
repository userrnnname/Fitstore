rootProject.name = "Fitstore"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}
include(":composeApp")
include(":data")
include(":di")
include(":feature:admin_panel")
include(":feature:admin_panel:manage_product")
include(":feature:auth")
include(":feature:details")
include(":feature:home")
include(":feature:home:cart")
include(":feature:home:cart:checkout")
include(":feature:home:categories")
include(":feature:home:categories:category_search")
include(":feature:home:products_overview")
include(":feature:login")
include(":feature:payment_completed")
include(":feature:profile")
include(":feature:profile:edit_profile")
include(":feature:register")
include(":navigation")
include(":shared")
