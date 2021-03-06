package com.devteam.episode;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // URL
    public static final String API_KEY = BuildConfig.API_KEY;

    // UI Elements
    public static BottomNavigationView bottomNavigationView;

    // Variables
    private static final String TAG = "MainActivity";

    //Progress Bar
    public static ProgressBar trendingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        init();

        // To select trending fragment as main fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerMain,
                new HomeFragment()).commit();

        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        setUpBottomNavigation();
    }

    private void init() {
        bottomNavigationView = findViewById(R.id.bottomNavigationMain);
        trendingProgressBar = findViewById(R.id.pbTrending);
    }

    private void setUpBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;

                Fragment fragment = getSupportFragmentManager().findFragmentByTag("TRENDING");
                if (fragment != null && fragment.isVisible()) {
                    // Scroll to top of recycler view
                }

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (menuItem.getItemId()) {
                    case R.id.navHome:
                        selectedFragment = new HomeFragment();
                        transaction.replace(R.id.fragmentContainerMain, selectedFragment, "HOME");
                        break;
                    case R.id.navTrending:
                        selectedFragment = new TrendingFragment();
                        transaction.replace(R.id.fragmentContainerMain, selectedFragment, "TRENDING");
                        break;
                    case R.id.navProfile:
                        selectedFragment = new ProfileFragment();
                        transaction.replace(R.id.fragmentContainerMain, selectedFragment, "PROFILE");
                        break;
                }
                transaction.commit();
                return true;
            }
        });
    }

    public static void updateFavourites(final Context context, String uid) {
        SplashScreenActivity.favouritesList = new ArrayList<>();

        Log.d(TAG, "getFavourites: new Arraylist");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(uid).child("favouriteList");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: Favourites exists");
                    for (DataSnapshot list : dataSnapshot.getChildren()) {
                        SplashScreenActivity.favouritesList.add(list.getValue(Favourite.class));
                    }
                } else {
                    Log.d(TAG, "onDataChange: Favourites do not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Database Error: " + databaseError.getMessage());
                Toast.makeText(context, "Database error :(", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
