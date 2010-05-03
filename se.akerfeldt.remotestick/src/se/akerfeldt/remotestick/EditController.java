package se.akerfeldt.remotestick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EditController extends Activity {

	private EditText name;
	private EditText uri;
	private EditText apiKey;

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
			apiKey = (EditText) findViewById(R.id.editTellstickApiKey);
			break;
		}

	}

	public void done(View v) {
		Intent intent = new Intent();
		intent.putExtra("name", name.getText().toString());
		intent.putExtra("uri", uri.getText().toString());
		intent.putExtra("apikey", apiKey.getText().toString());

		setResult(RESULT_OK, intent);
		finish();
	}

	public void cancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
}