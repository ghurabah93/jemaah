package com.ghurabah.jemaah.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.transition.TransitionManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ghurabah.jemaah.R;
import com.ghurabah.jemaah.models.Join;
import com.ghurabah.jemaah.models.Share;
import com.ghurabah.jemaah.utils.DateUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by musa on 11/6/17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String LESS_DETAILS = "\u2014 LESS DETAILS";
    private static final String MORE_DETAILS = "+ MORE DETAILS";
    public static final String CANCEL_BOOKING_TITLE = "Confirm Cancel Join?";
    private static final String CANCEL_BOOKING_INFO = "The driver will be informed of your cancellation.";
    private static final String KEEP = "Keep";
    private static final String CONFIRM = "Confirm";
    public static final String TERAWHERE_PRIMARY_COLOR = "#54d8bd";

    private Context context;

    private List<Join> joins;

    private ViewGroup viewGroup;

    public HistoryAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history, parent, false);
        viewGroup = parent;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Join join = joins.get(position);

        String meetUpTime = DateUtils.toFriendlyTimeString(join.getShare().getMeetupTime());
        String day = DateUtils.dateToString(join.getShare().getMeetupTime(), DateUtils.DAY_OF_MONTH_FORMAT);
        String month = DateUtils.dateToString(join.getShare().getMeetupTime(), DateUtils.MONTH_ABBREVIATED_FORMAT);

        // Set the value of the text
        final Share share = join.getShare();
        viewHolder.textViewMonth.setText(month);
        viewHolder.textViewDay.setText(day);
        viewHolder.textViewMeetupTime.setText(meetUpTime);
        viewHolder.textViewEndLocationName.setText(share.getEndLocationName());
        viewHolder.textViewEndLocationAddress.setText(share.getEndLocationAddress());
        viewHolder.textViewStartLocationName.setText(share.getStartLocationName());
        viewHolder.textViewStartLocationAddress.setText(share.getStartLocationAddress());
        viewHolder.textViewSeatsBooked.setText(Integer.toString(join.getSeatsJoined()));
        viewHolder.textViewTag.setText(share.getPreference());
        if (share.getRemarks() != null && !share.getRemarks().isEmpty()) {
            viewHolder.textViewRemarks.setText(share.getRemarks());
        }
        Log.i("SHARER", "" + share.getSharer().toString());
        viewHolder.textViewDriver.setText(share.getSharer().getUsername());
        if (join.getJoinStatus() == 2) {
            // Past Join
            viewHolder.textViewJoinPast.setText("Past Join");
            viewHolder.textViewJoinPast.setVisibility(View.VISIBLE);
            viewHolder.textViewCancel.setVisibility(View.GONE);
        } else if (join.getJoinStatus() == 1) {
            // Cancelled
            viewHolder.textViewJoinPast.setText("Cancelled");
            viewHolder.textViewJoinPast.setVisibility(View.VISIBLE);
            viewHolder.textViewCancel.setVisibility(View.GONE);
        } else {
            // Neither
            viewHolder.textViewJoinPast.setVisibility(View.INVISIBLE);
            viewHolder.textViewCancel.setVisibility(View.VISIBLE);
        }

        // check card collapse/expand
        final boolean[] shouldExpand = isCollapse(viewHolder, join);

        // set listeners for collapse/expand share details
        setDetailsTextViewListener(viewHolder, shouldExpand);

        // set listeners for directions
        viewHolder.textViewEndLocationAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:" + share.getEndLocationPoint().latitude + "," + share.getEndLocationPoint().longitude + "?q=" + share.getEndLocationName());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            }
        });
        viewHolder.textViewStartLocationAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:" + share.getStartLocationPoint().latitude + "," + share.getStartLocationPoint().longitude + "?q=" + share.getStartLocationName());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            }
        });

        viewHolder.textViewCancel.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder deleteConfirmationDialog = new AlertDialog.Builder(viewGroup.getContext());

                deleteConfirmationDialog.setTitle(CANCEL_BOOKING_TITLE);
                deleteConfirmationDialog.setMessage(CANCEL_BOOKING_INFO);
                deleteConfirmationDialog.setNegativeButton(KEEP, null); // dismisses by default
                deleteConfirmationDialog.setPositiveButton(CONFIRM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Join join = joins.get(position);
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Join");
                        query.getInBackground(join.getJoinId(), new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                if(e == null && object != null) {
                                    object.put("joinStatus", 1);
                                    object.saveInBackground();
                                }
                            }
                        });

                    }
                });

                AlertDialog alert = deleteConfirmationDialog.create();
                alert.show();

                createAlertDialog(alert);
            }
        });

    }


    private boolean[] isCollapse(ViewHolder viewHolder, Join join) {
        return new boolean[]{
                viewHolder.textViewEndLocationAddress.getVisibility() == View.GONE,
                viewHolder.textViewStartLocationAddress.getVisibility() == View.GONE,
                join.getShare().getRemarks() != null && !join.getShare().getRemarks().isEmpty() && viewHolder.textViewRemarks.getVisibility() == View.GONE,
                viewHolder.textViewDriverLabel.getVisibility() == View.GONE,
                viewHolder.textViewDriver.getVisibility() == View.GONE,
        };
    }

    private void setDetailsTextViewListener(final ViewHolder viewHolder, final boolean[] shouldExpand) {
        viewHolder.textViewViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpand(viewHolder, shouldExpand);
            }
        });
    }

    private void toggleExpand(final ViewHolder viewHolder, final boolean[] shouldExpand) {
        if (shouldExpand[0]) {
            viewHolder.textViewEndLocationAddress.setVisibility(View.VISIBLE);
            viewHolder.textViewStartLocationAddress.setVisibility(View.VISIBLE);
            if (viewHolder.textViewRemarks.getText() != null && !viewHolder.textViewRemarks.getText().toString().isEmpty()) {
                viewHolder.textViewRemarksLabel.setVisibility(View.VISIBLE);
                viewHolder.textViewRemarks.setVisibility(View.VISIBLE);
            }
            viewHolder.textViewDriverLabel.setVisibility(View.VISIBLE);
            viewHolder.textViewDriver.setVisibility(View.VISIBLE);
            viewHolder.textViewViewMore.setText(LESS_DETAILS);
            shouldExpand[0] = false;
        } else {
            viewHolder.textViewEndLocationAddress.setVisibility(View.GONE);
            viewHolder.textViewStartLocationAddress.setVisibility(View.GONE);
            if (viewHolder.textViewRemarks.getText() != null && !viewHolder.textViewRemarks.getText().toString().isEmpty()) {
                viewHolder.textViewRemarksLabel.setVisibility(View.GONE);
                viewHolder.textViewRemarks.setVisibility(View.GONE);
            }
            viewHolder.textViewDriverLabel.setVisibility(View.GONE);
            viewHolder.textViewDriver.setVisibility(View.GONE);
            viewHolder.textViewViewMore.setText(MORE_DETAILS);
            shouldExpand[0] = true;
        }

        TransitionManager.beginDelayedTransition(viewGroup);
        viewHolder.itemView.setActivated(shouldExpand[0]);
    }

    private void createAlertDialog(AlertDialog alert) {
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.BLACK);
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.parseColor(TERAWHERE_PRIMARY_COLOR));
    }

    @Override
    public int getItemCount() {
        return joins == null ? 0 : joins.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDay;
        private TextView textViewMonth;
        private TextView textViewEndLocationName;
        private TextView textViewEndLocationAddress;
        private TextView textViewStartLocationName;
        private TextView textViewStartLocationAddress;
        private TextView textViewMeetupTime;
        private TextView textViewRemarksLabel;
        private TextView textViewRemarks;
        private TextView textViewSeatsBookedLabel;
        private TextView textViewSeatsBooked;

        private TextView textViewDriverLabel;
        private TextView textViewDriver;

        private TextView textViewViewMore;
        private TextView textViewCancel;

        private TextView textViewJoinPast;
        private TextView textViewTag;

        private RelativeLayout relativeLayoutItem;

        private ViewHolder(View view) {
            super(view);

            relativeLayoutItem = (RelativeLayout) view.findViewById(R.id.relative_layout_item);
            textViewDay = (TextView) view.findViewById(R.id.text_view_day);
            textViewMonth = (TextView) view.findViewById(R.id.text_view_month);
            textViewEndLocationName = (TextView) view.findViewById(R.id.text_view_end_location_name);
            textViewEndLocationAddress = (TextView) view.findViewById(R.id.text_view_end_location_address);
            textViewStartLocationName = (TextView) view.findViewById(R.id.text_view_start_location_name);
            textViewStartLocationAddress = (TextView) view.findViewById(R.id.text_view_start_location_address);
            textViewMeetupTime = (TextView) view.findViewById(R.id.text_view_meetup_time);
            textViewSeatsBookedLabel = (TextView) view.findViewById(R.id.text_view_seats_booked_label);
            textViewSeatsBooked = (TextView) view.findViewById(R.id.text_view_seats_booked);
            textViewRemarksLabel = (TextView) view.findViewById(R.id.text_view_remarks_label);
            textViewRemarks = (TextView) view.findViewById(R.id.text_view_remarks);
            textViewViewMore = (TextView) view.findViewById(R.id.text_view_view_more);
            textViewCancel = (TextView) view.findViewById(R.id.text_view_cancel);
            textViewDriverLabel = (TextView) view.findViewById(R.id.text_view_driver_label);
            textViewDriver = (TextView) view.findViewById(R.id.text_view_driver);
            textViewJoinPast = (TextView) view.findViewById(R.id.text_view_join_past);
            textViewTag = (TextView) view.findViewById(R.id.text_view_tag);
        }
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }
}
