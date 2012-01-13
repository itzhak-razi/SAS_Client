/*
 * Locator is a class which allows getting a location with minimum requirements
 * of age and accuracy. There is also a timer which fires after 50 seconds and
 * uses the best location that was found until now. After a location is sent
 * the locator class auto unregisters.
 */

package code.SAS_Client.com;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public class SAS_Client_Locator 
{
	private static final int SECOND_MS = 1000; //1000 ms = 1 sec
	private static final int MINUTE_MS = 60 * SECOND_MS; //60 sec = 1 min
	
	
	private static final int MAX_WAITING_TIME_MS = 50 * SECOND_MS; //the max time trying to reach the location.
	private static final float REQUIRED_ACCURACY_METERS = 50; //the best location is by accuracy of 50 meters
	private static final int LARGE_LOCATION_AGE_MS = 2 * MINUTE_MS;
	
	public Location location = null;

	public LocationManager locationManager = null;
	public LocationListener locationListener;
	private BetterLocationListener blListener;
	private Timer waitForGoodLocationTimer;
	
    /*
     * We must implement the BetterLocationListener.
     */
	public interface BetterLocationListener
    {
        void onGoodLocation(Location location);
    }
//---------------------------------------------------------------------------------------
    
    /*
     * Constructor.  
     */
	public SAS_Client_Locator(final Context context, final BetterLocationListener blListener) 
    {
		if (this.locationManager != null) 
		{
			Log.e("Locator", "registered twice!");
			return;
		}
		
		this.blListener = blListener;
		
		// Acquire a reference to the system Location Manager
		this.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		this.location = getBestLastKnownLocation(locationManager);
		
		// Define a listener that responds to location updates
		this.locationListener = new GoodLocationListener();

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this.locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 0, 0, this.locationListener);
		
		this.waitForGoodLocationTimer = new Timer();
		this.waitForGoodLocationTimer.schedule(new GetLastLocation(), MAX_WAITING_TIME_MS);
    }
//---------------------------------------------------------------------------------------
	 
	//Gets the best already known location, if none exists, null is returned.
    public static Location getBestLastKnownLocation(LocationManager locationManager) 
    {
		Location satelliteLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //location of sattelite
		Location networkLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); //location of network
		
		if (null == networkLoc) //if network location is not available return satellite location
		{
			return satelliteLoc;
		} 
		
		if (null == satelliteLoc) //if satellite location is not available return network location
		{
			return networkLoc;
		}
		
		// both locations aren't null, try to find which accuracy is better 
		if (SAS_Client_Locator.isBetterLocation(networkLoc, satelliteLoc)) //if network is better
		{
			return networkLoc; 
		} 
		
		else //if satellite is better
		{
			return satelliteLoc;
		}
    }
//---------------------------------------------------------------------------------------
    
    private class GoodLocationListener implements LocationListener 
    {
		public void onLocationChanged(Location location) 
		{
			// Called when a new location is found by the network location or GPS provider.
			if (SAS_Client_Locator.this.location == null) 
			{
				// never use the first location, always compare because
				// that's the only way to find the age of the location.
				SAS_Client_Locator.this.location = location;
				return;
			}
			
			if (SAS_Client_Locator.isBetterLocation(location, SAS_Client_Locator.this.location)) 
			{
				SAS_Client_Locator.this.location = location;
				
				if (SAS_Client_Locator.isGoodLocation(SAS_Client_Locator.this.location)) 
				{
					SAS_Client_Locator.this.waitForGoodLocationTimer.cancel();
					SAS_Client_Locator.this.emitLocation();
				}
			}
		} //onLocationChanged

		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
		public void onProviderDisabled(String provider) {}
	};
    
    protected static boolean isGoodLocation(Location location) 
    {
    	if (! location.hasAccuracy()) 
    	{
    		return false;
    	}
    	    	
    	if (location.getAccuracy() > REQUIRED_ACCURACY_METERS ) 
    	{
    		return false;
    	}
    	
    	return true;
    }
//---------------------------------------------------------------------------------------
    
    
	protected static boolean isBetterLocation(Location location, Location currentBestLocation) 
	{
		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > LARGE_LOCATION_AGE_MS;
		boolean isSignificantlyOlder = timeDelta < -LARGE_LOCATION_AGE_MS;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer)
		{
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} 
		
		else if (isSignificantlyOlder)
		{
			return false;
		}

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
		
		if (location.hasAccuracy() && currentBestLocation.hasAccuracy())
		{
			// Check whether the new location fix is more or less accurate
			int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
			boolean isLessAccurate = accuracyDelta > 0;
			boolean isMoreAccurate = accuracyDelta < 0;
			boolean isSignificantlyLessAccurate = accuracyDelta > 200;
	
	
			// Determine location quality using a combination of timeliness and accuracy
			if (isMoreAccurate) 
			{
				return true;
			} 
			
			else if (isNewer && !isLessAccurate) 
			{
				return true;
			} 
			
			else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) 
			{
				return true;
			}
			
			return false;
		}
		
		// one or both locations don't have accuracy information
		
		// prefer GPS
		if (location.getProvider().equals(LocationManager.GPS_PROVIDER))
		{
			return true;
		}
		
		if ( ! currentBestLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) 
		{
			// the currentBestLocation isn't GPS, so go with the newer
			if (location.getTime() > currentBestLocation.getTime()) {
				return true;
			}
		}
		
		return false;
	}
//---------------------------------------------------------------------------------------

	private void emitLocation() {
		SAS_Client_Locator.this.unregister();
		SAS_Client_Locator.this.blListener.onGoodLocation(SAS_Client_Locator.this.location);
	}
//---------------------------------------------------------------------------------------
	
	// Checks whether two providers are the same
	private static boolean isSameProvider(String provider1, String provider2) 
	{
		if (provider1 == null) 
		{
			return provider2 == null;
		}
		
		return provider1.equals(provider2);
	}
//---------------------------------------------------------------------------------------
	
	class GetLastLocation extends TimerTask 
	{
		@Override
		public void run() 
		{
			// ran out of time to get a good location, go go go
			SAS_Client_Locator.this.emitLocation();
		}
	}
//---------------------------------------------------------------------------------------	
    
	public void unregister() {
		if (this.locationManager == null) 
		{
			return;
		}
		
		this.locationManager.removeUpdates(this.locationListener);
		this.locationManager = null;
	}
//---------------------------------------------------------------------------------------
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		unregister(); 
	}
//---------------------------------------------------------------------------------------
	
	
}
