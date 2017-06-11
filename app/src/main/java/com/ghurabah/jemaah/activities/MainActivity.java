package com.ghurabah.jemaah.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.ghurabah.jemaah.R;
import com.ghurabah.jemaah.fragments.ShareFragment;
import com.ghurabah.jemaah.fragments.HistoryFragment;
import com.ghurabah.jemaah.fragments.JoinFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BottomNavigationView bottomNavigation;
    private Fragment fragment;
    private FragmentManager fragmentManager;

    private int times = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = (BottomNavigationView)findViewById(R.id.navigation);
        fragmentManager = getSupportFragmentManager();

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.navigation_join:
                        times ++;
                        if (times == 1) {
                            fragment = new JoinFragment();
                        }
                        break;
                    case R.id.navigation_share:
                        fragment = new ShareFragment();
                        times = 0;
                        break;
                    case R.id.navigation_history:
                        fragment = new HistoryFragment();
                        times = 0;
                        break;
                }
                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.content, fragment).commit();
                return true;
            }
        });

        View view = bottomNavigation.findViewById(R.id.navigation_share);
        view.performClick();

//        ParseQuery<ParseObject> query  = ParseQuery.getQuery("Share");
//
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
//                if (e==null){
//
//                    Log.d("SIZE", "" +parseObjects.size());
//                    ParseRelation<ParseObject> relation = parseObjects.get(0).getRelation("location");
//                    relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
//                        @Override
//                        public void done(List<ParseObject> objects, ParseException e) {
//                            Log.d("RELATION SIZE", "" + objects.get(0).getString("startName"));
//
//                            ;
//                        }
//                    });
//
//                } else {
//                    Log.d("ERROR:", "" + e.getMessage());
//                }
//            }
//        });
////        ParseRelation<ParseObject> relation = user.getRelation("commentaires");
    }
}
