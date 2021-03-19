package com.lsorter.analyze.analyzers

import com.lsorter.connection.ConnectionManager
import com.lsorter.analyze.analyzers.remote.RemoteLegoBrickAnalyzer

class LegoBrickAnalyzersFactory {
    companion object {
        /**
         * Whether to run an object recognition in this android app or to send it to an external server.
         */
        private fun detectLocally(): Boolean {
            // TODO: Read from user preferences
            return false
        }

        fun getLegoBrickAnalyzer(): LegoBrickAnalyzer {
            return if (detectLocally()) {
                OnPremiseLegoBrickAnalyzer()
            } else {
                RemoteLegoBrickAnalyzer(ConnectionManager())
            }
        }
    }
}