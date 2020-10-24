package com.lsorter.detection.detectors

import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class RemoteLegoBrickDetector : LegoBrickDetector {

    override fun detectBricks(image: ImageProxy): Task<List<LegoBrickDetector.DetectedLegoBrick>> {
        return Tasks.call { emptyList<LegoBrickDetector.DetectedLegoBrick>() };
    }
}