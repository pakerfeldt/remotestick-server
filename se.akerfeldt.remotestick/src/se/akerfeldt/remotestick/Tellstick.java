package se.akerfeldt.remotestick;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Tellstick extends Activity implements OnGestureListener, OnSeekBarChangeListener {

	// private List<Device> devices;

	private ArrayAdapter<Device> aa;

	private static final int DELETE_ID = Menu.FIRST;
	private static final int MODIFY_ID = Menu.FIRST + 1;

	private static final int TYPE_NEXA_CODESWITCH = Menu.FIRST + 5;
	private static final int TYPE_NEXA_SELFLEARNING_SWITCH = Menu.FIRST + 6;

	private static final int INTENT_TYPE_ADD_CONTROLLER = 0;
	private static final int INTENT_TYPE_EDIT_CONTROLLER = 1;
	public static final int INTENT_TYPE_CREATE_CODESWITCH = 2;

	private static final int SLIDE_OFF = 0;
	private static final int SLIDE_RIGHT = 1;
	private static final int SLIDE_LEFT = 2;

	// public static final String address = "83.227.184.76:8000";
	// public static final String address = "192.168.1.10:8000";

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

	// private ListView activeList;
	// private ListView inactiveList;

	private List<Controller> controllers;
	private int currentController = -1;
	private int numControllers = 0;

	private TellstickDbAdapter dbAdapter;

	// private View activeEmptyLabel;
	// private View inactiveEmptyLabel;

	private LinearLayout activeLayout;
	private LinearLayout inactiveLayout;

	private Handler handler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("tellremote", "onCreate!");
		handler = new Handler();
		dbAdapter = new TellstickDbAdapter(this);
		dbAdapter.open();

		// dbAdapter.insertController("Test", "http://192.168.1.104:8001",
		// "test",
		// "test");
		controllers = dbAdapter.getAllControllers();
		// for (Controller controller : controllers) {
		// dbAdapter.removeController(controller.getId());
		// }
		// controllers.clear();
		dbAdapter.close();
		// currentController = -1;
		// if (controllers.isEmpty())
		// Log.v("tellremote", "empty controllers");
		// int i = 1;
		// for (Controller controller : controllers) {
		// ArrayList<Device> myDevices = new ArrayList<Device>();
		// Random generator = new Random(19580427);
		// int num = generator.nextInt(6) + 1;
		// for (int y = 1; y <= num; y++) {
		// Random r = new Random();
		// String token = Long.toString(Math.abs(r.nextLong()), 36);
		// myDevices.add(new Device(y, token, DeviceCommand.OFF,
		// new ArrayList<Integer>()));
		// }
		// Log.v("tellremote", myDevices.toString());
		// controller.setDevices(myDevices);
		// Log.v("tellremote", controller.getName());
		// i++;
		// }
		numControllers = controllers.size();
		Log.v("tellremote", "Size of controllers = " + numControllers);
		gestureDetector = new GestureDetector(this);

		setContentView(R.layout.main);
		activeLayout = (LinearLayout) findViewById(R.id.layout1);
		inactiveLayout = (LinearLayout) findViewById(R.id.layout2);

		// activeEmptyLabel = findViewById(R.id.emptyLabel1);
		// inactiveEmptyLabel = findViewById(R.id.emptyLabel2);
		// activeEmptyLabel.setVisibility(View.INVISIBLE);
		// inactiveEmptyLabel.setVisibility(View.INVISIBLE);

		ListView activeList = (ListView) activeLayout.findViewById(R.id.list);
		ListView inactiveList = (ListView) inactiveLayout.findViewById(R.id.list);
		activeList.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
		registerForContextMenu(activeList);
		registerForContextMenu(inactiveList);

		// devices = getDevices();

		// Log.v("tellremote", devices.toString());
		int resID = R.layout.device_item;

		if (savedInstanceState != null) {
			currentController = savedInstanceState.getInt("controller", 0);
			Log.v("tellremote", "Using controller " + currentController);
		} else {
			currentController = 0;
			Log.v("tellremote", "savedInstanceState is null, defaulting to controller 0");
		}
		if (controllers.isEmpty()) {
			/* Do something clever */
			Log.v("tellremote", "No controller exists.");
		} else {
			Controller controller;
			if (currentController > controllers.size() - 1) {
				/*
				 * We have controllers but the last used controller has been
				 * removed, default to first
				 */
				Log.v("tellremote", "Current controller does not exist. Defaulting to 0");
				controller = controllers.get(0);
			} else {
				/* Use previously used controller */
				controller = controllers.get(currentController);
				Log.v("tellremote", "Previous controller used.");
			}
			showCurrentController(SLIDE_OFF, true);
			// Response response = controller.refresh();
			// if (!response.isOk())
			// showErrorDialog(response);
			// aa = new DeviceAdapter(this, resID, controller.getDevices());
			// activeList.setAdapter(aa);
		}

		viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		// USE THIS TO GET THE CURRENT VIEW IN OPTIONS MENU:
		// viewFlipper.getCurrentView()
		// USE THIS INSTEAD: viewFlipper.setInAnimation(context, resourceID)
		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tellstickmenu, menu);

		// // Create and add new menu items.
		// MenuItem itemRefresh = menu.add(0, REFRESH_DEVICES, Menu.NONE,
		// R.string.refresh);
		// SubMenu menuAdd = menu.addSubMenu(0, ADD_DEVICE, Menu.NONE,
		// R.string.new_device);
		//
		// /* TEMPORARY */
		// MenuItem mi = menu.add(2, ADD_CONTROLLER, Menu.NONE,
		// "Add Controller");
		// mi.setIcon(R.drawable.ic_menu_add);
		//
		// mi = menu.add(2, REMOVE_CONTROLLER, Menu.NONE, "Remove Controller");
		// mi.setIcon(R.drawable.ic_menu_delete);
		//
		// mi = menu.add(2, EDIT_CONTROLLER, Menu.NONE, "Edit Controller");
		// mi.setIcon(R.drawable.ic_menu_edit);
		//
		// menuAdd.add(0, TYPE_NEXA_CODESWITCH, Menu.NONE,
		// R.string.nexa_codeswitch);
		// menuAdd.add(1, TYPE_NEXA_SELFLEARNING_SWITCH, Menu.NONE,
		// R.string.nexa_selflearning_switch);
		//
		// // Assign icons
		// itemRefresh.setIcon(R.drawable.ic_menu_refresh);
		// menuAdd.setIcon(R.drawable.ic_menu_add);
		//
		// // Allocate shortcuts to each of them.
		// itemRefresh.setShortcut('0', 'r');
		// // menuAdd.setShortcut('1', 'a');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.refreshDevices:
			if (currentController != -1) {
				refreshCurrentController();
			}
			return true;

		case TYPE_NEXA_CODESWITCH:
			Intent myIntent = new Intent(this, EditCodeswitch.class);
			myIntent.putExtra("REQUEST_CODE", INTENT_TYPE_CREATE_CODESWITCH);
			startActivityForResult(myIntent, INTENT_TYPE_CREATE_CODESWITCH);
			return true;

		case R.id.addController:
			Intent addControllerIntent = new Intent(this, EditController.class);
			startActivityForResult(addControllerIntent, INTENT_TYPE_ADD_CONTROLLER);
			return true;

		case R.id.editController:
			Intent editControllerIntent = new Intent(this, EditController.class);
			Controller controller = controllers.get(currentController);
			editControllerIntent.putExtra("name", controller.getName());
			editControllerIntent.putExtra("uri", controller.getUri());
			editControllerIntent.putExtra("username", controller.getUsername());
			editControllerIntent.putExtra("password", controller.getPassword());
			editControllerIntent.putExtra("identifier", controller.getId());
			startActivityForResult(editControllerIntent, INTENT_TYPE_EDIT_CONTROLLER);
			return true;

		case R.id.removeController:
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Remove controller").setMessage(
					"Do you really want to remove this controller?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dbAdapter.open();
					Controller controller = controllers.get(currentController);
					dbAdapter.removeController(controller.getId());
					dbAdapter.close();
					controller.dispose();
					controllers.remove(currentController);
					aa.notifyDataSetChanged();
					numControllers--;
					if (numControllers == 0) {
						Log.v("tellremote", "No more controllers");
						// Do something useful
					} else {
						if (currentController == 0) {
							showCurrentController(SLIDE_RIGHT, false);
						} else {
							currentController--;
							showCurrentController(SLIDE_LEFT, false);
						}
					}

				}

			}).setNegativeButton("No", null).show();

			return true;
		}

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
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
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterView.AdapterContextMenuInfo menuInfo;
			menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			int index = menuInfo.position;
			Device device = aa.getItem(index);
			Log.v("tellremote", "delete device " + device.getName());
			if (controllers.get(currentController).delete(device)) {
				aa.notifyDataSetChanged();
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v("tellremote", "Result code = " + String.valueOf(resultCode) + ", requestcode = " + requestCode);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case INTENT_TYPE_CREATE_CODESWITCH:
				if (data.hasExtra("name") && data.hasExtra("model") && data.hasExtra("protocol") && data.hasExtra("house") && data.hasExtra("unit")) {
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
					Device retval = controllers.get(currentController).createDevice(name, model, protocol, params);
					if (retval != null) {
						aa.notifyDataSetChanged();
					}
					Log.v("tellremote", "createDevice returned " + String.valueOf(retval));
				}
				Log.v("tellremote", String.valueOf(data.hasExtra("unit")));
				Integer unit = data.getIntExtra("unit", -1);
				if (unit == null)
					Log.v("tellremote", "unit = <null>");
				else
					Log.v("tellremote", "unit = " + unit);
				break;

			case INTENT_TYPE_ADD_CONTROLLER:
			case INTENT_TYPE_EDIT_CONTROLLER:
				if (data.hasExtra("name") && data.hasExtra("uri") && data.hasExtra("username") && data.hasExtra("password")) {
					String name = data.getStringExtra("name");
					String uri = data.getStringExtra("uri");
					String username = data.getStringExtra("username");
					String password = data.getStringExtra("password");

					if (requestCode == INTENT_TYPE_ADD_CONTROLLER) {
						dbAdapter.open();
						long identifier = dbAdapter.insertController(name, uri, username, password);
						dbAdapter.close();
						Log.v("tellremote", "insertController returned " + identifier);
						if (identifier != -1) {
							Controller controller = dbAdapter.getController(identifier);
							controllers.add(controller);
							currentController = controllers.indexOf(controller);
							numControllers = controllers.size();
						} else {
							Log.e("tellremote", "An error occurred while inserting information into database.");
						}
					} else if (requestCode == INTENT_TYPE_EDIT_CONTROLLER) {
						long identifier = data.getLongExtra("identifier", -1);
						if (identifier != -1) {
							dbAdapter.open();
							Log.v("tellremote", "name = " + name);
							if (dbAdapter.updateController(identifier, name, uri, username, password)) {
								Controller controller = dbAdapter.getController(identifier);
								dbAdapter.close();
								controllers.add(currentController, controller);
							} else {
								dbAdapter.close();
								// TODO: show error
								return;
							}
						}
					}
					showCurrentController(SLIDE_OFF, true);
				}

				break;

			}
		}
	}

	private void showErrorDialog(Response response) {
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error").setMessage(
				"(" + response.getResponseCode() + ") " + response.getErrorMsg()).setNeutralButton("OK", null).show();
	}

	public void turnOn(View v) {
		ListView activeList = (ListView) activeLayout.findViewById(R.id.list);
		int itemPosition = activeList.getPositionForView(v);
		Device device = aa.getItem(itemPosition);
		Response response = controllers.get(currentController).turnOn(device);
		if (!response.isOk())
			showErrorDialog(response);
	}

	public void turnOff(View v) {
		ListView activeList = (ListView) activeLayout.findViewById(R.id.list);
		int itemPosition = activeList.getPositionForView(v);
		Device device = aa.getItem(itemPosition);
		Response response = controllers.get(currentController).turnOff(device);
		if (!response.isOk())
			showErrorDialog(response);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			// right to left swipe
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Log.v("tellremote", "fling next");
				handleNext();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Log.v("tellremote", "fling previous");
				handlePrevious();

			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	private void refreshCurrentController() {
		((TextView) activeLayout.findViewById(R.id.emptyLabel)).setVisibility(TextView.INVISIBLE);

		final ProgressDialog pd = ProgressDialog.show(this, "Refreshing devices", "Please wait while devices are being fetched.");

		new Thread() {
			public void run() {
				Controller controller = controllers.get(currentController);
				final Response response = controller.refresh();
				handler.post(new Runnable() {
					public void run() {
						// Creating dialogs and modifying the data sets needs to
						// be done on the GUI thread.
						if (!response.isOk())
							showErrorDialog(response);
						aa.notifyDataSetChanged();
					}
				});
				pd.dismiss();
			}
		}.start();

	}

	private void showCurrentController(int slide, boolean forceRefresh) {
		final Controller controller = controllers.get(currentController);

		aa = new DeviceAdapter(this, controller.getDevices(), this);
		if (slide == SLIDE_LEFT || slide == SLIDE_RIGHT) {
			ListView inactiveList = (ListView) inactiveLayout.findViewById(R.id.list);

			/* Toggle active - inactive */
			inactiveList.setAdapter(aa);
			LinearLayout tmp = activeLayout;
			activeLayout = inactiveLayout;
			inactiveLayout = tmp;

		} else {
			ListView activeList = (ListView) activeLayout.findViewById(R.id.list);

			activeList.setAdapter(aa);
			// if (controller != null && !controller.getDevices().isEmpty())
			// ((TextView)
			// activeLayout.findViewById(R.id.emptyLabel)).setVisibility(TextView.INVISIBLE);
			// else
			// ((TextView)
			// activeLayout.findViewById(R.id.emptyLabel)).setVisibility(TextView.VISIBLE);
		}

		TextView header = (TextView) activeLayout.findViewById(R.id.listHeader);
		header.setText(controller.getName());

		if (slide == SLIDE_RIGHT) {
			/* Slide in the previously inactive view */
			viewFlipper.setInAnimation(slideLeftIn);
			viewFlipper.setOutAnimation(slideLeftOut);
			viewFlipper.showNext();
		} else if (slide == SLIDE_LEFT) {
			/* Slide in the previously inactive view */
			viewFlipper.setInAnimation(slideRightIn);
			viewFlipper.setOutAnimation(slideRightOut);
			viewFlipper.showPrevious();
		}

		if (forceRefresh || !controller.isRefreshed()) {
			refreshCurrentController();
		}

		if (controller != null && !controller.getDevices().isEmpty())
			((TextView) activeLayout.findViewById(R.id.emptyLabel)).setVisibility(TextView.INVISIBLE);
		else
			((TextView) activeLayout.findViewById(R.id.emptyLabel)).setVisibility(TextView.VISIBLE);

	}

	private void handleNext() {
		if (numControllers < 2)
			return;

		currentController++;
		if (currentController >= numControllers)
			currentController = 0;

		showCurrentController(SLIDE_RIGHT, false);
	}

	private void handlePrevious() {
		if (numControllers < 2)
			return;

		currentController--;
		if (currentController < 0)
			currentController = numControllers - 1;

		showCurrentController(SLIDE_LEFT, false);
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		ListView activeList = (ListView) activeLayout.findViewById(R.id.list);
		int itemPosition = activeList.getPositionForView(seekBar);
		Device device = aa.getItem(itemPosition);
		Log.v("tellremote", "dimming " + device.getName());
		int level = seekBar.getProgress() * 255 / 100;
		Response response = controllers.get(currentController).dim(device, level);
		if (!response.isOk())
			showErrorDialog(response);

	}
}
