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
                "server.sorter.ml:50051") ?: "server.sorter.ml:50051" //  ip:port
            return ManagedChannelBuilder.forAddress(
                addrPref.split(":")[0], addrPref.split(":")[1].toInt())
                .usePlaintext()
                .build()
        }

        private val channel: ManagedChannel = createManagedChannel()
    }
}
