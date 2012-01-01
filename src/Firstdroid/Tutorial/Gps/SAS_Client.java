package Firstdroid.Tutorial.Gps;


import Firstdroid.Tutorial.Gps.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


public class SAS_Client extends Activity
{
	private final static int STATE_X_OR_V = 0;
	
	static String locationString = "";
	static int locationState = STATE_X_OR_V;
	
	CheckBox confirmToSendSMS;
	
		
/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		this.restoreTextEdits(); //after loading the main activity, load the latest saved parameters.
		
		confirmToSendSMS = (CheckBox) findViewById(R.id.confirm_send_sms);
		
		ImageButton btnEmergency = (ImageButton) findViewById(R.id.help_button);
		btnEmergency.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				SAS_Client.this.helpButtonPressed();
			}
		});
	}
	
	
	
	private void setLocationState(String locationString, int locationState) 
	{
		SAS_Client.locationString = locationString;
		SAS_Client.locationState = locationState;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		restoreTextEdits();		
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
	}
	
	/*
	 * this function restore the TextEdits we wrote
	 */
	public void restoreTextEdits() 
	{
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		CheckBox confirmToSendSMS = (CheckBox) findViewById(R.id.confirm_send_sms); 
		
		SASClientData emergency = new SASClientData(this);
		
		txtPhoneNo.setText(emergency.getPhone());
		txtMessage.setText(emergency.getMessage());
		confirmToSendSMS.setChecked(true);
	}
	
	/*
	 * this function save the TextEdits we wrote
	 */
	public void saveTextEdits() 
	{
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		CheckBox confirmToSendSMS = (CheckBox) findViewById(R.id.confirm_send_sms);
//		boolean boolean_to_string = confirmToSendSMS.isChecked();
//		String value = new Boolean(boolean_to_string).toString();
		         
		       
		SASClientData emergency = new SASClientData(this);
		emergency.setPhone(txtPhoneNo.getText().toString());
		emergency.setMessage(txtMessage.getText().toString());
//		Toast.makeText(this, "checkBox = " + value , Toast.LENGTH_SHORT).show();
		emergency.setSendSms(confirmToSendSMS.isChecked());
	}
	
	/*
	 * this function called when pressed on the help button and we are going to try find the location and send SMS 
	 */
	public void helpButtonPressed() 
	{
		this.saveTextEdits();
		SASClientData emergency = new SASClientData(this);

		if (emergency.getPhone().length() == 0) //if the phone value is 0, return.
		{
			Toast.makeText(this, "Enter a phone number.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		//here we are going to change the activity
		Intent myIntent = new Intent(SAS_Client.this, SASSecondActivity.class);
		SAS_Client.this.startActivity(myIntent);
	}

}