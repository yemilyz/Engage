package com.codepath.engage.models;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.engage.EventDetailsActivity;
import com.codepath.engage.R;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by calderond on 7/12/17.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder>{
    private List<Event> mEvents;
    private Context context;
    public int recyclerType;
    private List<User> mUsers;
    View recycleView;

    public EventAdapter(List<Event> events, ArrayList<User> users, int i){
        mEvents = events;
        recyclerType = i;
        mUsers = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (recyclerType == 0) {
            recycleView = inflater.inflate(R.layout.item_user,parent,false);
        }
        else {
            recycleView = inflater.inflate(R.layout.event_item,parent,false);
        }
        ViewHolder viewHolder = new ViewHolder(recycleView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (recyclerType == 0){
            User u = mUsers.get(position);
            holder.name.setText(u.firstName + " " + u.lastName);
            Glide.with(context).load(u.profilePicture).centerCrop().into(holder.profileImage);
        }
        else {
            position = holder.getAdapterPosition();
            Event event = mEvents.get(position);
            holder.tvHost.setText(event.organizerName);
            holder.tvEventName.setText(event.tvEventName);
            holder.tvEventInfo.setText(event.tvEventInfo);
            holder.tvDescription.setText(event.tvDescription);
            Glide.with(context).load(event.ivEventImage).centerCrop().into(holder.ivProfileImage);
            //        holder.tvHost.setText(event.organizer.name);
        }
    }

    @Override
    public int getItemCount() {
        if (recyclerType == 0){
            return mUsers.size();
        }
        else{
            return mEvents.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvEventName;
        public TextView tvEventInfo;
        public TextView tvDescription;
        public TextView tvHost;
        public ImageView profileImage;
        public TextView name;
        public ViewHolder(View itemView){
            super(itemView);
            tvHost = (TextView) itemView.findViewById(R.id.tvHost);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvEventName = (TextView) itemView.findViewById(R.id.tvEventName);
            tvEventInfo = (TextView) itemView.findViewById(R.id.tvLocationInfo);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvHost = (TextView) itemView.findViewById(R.id.tvHost);
            profileImage = (ImageView) itemView.findViewById(R.id.profileImage);
            name = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                if (recyclerType == 0){
                    final User u = mUsers.get(position);
                }
                else {
                    final Event event = mEvents.get(position);
                    Intent intent = new Intent(context, EventDetailsActivity.class);
                    intent.putExtra(Event.class.getSimpleName(), Parcels.wrap(event));
                    context.startActivity(intent);
                }
            }
        }
    }

    public Event getEvent(int i){
        return mEvents.get(i);
    }

    public void clear(){
        mEvents.clear();
        mUsers.clear();
        notifyDataSetChanged();
    }
}