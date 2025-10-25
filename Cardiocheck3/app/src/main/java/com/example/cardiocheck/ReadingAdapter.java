package com.example.cardiocheck;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardiocheck.models.BloodPressureReading;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.VH> {

    public interface OnSelectionChangedListener { void onSelectionChanged(int count); }

    private final List<BloodPressureReading> data;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private final OnSelectionChangedListener listener;

    public ReadingAdapter(List<BloodPressureReading> data, OnSelectionChangedListener l) {
        this.data = data != null ? data : new ArrayList<BloodPressureReading>();
        this.listener = l;
    }

    public List<BloodPressureReading> getSelected() {
        List<BloodPressureReading> res = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos >= 0 && pos < data.size()) res.add(data.get(pos));
        }
        return res;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BloodPressureReading r = data.get(position);
        DateFormat df = DateFormat.getDateTimeInstance();
        h.tvDate.setText(df.format(new Date(r.getTimestamp())));
        h.tvValues.setText(r.getSystolic() + "/" + r.getDiastolic() + " mmHg — " + r.getPulse() + " bpm");
        h.tvAI.setText(r.getAiRecommendation() == null ? "" : r.getAiRecommendation());
        h.cb.setChecked(selectedPositions.contains(position));
        h.itemView.setOnClickListener(v -> toggle(position));
        h.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedPositions.add(h.getAdapterPosition());
            else selectedPositions.remove(h.getAdapterPosition());
            if (listener != null) listener.onSelectionChanged(selectedPositions.size());
        });
    }

    private void toggle(int position) {
        if (selectedPositions.contains(position)) selectedPositions.remove(position);
        else selectedPositions.add(position);
        notifyItemChanged(position);
        if (listener != null) listener.onSelectionChanged(selectedPositions.size());
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb; TextView tvDate; TextView tvValues; TextView tvAI;
        VH(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbSelect);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvValues = itemView.findViewById(R.id.tvValues);
            tvAI = itemView.findViewById(R.id.tvAI);
        }
    }
}

