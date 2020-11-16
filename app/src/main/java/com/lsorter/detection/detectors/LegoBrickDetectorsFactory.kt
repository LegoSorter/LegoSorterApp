package com.lsorter.detection.detectors

import com.lsorter.detection.detectors.remote.RemoteLegoBrickDetector

class LegoBrickDetectorsFactory {
    companion object {
        /**
         * Whether to run an object recognition in this android app or to send it to an external server.
         */
        private fun detectLocally(): Boolean {
            // TODO: Read from user preferences
            return true
        }

        fun getLegoBrickDetector(): LegoBrickDetector {
            return if (detectLocally()) {
                OnPremiseLegoBrickDetector()
            } else {
                RemoteLegoBrickDetector()
            }
        }
    }
}