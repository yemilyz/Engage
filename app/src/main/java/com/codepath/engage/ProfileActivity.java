package com.codepath.engage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.engage.models.DateProgram;
import com.codepath.engage.models.User;
import com.codepath.engage.models.UserEvents;
import com.facebook.Profile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mindorks.placeholderview.PlaceHolderView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ProfileActivity extends AppCompatActivity {

    public UpdateAdapter adapter;
    public ArrayList<UserEvents> events;
    public ArrayList<UserEvents> createdEventsList;
    String uid;
    String whichProfile;
    String profileUrl;
    DatabaseReference mDatabase;
    boolean isFollowing;
    User u;
    User currentProfile;
    ProgressDialog progress;
    Context context;
    List<String> eventIDs;
    String verb;
    public ArrayList<Date> dates;
    final int REQUEST_CODE = 1;
    String imgId;
    @BindView(R.id.rvUpdates) RecyclerView rvUpdates;
    @BindView(R.id.profileImage) ImageView profileImage;
    @BindView(R.id.profileHeader) TextView profileHeader;
    @BindView(R.id.numFollowing) TextView following;
    @BindView(R.id.numFollowers) TextView followers;
    @BindView(R.id.followers) TextView flws;
    @BindView(R.id.following) TextView flwg;
    @BindView(R.id.floatingActionButton) FloatingActionButton floatingActionButton;
    @BindView(R.id.header) ImageView header;
    StorageReference storage;
    @BindView(R.id.Home) ImageView home;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.drawer_view)
    PlaceHolderView mDrawerView;
    @BindView(R.id.profileUsername) TextView profileUsername;
    @BindView(R.id.toolbar_profile)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);
        dates = new ArrayList<>();
        eventIDs = new ArrayList<>();
        whichProfile = getIntent().getStringExtra("whichProfile");
        verb = getIntent().getStringExtra("verb");
        events = new ArrayList<>();
        //createdEventList = new ArrayList<>();
        eventIDs = new ArrayList<>();

        progress = new ProgressDialog(this);
        profileUrl = "";
        adapter = new UpdateAdapter(events, whichProfile, verb, dates, profileUrl);
        rvUpdates.setLayoutManager(new LinearLayoutManager(context));
        rvUpdates.setAdapter(adapter);
        storage = FirebaseStorage.getInstance().getReference();

        isFollowing = false;
        u = new User();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");

        setUpDrawer();

        profileHeader.setTypeface(font);
        profileUsername.setTypeface(font);

        if (Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName())) != null) {
            u = Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));
            uid = u.getUid();
            profileUsername.setText(u.firstName + " " + u.lastName);

        }
        else {
            uid = Profile.getCurrentProfile().getId();
            Log.d("uid", "null");
        }

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
                currentProfile = dataSnapshot.child(Profile.getCurrentProfile().getId()).getValue(User.class);
                if (uid.equals(Profile.getCurrentProfile().getId())){
                    u = currentProfile;
                    profileUsername.setText(u.firstName + " " + u.lastName);
                }
                profileUrl = u.profilePicture;

                Glide.with(getApplicationContext()).load(u.profilePicture).bitmapTransform(new RoundedCornersTransformation(getApplicationContext(), 100, 0)).centerCrop().into(profileImage);
                followers.setTypeface(font);
                following.setTypeface(font);
                flws.setTypeface(font);
                flwg.setTypeface(font);

                HashMap<String, String> followingList = (HashMap<String, String>) dataSnapshot.child(uid).child("following").getValue();
                HashMap<String, String> followerList = (HashMap<String, String>) dataSnapshot.child(uid).child("followers").getValue();

                if (followerList != null) {
                    followers.setText(followerList.size() + "");
                } else if (followerList == null) {
                    followers.setText("0");
                }

                if (followingList != null) {
                    following.setText(followingList.size() + "");
                } else if (followingList == null){
                    following.setText("0");
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        Glide.with(context).load(u.profilePicture).centerCrop().bitmapTransform(new RoundedCornersTransformation(getApplicationContext(), 100, 0)).into(profileImage);

        Glide.with(context).using(new FirebaseImageLoader())
                .load(storage.child("headers").child(uid))
                .error(R.color.red_300)
                .centerCrop().
                into(header);

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(ProfileActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(ProfileActivity.this);
                }
                builder.setTitle("Upload image")
                        .setMessage("Do you want to upload a header?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                pick();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        new getFirebaseData().execute();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!uid.equals(currentProfile.uid)) {
                    isFollowing = true;
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String keyFollowers = null;
                            HashMap<String, String> followingList = (HashMap<String, String>) dataSnapshot.child(currentProfile.uid).child("following").getValue();
                            int size = 0;
                            HashMap<String, String> followerList = (HashMap<String, String>) dataSnapshot.child(uid).child("followers").getValue();
                            try{
                                int i = 0;
                                size = followingList.size();
                                for (String key : followingList.keySet()) {
                                    String keyFollower = followingList.get(key);
                                    if (keyFollower.equals(uid)) {
                                        followingList.remove(key);
                                        for (String keyFollowing : followerList.keySet()) {
                                            if (followerList.get(keyFollowing).equals(currentProfile.uid)) {
                                                keyFollowers = keyFollowing;
                                            }
                                        }
                                        followerList.remove(keyFollowers);
                                        DatabaseReference deleteFollow = mDatabase.child(uid).child("followers");
                                        deleteFollow.setValue(followerList);
                                        DatabaseReference deleteFollowing = mDatabase.child(currentProfile.uid).child("following");
                                        deleteFollowing.setValue(followingList);
                                        if (followingList == null||followingList.size() == 0)
                                        {
                                            followers.setText("0");
                                        }
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                                if (i == size){
                                    DatabaseReference addFollow = mDatabase.child(uid).child("followers").push();
                                    addFollow.setValue(currentProfile.uid);
                                    DatabaseReference addFollowing = mDatabase.child(currentProfile.uid).child("following").push();
                                    addFollowing.setValue(uid);
                                    DatabaseReference addNotif = mDatabase.child(uid).child("notifList").push();
                                    DatabaseReference addNotifImg = mDatabase.child(uid).child("notifImg").push();
                                    addNotifImg.setValue(currentProfile.profilePicture + "");
                                    addNotif.setValue(currentProfile.firstName + " " + currentProfile.lastName + " followed you.");
                                }
                            } catch (NullPointerException e){
                                DatabaseReference addFollow = mDatabase.child(uid).child("followers").push();
                                addFollow.setValue(currentProfile.uid);
                                DatabaseReference addFollowing = mDatabase.child(currentProfile.uid).child("following").push();
                                addFollowing.setValue(uid);
                                DatabaseReference addNotif = mDatabase.child(uid).child("notifList").push();
                                if (dataSnapshot.child(uid).hasChild("notifImg")){
                                    imgId = dataSnapshot.child(uid).child("notifImg").getChildrenCount() + 1 + "" ;
                                }
                                else{
                                    imgId = 1 +"";
                                    //dataSnapshot.child(uid).child("notifImg").child(imgId);
                                }
                                mDatabase.child(uid).child("notifImg").child(imgId).setValue(currentProfile.profilePicture + "");

//                                DatabaseReference addNotifImg = mDatabase.child(uid).child("notifImg").push();
//                                addNotifImg.setValue(currentProfile.profilePicture + "");
                                addNotif.setValue(currentProfile.firstName + " " + currentProfile.lastName + " followed you.");

                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });
                }
            }
        });


        followers.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(ProfileActivity.this, FollowActivity.class);
                i.putExtra(User.class.getSimpleName(), Parcels.wrap(u));
                i.putExtra("f", "followers");
                startActivity(i);
            }
        });
        following.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(ProfileActivity.this, FollowActivity.class);
                i.putExtra(User.class.getSimpleName(), Parcels.wrap(u));
                i.putExtra("f", "following");
                startActivity(i);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HomePage.class);
                startActivity(intent);
            }
        });


    }

    public void pick(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            progress.setMessage("uploading");
            progress.show();
            Uri uri = data.getData();
            final StorageReference path = storage.child("headers").child(uid);
            path.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(), "Successfully uploaded image", Toast.LENGTH_LONG).show();
                    Glide.with(context).using(new FirebaseImageLoader())
                            .load(path).centerCrop().into(header);
                }
            });
        }
    }

    public void goHome(View view) {
        Intent i = new Intent(ProfileActivity.this,HomePage.class);
        startActivity(i);
    }

    private void setUpDrawer(){
        mDrawerView
                .addView(new DrawerHeader(this.getApplicationContext()))
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_PROFILE))
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_FEED))
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_EVENTS))
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_CREATE))
                .addView(new DrawerMenuItem(this.getApplicationContext(),DrawerMenuItem.DRAWER_MENU_ITEM_MESSAGE))
                .addView(new DrawerMenuItem(this.getApplicationContext(),DrawerMenuItem.DRAWER_MENU_ITEM_NOTIF))
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_LOGOUT));

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    public class getFirebaseData extends AsyncTask<Void, Void, Boolean> {
        List<String> eventIDs;
        Boolean isEventIds;

        @Override
        protected Boolean doInBackground(Void... params) {
            isEventIds = false;
            DatabaseReference evDatabase = FirebaseDatabase.getInstance().getReference("users").child(uid).child("eventsList");
            eventIDs = new ArrayList<>();
            evDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() { };
                    eventIDs = dataSnapshot.getValue(t);
                    if (eventIDs == null) {
                        Log.d("Event IDs", "null");
                        isEventIds = false;
                    } else {
                        isEventIds = true;
                        Log.d("eventIds", eventIDs.toString());
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Cancelled", databaseError.toString());
                }
            });
            return isEventIds;
        }

        @Override
        protected void onPostExecute(Boolean isEventIds) {
            final DatabaseReference savedEvents = FirebaseDatabase.getInstance().getReference("savedEvents");
            final DatabaseReference createdEvents = FirebaseDatabase.getInstance().getReference("CreatedEvents");
            events.clear();
            dates.clear();
            if (eventIDs!=null) {
                savedEvents.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (eventIDs != null) {
                            for (String id : eventIDs) {
                                for (final DataSnapshot evSnapshot : dataSnapshot.getChildren()) {
                                    if (id.equals(evSnapshot.getKey())) {
                                        savedEvents.child(id).child("date").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                                UserEvents e = evSnapshot.getValue(UserEvents.class);
                                                DateProgram date = dataSnapshot2.getValue(DateProgram.class);
                                                date.setDateConstructed(date.getYear(), date.getMonth(), date.getTimezoneOffset(), date.getTime(), date.getMinutes(), date.getSeconds(), date.getHours(), date.getDay(), date.getDate());
                                                e.setDate(date.getDateConstructed());
                                                events.add(e);
                                                Collections.sort(events, new Comparator<UserEvents>() {
                                                    @Override
                                                    public int compare(UserEvents o1, UserEvents o2) {
                                                        if (o1.getDate() == null || o2.getDate() == null)
                                                            return 0;
                                                        return o1.getDate().compareTo(o2.getDate());
                                                    }
                                                });
                                                Collections.reverse(events);
                                                Log.d("Saved Events", events.toString());
                                                dates.add(e.date);
                                                Collections.sort(dates, new Comparator<Date>() {
                                                    @Override
                                                    public int compare(Date o1, Date o2) {
                                                        return o1.compareTo(o2);
                                                    }
                                                });
                                                Collections.reverse(dates);
                                                adapter.notifyItemInserted(events.size() - 1);
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                }
                                        });
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                createdEvents.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot evSnapshot : dataSnapshot.getChildren()) {
                            if (uid.equals((String) evSnapshot.child("uid").getValue())) {
                                UserEvents e = evSnapshot.getValue(UserEvents.class);
                                e.setCreatedByUser(true);
                                events.add(e);
                                Collections.sort(events, new Comparator<UserEvents>() {
                                    @Override
                                    public int compare(UserEvents o1, UserEvents o2) {
                                        if (o1.getDate() == null || o2.getDate() == null)
                                            return 0;
                                        return o1.getDate().compareTo(o2.getDate());
                                    }
                                });
                                Collections.reverse(events);
                                Log.d("Created Events", events.toString());
                                dates.add(e.date);
                                Collections.sort(dates, new Comparator<Date>() {
                                    @Override
                                    public int compare(Date o1, Date o2) {
                                        return o1.compareTo(o2);
                                    }
                                });
                                Collections.reverse(dates);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                events = null;
                eventIDs = null;
                dates = null;
            }
        }
    }
    
}
