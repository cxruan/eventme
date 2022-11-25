package com.example.eventme.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.BuildConfig;
import com.example.eventme.R;
import com.example.eventme.models.Event;
import com.example.eventme.models.User;
import com.example.eventme.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class EventBoxAdapter extends RecyclerView.Adapter<EventBoxAdapter.ViewHolder> {
    private static ClickListener clickListener;
    private static ClickListener savedClickListener;
    private List<Event> mEvents;

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public EventBoxAdapter() {
        mEvents = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_box, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(mEvents.get(position), position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public void setItems(List<Event> events) {
        mEvents = events;
        notifyDataSetChanged();
    }

    public Event getItemByPos(int position) {
        return mEvents.get(position);
    }

    public List<Event> getAllItems() {
        return mEvents;
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        EventBoxAdapter.clickListener = clickListener;
    }

    public void setOnSavedClickListener(ClickListener clickListener) {
        EventBoxAdapter.savedClickListener = clickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameView;
        public TextView costView;
        public TextView dateView;
        public TextView timeView;
        public TextView locationView;
        public TextView sponsorView;
        public TextView distaneView;
        public ImageButton savedButton;
        private FirebaseDatabase mDatabase;
        private FirebaseAuth mAuth;
        public Boolean saved;
        public int position;


        public ViewHolder(View itemView) {
            super(itemView);

            mDatabase = FirebaseDatabase.getInstance();
            mAuth = FirebaseAuth.getInstance();
            if (BuildConfig.DEBUG) {
                mAuth.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_AUTH_PORT);
                mDatabase.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_DATABASE_PORT);
            }

            itemView.setOnClickListener(this);

            nameView = itemView.findViewById(R.id.name);
            costView = itemView.findViewById(R.id.cost);
            dateView = itemView.findViewById(R.id.date);
            timeView = itemView.findViewById(R.id.time);
            locationView = itemView.findViewById(R.id.location);
            sponsorView = itemView.findViewById(R.id.sponsor);
            distaneView = itemView.findViewById(R.id.distance);
            savedButton = itemView.findViewById(R.id.savedButton);
            savedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(saved){
                        savedButton.setImageResource(R.drawable.ic_baseline_turned_in_not_24);
                        saved = false;
                        savedClickListener.onItemClick(position, view);
                    } else {
                        savedButton.setImageResource(R.drawable.ic_baseline_turned_in_24);
                        saved = true;
                        savedClickListener.onItemClick(position, view);
                    }
                }
            });
        }

        public void bind(Event event) {
            nameView.setText(event.getName());
            costView.setText(event.getCost().toString());
            dateView.setText(Utils.formatDate(event.getDate()));
            timeView.setText(event.getTime());
            locationView.setText(event.getLocation());
            sponsorView.setText(event.getSponsor());
            if (event.getDistanceFromUserLocation() != null) {
                distaneView.setVisibility(View.VISIBLE);
                distaneView.setText(String.format("%.1f km", event.getDistanceFromUserLocation()));
            }else{
                distaneView.setVisibility(View.GONE);
            }
        }

        public void bind(Event event, int position) {
            nameView.setText(event.getName());
            costView.setText(event.getCost().toString());
            dateView.setText(Utils.formatDate(event.getDate()));
            timeView.setText(event.getTime());
            locationView.setText(event.getLocation());
            sponsorView.setText(event.getSponsor());
            if (event.getDistanceFromUserLocation() != null) {
                distaneView.setVisibility(View.VISIBLE);
                distaneView.setText(String.format("%.1f km", event.getDistanceFromUserLocation()));
            }else{
                distaneView.setVisibility(View.GONE);
            }
            this.position = position;

            mDatabase.getReference().child("users").child(mAuth.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    if(user.getSavedEvents().containsKey(event.getEventId())){
                        savedButton.setImageResource(R.drawable.ic_baseline_turned_in_24);
                        saved = true;
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }


}
