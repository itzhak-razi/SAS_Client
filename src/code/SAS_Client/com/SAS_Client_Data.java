/*
 * in this class we are going to save the phone and massage on the device by using SharedPereferences method.
 */
package code.SAS_Client.com;

import android.content.Context;
import android.content.SharedPreferences;

public class SAS_Client_Data {
	
	Context context;
	SharedPreferences settings; // saving phone number and message

	private static final String PREFS_NAME = "SASPrefsFile"; 
	private static final String PHONE = "phoneNo"; 
	private static final String MESSAGE = "message";
	private static final String SENDSMS = "true";
	private static final String LATITUDE = "0";
	private static final String LONGITUDE = "0";
	
	
	/*
	 * Constructor - initial the context and the settings.
	 */
	public SAS_Client_Data(Context context) 
	{
		this.context = context;
		this.settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
//---------------------------------------------------------------------------------------
	
	/*
	 * getters and setters
	 */
	public String getPhone() 
	{
		return settings.getString(PHONE, "");
	}
//---------------------------------------------------------------------------------------
	
	public String getMessage() 
	{
		return settings.getString(MESSAGE, "");
	}
//---------------------------------------------------------------------------------------
	
	public boolean getSendSms()
	{
		return settings.getBoolean(SENDSMS, true);
	}
//---------------------------------------------------------------------------------------
	
	public String getLatitude() 
	{
		return settings.getString(LATITUDE, "");
	}
//---------------------------------------------------------------------------------------

	public String getLongitude() 
	{
		return settings.getString(LONGITUDE, "");
	}
//---------------------------------------------------------------------------------------
		
	public void setPhone(String phone) 
	{
		commitString(PHONE, phone);
	}
//---------------------------------------------------------------------------------------
	
	public void setMessage(String message) 
	{
		commitString(MESSAGE, message);
	}
//---------------------------------------------------------------------------------------
	
	public void setSendSms(Boolean confirmToSendSms)
	{
		commitBoolean(confirmToSendSms);
	}
//---------------------------------------------------------------------------------------
	
	public void setLatitude(String latitude) 
	{
		commitString(LATITUDE, latitude);
	}
//---------------------------------------------------------------------------------------
	
	public void setLongitude(String longitude) 
	{
		commitString(LONGITUDE, longitude);
	}
//---------------------------------------------------------------------------------------
	
	/*
	 * this function assign new String to another saved String
	 * get string (value) -> assign to String (id)
	 */
	private void commitString(String id, String value) 
	{
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(id, value);
		editor.commit();
	}
//---------------------------------------------------------------------------------------

	/*
	 * this function assign boolean value to saved variable
	 * get boolean value -> assign to SENSSMS variable 
	 */
	private void commitBoolean(final boolean isChecked) {
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(SENDSMS, isChecked);
	    editor.commit();
	}
//---------------------------------------------------------------------------------------
}
