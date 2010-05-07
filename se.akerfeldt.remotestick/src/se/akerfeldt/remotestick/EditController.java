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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int requestCode = getIntent().getIntExtra("REQUEST_CODE", -1);

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
			break;
		}

	}

	public void done(View v) {
		Intent intent = new Intent();
		intent.putExtra("name", name.getText().toString());
		intent.putExtra("uri", uri.getText().toString());
		intent.putExtra("username", username.getText().toString());
		intent.putExtra("password", password.getText().toString());

		setResult(RESULT_OK, intent);
		finish();
	}

	public void cancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
}