package com.ghurabah.jemaah.models;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by musa on 10/6/17.
 */

public class Share {

    private String shareId;
    private ParseUser sharer;

    private String startLocationName;
    private String startLocationAddress;
    private LatLng startLocationPoint;
    private String endLocationName;
    private String endLocationAddress;
    private LatLng endLocationPoint;

    private Integer seatsRemaning;
    private Integer seatsJoined;
    private Integer seatsShared;

    private Date meetupTime;



    private String preference;
    private Double estimatedCost;

    private String remarks;

    public Share(ParseUser sharer, String shareId, String startLocationName, String startLocationAddress,
                 LatLng startLocationPoint, String endLocationName, String endLocationAddress,
                 LatLng endLocationPoint, Integer seatsRemaning, Integer seatsJoined, Integer seatsShared,
                 Date meetupTime, Double estimatedCost, String remarks, String preference) {
        this.sharer = sharer;
        this.shareId = shareId;
        this.startLocationName = startLocationName;
        this.startLocationAddress = startLocationAddress;
        this.startLocationPoint = startLocationPoint;
        this.endLocationName = endLocationName;
        this.endLocationAddress = endLocationAddress;
        this.endLocationPoint = endLocationPoint;
        this.seatsRemaning = seatsRemaning;
        this.seatsJoined = seatsJoined;
        this.seatsShared = seatsShared;
        this.meetupTime = meetupTime;
        this.estimatedCost = estimatedCost;
        this.remarks = remarks;
        this.preference = preference;
    }

//    public Share(String shareId, String startLocationName, String startLocationAddress,
//                 LatLng startLocationPoint, String endLocationName, String endLocationAddress,
//                 LatLng endLocationPoint, Integer seatsRemaning, Integer seatsJoined, Integer seatsShared,
//                 Date meetupTime, Double estimatedCost, String remarks) {
//        this.shareId = shareId;
//        this.startLocationName = startLocationName;
//        this.startLocationAddress = startLocationAddress;
//        this.startLocationPoint = startLocationPoint;
//        this.endLocationName = endLocationName;
//        this.endLocationAddress = endLocationAddress;
//        this.endLocationPoint = endLocationPoint;
//        this.seatsRemaning = seatsRemaning;
//        this.seatsJoined = seatsJoined;
//        this.seatsShared = seatsShared;
//        this.meetupTime = meetupTime;
//        this.estimatedCost = estimatedCost;
//        this.remarks = remarks;
//    }

    public String getPreference() {
        return preference;
    }
    public String getShareId() { return shareId; }

    public ParseUser getSharer() {
        return sharer;
    }

    public String getStartLocationName() {
        return startLocationName;
    }

    public String getStartLocationAddress() {
        return startLocationAddress;
    }

    public LatLng getStartLocationPoint() {
        return startLocationPoint;
    }

    public String getEndLocationName() {
        return endLocationName;
    }

    public String getEndLocationAddress() {
        return endLocationAddress;
    }

    public LatLng getEndLocationPoint() {
        return endLocationPoint;
    }

    public Integer getSeatsRemaining() {
        return seatsRemaning;
    }

    public Integer getSeatsJoined() {
        return seatsJoined;
    }

    public Integer getSeatsShared() {
        return seatsShared;
    }

    public Date getMeetupTime() {
        return meetupTime;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public String getRemarks() {
        return remarks;
    }

    public boolean isPast() {
        if (getMeetupTime().before(new Date())) {
            return true;
        }

        return false;
    }
}
