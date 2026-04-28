package com.fitstore.edit_profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.shared.domain.Customer
import com.fitstore.shared.domain.PhoneNumber
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class EditProfileState(
    val id: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val email: String = "",
    val city: String? = null,
    val postalCode: Int? = null,
    val address: String? = null,
    val phoneNumber: PhoneNumber? = null
)

class EditProfileViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {
    var screenReady: RequestState<Unit> by mutableStateOf(RequestState.Loading)
    var screenState by mutableStateOf(EditProfileState())
        private set

    val isFormValid: Boolean get() = with(screenState) {
        val requiredFieldsValid = lastName.length in 3..50 && firstName.length in 3..50

        val optionalFieldsValid =
            (city == null || city.length in 3..50) &&
                    (address == null || address.length in 3..100) &&
                    (postalCode == null || postalCode.toString().length in 4..10) &&
                    (phoneNumber?.number == null || phoneNumber.number.length == 10)

        requiredFieldsValid && optionalFieldsValid
    }

    init {
        viewModelScope.launch {
            customerRepository.readCustomerFlow().collectLatest { result ->
                if (result.isSuccess()) {
                    val c = result.getSuccessData()
                    screenState = EditProfileState(
                        c.id,
                        c.lastName,
                        c.firstName,
                        c.email, 
                        c.city, 
                        c.postalCode, 
                        c.address, 
                        c.phoneNumber)
                    screenReady = RequestState.Success(Unit)
                }
            }
        }
    }

    fun updateFirstName(v: String) { screenState = screenState.copy(firstName = v) }
    fun updateLastName(v: String) { screenState = screenState.copy(lastName = v) }
    fun updateCity(v: String) { screenState = screenState.copy(city = v) }
    fun updateAddress(v: String) { screenState = screenState.copy(address = v) }
    fun updatePostalCode(v: Int?) { screenState = screenState.copy(postalCode = v) }
    fun updatePhoneNumber(v: String) { screenState = screenState.copy(phoneNumber = PhoneNumber(7, v)) }

    fun updateCustomer(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            customerRepository.updateCustomer(
                customer = Customer(
                    screenState.id,
                    screenState.lastName,
                    screenState.firstName,
                    screenState.email,
                    screenState.city,
                    screenState.postalCode,
                    screenState.address,
                    screenState.phoneNumber
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }
}
