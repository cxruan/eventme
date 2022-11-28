package com.example.eventme.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.BuildConfig;
import com.example.eventme.R;
import com.example.eventme.models.Event;
import com.example.eventme.models.User;
import com.example.eventme.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventBoxAdapter extends RecyclerView.Adapter<EventBoxAdapter.ViewHolder> {
    private static ClickListener clickListener;
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
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_box, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(mEvents.get(position));
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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameView;
        public TextView costView;
        public TextView dateView;
        public TextView timeView;
        public TextView locationView;
        public TextView sponsorView;
        public TextView distanceView;
        public ImageButton savedButtonView;
        private final FirebaseDatabase mDatabase;
        private final FirebaseAuth mAuth;
        public Boolean saved = false;


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
            distanceView = itemView.findViewById(R.id.distance);
            savedButtonView = itemView.findViewById(R.id.action_save);
        }

        public void bind(Event event) {
            nameView.setText(event.getName());
            costView.setText(event.getCost().toString());
            dateView.setText(Utils.formatDate(event.getDate()));
            timeView.setText(event.getTime());
            locationView.setText(event.getLocation());
            sponsorView.setText(event.getSponsor());
            if (event.getDistanceFromUserLocation() != null) {
                distanceView.setVisibility(View.VISIBLE);
                distanceView.setText(String.format("%.1f km", event.getDistanceFromUserLocation()));
            } else {
                distanceView.setVisibility(View.GONE);
            }

            if (mAuth.getCurrentUser() != null) {
                mDatabase.getReference().child("users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user.getSavedEvents().containsKey(event.getEventId())) {
                            savedButtonView.setImageResource(R.drawable.ic_baseline_turned_in_24);
                            saved = true;
                        } else {
                            savedButtonView.setImageResource(R.drawable.ic_baseline_turned_in_not_24);
                            saved = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                savedButtonView.setOnClickListener(view -> {
                    if (saved) {
                        mDatabase.getReference().child("users").child(mAuth.getUid()).child("savedEvents").child(event.getEventId()).removeValue().addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                saved = false;
                                savedButtonView.setImageResource(R.drawable.ic_baseline_turned_in_not_24);
                                Toast.makeText(view.getContext(), "Event unsaved successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(view.getContext(), "Failed unsaving event", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        mDatabase.getReference().child("users").child(mAuth.getUid()).child("savedEvents").child(event.getEventId()).setValue(true).addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                saved = true;
                                savedButtonView.setImageResource(R.drawable.ic_baseline_turned_in_24);
                                Toast.makeText(view.getContext(), "Event saved successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(view.getContext(), "Failed saving event", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            } else {
                savedButtonView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }
}
