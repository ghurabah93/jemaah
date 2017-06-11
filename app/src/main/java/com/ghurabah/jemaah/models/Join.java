package com.ghurabah.jemaah.models;

import com.parse.ParseUser;

/**
 * Created by musa on 10/6/17.
 */

public class Join {


    private String joinId;
    private ParseUser joiner;
    private Integer seatsJoined;



    private Integer joinStatus;
    private Share share;

    public Join(String joinId, Integer joinStatus, ParseUser joiner, Integer seatsJoined, Share share) {
        this.joinId = joinId;
        this.joinStatus = joinStatus;
        this.joiner = joiner;
        this.seatsJoined = seatsJoined;
        this.share = share;
    }

    public ParseUser getJoiner() {
        return joiner;
    }

    public Integer getJoinStatus() {
        return joinStatus;
    }
    public Integer getSeatsJoined() {
        return seatsJoined;
    }

    public Share getShare() {
        return share;
    }

    public String getJoinId() {
        return joinId;
    }

}
