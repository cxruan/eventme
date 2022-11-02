package com.example.eventme.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.R;
import com.example.eventme.models.Event;
import com.example.eventme.utils.Utils;

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
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_box, viewGroup, false);
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

    public void addItem(Event event) {
        mEvents.add(event);
        notifyItemInserted(mEvents.size() - 1);
    }

    public void setItems(List<Event> events) {
        mEvents = events;
        notifyItemRangeChanged(0, mEvents.size());
    }

    public void clearAllItem() {
        int size = mEvents.size();
        mEvents.clear();
        notifyItemRangeRemoved(0, size);
    }

    public Event getItemByPos(int position) {
        return mEvents.get(position);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        EventBoxAdapter.clickListener = clickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameView;
        public TextView costView;
        public TextView dateView;
        public TextView timeView;
        public TextView locationView;
        public TextView sponsorView;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            nameView = itemView.findViewById(R.id.name);
            costView = itemView.findViewById(R.id.cost);
            dateView = itemView.findViewById(R.id.date);
            timeView = itemView.findViewById(R.id.time);
            locationView = itemView.findViewById(R.id.location);
            sponsorView = itemView.findViewById(R.id.sponsor);
        }

        public void bind(Event event) {
            nameView.setText(event.getName());
            costView.setText(event.getCost().toString());
            dateView.setText(Utils.formatDate(event.getDate()));
            timeView.setText(event.getTime());
            locationView.setText(event.getLocation());
            sponsorView.setText(event.getSponsor());
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }


}
