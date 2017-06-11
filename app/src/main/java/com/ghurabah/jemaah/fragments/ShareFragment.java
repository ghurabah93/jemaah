package com.ghurabah.jemaah.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ghurabah.jemaah.R;
import com.ghurabah.jemaah.activities.CreateShareActivity;
import com.ghurabah.jemaah.adapters.SharesAdapter;
import com.ghurabah.jemaah.models.Share;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShareFragment extends Fragment {

    private SharesAdapter sharesAdapter;

    private SwipeRefreshLayout swipeRefreshLayoutOffers;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_share, container, false);
        swipeRefreshLayoutOffers = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_offers);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initFab();
        initRecyclerView();
        getSharesFromServer();
    }

    private void initFab() {
        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.fabAddRecord);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CreateShareActivity.class);
                startActivity(intent);

            }
        });
    }


    private void initRecyclerView() {
        swipeRefreshLayoutOffers.setColorSchemeResources(R.color.jemaah_primary_color);

        swipeRefreshLayoutOffers.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSharesFromServer();
            }
        });


    }

    private void getSharesFromServer() {
        swipeRefreshLayoutOffers.setRefreshing(true);

        final ParseUser sharerParseUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Share");
        query.whereEqualTo("sharer", sharerParseUser);
        query.orderByDescending("meetupTime");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> shareList, ParseException e) {
                swipeRefreshLayoutOffers.setRefreshing(false);

                if (e == null) {
                    List<Share> shares = new ArrayList<Share>();
                    if (shareList.size() > 0) {

                        for (int i = 0; i < shareList.size(); i++) {
                            ParseObject p = shareList.get(i);

                            String shareId = p.getObjectId();
                            String startName = p.getString("startLocationName");
                            String startAddress = p.getString("startLocationAddress");
                            double startLatitude = p.getNumber("startLatitude").doubleValue();
                            double startLongitude = p.getNumber("startLongitude").doubleValue();

                            String endName = p.getString("endLocationName");
                            String endAddress = p.getString("endLocationAddress");
                            double endLatitude = p.getNumber("endLatitude").doubleValue();
                            double endLongitude = p.getNumber("endLongitude").doubleValue();

                            int seatsRemaining = p.getNumber("seatsRemaining").intValue();
                            int seatsJoined = p.getNumber("seatsJoined").intValue();
                            int seatsShared = p.getNumber("seatsShared").intValue();

                            Date meetupTime = p.getDate("meetupTime");

                            double estimatedCost = p.getNumber("estimatedCost").doubleValue();

                            String remarks = p.getString("remarks");
                            String preference = p.getString("preference");
                            Log.i("Pref", "" + preference);
                            Share share = new Share(sharerParseUser, shareId, startName, startAddress, new LatLng(startLatitude, startLongitude),
                                    endName, endAddress, new LatLng(endLatitude, endLongitude), seatsRemaining, seatsJoined, seatsShared, meetupTime, estimatedCost, remarks, preference);
                            shares.add(share);

                        }
                    }

                    sharesAdapter = new SharesAdapter(getContext());

                    sharesAdapter.setShares(shares);
                    sharesAdapter.notifyDataSetChanged();

                    RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view_my_offers);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setHasFixedSize(true);

                    recyclerView.setAdapter(sharesAdapter);

                } else {
                    Log.d("Error", e.getMessage());
                }
            }
        });


    }

}
