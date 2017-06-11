package com.ghurabah.jemaah.activities;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ghurabah.jemaah.R;
import com.ghurabah.jemaah.models.Share;
import com.ghurabah.jemaah.utils.DateUtils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CreateShareActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    public static final int REQUEST_CODE_GET_START_PLACE = 1;
    public static final int REQUEST_CODE_GET_END_PLACE = 2;
    public static final double OFFSET_LATITUDE = 0.000225;
    public static final double OFFSET_LONGITUDE = 0.0043705;

    private Location currentLocation;
    private Place placeStart;
    private Place placeEnd;


    private Button buttonCreateShare;
    private TextInputEditText textInputEditTextMeetUpTime;
    private TextInputEditText textInputEditTextStartLocation;
    private TextInputEditText textInputEditTextEndLocation;
    //    private TextInputEditText textInputEditTextSeatsAvailable;
    private TextInputEditText textInputEditTextRemarks;
    //    private TextInputEditText textInputEditTextEstimatedCost;
    private LinearLayout linearLayoutCreateShare;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_share);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Share");

        trackCurrentLocation();
        initViewHandles();
        initTimePickerDialog(null);
        setClickListeners();

        Log.i("CURRENTLOCATIONLAT", "" + currentLocation.getLatitude());
        Log.i("CURRENTLOCATIONLNG", "" + currentLocation.getLongitude());


    }

    private void initTimePickerDialog(Object o) {
    }

    private void initViewHandles() {
        buttonCreateShare = (Button) findViewById(R.id.button_create_offer);
        textInputEditTextMeetUpTime = (TextInputEditText) findViewById(R.id.text_input_edit_text_meetup_time);
        textInputEditTextStartLocation = (TextInputEditText) findViewById(R.id.text_input_edit_text_start_location);
        textInputEditTextEndLocation = (TextInputEditText) findViewById(R.id.text_input_edit_text_end_location);
//        textInputEditTextSeatsAvailable = (TextInputEditText) findViewById(R.id.text_input_edit_text_seats_available);
//        textInputEditTextEstimatedCost = (TextInputEditText) findViewById(R.id.text_input_edit_text_estimated_cost);
        textInputEditTextRemarks = (TextInputEditText) findViewById(R.id.text_input_edit_text_remarks);
        linearLayoutCreateShare = (LinearLayout) findViewById(R.id.linear_layout_create_share);
    }

    private void setClickListeners() {
        linearLayoutCreateShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
        textInputEditTextStartLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callStartPlaceAutocompleteActivityIntent();
            }
        });

        textInputEditTextEndLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callEndPlaceAutocompleteActivityIntent();
            }
        });

//        textInputEditTextStartLocation.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                callStartPlacePickerActivityIntent();
//                return true;
//            }
//        });
//
//        textInputEditTextEndLocation.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                callEndPlacePickerActivityIntent();
//                return true;
//            }
//        });

        buttonCreateShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isFormFilled()) {
                    Toast.makeText(CreateShareActivity.this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Date dateWithTimeComponent = DateUtils.fromFriendlyTimeString(textInputEditTextMeetUpTime.getText().toString());
                Date dateComplete = DateUtils.getDateFromDates(new Date(), dateWithTimeComponent);

                // if the time user select has passed, increment the day
                if (dateComplete.before(new Date())) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(dateComplete);
                    c.add(Calendar.DATE, 1);
                    dateComplete = c.getTime();
                }

                showBookingDialog(dateComplete);

//                Log.i("SEATSREMAINING", "" + Integer.parseInt(textInputEditTextSeatsAvailable.getText().toString()));
//                Log.i("MEETUPTIME", "" + dateComplete.toString());
//                Log.i("REMARKS", "" + textInputEditTextRemarks.getText().toString());
//                Log.i("estimatedCost", textInputEditTextEstimatedCost.getText().toString());
//

//                if (!isFormFilled()) {
//                    Toast.makeText(CreateShareActivity.this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                String startName;
//                String startAddress;
//                double startLatitude;
//                double startLongitude;
//                String endName;
//                String endAddress;
//                double endLatitude;
//                double endLongitude;
//
//                startName = placeStart.getName().toString();
//                startAddress = placeStart.getAddress().toString();
//                startLatitude = placeStart.getLatLng().latitude;
//                startLongitude = placeStart.getLatLng().longitude;
//
//                endName = placeEnd.getName().toString();
//                endAddress = placeEnd.getAddress().toString();
//                endLatitude = placeEnd.getLatLng().latitude;
//                endLongitude = placeEnd.getLatLng().longitude;
//
//                Date dateWithTimeComponent = DateUtils.fromFriendlyTimeString(textInputEditTextMeetUpTime.getText().toString());
//                Date dateComplete = DateUtils.getDateFromDates(new Date(), dateWithTimeComponent);
//
//                // if the time user select has passed, increment the day
//                if (dateComplete.before(new Date())) {
//                    Calendar c = Calendar.getInstance();
//                    c.setTime(dateComplete);
//                    c.add(Calendar.DATE, 1);
//                    dateComplete = c.getTime();
//                }
//


//
//                Log.i("USER", "" + sharerParseUser.getUsername());
//                Log.i("STARTNAME", "" + startName);
//                Log.i("STARTADDRESS", "" + startAddress);
//                Log.i("ENDADDRESS", "" + endAddress);
//                Log.i("ENDNAME", "" + endName);
//                Log.i("SEATSREMAINING", "" + Integer.parseInt(textInputEditTextSeatsAvailable.getText().toString()));
//                Log.i("MEETUPTIME", "" + dateComplete.toString());
//                Log.i("REMARKS", "" + textInputEditTextRemarks.getText().toString());
//                Log.i("estimatedCost", textInputEditTextEstimatedCost.getText().toString());
//
//
//                final ParseObject shareParseObject = new ParseObject("Share");
//
//                sharerParseUser.saveInBackground(new SaveCallback() {
//                                                     public void done(ParseException e) {
//                                                         shareParseObject.getRelation("sharer").add(sharerParseUser);
//                                                         shareParseObject.saveInBackground();
//                                                     }
//                                                 }
//                );
//
//
//                shareParseObject.put("startLocationName", startName);
//                shareParseObject.put("startLocationAddress", startAddress);
//                shareParseObject.put("startLatitude", startLatitude);
//                shareParseObject.put("startLongitude", startLongitude);
//
//                shareParseObject.put("endLocationName", endName);
//                shareParseObject.put("endLocationAddress", endAddress);
//                shareParseObject.put("endLatitude", endLatitude);
//                shareParseObject.put("endLongitude", endLongitude);
//
//                shareParseObject.put("seatsRemaining", Integer.parseInt(textInputEditTextSeatsAvailable.getText().toString()));
//                shareParseObject.put("seatsJoined", 0);
//                shareParseObject.put("seatsShared", Integer.parseInt(textInputEditTextSeatsAvailable.getText().toString()));
//                shareParseObject.put("meetupTime", dateComplete);
//                shareParseObject.put("estimatedCost", Integer.parseInt(textInputEditTextEstimatedCost.getText().toString()));
//                shareParseObject.put("remarks", textInputEditTextRemarks.getText().toString());
//
//                finish();


            }

            private Share getShare() {
                String startName;
                String startAddress;
                double startLatitude;
                double startLongitude;
                String endName;
                String endAddress;
                double endLatitude;
                double endLongitude;

                startName = placeStart.getName().toString();
                startAddress = placeStart.getAddress().toString();
                startLatitude = placeStart.getLatLng().latitude;
                startLongitude = placeStart.getLatLng().longitude;

                endName = placeEnd.getName().toString();
                endAddress = placeEnd.getAddress().toString();
                endLatitude = placeEnd.getLatLng().latitude;
                endLongitude = placeEnd.getLatLng().longitude;

                Date dateWithTimeComponent = DateUtils.fromFriendlyTimeString(textInputEditTextMeetUpTime.getText().toString());
                Date dateComplete = DateUtils.getDateFromDates(new Date(), dateWithTimeComponent);

                // if the time user select has passed, increment the day
                if (dateComplete.before(new Date())) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(dateComplete);
                    c.add(Calendar.DATE, 1);
                    dateComplete = c.getTime();
                }
//public Share(ParseUser sharer, String startLocationName, String startLocationAddress,
//                        LatLng startLocationPoint, String endLocationName, String endLocationAddress,
//                        LatLng endLocationPoint, Integer seatsRemaning, Integer seatsJoined, Integer seatsShared,
//                        Date meetupTime, Double estimatedCost, String remarks) {


//                return new Share(
//                        DateUtils.dateToString(dateComplete, DateUtils.MYSQL_DATE_TIME_FORMAT, DateUtils.TIMEZONE_UTC),
//                        startName,
//                        startAddress,
//                        startLatitude,
//                        startLongitude,
//                        endName,
//                        endAddress,
//                        endLatitude,
//                        endLongitude,
//                        Integer.parseInt(textInputEditTextSeatsAvailable.getText().toString()),
//                        textInputEditTextRemarks.getText().toString()
//
//                );
                return null;
            }
        });

    }

    private boolean isFormFilled() {
        return !(textInputEditTextMeetUpTime.getText().toString().matches("") || textInputEditTextStartLocation.getText().toString().matches("") || textInputEditTextEndLocation.getText().toString().matches(""));
    }

    private void trackCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        currentLocation = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    private void initTimePickerDialog(Date meetupTime) {
        if (meetupTime == null) {
            meetupTime = new Date();
        }

        final Calendar calendar = DateUtils.dateToCalendar(meetupTime);

        textInputEditTextMeetUpTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog;
                timePickerDialog = new TimePickerDialog(CreateShareActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        if (calendar.getTime().before(new Date())) {
                            Toast.makeText(CreateShareActivity.this, "Cannot select time that is in the past.", Toast.LENGTH_SHORT).show();
                        } else {
                            textInputEditTextMeetUpTime.setText(DateUtils.toFriendlyTimeString(calendar.getTime()));
                        }
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });
    }

    private void callStartPlaceAutocompleteActivityIntent() {
        LatLng minimumBound = new LatLng(currentLocation.getLatitude() - OFFSET_LATITUDE, currentLocation.getLongitude() - OFFSET_LONGITUDE);
        LatLng maximumBound = new LatLng(currentLocation.getLatitude() + OFFSET_LATITUDE, currentLocation.getLongitude() + OFFSET_LONGITUDE);
        LatLngBounds placePickerMapBounds = new LatLngBounds(minimumBound, maximumBound);
        try {
            AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                    .setCountry("SG")
                    .build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(autocompleteFilter)
                    .setBoundsBias(placePickerMapBounds)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_GET_START_PLACE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "callStartPlaceAutocompleteActivityIntent: ", e);
        }
    }

    private void callEndPlaceAutocompleteActivityIntent() {
        LatLng minimumBound = new LatLng(currentLocation.getLatitude() - OFFSET_LATITUDE, currentLocation.getLongitude() - OFFSET_LONGITUDE);
        LatLng maximumBound = new LatLng(currentLocation.getLatitude() + OFFSET_LATITUDE, currentLocation.getLongitude() + OFFSET_LONGITUDE);
        LatLngBounds placePickerMapBounds = new LatLngBounds(minimumBound, maximumBound);
        try {
            AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                    .setCountry("SG")
                    .build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(autocompleteFilter).setBoundsBias(placePickerMapBounds)
                            .build(this);
            startActivityForResult(intent, REQUEST_CODE_GET_END_PLACE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "callEndPlaceAutocompleteActivityIntent: ", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_START_PLACE && resultCode == RESULT_OK) {
            placeStart = PlacePicker.getPlace(this, data);
            textInputEditTextStartLocation.setText(getPlaceNameWithPrefix(placeStart));
        } else if (requestCode == REQUEST_CODE_GET_END_PLACE && resultCode == RESULT_OK) {
            placeEnd = PlacePicker.getPlace(this, data);
            textInputEditTextEndLocation.setText(getPlaceNameWithPrefix(placeEnd));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private CharSequence getPlaceNameWithPrefix(Place place) {
        String name = place.getName().toString();
        if (name.contains("°") && name.contains("'") && name.contains("\"")) {
            return place.getAddress().toString().split(",")[0];
        } else if (place.getPlaceTypes() == null) {
        } else if (place.getPlaceTypes().contains(Place.TYPE_BUS_STATION)) {
            return "Bus Stop @ " + name;
        } else if (place.getPlaceTypes().contains(Place.TYPE_SUBWAY_STATION)) {
            return "MRT Station @ " + name;
        } else if (place.getPlaceTypes().contains(Place.TYPE_TRAIN_STATION)) {
            return "Train Station @ " + name;
        } else if (place.getPlaceTypes().contains(Place.TYPE_TAXI_STAND)) {
            return "Taxi Stand @ " + name;
        } else if (place.getPlaceTypes().contains(Place.TYPE_PARKING)) {
            return "Parking @ " + name;
        } else if (place.getPlaceTypes().contains(Place.TYPE_AIRPORT)) {
            return "Airport @ " + name;
        }

        return place.getName();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showBookingDialog(final Date meetupTime) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(CreateShareActivity.this);

        final LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_create_share, null);
        builder.setView(dialogView);

        final Spinner spinnerSeatsAvailable = (Spinner) dialogView.findViewById(R.id.spinner_seats_available);
        final Spinner spinnerCostPreference = (Spinner) dialogView.findViewById(R.id.spinner_cost_preference);


        TextView textViewDay = (TextView) dialogView.findViewById(R.id.text_view_day);
        TextView textViewMonth = (TextView) dialogView.findViewById(R.id.text_view_month);
        TextView textViewEndLocationName = (TextView) dialogView.findViewById(R.id.text_view_end_location_name);
        TextView textViewEndLocationAddress = (TextView) dialogView.findViewById(R.id.text_view_end_location_address);
        TextView textViewStartLocationName = (TextView) dialogView.findViewById(R.id.text_view_start_location_name);
        TextView textViewStartLocationAddress = (TextView) dialogView.findViewById(R.id.text_view_start_location_address);
        TextView textViewMeetupTime = (TextView) dialogView.findViewById(R.id.text_view_meetup_time);
        TextView textViewRemarksLabel = (TextView) dialogView.findViewById(R.id.text_view_remarks_label);
        TextView textViewRemarks = (TextView) dialogView.findViewById(R.id.text_view_remarks);

        String meetUpTime = DateUtils.toFriendlyTimeString(meetupTime);
        String day = DateUtils.dateToString(meetupTime, DateUtils.DAY_OF_MONTH_FORMAT);
        String month = DateUtils.dateToString(meetupTime, DateUtils.MONTH_ABBREVIATED_FORMAT);

        int min = 5;
        int max = 30;

        Random r = new Random();
        final int i1 = r.nextInt(max - min + 1) + min;

        builder.setTitle("ESTIMATED COST IS: $" + i1);

        textViewMonth.setText(month);
        textViewDay.setText(day);
        textViewMeetupTime.setText(meetUpTime);
        textViewEndLocationName.setText(placeEnd.getName().toString());
        textViewEndLocationAddress.setText(placeEnd.getAddress().toString());
        textViewStartLocationName.setText(placeStart.getName().toString());
        textViewStartLocationAddress.setText(placeStart.getAddress().toString());

        String remarks = textInputEditTextRemarks.getText().toString();
        if (remarks != null && !remarks.isEmpty()) {
            textViewRemarks.setText(remarks);
        } else {
            textViewRemarksLabel.setVisibility(View.GONE);
            textViewRemarks.setVisibility(View.GONE);
        }


        List<String> categories = new ArrayList<String>();
        int seatsAvailable = 10;
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

        spinnerSeatsAvailable.setAdapter(dataAdapter);

        List<String> categories2 = new ArrayList<String>();
        categories2.add("Shared");
        categories2.add("Sponsored");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(dialogView.getContext(), android.R.layout.simple_spinner_item, categories2) {
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
                textView.setTextSize(15);
                textView.setGravity(Gravity.CENTER);
                return view;
            }
        };

        spinnerCostPreference.setAdapter(dataAdapter2);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                final ParseUser sharerParseUser = ParseUser.getCurrentUser();

                final ParseObject shareParseObject = new ParseObject("Share");
                sharerParseUser.saveInBackground(new SaveCallback() {
                                                     public void done(ParseException e) {
                                                         shareParseObject.getRelation("sharer").add(sharerParseUser);
                                                         shareParseObject.saveInBackground();
                                                     }
                                                 }
                );


                shareParseObject.put("startLocationName", placeStart.getName().toString());
                shareParseObject.put("startLocationAddress", placeStart.getAddress().toString());
                shareParseObject.put("startLatitude", placeStart.getLatLng().latitude);
                shareParseObject.put("startLongitude", placeStart.getLatLng().longitude);

                shareParseObject.put("endLocationName", placeEnd.getName().toString());
                shareParseObject.put("endLocationAddress", placeEnd.getAddress().toString());
                shareParseObject.put("endLatitude", placeEnd.getLatLng().latitude);
                shareParseObject.put("endLongitude", placeEnd.getLatLng().longitude);

                shareParseObject.put("preference", spinnerCostPreference.getSelectedItem().toString());
                shareParseObject.put("seatsRemaining", Integer.parseInt(spinnerSeatsAvailable.getSelectedItem().toString()));
                shareParseObject.put("seatsJoined", 0);
                shareParseObject.put("seatsShared", Integer.parseInt(spinnerSeatsAvailable.getSelectedItem().toString()));
                shareParseObject.put("meetupTime", meetupTime);
                shareParseObject.put("estimatedCost", i1);
                shareParseObject.put("remarks", textInputEditTextRemarks.getText().toString());

                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        decorateAlertDialog(alert);
    }

    private void decorateAlertDialog(AlertDialog alertDialog) {
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.BLACK);
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.parseColor("#54d8bd"));
    }
}
