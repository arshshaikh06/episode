package com.devteam.episode;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    // URL
    private final String BASE_URL = "https://api.themoviedb.org/3/tv/";
    private final String REMAINING_URL = "?api_key=" + BuildConfig.API_KEY + "&language=en-US";
    private final String IMAGE_URL = "https://image.tmdb.org/t/p/w500";
    private JsonObjectRequest homeRequest;
    private RequestQueue requestQueue;

    // UI Elements
    private View view;
    private RecyclerView homeNextRecyclerView, homeLastRecyclerView;
    private RelativeLayout homeLastRelative, homeNextRelative;
    private TextView tvNextEpisode, tvHomeNoFavs;
    private HomeAdapter homeAdapter;

    //Variables
    public static final String TAG = "HomeFragment";
    private List<Home> nextHomeList, lastHomeList;

    //AirDate Status
    public static final String AIRED = "aired";
    public static final String UNAIRED = "unaired";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_fragment, container, false);
        init();
        homeLastRelative.setVisibility(View.INVISIBLE);
        homeNextRelative.setVisibility(View.INVISIBLE);

        tvHomeNoFavs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrendingFragment trendingFragment = new TrendingFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup) getView().getParent()).getId(), trendingFragment, "TRENDING")
                        .addToBackStack(null)
                        .commit();
                MainActivity.bottomNavigationView.getMenu().findItem(R.id.navTrending).setChecked(true);
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastHomeList = new ArrayList<>();
        nextHomeList = new ArrayList<>();
        getJSONRequest();
    }

    private void init() {
        homeNextRecyclerView = view.findViewById(R.id.homeNextRecyclerView);
        homeLastRecyclerView = view.findViewById(R.id.homeLastRecyclerView);
        homeLastRelative = view.findViewById(R.id.homeLastRelative);
        homeNextRelative = view.findViewById(R.id.homeNextRelative);
        tvNextEpisode = view.findViewById(R.id.tvNextEpisode);
        tvHomeNoFavs = view.findViewById(R.id.tvHomeNoFavourite);

        // To set visibility at startup
        if(SplashScreenActivity.favouritesList.size() == 0)
            tvHomeNoFavs.setVisibility(View.VISIBLE);
        else
            tvHomeNoFavs.setVisibility(View.INVISIBLE);
    }

    private void getJSONRequest() {
        //Log.d(TAG, "" + SplashScreenActivity.favouritesId.size());
        Log.d(TAG, "getJSONRequest: Checking for null pointer");
        requestQueue = Volley.newRequestQueue(getActivity());
        for (int i = 0; i < SplashScreenActivity.favouritesList.size(); i++) {
            final int tvId = SplashScreenActivity.favouritesList.get(i).getId();
            String URL = BASE_URL + tvId + REMAINING_URL;
            Log.d(TAG, "getDetails: Requesting details for URL: " + URL);
            homeRequest = new JsonObjectRequest(URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d(TAG, "onResponse: Details found");
                        String backdropURL = IMAGE_URL + response.getString("backdrop_path");
                        String showName = response.getString("name");

                        // To get networks
                        String networks = "";
                        JSONArray networkArray = response.getJSONArray("networks");
                        for (int i = 0; i < networkArray.length(); i++) {
                            JSONObject networkObject = networkArray.getJSONObject(i);
                            if (i == 0) {
                                networks = networks + networkObject.getString("name");
                            } else {
                                networks = networks + ", " + networkObject.getString("name");
                            }
                        }

                        // Date Difference Calculations
                        String nextAirDateStr = "", lastAirDateStr = "";

                        // To get last episode details
                        JSONObject lastEpisodeToAir = response.getJSONObject("last_episode_to_air");
                        String lastEpisodeName = lastEpisodeToAir.getString("name");
                        int lastEpisodeEpisodeNo = lastEpisodeToAir.getInt("episode_number");
                        int lastEpisodeSeasonNo = lastEpisodeToAir.getInt("season_number");
                        String lastAirDate = lastEpisodeToAir.getString("air_date");

                        int lastAirDateInt = getDateDifference(lastAirDate);

                        if (lastAirDateInt >= 0 && lastAirDateInt < 10)
                            lastAirDateStr = "00" + lastAirDateInt;
                        else if (lastAirDateInt >= 10 && lastAirDateInt < 100)
                            lastAirDateStr = "0" + lastAirDateInt;
                        else
                            lastAirDateStr = "" + lastAirDateInt;

                        // Check if Air Dates are between -365 & 365
                        if (lastAirDateInt < 365) {
                            Home lastHome = new Home(lastEpisodeSeasonNo, lastEpisodeEpisodeNo, lastAirDateInt, tvId, showName, networks, lastEpisodeName, backdropURL, lastAirDateStr, AIRED);
                            lastHomeList.add(lastHome);
                        }

                        // To get next episode details
                        JSONObject nextEpisodeToAir = response.getJSONObject("next_episode_to_air");
                        String nextEpisodeName = nextEpisodeToAir.getString("name");
                        int nextEpisodeEpisodeNo = nextEpisodeToAir.getInt("episode_number");
                        int nextEpisodeSeasonNo = nextEpisodeToAir.getInt("season_number");
                        String nextAirDate = nextEpisodeToAir.getString("air_date");

                        int nextAirDateInt = getDateDifference(nextAirDate);

                        Log.d(TAG, "onResponse: Date Check: " + showName + " LastAirDate-" + lastAirDateInt + "NextAirDate-" + nextAirDateInt);


                        if (nextAirDateInt >= 0 && nextAirDateInt < 10)
                            nextAirDateStr = "00" + nextAirDateInt;
                        else if (nextAirDateInt >= 10 && nextAirDateInt < 100)
                            nextAirDateStr = "0" + nextAirDateInt;
                        else
                            nextAirDateStr = "" + nextAirDateInt;


                        if (nextAirDateInt < 365) {
                            Home nextHome = new Home(nextEpisodeSeasonNo, nextEpisodeEpisodeNo, nextAirDateInt, tvId, showName, networks, nextEpisodeName, backdropURL, nextAirDateStr, UNAIRED);
                            nextHomeList.add(nextHome);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "onResponse: Exception: " + e.getMessage());
                    }
                    setUpLastRecyclerView();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: Error: " + error.toString());
                }
            });
            requestQueue.add(homeRequest);
        }
    }


    private void setUpLastRecyclerView() {
        Log.d(TAG, "setUpLastRecyclerView: There are " + lastHomeList.size() + " seasons");
        homeAdapter = new HomeAdapter(lastHomeList, getActivity());

        Collections.sort(lastHomeList, new Comparator<Home>() {
            @Override
            public int compare(Home o1, Home o2) {
                return o1.getAirDate().compareTo(o2.getAirDate());
            }
        });

        homeLastRecyclerView.setAdapter(homeAdapter);
        homeLastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (lastHomeList.size() != 0) {
            homeLastRelative.setVisibility(View.VISIBLE);
        } else {
            homeLastRelative.setVisibility(View.GONE);
        }
        setUpNextRecyclerView();
    }

    private void setUpNextRecyclerView() {
        Log.d(TAG, "setUpNextRecyclerView: There are " + nextHomeList.size() + " seasons");
        homeAdapter = new HomeAdapter(nextHomeList, getActivity());

        Collections.sort(nextHomeList, new Comparator<Home>() {
            @Override
            public int compare(Home o1, Home o2) {
                return o1.getAirDate().compareTo(o2.getAirDate());
            }
        });

        homeNextRecyclerView.setAdapter(homeAdapter);
        homeNextRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvNextEpisode.setFocusable(true);
        }
        Log.d(TAG, "setUpNextRecyclerView: " + nextHomeList.size() + " " + lastHomeList.size());
        if (nextHomeList.size() != 0) {
            homeNextRelative.setVisibility(View.VISIBLE);
        } else {
            homeNextRelative.setVisibility(View.GONE);
        }
        homeNextRelative.requestFocus();
    }

    // Date Format: MM/dd/yyyy
    private static String changeDateFormat(String oldDate) {
        String oldDateFormat = "yyyy-MM-dd";
        String newDateFormat = "MM/dd/yyyy";
        String newDate;
        SimpleDateFormat sdf = new SimpleDateFormat(oldDateFormat);
        try {
            Date date = sdf.parse(oldDate);
            sdf.applyPattern(newDateFormat);
            newDate = sdf.format(date);
            return newDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private int getDateDifference(String airDate) {
        String dayDifference = "";
        int dayDifferenceInt;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            Date date = new Date();


            //Dates to compare
            String CurrentDate = formatter.format(date);
            String FinalDate = changeDateFormat(airDate);

            Date date1;
            Date date2;

            SimpleDateFormat dates = new SimpleDateFormat("MM/dd/yyyy");

            //Setting dates
            date1 = dates.parse(CurrentDate);
            date2 = dates.parse(FinalDate);

            //Comparing dates
            long difference = Math.abs(date1.getTime() - date2.getTime());
            long differenceDates = difference / (24 * 60 * 60 * 1000);

            //Convert long to String
            dayDifference = Long.toString(differenceDates);

            Log.e("HERE", "HERE: " + dayDifference);

        } catch (Exception exception) {
            Log.e("DIDN'T WORK", "exception " + exception);
        }

        dayDifferenceInt = Integer.parseInt(dayDifference);

        return dayDifferenceInt;
    }


}
