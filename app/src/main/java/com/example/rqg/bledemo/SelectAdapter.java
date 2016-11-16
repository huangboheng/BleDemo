package com.example.rqg.bledemo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import fantasy.rqg.blemodule.scan.BleDevice;

/**
 * * Created by rqg on 08/11/2016.
 */

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.BleViewHolder> {

    private BleDevice[] mBleDevices;
    private OnBleClickListener mOnBleClickListener;

    public SelectAdapter(@NotNull OnBleClickListener onBleClickListener) {
        mOnBleClickListener = onBleClickListener;
    }

    @Override
    public BleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ble_device, parent, false);
        return new BleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BleViewHolder holder, int position) {
        final BleDevice bleDevice = mBleDevices[position];

        holder.rssi.setText(String.valueOf(bleDevice.rssi));
        holder.name.setText(bleDevice.name);
        holder.mac.setText(bleDevice.mac);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnBleClickListener != null)
                    mOnBleClickListener.onBleClick(bleDevice);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mBleDevices == null)
            return 0;

        return mBleDevices.length;
    }

    public static class BleViewHolder extends RecyclerView.ViewHolder {
        TextView name, rssi, mac;

        public BleViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            rssi = (TextView) itemView.findViewById(R.id.rssi);
            mac = (TextView) itemView.findViewById(R.id.mac);
        }
    }


    public void setBleDevices(BleDevice[] bleDevices) {
        mBleDevices = bleDevices;

        notifyDataSetChanged();
    }

    public interface OnBleClickListener {
        void onBleClick(BleDevice device);
    }
}
