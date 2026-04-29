package com.fitstore.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.SupplementRepository
import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class ProfileScreenState(
    val id: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val email: String = "",
    val supplements: List<SupplementTrack> = emptyList()
    //val lastPurchases: List<Product> = emptyList()
)

class ProfileViewModel(
    private val customerRepository: CustomerRepository,
    private val supplementRepository: SupplementRepository
) : ViewModel() {
    private val refreshSignal = MutableStateFlow(0)
    var screenReady: RequestState<Unit> by mutableStateOf(RequestState.Loading)
    var screenState by mutableStateOf(ProfileScreenState())
        private set

    init { readCustomerFlow() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun readCustomerFlow() {
        viewModelScope.launch {
            refreshSignal.flatMapLatest {
                combine(
                    customerRepository.readCustomerFlow(),
                    supplementRepository.readSupplementTracksFlow()
                ) { customerResult, tracksResult ->
                    if (customerResult.isSuccess()) {
                        val customer = customerResult.getSuccessData()
                        val tracks = if (tracksResult.isSuccess()) tracksResult.getSuccessData() else emptyList()

                        screenState = screenState.copy(
                            id = customer.id,
                            firstName = customer.firstName,
                            lastName = customer.lastName,
                            email = customer.email,
                            supplements = tracks
                        )

                        if (screenReady is RequestState.Loading) {
                            screenReady = RequestState.Success(Unit)
                        }
                    } else if (customerResult.isError() && screenReady is RequestState.Loading) {
                        screenReady = RequestState.Error(customerResult.getErrorMessage())
                    }
                }
            }.collect()
        }
    }

    fun takeServing(track: SupplementTrack, onError: (String) -> Unit) {
        if (track.remainingServings <= 0) return

        viewModelScope.launch {
            val oldSupplements = screenState.supplements
            val currentTime = Clock.System.now().toEpochMilliseconds().toString()
            val updatedList = screenState.supplements.map {
                if (it.id == track.id) it.copy(
                    remainingServings = it.remainingServings - 1,
                    lastTakenDate = currentTime
                ) else it
            }
            screenState = screenState.copy(supplements = updatedList)

            supplementRepository.takeServing(
                track = track,
                onSuccess = { refreshSignal.value++ },
                onError = {
                    screenState = screenState.copy(supplements = oldSupplements)
                    onError(it)
                }
            )
        }
    }


    fun updateSupplementTrack(
        track: SupplementTrack,
        newRemaining: Int,
        newTotal: Int,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val updatedTrack = track.copy(
                remainingServings = newRemaining,
                totalServings = newTotal
            )
            supplementRepository.updateSupplementTrack(
                track = updatedTrack,
                onSuccess = {
                    refreshSignal.value++
                    val updatedList = screenState.supplements.map { item ->
                        if (item.id == track.id) updatedTrack else item
                    }
                    screenState = screenState.copy(supplements = updatedList)
                },
                onError = onError
            )
        }
    }

    fun deleteSupplementTrack(id: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            supplementRepository.deleteSupplementTrack(
                trackId = id,
                onSuccess = { refreshSignal.value++ },
                onError = onError
            )
        }
    }
}