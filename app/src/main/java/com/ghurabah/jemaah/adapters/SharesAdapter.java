package com.ghurabah.jemaah.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.transition.TransitionManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ghurabah.jemaah.R;
import com.ghurabah.jemaah.models.Share;
import com.ghurabah.jemaah.utils.DateUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by musa on 10/6/17.
 */

public class SharesAdapter extends RecyclerView.Adapter<SharesAdapter.ViewHolder> {
    private static final String LESS_DETAILS = "\u2014 LESS DETAILS";
    private static final String MORE_DETAILS = "+ MORE DETAILS";
    private static final String DELETE_SHARE_TITLE = "Confirm Cancel Share?";
    private static final String DELETE_SHARE_INFO = "The passengers will be informed of your cancellation.";
    private static final String CANCEL = "Keep";
    private static final String DELETE = "Confirm";
    private static final String CONFIRM = "Confirm";
    private static final String TERAWHERE_PRIMARY_COLOR = "#54d8bd";

    private Context context;

    private List<Share> shares;

    private ViewGroup viewGroup;

    public SharesAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_share, parent, false);
        viewGroup = parent;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Share share = shares.get(position);

        String meetUpTime = DateUtils.toFriendlyTimeString(share.getMeetupTime());

        String day = DateUtils.dateToString(share.getMeetupTime(), DateUtils.DAY_OF_MONTH_FORMAT);
        String month = DateUtils.dateToString(share.getMeetupTime(), DateUtils.MONTH_ABBREVIATED_FORMAT);

        viewHolder.textViewMonth.setText(month);
        viewHolder.textViewDay.setText(day);
        viewHolder.textViewMeetupTime.setText(meetUpTime);
        viewHolder.textViewEndLocationName.setText(share.getEndLocationName());
        viewHolder.textViewEndLocationAddress.setText(share.getEndLocationAddress());
        viewHolder.textViewStartLocationName.setText(share.getStartLocationName());
        viewHolder.textViewStartLocationAddress.setText(share.getStartLocationAddress());
        viewHolder.textViewSeatsLeft.setText(share.getSeatsRemaining() + " of " + share.getSeatsShared());
        viewHolder.textViewTag.setText(share.getPreference());
        if (share.getRemarks() != null && !share.getRemarks().isEmpty()) {
            viewHolder.textViewRemarks.setText(share.getRemarks());
            viewHolder.textViewRemarksLabel.setVisibility(View.VISIBLE);
            viewHolder.textViewRemarks.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textViewRemarksLabel.setVisibility(View.GONE);
            viewHolder.textViewRemarks.setVisibility(View.GONE);
        }

        // Hide action buttons if share has passed
        if (share.isPast()) {
            viewHolder.textViewCancel.setVisibility(View.GONE);
            viewHolder.textViewSharePast.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textViewCancel.setVisibility(View.VISIBLE);
            viewHolder.textViewSharePast.setVisibility(View.INVISIBLE);
        }

        final boolean[] shouldExpand = isCollapse(viewHolder, share);

        setShareItemRelativeLayoutListener(viewHolder, shouldExpand);
        setDetailsTextViewListener(viewHolder, shouldExpand);
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

        // set listeners for edit/delete share
//        setEditShareButtonListener(viewHolder, share);
        setDeleteShareButtonListener(viewHolder, position);
    }

    private boolean[] isCollapse(ViewHolder viewHolder, Share share) {
        return new boolean[]{
                viewHolder.textViewEndLocationAddress.getVisibility() == View.GONE,
                viewHolder.textViewStartLocationAddress.getVisibility() == View.GONE,
                share.getRemarks() != null && !share.getRemarks().isEmpty() && viewHolder.textViewRemarks.getVisibility() == View.GONE

        };
    }

    private void setShareItemRelativeLayoutListener(final ViewHolder viewHolder, final boolean[] shouldExpand) {
//        viewHolder.relativeLayoutItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Context context = viewGroup.getContext();
//                Intent intent = new Intent(context, BookingInfoActivity.class);
//                intent.putExtra(BookingInfoActivity.INTENT_SHARE_ID, shares.get(viewHolder.getAdapterPosition()).getShareId());
//                context.startActivity(intent);
//            }
//        });
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
            viewHolder.textViewViewMore.setText(LESS_DETAILS);
            shouldExpand[0] = false;
        } else {
            viewHolder.textViewEndLocationAddress.setVisibility(View.GONE);
            viewHolder.textViewStartLocationAddress.setVisibility(View.GONE);
            viewHolder.textViewViewMore.setText(MORE_DETAILS);
            shouldExpand[0] = true;
        }

        TransitionManager.beginDelayedTransition(viewGroup);
        viewHolder.itemView.setActivated(shouldExpand[0]);
    }

//    private void setEditShareButtonListener(ViewHolder viewHolder, final Share share) {
//        viewHolder.textViewEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Context context = viewGroup.getContext();
//                Intent intent = CreateShareActivity.getIntentToStartInEditMode(context, share);
//                context.startActivity(intent);
//            }
//        });
//    }

    private void setDeleteShareButtonListener(final ViewHolder viewHolder, final int position) {
        viewHolder.textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder adbDeleteShare = new AlertDialog.Builder(viewGroup.getContext());
                createAdbDeleteShare(viewHolder, adbDeleteShare, position);
            }
        });
    }

    private void createAdbDeleteShare(ViewHolder viewHolder, AlertDialog.Builder adbDeleteShare, final int position) {
        setAdbDeleteShareTitle(adbDeleteShare);
        setAdbDeleteShareMessage(adbDeleteShare);
        setAdbDeleteShareCancelButton(adbDeleteShare);
        setAdbDeleteShareConfirmButton(viewHolder, adbDeleteShare, position);
        setAdbDeleteShareStyle(adbDeleteShare);
    }

    private void setAdbDeleteShareConfirmButton(final ViewHolder viewHolder, AlertDialog.Builder adbDeleteShare, final int position) {
        adbDeleteShare.setPositiveButton(DELETE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Share share = shares.get(position);

                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Share");

                query.whereEqualTo("objectId", share.getShareId());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject share, ParseException e) {
                        try {
                            share.delete();
                            share.saveInBackground();
                            shares.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, shares.size());

                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    private void setAdbDeleteShareCancelButton(AlertDialog.Builder adbDeleteShare) {
        adbDeleteShare.setNegativeButton(CANCEL, null);
    }

    private void setAdbDeleteShareMessage(AlertDialog.Builder adbDeleteShare) {
        adbDeleteShare.setMessage(DELETE_SHARE_INFO);
    }

    private void setAdbDeleteShareTitle(AlertDialog.Builder adbDeleteShare) {
        adbDeleteShare.setTitle(DELETE_SHARE_TITLE);
    }

    private void setAdbDeleteShareStyle(AlertDialog.Builder adbDeleteShare) {
        AlertDialog deleteShareAlertDialog = adbDeleteShare.create();
        deleteShareAlertDialog.show();
        setDeleteShareDialogStyle(deleteShareAlertDialog);
    }

    private void setDeleteShareDialogStyle(AlertDialog alert) {
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.BLACK);
        nbutton.setText(CANCEL);
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.parseColor(TERAWHERE_PRIMARY_COLOR));
        pbutton.setText(CONFIRM);
    }

    public void setShares(List<Share> shares) {
        this.shares = shares;
    }

    public Share getLastShare() {
        if (shares == null || shares.isEmpty()) return null;
        return shares.get(shares.size() - 1);
    }

    @Override
    public int getItemCount() {
        return shares == null ? 0 : shares.size();
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
        private TextView textViewSeatsLeft;
        private TextView textViewViewMore;
        private TextView textViewCancel;
        private TextView textViewSharePast;
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
            textViewSeatsLeft = (TextView) view.findViewById(R.id.text_view_seats_left);
            textViewRemarksLabel = (TextView) view.findViewById(R.id.text_view_remarks_label);
            textViewRemarks = (TextView) view.findViewById(R.id.text_view_remarks);
            textViewViewMore = (TextView) view.findViewById(R.id.text_view_view_more);
            textViewCancel = (TextView) view.findViewById(R.id.text_view_cancel);
            textViewSharePast = (TextView) view.findViewById(R.id.text_view_share_past);
            textViewTag = (TextView) view.findViewById(R.id.text_view_tag);
        }
    }
}
