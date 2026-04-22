package com.fitstore.home.domain

import com.fitstore.shared.Resources
import org.jetbrains.compose.resources.DrawableResource

enum class DrawerItem(
    val title: String,
    val icon: DrawableResource
) {
    Profile(
        title = "Профиль",
        icon = Resources.Icon.Person
    ),
    Blog(
        title = "Блог",
        icon = Resources.Icon.Book
    ),
    Locations(
        title = "Местоположение",
        icon = Resources.Icon.MapPin
    ),
    Contact(
        title = "Контакты",
        icon = Resources.Icon.Edit
    ),
    SignOut(
        title = "Выйти",
        icon = Resources.Icon.SignOut
    ),
    Admin(
        title = "Админ-панель",
        icon = Resources.Icon.Unlock
    )
}