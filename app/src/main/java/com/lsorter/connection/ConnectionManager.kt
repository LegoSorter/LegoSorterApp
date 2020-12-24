package com.lsorter.connection

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

class ConnectionManager {

    fun getConnectionChannel(): ManagedChannel {
        return channel
    }

    companion object {
        private val channel: ManagedChannel =
            ManagedChannelBuilder.forAddress("192.168.0.94", 50051)
                .usePlaintext()
                .build()
    }
}