package com.codepath.engage.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by calderond on 7/11/17.
 */

public class Event implements Parcelable {
    public String tvEventName;
    public String tvEventInfo;
    public String tvDescription;
    public String ivEventImage;
    public String organizerName;
    public String eventId;
    public String venueId;
    public String organizerId;
    public Venue venue;
    public Organizer organizer;
    public String timeStart;
    boolean createdEvent;

    public boolean isCreatedEvent() {
        return createdEvent;
    }

    public void setCreatedEvent(boolean createdEvent) {
        this.createdEvent = createdEvent;
    }

    public Event(String tvEventName, String tvEventInfo, String tvDescription, String ivEventImage, String eventId,String organizerName) {
        this.tvEventName = tvEventName;
        this.tvEventInfo = tvEventInfo;
        this.tvDescription = tvDescription;
        this.ivEventImage = ivEventImage;
        this.eventId = eventId;
        this.organizerName = organizerName;
        venueId = "";
        organizerId="";
        venue = null;
        organizer = null;
        timeStart = tvEventInfo;
    }

    public Event(String tvEventName, String tvEventInfo, String tvDescription, String ivEventImage, String eventId, String veneuId, Venue venue, Organizer organizer, String timeStart) {
        this.tvEventName = tvEventName;
        this.tvEventInfo = tvEventInfo;
        this.tvDescription = tvDescription;
        this.ivEventImage = ivEventImage;
        this.eventId = eventId;
        this.venueId = veneuId;
        this.timeStart = timeStart;
    }

    public Event() {
    }

    protected Event(Parcel in) {
        tvEventName = in.readString();
        tvEventInfo = in.readString();
        tvDescription = in.readString();
        ivEventImage = in.readString();
        organizerName = in.readString();
        eventId = in.readString();
        venueId = in.readString();
        organizerId = in.readString();
        venue = in.readParcelable(Venue.class.getClassLoader());
        organizer = in.readParcelable(Organizer.class.getClassLoader());
        timeStart = in.readString();
    }


    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public static Event fromJSON(JSONObject jsonObject)throws JSONException {
        Event event = new Event();
        //Getting the name of the event
        JSONObject nameEvent = jsonObject.getJSONObject("name");
        event.tvEventName = nameEvent.getString("text");
        //Getting the description for the event Time and location only
        JSONObject eventInfo = jsonObject.getJSONObject("start");
        event.tvEventInfo = eventInfo.getString("utc");
        SimpleDateFormat existingUTCFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat requiredFormat = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa");
        event.eventId = jsonObject.getString("id");
        try{
            Date getDate = existingUTCFormat.parse(event.tvEventInfo);
            String mydate = requiredFormat.format(getDate);
            event.tvEventInfo = mydate;
            event.timeStart = mydate;
        }
        catch(ParseException e){
            e.printStackTrace();
        }
        //Getting the description of the event
        JSONObject eventDescription = jsonObject.getJSONObject("description");
        event.tvDescription = eventDescription.getString("text");
        //Getting a thumbnail of the image for future use.
        try{
            jsonObject.getJSONObject("logo");
            JSONObject logo = jsonObject.getJSONObject("logo");
            JSONObject original= logo.getJSONObject("original");
            event.ivEventImage = original.getString("url");
            Log.i("Ingo",event.ivEventImage);
        }
        catch (Exception exception){
            event.ivEventImage = "null";
            Log.d("Ingo", "null");
        }
        event.venueId = jsonObject.getString("venue_id");
        event.organizerId = jsonObject.getString("organizer_id");
        return event;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getTvEventName() {
        return tvEventName;
    }

    public void setTvEventName(String tvEventName) {
        this.tvEventName = tvEventName;
    }

    public String getTvEventInfo() {
        return tvEventInfo;
    }

    public void setTvEventInfo(String tvEventInfo) {
        this.tvEventInfo = tvEventInfo;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public String getTvDescription() {
        return tvDescription;
    }

    public void setTvDescription(String tvDescription) {
        this.tvDescription = tvDescription;
    }

    public String getIvEventImage() {
        return ivEventImage;
    }

    public void setIvEventImage(String ivEventImage) {
        this.ivEventImage = ivEventImage;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getTimeStart() {
        return timeStart;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tvEventName);
        dest.writeString(tvEventInfo);
        dest.writeString(tvDescription);
        dest.writeString(ivEventImage);
        dest.writeString(organizerName);
        dest.writeString(eventId);
        dest.writeString(venueId);
        dest.writeString(organizerId);
        dest.writeParcelable( this.venue,flags);
        dest.writeParcelable(this.organizer,flags);
        dest.writeString(timeStart);
    }
}

