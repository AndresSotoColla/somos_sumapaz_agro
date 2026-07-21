package com.example.somos_sumapaz_agro.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onLocationFetched: (lat: Double, lon: Double, alt: Double?) -> Unit,
        onError: (String) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError("Servicio de ubicación no disponible.")
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            onError("GPS desactivado. Por favor encienda la ubicación.")
            return
        }

        val provider = when {
            isGpsEnabled -> LocationManager.GPS_PROVIDER
            else -> LocationManager.NETWORK_PROVIDER
        }

        try {
            // Intentar obtener última ubicación conocida para rapidez
            val lastKnown = locationManager.getLastKnownLocation(provider)
            if (lastKnown != null) {
                onLocationFetched(lastKnown.latitude, lastKnown.longitude, lastKnown.altitude)
            }

            // Registrar receptor para actualización en tiempo real (una sola vez)
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationFetched(location.latitude, location.longitude, location.altitude)
                    locationManager.removeUpdates(this)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationManager.requestLocationUpdates(provider, 0L, 0f, listener, context.mainLooper)
        } catch (e: SecurityException) {
            onError("Permisos de ubicación no concedidos: ${e.localizedMessage}")
        } catch (e: Exception) {
            onError("Error al obtener ubicación: ${e.localizedMessage}")
        }
    }
}
