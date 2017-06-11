package com.ghurabah.jemaah.fragments;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ghurabah.jemaah.AndroidSdkCheckerUtils;
import com.ghurabah.jemaah.ClusterMarkerLocation;
import com.ghurabah.jemaah.ClusterRenderer;
import com.ghurabah.jemaah.R;
import com.ghurabah.jemaah.ShareInfoViewAdapter;
import com.ghurabah.jemaah.models.Share;
import com.ghurabah.jemaah.utils.DateUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class JoinFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;

    private Location location;

    private boolean firstLoadInit;

    private ViewPager viewPager;

    private ClusterManager<ClusterMarkerLocation> clusterManager;

    private GoogleMap googleMap;

    private boolean isVisibleToUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_join, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        trackCurrentLocation();

        buildGoogleApiClient();

        initializeSupportMapFragment();
    }


    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    private void trackCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        this.location = ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    private void initializeSupportMapFragment() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.map_container, supportMapFragment).commit();
        }
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (AndroidSdkCheckerUtils.isMarshmallow()) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        initClusterManager();
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(60);
        locationRequest.setFastestInterval(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;

        // Alternative entry point for data loading
        if (!firstLoadInit) {
            loadMarkers();
            firstLoadInit = true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_join_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            Toast.makeText(getContext(), "Refreshing...", Toast.LENGTH_SHORT).show();
            if (firstLoadInit) {
                loadMarkers();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void initClusterManager() {
        clusterManager = new ClusterManager<>(getContext(), googleMap);
        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarkerLocation>() {
            @Override
            public void onClusterItemInfoWindowClick(ClusterMarkerLocation clusterMarkerLocation) {
                Share share = clusterMarkerLocation.getShare();
                showBookingDialog(share);
            }
        });
        clusterManager.setRenderer(new ClusterRenderer(getContext(), googleMap, clusterManager));
        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterMarkerLocation>() {
            @Override
            public boolean onClusterClick(Cluster<ClusterMarkerLocation> cluster) {
                ArrayList<String> items = new ArrayList<>();
                final ArrayList<Share> shares = new ArrayList<>();
                for (ClusterMarkerLocation clusterMarkerLocation : cluster.getItems()) {
                    Share share = clusterMarkerLocation.getShare();
                    String time = DateUtils.toFriendlyTimeString(share.getMeetupTime());
                    items.add(time + " to " + share.getEndLocationName());
                    shares.add(share);
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(shares.size() + " Destinations")
                        .setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Share share = shares.get(which);
                                showBookingDialog(share);
                            }
                        })
                        .show();
                return true;
            }
        });
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnInfoWindowClickListener(clusterManager);
        googleMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        googleMap.setOnMarkerClickListener(clusterManager);
    }

    private void loadMarkers() {

        final ParseUser sharerParseUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Share");
        query.whereEqualTo("sharer", sharerParseUser);
        query.orderByDescending("meetupTime");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> shareList, ParseException e) {

                if (e == null) {
                    googleMap.clear();
                    clusterManager.clearItems();


                    final List<Share> shares = new ArrayList<Share>();
                    if (shareList.size() > 0) {

                        for (int i = 0; i < shareList.size(); i++) {
                            ParseObject p = shareList.get(i);

                            final String shareId = p.getObjectId();
                            final ParseRelation<ParseUser> relation = p.getRelation("sharer");
                            final String startName = p.getString("startLocationName");
                            final String startAddress = p.getString("startLocationAddress");
                            final double startLatitude = p.getNumber("startLatitude").doubleValue();
                            final double startLongitude = p.getNumber("startLongitude").doubleValue();

                            final String endName = p.getString("endLocationName");
                            final String endAddress = p.getString("endLocationAddress");
                            final double endLatitude = p.getNumber("endLatitude").doubleValue();
                            final double endLongitude = p.getNumber("endLongitude").doubleValue();

                            final int seatsRemaining = p.getNumber("seatsRemaining").intValue();
                            final int seatsJoined = p.getNumber("seatsJoined").intValue();
                            final int seatsShared = p.getNumber("seatsShared").intValue();

                            final Date meetupTime = p.getDate("meetupTime");

                            final double estimatedCost = p.getNumber("estimatedCost").doubleValue();

                            final String remarks = p.getString("remarks");
                            final String preference = p.getString("preference");

                            final ParseUser[] sharer = {null};
                            relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
                                @Override
                                public void done(List<ParseUser> objects, ParseException e) {

                                    sharer[0] = objects.get(0);
                                    Share share = new Share(sharer[0], shareId, startName, startAddress, new LatLng(startLatitude, startLongitude),
                                            endName, endAddress, new LatLng(endLatitude, endLongitude), seatsRemaining, seatsJoined, seatsShared, meetupTime, estimatedCost, remarks, preference);
                                    shares.add(share);


                                    LatLng startLatLng = share.getStartLocationPoint();

                                    if (share.getSeatsRemaining() > 0) {
                                        clusterManager.addItem(new ClusterMarkerLocation(share, startLatLng));
                                    }

                                    clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new ShareInfoViewAdapter(LayoutInflater.from(getContext())));

                                    // Zoom in after markers loaded
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));

                                    // Update cluster (needed for refresh)
                                    clusterManager.cluster();

                                    if (isVisibleToUser) {
                                        if (shares.size() > 0) {
                                            Toast.makeText(getActivity(), shares.size() + " rides available.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "No rides available.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });


                        }
                    }


                } else {
                    Log.d("Error", e.getMessage());
                }
            }
        });
//        Call<GetOffersResponse> callGetOffers = TerawhereBackendServer.getApiInstance().getNearbyOffers(new LocationRequestBody(location.getLatitude(), location.getLongitude()));
//        callGetOffers.enqueue(new Callback<GetOffersResponse>() {
//            @Override
//            public void onResponse(Call<GetOffersResponse> call, Response<GetOffersResponse> response) {
//                if (response.isSuccessful()) {
//                    googleMap.clear();
//                    clusterManager.clearItems();
//
//                    GetOffersResponse getOffersResponse = response.body();
//                    List<Offer> offers = OfferFactory.createFromResponse(getOffersResponse);
//
//                    for (int i = 0; i < offers.size(); i++) {
//                        Offer offer = offers.get(i);
//                        LatLng startLatLng = new LatLng(offer.getStartTerawhereLocation().getLatitude(), offer.getStartTerawhereLocation().getLongitude());
//
//                        if (offers.get(i).getSeatsRemaining() > 0) {
//                            clusterManager.addItem(new ClusterMarkerLocation(offer, startLatLng));
//                        } else {
//                            offers.remove(i);
//                            i--;
//                        }
//                    }
//                    clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new OfferInfoViewAdapter(LayoutInflater.from(getContext())));
//
//                    // Zoom in after markers loaded
//                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
//
//                    // Update cluster (needed for refresh)
//                    clusterManager.cluster();
//
//                    if (isVisibleToUser) {
//                        if (offers.size() > 0) {
//                            Toast.makeText(getActivity(), offers.size() + " rides available.", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(getActivity(), "No rides available.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                } else {
//                    onFailure(call, new NetworkCallFailedException("Response not successful."));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<GetOffersResponse> call, Throwable t) {
//                Log.e(TAG, "failed to fetch offers via network call", t);
//            }
//        });
    }


    // TODO: Can clean code up further
    private void showBookingDialog(final Share share) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_booking, null);
        builder.setView(dialogView);

        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner);

        TextView textViewDay = (TextView) dialogView.findViewById(R.id.text_view_day);
        TextView textViewMonth = (TextView) dialogView.findViewById(R.id.text_view_month);
        TextView textViewEndLocationName = (TextView) dialogView.findViewById(R.id.text_view_end_location_name);
        TextView textViewEndLocationAddress = (TextView) dialogView.findViewById(R.id.text_view_end_location_address);
        TextView textViewStartLocationName = (TextView) dialogView.findViewById(R.id.text_view_start_location_name);
        TextView textViewStartLocationAddress = (TextView) dialogView.findViewById(R.id.text_view_start_location_address);
        TextView textViewMeetupTime = (TextView) dialogView.findViewById(R.id.text_view_meetup_time);
        TextView textViewSeatsLeft = (TextView) dialogView.findViewById(R.id.text_view_seats_left);
        TextView textViewRemarksLabel = (TextView) dialogView.findViewById(R.id.text_view_remarks_label);
        TextView textViewRemarks = (TextView) dialogView.findViewById(R.id.text_view_remarks);
        TextView textViewDriver = (TextView) dialogView.findViewById(R.id.text_view_driver);

        String meetUpTime = DateUtils.toFriendlyTimeString(share.getMeetupTime());
        String day = DateUtils.dateToString(share.getMeetupTime(), DateUtils.DAY_OF_MONTH_FORMAT);
        String month = DateUtils.dateToString(share.getMeetupTime(), DateUtils.MONTH_ABBREVIATED_FORMAT);

        textViewMonth.setText(month);
        textViewDay.setText(day);
        textViewMeetupTime.setText(meetUpTime);
        textViewEndLocationName.setText(share.getEndLocationName());
        textViewEndLocationAddress.setText(share.getEndLocationAddress());
        textViewStartLocationName.setText(share.getStartLocationName());
        textViewStartLocationAddress.setText(share.getStartLocationAddress());
        textViewSeatsLeft.setText(Integer.toString(share.getSeatsRemaining()));
        if (share.getRemarks() != null && !share.getRemarks().isEmpty()) {
            textViewRemarks.setText(share.getRemarks());
        } else {
            textViewRemarksLabel.setVisibility(View.GONE);
            textViewRemarks.setVisibility(View.GONE);
        }

        Log.i("SHARER", share.getSharer().toString());
        textViewDriver.setText(share.getSharer().getUsername());

        List<String> categories = new ArrayList<String>();
        int seatsAvailable = share.getSeatsRemaining() < 2 ? share.getSeatsRemaining() : 2;
        for (int i = 1; i <= seatsAvailable; i++) {
            categories.add(Integer.toString(i));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(dialogView.getContext(), android.R.layout.simple_spinner_item, categories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return setCentered(super.getView(position, convertView, parent));
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return setCentered(super.getDropDownView(position, convertView, parent));
            }

            private View setCentered(View view) {
                view.setPadding(10, 20, 10, 10);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
                return view;
            }
        };

        spinner.setAdapter(dataAdapter);

//        if (AppPrefs.with(TerawhereApplication.ApplicationContext).getUserId().equals(share.getOffererId())) {
//            ((View) spinner.getParent()).setVisibility(View.GONE);
//            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//        } else {
        builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showConfirmBookDialog(share, spinner.getSelectedItem().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
//        }

        AlertDialog alert = builder.create();
        alert.show();
        decorateAlertDialog(alert);
    }

    private void showConfirmBookDialog(final Share share, final String numSeats) {

        if (numSeats.matches("")) {
            Toast.makeText(getContext(), "Please enter number of seats", Toast.LENGTH_SHORT).show();
        } else {
            String shareId = share.getShareId();

            final ParseUser sharerParseUser = ParseUser.getCurrentUser();

            final ParseObject joinParseObject = new ParseObject("Join");
            sharerParseUser.saveInBackground(new SaveCallback() {
                                                 public void done(ParseException e) {
                                                     joinParseObject.getRelation("joiner").add(sharerParseUser);
                                                     joinParseObject.saveInBackground();
                                                 }
                                             }
            );


            joinParseObject.put("seatsJoined", Integer.parseInt(numSeats));
            joinParseObject.put("joinStatus", 0);


            try {
                joinParseObject.save();

                ParseObject shareParseObject = new ParseObject("Share");

                joinParseObject.getRelation("share").add(ParseObject.createWithoutData("Share", share.getShareId()));

                shareParseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i("Parse", "Save Succeeded");
                        } else {
                            Log.i("Parse", e.toString());
                        }
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }




            final Dialog successDialog = new Dialog(getActivity());
            successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            successDialog.setContentView(R.layout.dialog_join_successful);
            successDialog.getWindow().

                    setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            successDialog.setCanceledOnTouchOutside(false);
            successDialog.setCancelable(false);

            Button okButton = (Button) successDialog.findViewById(R.id.button_ok);
            TextView dialogInfo = (TextView) successDialog.findViewById(R.id.text_view_info);
            TextView dialogNotice = (TextView) successDialog.findViewById(R.id.text_view_notice);

            dialogInfo.setText(Html.fromHtml("Driver: <b>" + share.getSharer().getUsername() + "</b>"
                    + "<br/>Remarks: <b>" + share.getRemarks()

                    + "</b>"));
            dialogNotice.setText(Html.fromHtml("<b>Please be punctual!</b>"));
            okButton.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick(View v) {
                    successDialog.dismiss();
                    //viewPager.setCurrentItem(2);
                }
            });
            successDialog.show();
        }


    }

    private void decorateAlertDialog(AlertDialog alertDialog) {
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.BLACK);
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.parseColor("#54d8bd"));
    }

//    @Override
//    public void onDestroyView() {
//        googleMap.clear();
//        clusterManager.clearItems();
//
//        Fragment f = (Fragment) getFragmentManager().findFragmentById(R.id.map_container);
//        if (f != null) {
//            getFragmentManager().beginTransaction().remove(f).commit();
//        }
//
//        super.onDestroyView();
//
//
//    }

    @Override
    public void onDestroy() {
        googleMap.clear();
        clusterManager.clearItems();
        SupportMapFragment f = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (f.isResumed()) {
            getFragmentManager().beginTransaction().remove(f).commit();
        }

        super.onDestroy();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
    }
}
