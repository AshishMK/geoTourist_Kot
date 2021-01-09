package com.x.geotourist.scenes.mainScene

import com.x.geotourist.data.local.entity.TourDataEntity

interface TourListListener {
    public fun onTourClicked(entity: TourDataEntity)
}