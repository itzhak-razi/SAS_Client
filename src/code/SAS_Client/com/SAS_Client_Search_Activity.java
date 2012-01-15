/*
 * This class is the handle the search screen that looking for location and send it by SMS.
 */
package code.SAS_Client.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SAS_Client_Search_Activity extends Activity
{
	private final int SECOND_MS = 1000; //1000 ms = 1 sec
	
	Location location = null;
	SAS_Client_Locator locator = null;
	
	SAS_Client_SMS_Sender sender; //initial the SMS_SENDER object
	int counter = 0; //the starting counter for the maxWaitingTime
	int maxWaitingTime = 60; //max waiting time for obtain the location (in seconds)
	
	TextView tv, gettingLocationTextView, sendingSmsTextView; 
	long startTime; //starting time when try to locate the position
	private Handler handler = new Handler();
	
	SAS_Client_Data dataToSend; //the saved phone number and message
    
	ImageView rotateGlobus; //the rotate Globus during looking for location
    RotateAnimation mRotateAnimation; 
    
    boolean foundLocation; 
    boolean newLocation;
    
    ImageView imageLocationStatus, imageSmsStatus;
    
    private ProgressBar progressBar; //the progress bar of waiting for the location
    private int progressStatus = 0; //the progressStatus initial value is 0
        
    /* Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		sender = new SAS_Client_SMS_Sender();
        
        foundLocation = false; //since we don't have new location, foundLocation is false
        newLocation = false;
        
        setContentView(R.layout.sending_values); //change the layout to sending_values
        
        //configure the rotate globus parameters
        rotateGlobus = (ImageView) findViewById(R.id.globus);
        mRotateAnimation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setDuration(700);
        rotateGlobus.startAnimation(mRotateAnimation);

    	dataToSend = new SAS_Client_Data(this);
    	
        startLocator(); //Running the looking for location in the UI thread
        
        requestGPSDialog(); //check the GPS HW available
               
        /*
         * this thread is responsible on parsing the location and when finding, 
         * stop to waiting and send the SMS.
         */
        new Thread(new Runnable() 
        {
        	 	
    		public void run() 
            {
    			tv = (TextView) findViewById(R.id.location_info);
    			gettingLocationTextView = (TextView) findViewById(R.id.getting_location_txt);
    			sendingSmsTextView = (TextView) findViewById(R.id.sending_sms_txt);
    			imageLocationStatus = (ImageView)findViewById(R.id.imgLocation);
    			imageSmsStatus = (ImageView)findViewById(R.id.img_sms);
    			
    			startTime = System.currentTimeMillis(); //save the starting time of trying to locate the location
    			progressBar = (ProgressBar) findViewById(R.id.progressbar); //initial the progressBar
    			
            	while (counter < maxWaitingTime && !foundLocation) 
            	{
            		counter = waitTime(); //increase the counter by 1 second based on the waitTime() function.
            		                                 	   
	                handler.post(new Runnable() 
	                {
	                    public void run() 
	                    {
	                    	progressBar.setProgress(progressStatus); //start the progressBar
	                    	
	                    	if (location != null) //if found the location
	                    	{
	                    		Thread.interrupted(); //stop all the threads
	                    		foundLocation = true; //set the foundLocation variable
	                    		gettingLocationTextView.setText("");
	                    		mRotateAnimation.cancel(); //stop the globus animation
	                    		mRotateAnimation.reset();
	                    		progressBar.setVisibility(8); //hide the progress bar 
	                    		
	                    		//calculate the time from the latest fix 
	                    		Long timeFromLastFix = (location.getTime() - startTime) / 1000;
	                        	
	                    		if (timeFromLastFix < 0) //if there is no newer location
	                        		locationIconFalse(); //update the icon to red X
	                        		                        	
	                        	else //we got newer location
	                        	{
	                        		newLocation = true;
	                        		locationIconTrue(); //update the icon to green V
	                           	} //else
       		            	
	                    	} //if
	                    	
	                    	if (counter >= maxWaitingTime && !foundLocation) //if the max time is over and no location found
	                    	{
	                    		//here we update the device screen
	                    		gettingLocationTextView.setText("");
	                    		progressBar.setVisibility(8); //hide the progress bar
	                    		locationIconFalse(); //set location icon to X
	                    		mRotateAnimation.cancel(); //stop the globus
	                    		mRotateAnimation.reset();
	                    	}
	                    
	                    } //run
	                
	                }); //handler post Runnable
            	
            	} //while
            	
            	//after the while loop is finished I want to perform the following action
            	handler.post(new Runnable() 
                {
                    public void run() 
                    {
                    	if (location == null) //if we don't have any location at all also in memory
                		{
                			tv.setText("phone: " + dataToSend.getPhone() + 
                					"\n" + "Latitude: None" + 
                					"\n" + "Longitude: None" +
                					"\n" + "Provider: None" +
                					"\n" + "Msg: " + dataToSend.getMessage());
                			
                			if (dataToSend.getSendSms()) //if checked the send sms checkBox
                			{
                				//send to SMS just with the message
                				sender.sendSMS(dataToSend.getPhone(), "None", "None", "None", dataToSend.getMessage());
                				smsIconTrue(); //set the SMS icon to V
                			} 
                			
                			else //if didn't checked the send SMS checkBox
                			{
                				smsIconFalse(); //set the SMS icon to X
                			}
                			
                		} //if
                    	
                    	else //if got new location during the while loop
                        {
                    		if (newLocation) //prepare the text and message and send them
	                		{
                    			tv.setText("phone: " + dataToSend.getPhone() + 
	                					"\n" + "Latitude: " + location.getLatitude() +
	                					"\n" + "Longitude: " + location.getLongitude() + 
	                					"\n" + "Provider: " + location.getProvider() +
	                					"\n" + "Accuracy: " + location.getAccuracy() +
	                					"\n" + "Msg: " + dataToSend.getMessage());
	                    	
	                        	if (dataToSend.getSendSms()) //if checked the send SMS checkBox
	                        	{
	                        		//send the message with the location
	                        		sender.sendSMS(dataToSend.getPhone(),
	                        					   Double.toString(location.getLatitude()), 
	                        					   Double.toString(location.getLongitude()),
	                        					   location.getProvider(),
	                        					   Double.toString(location.getAccuracy()),
	                        					   dataToSend.getMessage());
	                        		smsIconTrue(); //set the SMS icon to V
	                        	} 
	                        	
	                        	else //if didn't checked the send SMS checkBox 
	                        	{
	                        		smsIconFalse(); //set the SMS icon to X
	                        	} 
	                		} //end of if (newLocation)
                        
                    	   	else //if didn't get new location during the while loop but has location in memory
	                    	{
	                    			//calculate the latest location time
		                    		//prepare the text and message and send them
		                    		Long serverUptimeSeconds =(startTime - location.getTime()); 
		                			String serverUptimeText = 
		                			String.format("%d days %d hours %d min %d sec",
		                					((serverUptimeSeconds/(1000*60*60))%24)/24,
		                					(serverUptimeSeconds/(1000*60*60))%24,
		                					(serverUptimeSeconds/(1000*60))%60,
		                					(serverUptimeSeconds/1000)%60);
		                    		String msg = ("There is no new location" + 
		                    						"\n" + "The latest was before:" + "\n" + serverUptimeText +
		                    						"\n" + "Latitude: " + location.getLatitude() +
		                    						"\n" + "Longitude: " + location.getLongitude() +
		                    						"\n" + "Provider: " + location.getProvider() +
		                        					"\n" + "Accuracy: " + location.getAccuracy()); 
		                    		
		                    		tv.setText("phone: " + dataToSend.getPhone() + 
		                    					"\n" + "Latitude: None" + 
		                    					"\n" + "Longitude: None" +
		                    					"\n" + "Provider: None" +
		                    					"\n" + "Msg: " + dataToSend.getMessage() + 
		                    					"\n\n" + msg);
		                    		
		                    		
		                    		if (dataToSend.getSendSms()) //if checked the send sms checkBox
		                    		{
		                    			sender.sendSMS(dataToSend.getPhone(), "None", "None", "None", dataToSend.getMessage() + msg);
		                    		
		                    			smsIconTrue();
		                    		} 
		                    		
		                    		else //if didn't checked the send SMS checkBox
		                    		{
		                    			smsIconFalse();
		                    		} //else
	                    	} //else

                        } //else
                        
                    } //run
                    
                }); //handler runnable
            		
            } //run 
    		

 
	        // this is the waiting time function
	        private int waitTime() 
	        {
	            try 
	            {
	                Thread.sleep(SECOND_MS); //wait 1 second
	            } 
	            
	            catch (InterruptedException e) 
	            {
	                e.printStackTrace();
	            }
	            
	            ++progressStatus; //update the progress bar by 2 because progress bar need to get to 100    
	            ++progressStatus; //and my locator is until 50 seconds
	            return ++counter; 
	        }
	        
	        
	        // this function responsible to replace the Location icon to V
	        private void locationIconTrue()
	    	{
	    		imageLocationStatus.setBackgroundColor(android.R.color.transparent);
	    		imageLocationStatus.setImageResource(R.drawable.green_v_70_70);
	    	}
	    	
	        // this function responsible to replace the Location icon to X
	    	private void locationIconFalse()
	    	{
	    		imageLocationStatus.setBackgroundColor(android.R.color.transparent);
	    		imageLocationStatus.setImageResource(R.drawable.red_x_75_75);
	    	}
	    	
	    	// this function responsible to replace the SMS icon to V
	        private void smsIconTrue()
	    	{
	        	sendingSmsTextView.setText("");
	        	imageSmsStatus.setBackgroundColor(android.R.color.transparent);
	    		imageSmsStatus.setImageResource(R.drawable.green_v_70_70);
	    	}
	    	
	        // this function responsible to replace the SMS icon to X
	    	private void smsIconFalse()
	    	{
	        	sendingSmsTextView.setText("");
	    		imageSmsStatus.setBackgroundColor(android.R.color.transparent);
	    		imageSmsStatus.setImageResource(R.drawable.red_x_75_75);
	    	}
	    	
        }).start();  //Thread Runnable
        
	}
//---------------------------------------------------------------------------------------
	
	/*
	 * this function responsible to try obtain the location
	 */
	private void startLocator() {
		final Context context = this;
		this.locator = new SAS_Client_Locator(context,	new EmergencyLocator());
	}
//---------------------------------------------------------------------------------------

	/*
	 * initial the location variable of current class from the Locator class
	 */
	private class EmergencyLocator implements SAS_Client_Locator.BetterLocationListener 
	{
		public void onGoodLocation(Location location) 
		{
			SAS_Client_Search_Activity.this.location = location;
		}
	}
//---------------------------------------------------------------------------------------
	
	/*
	 * This class check if the GPS HW is enable, if no - print alert and ask if we want to enable it.
	 */
	private void requestGPSDialog() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}
	}
//---------------------------------------------------------------------------------------

	/*
	 * This function represent the Alert message if GPS HW id disable
	 */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled,\n" +
				"Do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									final DialogInterface dialog,
									final int id) {
								
								showGpsOptions();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}
//---------------------------------------------------------------------------------------
	
	private void showGpsOptions() {
		Intent gpsOptionsIntent = new Intent(
		android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(gpsOptionsIntent);
	}
//---------------------------------------------------------------------------------------
	
	/*
	 * This function handle the case back button pressed. 
	 */
	public void backButtonHandler(View view) 
    {
		if (this.locator.locationManager != null)
			this.locator.locationManager.removeUpdates(locator.locationListener); //stop the GPS
		
		finish(); //stop the activity
		Intent intentExercise = new Intent(view.getContext(), SAS_Client_Main_Activity.class);
    	startActivity(intentExercise);
    }
//---------------------------------------------------------------------------------------

}

/*		
	seconds=(serverUptimeSeconds/1000)%60
	minutes=(serverUptimeSeconds/(1000*60))%60
	hours=(serverUptimeSeconds/(1000*60*60))%24
	days=((serverUptimeSeconds/(1000*60*60))%24)/24
*/





