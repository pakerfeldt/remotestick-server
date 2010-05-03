package se.akerfeldt.remotestick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class EditCodeswitch extends Activity {

	private int requestCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestCode = getIntent().getIntExtra("REQUEST_CODE", -1);

		switch (requestCode) {

		case Tellstick.REQUEST_CREATE_CODESWITCH:
			createEditCodeswitch(savedInstanceState);
			break;

		default:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}

	}

	public void createEditCodeswitch(Bundle savedInstanceState) {
		setContentView(R.layout.edit_codeswitch);

		Spinner s = (Spinner) findViewById(R.id.spinnerHouse);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.houses, android.R.layout.simple_spinner_item);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);

		s = (Spinner) findViewById(R.id.spinnerUnit);
		Integer[] units = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
				16 };
		ArrayAdapter<Integer> aa = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_spinner_item, units);
		aa
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(aa);
	}

	public void done(View v) {
		Intent intent = new Intent();
		TextView nameView = (TextView) findViewById(R.id.editName);
		intent.putExtra("name", nameView.getText());

		switch (requestCode) {
		case Tellstick.REQUEST_CREATE_CODESWITCH:
			intent.putExtra("model", "codeswitch");
			intent.putExtra("protocol", "arctech");
			intent.putExtra("house", (String)(((Spinner)findViewById(R.id.spinnerHouse)).getSelectedItem()));
			intent.putExtra("unit", (Integer)(((Spinner)findViewById(R.id.spinnerUnit)).getSelectedItem()));
			break;
		}

		setResult(RESULT_OK, intent);
		finish();
	}

	public void cancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
}
