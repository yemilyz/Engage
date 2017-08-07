package com.codepath.engage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;

import com.codepath.engage.models.Event;
import com.codepath.engage.models.UserEvents;

import java.util.ArrayList;

import static com.codepath.engage.EventDetailsFragment.newInstance;

/**
 * Created by emilyz on 8/1/17.
 */

public class TabFragmentAdapter extends FragmentPagerAdapter {
    private Context context;
    private int numOfTabs;
    Event event;
    ArrayList<String> createdEventsInfo;
    UserEvents currentUpdate;

    public TabFragmentAdapter(FragmentManager fm, Context context, ArrayList<String> createdEventsInfo, UserEvents currentUpdate, Event event) {
        super(fm);
        this.context = context;
        this.event = event;
        this.createdEventsInfo = createdEventsInfo;
        this.currentUpdate = currentUpdate;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                new EventDetailsFragment();
                return EventDetailsFragment.newInstance(createdEventsInfo, currentUpdate, event);
            case 1:
                new MapFragment();
                return  MapFragment.newInstance(createdEventsInfo, currentUpdate, event);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Event Details";
            case 1:
                return "Map";
            default:
                return null;
        }
    }
}