package se.akerfeldt.remotestick;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class DeviceAdapter extends ArrayAdapter<Device> {

	int resource;
	private final OnSeekBarChangeListener seekBarChangeListener;

	public DeviceAdapter(Context context, List<Device> items, OnSeekBarChangeListener seekBarChangeListener) {
		super(context, 0, items);
		this.seekBarChangeListener = seekBarChangeListener;
	}

	@Override
	public int getItemViewType(int position) {
		Device item = getItem(position);
		List<Integer> supportedMethods = item.getSupportedMethods();
		if (supportedMethods.contains(Device.TELLSTICK_DIM)) {
			return 1;
		} else if (supportedMethods.contains(Device.TELLSTICK_TURNON) && supportedMethods.contains(Device.TELLSTICK_TURNOFF)) {
			return 0;
		} else
			// Default
			return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout deviceView;

		Device item = getItem(position);

		int resource;

		List<Integer> supportedMethods = item.getSupportedMethods();
		if (supportedMethods.contains(Device.TELLSTICK_DIM)) {
			resource = R.layout.device_dim;
		} else if (supportedMethods.contains(Device.TELLSTICK_TURNON) && supportedMethods.contains(Device.TELLSTICK_TURNOFF)) {
			resource = R.layout.device_item;
		} else
			// Default
			resource = R.layout.device_item;

		String name = item.getName();

		if (convertView == null) {
			deviceView = new LinearLayout(getContext());
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			vi.inflate(resource, deviceView, true);

			if (resource == R.layout.device_dim) {
				// TODO: Could this be done more efficiently?
				SeekBar dimBar = (SeekBar) deviceView.findViewById(R.id.dimBar);
				dimBar.setOnSeekBarChangeListener(seekBarChangeListener);
			}
		} else {
			deviceView = (LinearLayout) convertView;
		}

		TextView nameView = (TextView) deviceView.findViewById(R.id.deviceName);

		nameView.setText(name);

		return deviceView;
	}
}