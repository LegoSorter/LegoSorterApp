package com.lsorter.connection

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

class ConnectionManager {

    // TODO Read an address from properties
    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress("192.168.0.94", 50051)
            .usePlaintext()
            .build()

    fun getConnectionChannel(): ManagedChannel {
        return channel
    }
}