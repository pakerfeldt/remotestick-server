package se.akerfeldt.remotestick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EditController extends Activity {

	private EditText name;
	private EditText uri;
	private EditText username;
	private EditText password;
	private long identifier = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		int requestCode = intent.getIntExtra("REQUEST_CODE", -1);

		switch (requestCode) {

		// case Tellstick.REQUEST_CREATE_CONTROLLER:
		// setContentView(R.layout.edit_controller);
		// break;

		default:
			// setResult(RESULT_CANCELED);
			// finish();
			setContentView(R.layout.edit_controller);
			name = (EditText) findViewById(R.id.editTellstickName);
			uri = (EditText) findViewById(R.id.editTellstickUri);
			username = (EditText) findViewById(R.id.editTellstickUsername);
			password = (EditText) findViewById(R.id.editTellstickPassword);
			if(intent.hasExtra("name"))
				name.setText(intent.getStringExtra("name"));
			if(intent.hasExtra("uri"))
				uri.setText(intent.getStringExtra("uri"));
			if(intent.hasExtra("username"))
				username.setText(intent.getStringExtra("username"));
			if(intent.hasExtra("password"))
				password.setText(intent.getStringExtra("password"));
			if(intent.hasExtra("identifier")) {
				identifier = intent.getLongExtra("identifier", -1);
			}
			break;
		}

	}

	public void done(View v) {
		Intent intent = new Intent();
		intent.putExtra("name", name.getText().toString());
		intent.putExtra("uri", uri.getText().toString());
		intent.putExtra("username", username.getText().toString());
		intent.putExtra("password", password.getText().toString());
		intent.putExtra("identifier", identifier);

		setResult(RESULT_OK, intent);
		finish();
	}

	public void cancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
}