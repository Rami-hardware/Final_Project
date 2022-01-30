package com.example.heart

import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Entry point for [HealthServicesClient] APIs, wrapping them in coroutine-friendly APIs.
 */
class HealthServicesManager @Inject constructor(
    healthServicesClient: HealthServicesClient
) {
    private val measureClient = healthServicesClient.measureClient
    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = measureClient.capabilities.await()
        return (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure)
    }
    /**
     * Returns a cold flow. When activated, the flow will register a callback for heart rate data
     * and start to emit messages. When the consuming coroutine is cancelled, the measure callback
     * is unregistered.
     *
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    fun heartRateMeasureFlow() = callbackFlow<MeasureMessage> {


        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DataType, availability: Availability) {
                // Only send back DataTypeAvailability (not LocationAvailability)
                if (availability is DataTypeAvailability) {

                    sendBlocking(MeasureMessage.MeasureAvailabilty(availability))
                }
            }

            override fun onData(data: List<DataPoint>) {
                sendBlocking(MeasureMessage.MeasureData(data))

            }
        }

        Log.d(TAG, "Registering for data")
        measureClient.registerCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            Log.d(TAG, "Unregistering for data")
            measureClient.unregisterCallback(DataType.HEART_RATE_BPM, callback)
        }
    }
}

sealed class MeasureMessage {
    class MeasureAvailabilty(val availability: DataTypeAvailability) : MeasureMessage()
    class MeasureData(val data: List<DataPoint>): MeasureMessage()
}
