package se.akerfeldt.remotestick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Tellstick extends ListActivity implements OnGestureListener {

	// private List<Device> devices;

	private ArrayAdapter<Device> aa;

	private SAXParserFactory factory;

	private static final int DELETE_ID = Menu.FIRST;
	private static final int MODIFY_ID = Menu.FIRST + 1;

	private static final int REFRESH_DEVICES = Menu.FIRST;
	private static final int ADD_DEVICE = Menu.FIRST + 1;
	private static final int TYPE_NEXA_CODESWITCH = Menu.FIRST + 2;
	private static final int TYPE_NEXA_SELFLEARNING_SWITCH = Menu.FIRST + 3;

	public static final int REQUEST_CREATE_CODESWITCH = 0;

	// public static final String address = "83.227.184.76:8000";
	public static final String address = "192.168.1.10:8000";

	/* Swipe related stuff */
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	private ViewFlipper viewFlipper;

	private GestureDetector gestureDetector;

	private ListView activeList;

	private ListView inactiveList;

	private List<Controller> controllers;
	private LinkedList<Controller> myControllers;
	private int currentController;
	private int numControllers;

	private TellstickDbAdapter dbAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new TellstickDbAdapter(this);
		dbAdapter.open();
		
//		dbAdapter.insertController("Test", "http://192.168.1.104:8001", "test",
//				"test");
		controllers = dbAdapter.getAllControllers();
//		for (Controller controller : controllers) {
//			dbAdapter.removeController(controller.getId());
//		}
//		controllers.clear();
		dbAdapter.close();
		currentController = 0;
//		if (controllers.isEmpty())
//			Log.v("tellremote", "empty controllers");
//		int i = 1;
//		for (Controller controller : controllers) {
//			ArrayList<Device> myDevices = new ArrayList<Device>();
//			Random generator = new Random(19580427);
//			int num = generator.nextInt(6) + 1;
//			for (int y = 1; y <= num; y++) {
//				Random r = new Random();
//				String token = Long.toString(Math.abs(r.nextLong()), 36);
//				myDevices.add(new Device(y, token, DeviceCommand.OFF,
//						new ArrayList<Integer>()));
//			}
//			Log.v("tellremote", myDevices.toString());
//			controller.setDevices(myDevices);
//			Log.v("tellremote", controller.getName());
//			i++;
//		}
		numControllers = controllers.size();
		gestureDetector = new GestureDetector(this);

		setContentView(R.layout.main);

		inactiveList = (ListView) findViewById(R.id.secondListView);
		Log.v("tellremote", inactiveList.getClass().getCanonicalName());
		activeList = (ListView) findViewById(android.R.id.list);
		activeList.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		// devices = getDevices();

		// Log.v("tellremote", devices.toString());
		int resID = R.layout.device_item;

		int controllerIndex = 0;
		if (savedInstanceState != null)
			savedInstanceState.getInt("controller", 0);
		if (controllers.isEmpty()) {
			/* Do something clever */

		} else {
			Controller controller;
			if (controllerIndex > controllers.size() - 1) {
				/*
				 * We have controllers but the last used controller has been
				 * removed, default to first
				 */
				controller = controllers.get(0);
			} else {
				/* Use previously used controller */
				controller = controllers.get(controllerIndex);
			}
			controller.refresh();
			aa = new DeviceAdapter(this, resID, controller.getDevices());
			activeList.setAdapter(aa);
			registerForContextMenu(activeList);
		}

		viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		// USE THIS TO GET THE CURRENT VIEW IN OPTIONS MENU:
		// viewFlipper.getCurrentView()
		// USE THIS INSTEAD: viewFlipper.setInAnimation(context, resourceID)
		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils
				.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils
				.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this,
				R.anim.slide_right_out);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemRefresh = menu.add(0, REFRESH_DEVICES, Menu.NONE,
				R.string.refresh);
		SubMenu menuAdd = menu.addSubMenu(0, ADD_DEVICE, Menu.NONE,
				R.string.new_device);

		/* TEMPORARY */
		MenuItem mi = menu.add(2, TYPE_NEXA_SELFLEARNING_SWITCH + 1, Menu.NONE,
				"Add Controller");
		mi.setIcon(R.drawable.ic_menu_add);

		menuAdd.add(0, TYPE_NEXA_CODESWITCH, Menu.NONE,
				R.string.nexa_codeswitch);
		menuAdd.add(1, TYPE_NEXA_SELFLEARNING_SWITCH, Menu.NONE,
				R.string.nexa_selflearning_switch);

		// Assign icons
		itemRefresh.setIcon(R.drawable.ic_menu_refresh);
		menuAdd.setIcon(R.drawable.ic_menu_add);

		// Allocate shortcuts to each of them.
		itemRefresh.setShortcut('0', 'r');
		// menuAdd.setShortcut('1', 'a');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		Log.v("tellremote", "Menu clicked!");
		// int index = getListView().getSelectedItemPosition();

		switch (item.getItemId()) {
		case (REFRESH_DEVICES):
			controllers.get(currentController).refresh();
			aa.notifyDataSetChanged();
			return true;

		case (TYPE_NEXA_CODESWITCH):
			Intent myIntent = new Intent(this, EditCodeswitch.class);
			myIntent.putExtra("REQUEST_CODE", REQUEST_CREATE_CODESWITCH);
			startActivityForResult(myIntent, REQUEST_CREATE_CODESWITCH);
			return true;

		case (TYPE_NEXA_SELFLEARNING_SWITCH + 1):
			Intent i = new Intent(this, EditController.class);
			startActivityForResult(i, 911);
			return true;
		}

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.v("tellremote", "onCreateContextMenu");

		ListView lw = (ListView) findViewById(android.R.id.list);
		Log.v("tellremote", v.getClass().getCanonicalName());
		// int itemPosition = lw.getPositionForView(v);
		// Device device = aa.getItem(itemPosition);
		//
		menu.setHeaderTitle("Device");
		menu.add(0, DELETE_ID, 0, "Delete");
		menu.add(0, MODIFY_ID, 0, "Modify");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.v("tellremote", "onContextItemSelected");
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterView.AdapterContextMenuInfo menuInfo;
			menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			int index = menuInfo.position;
			Device device = aa.getItem(index);
			Log.v("tellremote", "delete device " + device.getName());
			deleteDevice(device);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v("tellremote", String.valueOf(resultCode));
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CREATE_CODESWITCH:
				if (data.hasExtra("name") && data.hasExtra("model")
						&& data.hasExtra("protocol") && data.hasExtra("house")
						&& data.hasExtra("unit")) {
					String name = data.getCharSequenceExtra("name").toString();
					String model = data.getStringExtra("model");
					String protocol = data.getStringExtra("protocol");
					String house = data.getStringExtra("house");
					Integer unit = data.getIntExtra("unit", -1);
					if (unit == -1) {
						/* TODO: Show error dialog */
						return;
					}
					Map<String, String> params = new HashMap<String, String>();
					params.put("house", house);
					params.put("unit", String.valueOf(unit));
					Device retval = controllers.get(currentController)
							.createDevice(name, model, protocol, params);
					if (retval != null) {
						aa.notifyDataSetChanged();
					}
					Log.v("tellremote", "createDevice returned "
							+ String.valueOf(retval));
				}
				Log.v("tellremote", String.valueOf(data.hasExtra("unit")));
				Integer unit = data.getIntExtra("unit", -1);
				if (unit == null)
					Log.v("tellremote", "unit = <null>");
				else
					Log.v("tellremote", "unit = " + unit);
				break;

			case 911:
				if (data.hasExtra("name") && data.hasExtra("uri")
						&& data.hasExtra("apikey")) {
					String name = data.getStringExtra("name");
					String uri = data.getStringExtra("uri");
					String username = data.getStringExtra("username");
					String password = data.getStringExtra("password");

					TellstickDbAdapter dbAdapter = new TellstickDbAdapter(this);
					dbAdapter.open();
					long identifier = dbAdapter.insertController(name, uri,
							username, password);
					if (identifier != -1) {
						Controller controller = dbAdapter
								.getController(identifier);
						controllers.add(controller);
						numControllers = controllers.size();
						
						/* FIXME: Call handleNext() when that method has been fixed? */
						if(currentController == -1) {
							currentController = controller.getId();
						}
						// ok
					} else {
						Log
								.e("tellremote",
										"Un error occurred while inserting information into database.");
					}
					Cursor cursor = dbAdapter.getAllControllersCursor();
					cursor.requery();

					if (cursor.moveToFirst()) {
						do {
							String name1 = cursor
									.getString(cursor
											.getColumnIndex(TellstickDbAdapter.KEY_NAME));
							String uri1 = cursor
									.getString(cursor
											.getColumnIndex(TellstickDbAdapter.KEY_URI));
							String username1 = cursor
									.getString(cursor
											.getColumnIndex(TellstickDbAdapter.KEY_USERNAME));
							String password1 = cursor
									.getString(cursor
											.getColumnIndex(TellstickDbAdapter.KEY_PASSWORD));
							Log.v("tellremote", "Found controller " + name1
									+ ", " + uri1 + ", " + username1 + ", "
									+ password1);
						} while (cursor.moveToNext());
					}
					cursor.deactivate();
					dbAdapter.close();

				}

				break;
			}
		}
	}

	public void turnOn(View v) {
		int itemPosition = activeList.getPositionForView(v);
		Device device = aa.getItem(itemPosition);
		boolean result = controllers.get(currentController).turnOn(device);
		Log.v("tellremote", "Turn on device: " + String.valueOf(result));

		// get the row the clicked button is in

		// RelativeLayout vwParentRow = (RelativeLayout) v.getParent();
		/*
		 * get the 2nd child of our ParentRow (remember in java that arrays
		 * start with zero, so our 2nd child has an index of 1)
		 */

		// View childAt = vwParentRow.getChildAt(0);
		// Log.v("tellremote", childAt.getClass().getCanonicalName());
		// Button btnChild = (Button) vwParentRow.getChildAt(1);
		//
		// // now set the text of our button
		// btnChild.setText("I've been clicked!");
		//
		// // .. and change the colour of our row
		// int c = Color.CYAN;
		//
		// vwParentRow.setBackgroundColor(c);

		// and redraw our row to reflect our colour change
		// vwParentRow.refreshDrawableState();
	}

	public void turnOff(View v) {
		int itemPosition = activeList.getPositionForView(v);
		Device device = aa.getItem(itemPosition);
		boolean result = controllers.get(currentController).turnOff(device);
		Log.v("tellremote", "Turn off device: " + String.valueOf(result));
	}

	private boolean deleteDevice(Device device) {
		// TellRemoteServerHandler handler = new TellRemoteServerHandler();
		// try {
		// SAXParser parser = factory.newSAXParser();
		// HttpClient client = new DefaultHttpClient();
		// String url = "http://" + address + "/devices/" + device.getId()
		// + "?apikey=520397fa-5357-4285-b37a-5dad54702a01";
		// HttpDelete request = new HttpDelete(url);
		// HttpResponse response = client.execute(request);
		// parser.parse(response.getEntity().getContent(), handler);
		// if (response.getStatusLine().getStatusCode() == 200) {
		// devices.remove(device);
		// aa.notifyDataSetChanged();
		// return true;
		// } else
		// return false;
		// } catch (Exception e) {
		// Log.v("tellremote", "Exception " + e.getMessage());
		// e.printStackTrace();
		// return false;
		// }
		return false;
	}

	private boolean refresh() {
		// TellRemoteServerHandler handler = new TellRemoteServerHandler();
		// try {
		// SAXParser parser = factory.newSAXParser();
		// HttpClient client = new DefaultHttpClient();
		// String url = "http://" + address
		// + "/devices/?apikey=520397fa-5357-4285-b37a-5dad54702a01";
		// HttpGet request = new HttpGet(url);
		// HttpResponse response = client.execute(request);
		// parser.parse(response.getEntity().getContent(), handler);
		// if (response.getStatusLine().getStatusCode() == 200) {
		// devices.clear();
		// devices.addAll(handler.getDevices());
		// aa.notifyDataSetChanged();
		// return true;
		// } else
		// return false;
		// } catch (Exception e) {
		// Log.v("tellremote", "Exception " + e.getMessage());
		// e.printStackTrace();
		// return false;
		// }
		return false;

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			// right to left swipe
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				handleNext();
				viewFlipper.setInAnimation(slideLeftIn);
				viewFlipper.setOutAnimation(slideLeftOut);
				viewFlipper.showNext();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				handlePrevious();
				viewFlipper.setInAnimation(slideRightIn);
				viewFlipper.setOutAnimation(slideRightOut);
				viewFlipper.showPrevious();

			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	private void handleNext() {
		/* FIXME: This won't work! We can't guarantee that the controller ids are 1,2,3,4 */
		currentController++;
		if (currentController >= numControllers)
			currentController = 0;

		Controller controller = controllers.get(currentController);

		View emptyLabel = findViewById(R.id.emptyLabel);
		if (controller != null && !controller.getDevices().isEmpty()) {
			emptyLabel.setVisibility(TextView.INVISIBLE);
			int resID = R.layout.device_item;
			aa = new DeviceAdapter(this, resID, controller.getDevices());
			inactiveList.setAdapter(aa);
			ListView tmp = activeList;
			activeList = inactiveList;
			inactiveList = tmp;
		} else {
			emptyLabel.setVisibility(TextView.VISIBLE);
		}

		// devices = getDevices();
		//
		// Log.v("tellremote", devices.toString());
		// int resID = R.layout.device_item;
		//
		// aa = new DeviceAdapter(this, resID, devices);
		//
		// inactiveList.setAdapter(aa);
	}

	private void handlePrevious() {
		currentController--;
		if (currentController < 0)
			currentController = numControllers - 1;

		Controller controller = controllers.get(currentController);

		View emptyLabel = findViewById(R.id.emptyLabel);
		if (controller != null && !controller.getDevices().isEmpty()) {
			emptyLabel.setVisibility(TextView.INVISIBLE);
			int resID = R.layout.device_item;
			aa = new DeviceAdapter(this, resID, controller.getDevices());
			inactiveList.setAdapter(aa);
			ListView tmp = activeList;
			activeList = inactiveList;
			inactiveList = tmp;
		} else {
			emptyLabel.setVisibility(TextView.VISIBLE);
		}
	}

	private List<Device> getDevices() {
		Log.v("tellremote", "Connecting...");
		factory = SAXParserFactory.newInstance();

		TellRemoteServerHandler handler = new TellRemoteServerHandler();
		try {
			SAXParser parser = factory.newSAXParser();
			HttpClient client = new DefaultHttpClient();
			String url = "http://" + address
					+ "/devices/?apikey=520397fa-5357-4285-b37a-5dad54702a01";
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			Log.v("tellremote", "parsing...");
			parser.parse(response.getEntity().getContent(), handler);

		} catch (Exception e) {
			Log.v("tellremote", "Exception " + e.getMessage());
			e.printStackTrace();
		}

		return handler.getDevices();
	}
}