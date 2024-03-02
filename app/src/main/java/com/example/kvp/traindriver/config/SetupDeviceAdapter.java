package com.example.kvp.traindriver.config;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.kvp.traindriver.DeviceDescriptor;
import com.example.kvp.traindriver.MainContext;
import com.example.kvp.traindriver.R;
import com.example.kvp.traindriver.btscanner.BluetoothScanActivity;

public class SetupDeviceAdapter extends RecyclerView.Adapter<SetupDeviceAdapter.ViewHolder>
{
    Context context;
    ViewGroup viewGroup;
    MainContext mainContext;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public Spinner spType;
        public Spinner spChannels;
        public EditText etAddress;
        public EditText etName;
        public EditText etPassword;
        public Button btSearch;
        public Button btDelete;
        ArrayAdapter<String> typeAdapter;
        ArrayAdapter<String> channelsAdapter;

        public ViewHolder(View view)
        {
            super(view);
            spType = view.findViewById(R.id.setupDeviceTypeSpinner);
            spChannels = view.findViewById(R.id.setupDeviceChannelsSpinner);
            etAddress = view.findViewById(R.id.setupDeviceAddressText);
            etName = view.findViewById(R.id.setupDeviceNameText);
            etPassword = view.findViewById(R.id.setupDevicePasswordText);
            btSearch = view.findViewById(R.id.setupDeviceSearchButton);
            btDelete = view.findViewById(R.id.setupDeviceDeleteButton);
        }
    }

    public SetupDeviceAdapter(Context context)
    {
        this.context = context;
        mainContext = MainContext.getMainContext(context);
    }

    @Override
    public SetupDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        this.viewGroup = viewGroup;
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_setupdevice, viewGroup, false);
        return new SetupDeviceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position)
    {
        DeviceDescriptor deviceDescriptor = mainContext.getEditableDevices().get(position);

        viewHolder.typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, DeviceDescriptor.getProtocols());
        viewHolder.typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spType.setAdapter(viewHolder.typeAdapter);
        viewHolder.spType.setSelection(DeviceDescriptor.getTypeSelection(deviceDescriptor.protocol));

        viewHolder.channelsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, DeviceDescriptor.getChannels(deviceDescriptor.protocol));
        viewHolder.channelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spChannels.setAdapter(viewHolder.channelsAdapter);
        viewHolder.spChannels.setSelection(DeviceDescriptor.getChannelSelection(deviceDescriptor.protocol, deviceDescriptor.channelSetup));

        viewHolder.spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
            {
                String newProtocol = viewHolder.typeAdapter.getItem(pos);
                if (newProtocol.compareTo(deviceDescriptor.protocol) == 0)
                    return;
                deviceDescriptor.protocol = newProtocol;
                viewHolder.channelsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, DeviceDescriptor.getChannels(deviceDescriptor.protocol));
                viewHolder.spChannels.setAdapter(viewHolder.channelsAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
            }
        });

        viewHolder.spChannels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
            {
                String newChannelSetup = viewHolder.channelsAdapter.getItem(pos);
                if (newChannelSetup.compareTo(deviceDescriptor.channelSetup) == 0)
                    return;
                deviceDescriptor.channelSetup = viewHolder.channelsAdapter.getItem(pos);
                deviceDescriptor.channelCount = DeviceDescriptor.getChannelCount(deviceDescriptor.channelSetup);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
            }
        });

        viewHolder.etAddress.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after)
            {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                String text = charSequence.toString();
                String newAddress = text.toUpperCase();
                if (deviceDescriptor.address.compareTo(newAddress) != 0)
                    deviceDescriptor.address = newAddress;
            }
            @Override
            public void afterTextChanged(Editable editable)
            {
            }
        });

        viewHolder.etName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after)
            {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                deviceDescriptor.name = charSequence.toString();
            }
            @Override
            public void afterTextChanged(Editable editable)
            {
            }
        });
        viewHolder.etPassword.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after)
            {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                deviceDescriptor.password = charSequence.toString();
            }
            @Override
            public void afterTextChanged(Editable editable)
            {
            }
        });

        viewHolder.etAddress.setText(deviceDescriptor.address);
        viewHolder.etName.setText(deviceDescriptor.name);
        viewHolder.etPassword.setText(deviceDescriptor.password);
        viewHolder.btSearch.setOnClickListener(view ->
        {
            Intent intent = new Intent(view.getContext(), BluetoothScanActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("deviceNumber", position);
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
        viewHolder.btDelete.setOnClickListener(view ->
                mainContext.deleteEditableDevice(position));
    }

    @Override
    public int getItemCount ()
    {
        return mainContext.getEditableDevices().size();
    }

}
