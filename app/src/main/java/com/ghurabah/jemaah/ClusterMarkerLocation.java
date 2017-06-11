package com.ghurabah.jemaah;

import com.ghurabah.jemaah.models.Share;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by musa on 10/6/17.
 */

public class ClusterMarkerLocation implements ClusterItem {

    private Share share;

    private LatLng position;

    public ClusterMarkerLocation(Share share, LatLng position) {
        this.position = position;
        this.share = share;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public Share getShare() {
        return share;
    }

}