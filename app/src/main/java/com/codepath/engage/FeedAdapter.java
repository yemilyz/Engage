package com.codepath.engage;

/**
 * Created by awestort on 7/27/17.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.engage.models.UserEvents;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    public ArrayList<UserEvents> mEvents;
    private Context context;
    private String profilePage;
    public ArrayList<String> feedUsers;


    public FeedAdapter(ArrayList<UserEvents> events, ArrayList<String> users ) {
        mEvents = events;
        feedUsers = users;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // create the view using the item_movie layout
        View updateView = inflater.inflate(R.layout.item_update, parent, false);
        ViewHolder viewHolder = new ViewHolder(updateView);

        return viewHolder;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserEvents e = mEvents.get(position);
        String currentUser = feedUsers.get(position);
        holder.update.setText(currentUser + " is interested in " + e.eventName);
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.notification)
        TextView update;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //get the position of row element
                    int position = getAdapterPosition();
                    // fire the listener callback
                    if (position != RecyclerView.NO_POSITION) {
                        UserEvents currentUpdate = mEvents.get(position);
                        // create intent for the new activity
                        Intent intent = new Intent(context, EventDetailsActivity.class);
                        intent.putExtra("current", Parcels.wrap(currentUpdate));
                        // serialize the movie using parceler, use its short name as a key
                        // show the activity
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {

        }
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

}