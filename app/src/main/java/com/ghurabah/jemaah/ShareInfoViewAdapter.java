package com.ghurabah.jemaah;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ghurabah.jemaah.models.Share;
import com.ghurabah.jemaah.utils.DateUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by musa on 10/6/17.
 */

public class ShareInfoViewAdapter implements GoogleMap.InfoWindowAdapter {
    private final LayoutInflater mInflater;

    View popup;

    public ShareInfoViewAdapter(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        popup = mInflater.inflate(R.layout.map_info_view, null);
        final TextView endingLocationTextView = (TextView) popup.findViewById(R.id.textViewEndingLocation);
        final TextView meetUpTimeTextView = (TextView) popup.findViewById(R.id.textViewMeetUpTime);
        final TextView seatsAvailableTextView = (TextView) popup.findViewById(R.id.textViewSeatsAvailable);
        final TextView tapToBookTextView = (TextView) popup.findViewById(R.id.textViewTapToBook);

        Share share = (Share) marker.getTag();

        String destination = share.getEndLocationName();
        if (destination == null || destination.isEmpty()) {
            destination = share.getEndLocationAddress();
        } else {
            destination = "To: " + destination;
        }
        endingLocationTextView.setText(destination);

        meetUpTimeTextView.setText("Later at " + DateUtils.toFriendlyTimeString(share.getMeetupTime()));
        String color = "#4CAF50";
        if (share.getSeatsRemaining() == 1) {
            color = "#F44336";
        }

//        if (AppPrefs.with(TerawhereApplication.ApplicationContext).getUserId().equals(offer.getOffererId())) {
//            tapToBookTextView.setVisibility(View.GONE);
//        }

        seatsAvailableTextView.setText(Html.fromHtml("<font color='" + color + "'>" + share.getSeatsRemaining() + "</font> seat" + (share.getSeatsRemaining() > 1 ? "s" : "") + " left"));

        return popup;
    }
}
