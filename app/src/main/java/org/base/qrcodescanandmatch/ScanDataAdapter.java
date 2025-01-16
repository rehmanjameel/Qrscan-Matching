package org.base.qrcodescanandmatch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScanDataAdapter extends RecyclerView.Adapter<ScanDataAdapter.ScanViewHolder> {

    private List<ScanData> scanDataList;

    public ScanDataAdapter(List<ScanData> scanDataList) {
        this.scanDataList = scanDataList;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_data_card, parent, false);
        return new ScanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ScanData data = scanDataList.get(position);
        holder.userNameTV.setText(data.getUserName());
        holder.timestampTextView.setText(data.getTimestamp());
        holder.tagTypeTextView.setText(data.getTagType());
        holder.ctnrTextView.setText(data.getCtnr());
        holder.partNrTextView.setText(data.getPartNr());
        holder.dnrTextView.setText(data.getDnr());
        holder.qtyTextView.setText(data.getQty());
    }

    @Override
    public int getItemCount() {
        return scanDataList.size();
    }

    public static class ScanViewHolder extends RecyclerView.ViewHolder {
        TextView timestampTextView, tagTypeTextView, ctnrTextView, partNrTextView, dnrTextView, qtyTextView,
        userNameTV;

        public ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTV = itemView.findViewById(R.id.userNameTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            tagTypeTextView = itemView.findViewById(R.id.tagTypeTextView);
            ctnrTextView = itemView.findViewById(R.id.ctnrTextView);
            partNrTextView = itemView.findViewById(R.id.partNrTextView);
            dnrTextView = itemView.findViewById(R.id.dnrTextView);
            qtyTextView = itemView.findViewById(R.id.qtyTextView);
        }
    }
}

