package com.lsorter.connection

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

class ConnectionManager {

    // TODO Read an address from properties
    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress("10.0.2.2", 50051)
            .usePlaintext()
            .build()

    fun getConnectionChannel(): ManagedChannel {
        return channel
    }
}