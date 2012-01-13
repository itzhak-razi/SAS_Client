/*
 * This is the main class of the project.
 * The class extends the Activity class.
 */
package code.SAS_Client.com;

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
//	private final static int STATE_X_OR_V = 0;
//	static String locationString = "";
//	static int locationState = STATE_X_OR_V;
	CheckBox confirmToSendSMS; //this is the check box on the main screen that give an option the send SMS or not.
	
		
/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main); //show the main.xml activity on the device screen.
		
		this.restoreTextEdits(); //after loading the main activity, load the latest saved parameters.
		
		confirmToSendSMS = (CheckBox) findViewById(R.id.confirm_send_sms); //initial the check box.
		
		ImageButton helpButton = (ImageButton) findViewById(R.id.help_button); //initial the helpButton
		helpButton.setOnClickListener(new View.OnClickListener() // listener of the helpButton
		{
			public void onClick(View v) 
			{
				SAS_Client.this.helpButtonPressed(); //if pressed, call the helpButtonPressed function
			}
		});
	}
//---------------------------------------------------------------------------------------

	/*
	 * This function called if we moved out from the class and back.
	 * We we back to the class, we want to restore the latest phone number and message we used. 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		restoreTextEdits();		
	}
//---------------------------------------------------------------------------------------

	/*
	 * this function restore the TextEdits we wrote
	 */
	public void restoreTextEdits() 
	{
		//we will update the device screen based on the EditText and CheckBox variable 
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo); 
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		CheckBox confirmToSendSMS = (CheckBox) findViewById(R.id.confirm_send_sms); 
		
		SAS_Client_Data data = new SAS_Client_Data(this);
		
		txtPhoneNo.setText(data.getPhone()); //get the latest wrote phone number and set it to txtPhoneNo variable
		txtMessage.setText(data.getMessage()); //get the latest wrote message and set it to txtMessage variable
		confirmToSendSMS.setChecked(true); //we want to set the send SMS checkBox to true by default
	}
//---------------------------------------------------------------------------------------

	/*
	 * this function save the TextEdits we wrote to the SharedPereferences method.
	 */
	public void saveTextEdits() 
	{
		//we will save the parameters on device screen to the EditText and CheckBox variable 
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		CheckBox confirmToSendSMS = (CheckBox) findViewById(R.id.confirm_send_sms);
		       
		SAS_Client_Data data = new SAS_Client_Data(this);
		
		data.setPhone(txtPhoneNo.getText().toString()); //save the written phone to the data
		data.setMessage(txtMessage.getText().toString()); //save the written phone to the data
		data.setSendSms(confirmToSendSMS.isChecked()); //save the written send to the data
	}
//---------------------------------------------------------------------------------------
	
	/*
	 * This function called when pressed on the help button.
	 * We are going to try find the location and send SMS. 
	 */
	public void helpButtonPressed() 
	{
		this.saveTextEdits(); //save the parameters we wrote on device to the data
		SAS_Client_Data data = new SAS_Client_Data(this);

		if (data.getPhone().length() == 0) //if the phone value is 0, return.
		{
			Toast.makeText(this, "Enter a phone number.", //print message on the screen and don't continue
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		//here we are going to change the activity to SAS_Search_Activity
		Intent myIntent = new Intent(SAS_Client.this, SAS_Search_Activity.class);
		SAS_Client.this.startActivity(myIntent);
	}
//---------------------------------------------------------------------------------------
}