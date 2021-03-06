package com.codepath.engage;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.engage.models.Event;
import com.codepath.engage.models.UserEvents;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by emilyz on 7/31/17.
 */

public class EventDetailsFragment extends Fragment {

    @BindView(R.id.tvHost) TextView tvHost;
    @BindView(R.id.tvEventInfo) TextView tvEventInfo;
    @BindView(R.id.tvEventDescription) TextView tvEventDescription;
    @BindView(R.id.tvEventLocation) TextView tvEventLocation;
    YouTubePlayerSupportFragment youtubeFragment;

    UserEvents currentUpdate;
    Event event;
    Boolean isUserCreated;

    List<String> events;
    String queryTerm;


    //Define a global variable that identifies the name of a file thatcontains the developer's API key.
    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    public static EventDetailsFragment newInstance(UserEvents currentUpdate, Event event, boolean isUserCreated){
        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
        Bundle args = new Bundle();

        if (event != null) {
            args.putParcelable("event", event);
        }
        if (currentUpdate != null){
            args.putParcelable("currentUpdate", currentUpdate);
        }

        args.putBoolean("isUserCreated", isUserCreated);

        eventDetailsFragment.setArguments(args);

        return eventDetailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        events = new ArrayList<String>();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Bundle bundle = getArguments();
        try {
            event = bundle.getParcelable("event");
        } catch (Exception e){
            e.printStackTrace();
            event = null;
        }

        try{
            currentUpdate = bundle.getParcelable("currentUpdate");
        } catch (Exception e){
            e.printStackTrace();
            currentUpdate = null;
        }

        try {
            isUserCreated = bundle.getBoolean("isUserCreated");
        } catch (Exception e){
            e.printStackTrace();
            isUserCreated = false;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        ButterKnife.bind(this,view);
        tvEventDescription.setTypeface(font);
        tvEventInfo.setTypeface(font);
        tvHost.setTypeface(font);
        if (event != null){
            tvEventDescription.setText(event.tvDescription);
            tvEventInfo.setText(event.tvEventInfo);
            if (event.organizerName != null) {
                tvHost.setText(event.organizerName);
            } else if (event.organizer.name != null){
                tvHost.setText(event.organizer.name);
            }
            tvEventLocation.setVisibility(View.GONE);
            queryTerm = event.organizerName;

        } else if (currentUpdate != null) {
            tvEventDescription.setText(currentUpdate.eventDescription);
            tvEventInfo.setText(currentUpdate.eventTime);
            if (currentUpdate.eventAddress != null){
                tvEventLocation.setText(currentUpdate.eventAddress);
            } else if (currentUpdate.eventLocation != null) {
                tvEventLocation.setText(currentUpdate.eventLocation);
            }
            tvHost.setText(currentUpdate.eventHost);
            if (currentUpdate.eventAddress != null){
                tvEventLocation.setText(currentUpdate.eventAddress);
            } else if (currentUpdate.eventLocation != null){
                tvEventLocation.setText(currentUpdate.eventLocation);
            }

        }

        try {
            // This object is used to make YouTube Data API requests. The last argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override the interface and provide a no-op function.

            //Define a global instance of a Youtube object, which will be used to make YouTube Data API requests.
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                @Override
                public void initialize(com.google.api.client.http.HttpRequest request) throws IOException { } }).setApplicationName("youtube-cmdline-search-sample").build();

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            String apiKey = "AIzaSyBf2t2bQwGPJqMF9O0XyQZgz8q77e-Kgz8";
            search.setKey(apiKey);
            search.setQ(queryTerm);
            Log.i("INFO", queryTerm);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null && searchResultList.size() != 0) {
                final SearchResult singleVideo = searchResultList.get(0);
                final ResourceId rId = singleVideo.getId();
                Log.i("INFO",singleVideo.getSnippet().getTitle());
                youtubeFragment = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtubeFragment);
                youtubeFragment.initialize(apiKey,
                        new YouTubePlayer.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                                YouTubePlayer youTubePlayer, boolean b) {
                                // do any work here to cue video, play video, etc.
                                youTubePlayer.cueVideo(rId.getVideoId());
                            }
                            @Override
                            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                                youtubeFragment = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtubeFragment);
                                youtubeFragment.getView().setVisibility(View.INVISIBLE);
                            }
                        });
            } else {
                youtubeFragment = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtubeFragment);
                youtubeFragment.getView().setVisibility(View.INVISIBLE);
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            youtubeFragment = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtubeFragment);
            youtubeFragment.getView().setVisibility(View.INVISIBLE);
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
            youtubeFragment = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtubeFragment);
            youtubeFragment.getView().setVisibility(View.INVISIBLE);
        } catch (Throwable t) {
            t.printStackTrace();
            youtubeFragment = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtubeFragment);
            youtubeFragment.getView().setVisibility(View.INVISIBLE);
        }

        return view;
    }

}
