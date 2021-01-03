package com.lsorter.connection

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import android.content.Context
import com.lsorter.App
import com.lsorter.R

class ConnectionManager {

    fun getConnectionChannel(): ManagedChannel {
        return channel
    }

    companion object {
        private fun createManagedChannel(): ManagedChannel{
            val addrPref = App.sharedPreferences().getString(
                App.applicationContext().getString(R.string.saved_server_address_key),
                "10.0.2.2:50051") ?: "10.0.2.2:50051"
            return ManagedChannelBuilder.forAddress(
                addrPref.split(":")[0], addrPref.split(":")[1].toInt())
                .usePlaintext()
                .build()
        }

        private val channel: ManagedChannel = createManagedChannel()
    }
}
