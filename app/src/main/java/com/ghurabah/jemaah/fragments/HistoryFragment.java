package com.ghurabah.jemaah.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ghurabah.jemaah.R;
import com.ghurabah.jemaah.adapters.HistoryAdapter;
import com.ghurabah.jemaah.models.Join;
import com.ghurabah.jemaah.models.Share;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {


    private HistoryAdapter historyAdapter;

    private SwipeRefreshLayout swipeRefreshLayoutOffers;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        swipeRefreshLayoutOffers = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_offers);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initRecyclerView();
        getJoinsFromServer();
    }

    


    private void initRecyclerView() {
        swipeRefreshLayoutOffers.setColorSchemeResources(R.color.jemaah_primary_color);

        swipeRefreshLayoutOffers.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getJoinsFromServer();
            }
        });


    }

    private void getJoinsFromServer() {
        swipeRefreshLayoutOffers.setRefreshing(true);

        final ParseUser joinerParseUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Join");
        query.whereEqualTo("joiner", joinerParseUser);
        query.orderByDescending("meetupTime");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> joinList, ParseException e) {
                swipeRefreshLayoutOffers.setRefreshing(false);

                if (e == null) {
                    final List<Join> joins = new ArrayList<Join>();
                    if (joinList.size() > 0) {

                        for (int i = 0; i < joinList.size(); i++) {
                            ParseObject p = joinList.get(i);
                            final int index = i;

                            final String joinId = p.getObjectId();
                            final Integer seatsJoined = p.getNumber("seatsJoined").intValue();
                            final Integer joinStatus = p.getNumber("joinStatus").intValue();
                            ParseRelation<ParseObject> relation = p.getRelation("share");
                            final Share[] share = new Share[1];
                            relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
                                                                     @Override
                                                                     public void done(List<ParseObject> objects, ParseException e) {

                                                                         final ParseObject shareParseObject = objects.get(0);
                                                                         final ParseRelation<ParseUser> relation = shareParseObject.getRelation("sharer");

                                                                         final ParseUser[] sharer = {null};
                                                                         relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
                                                                                                                  @Override
                                                                                                                  public void done(List<ParseUser> objects, ParseException e) {

                                                                                                                      sharer[0] = objects.get(0);
                                                                                                                      share[0] = new Share(sharer[0],
                                                                                                                              shareParseObject.getObjectId(), shareParseObject.getString("startLocationName"),
                                                                                                                              shareParseObject.getString("startLocationAddress"),
                                                                                                                              new LatLng(shareParseObject.getNumber("startLatitude").doubleValue(), shareParseObject.getNumber("startLongitude").doubleValue()),
                                                                                                                              shareParseObject.getString("endLocationName"), shareParseObject.getString("endLocationAddress"),
                                                                                                                              new LatLng(shareParseObject.getNumber("endLatitude").doubleValue(), shareParseObject.getNumber("endLongitude").doubleValue()),
                                                                                                                              shareParseObject.getNumber("seatsRemaining").intValue(),
                                                                                                                              shareParseObject.getNumber("seatsJoined").intValue(),
                                                                                                                              shareParseObject.getNumber("seatsShared").intValue(),
                                                                                                                              shareParseObject.getDate("meetupTime"),
                                                                                                                              shareParseObject.getNumber("estimatedCost").doubleValue(),
                                                                                                                              shareParseObject.getString("remarks"),
                                                                                                                              shareParseObject.getString("preference"));

                                                                                                                      Join join = new Join(joinId, joinStatus, joinerParseUser, seatsJoined, share[0]);
                                                                                                                      joins.add(join);
                                                                                                                      if (index == joinList.size() - 1) {
                                                                                                                          historyAdapter = new HistoryAdapter(getContext());

                                                                                                                          historyAdapter.setJoins(joins);
                                                                                                                          historyAdapter.notifyDataSetChanged();

                                                                                                                          RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view_my_joins);
                                                                                                                          RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                                                                                                          recyclerView.setLayoutManager(layoutManager);
                                                                                                                          recyclerView.setHasFixedSize(true);

                                                                                                                          recyclerView.setAdapter(historyAdapter);
                                                                                                                      }
                                                                                                                  }
                                                                                                              });




                                                                         }
                                                                 });


                        }


                    }



                } else {
                    Log.d("Error", e.getMessage());
                }
            }
        });


    }


  

}
