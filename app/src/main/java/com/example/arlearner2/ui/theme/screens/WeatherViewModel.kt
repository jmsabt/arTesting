    package com.example.arlearner2.ui.theme.screens

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.arlearner2.network.RetrofitInstance
    import com.example.arlearner2.network.WeatherResponse
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch

    class WeatherViewModel : ViewModel() {

        private val _weatherState = MutableStateFlow<WeatherResponse?>(null)
        val weatherState: StateFlow<WeatherResponse?> = _weatherState

        private val _errorState = MutableStateFlow<String?>(null)
        val errorState: StateFlow<String?> = _errorState

        private val apiKey = "bd5e378503939ddaee76f12ad7a97608"  // your API key

        fun fetchWeather(city: String) {
            viewModelScope.launch {
                try {
                    val response = RetrofitInstance.api.getCurrentWeather("$city,PH", apiKey)
                    _weatherState.value = response
                    _errorState.value = null
                } catch (e: Exception) {
                    _errorState.value = e.localizedMessage ?: "Unknown Error"
                    _weatherState.value = null
                }
            }
        }
    }
