package se.akerfeldt.remotestick;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeviceAdapter extends ArrayAdapter<Device> {

  int resource;

  public DeviceAdapter(Context _context, 
                             int _resource, 
                             List<Device> _items) {
    super(_context, _resource, _items);
    resource = _resource;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout deviceView;

    Device item = getItem(position);

    String name = item.getName();
    int id = item.getId();

    if (convertView == null) {
    	deviceView = new LinearLayout(getContext());
      String inflater = Context.LAYOUT_INFLATER_SERVICE;
      LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
      vi.inflate(resource, deviceView, true);
    } else {
    	deviceView = (LinearLayout) convertView;
    }

    TextView nameView = (TextView)deviceView.findViewById(R.id.deviceName);

//    if(item.getLastCommand() == DeviceCommand.ON) {
//        Button onButton = (Button) deviceView.findViewById(R.id.turnOn);
//    } else if(item.getLastCommand() == DeviceCommand.OFF) {
//        Button offButton = (Button) deviceView.findViewById(R.id.turnOff);
//    }
     
    
//    TextView idView = (TextView)deviceView.findViewById(R.id.deviceId);
      
    nameView.setText(name);
//    idView.setText(String.valueOf(id));

    return deviceView;
  }
}