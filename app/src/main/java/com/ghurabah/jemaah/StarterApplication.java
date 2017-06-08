package com.ghurabah.jemaah;

/**
 * Created by musa on 8/6/17.
 */

/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("023001f72dddb2d0116f0ea6dcb3276d4bd12f20")
                .clientKey("60e595004112d813cdd35f65309f0d4b62c27360")
                .server("http://ec2-54-245-203-189.us-west-2.compute.amazonaws.com/parse/")
                .build()
        );

        ParseObject object = new ParseObject("ExampleObject");
        object.put("MyNumber", "123");
        object.put("MyString","String");

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("Result", "Successful");
                }
                else {
                    Log.i("Result",":" + e.getMessage());
                }
            }
        });
        ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
