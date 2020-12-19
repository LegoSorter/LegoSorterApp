package com.lsorter.connection

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

class ConnectionManager {

    fun getConnectionChannel(): ManagedChannel {
        return ManagedChannelBuilder.forAddress("10.0.2.2", 50051)
            .usePlaintext()
            .build()
    }
}