package edu.umass.cs.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import edu.umass.cs.accelerometer.*;

/**
 * 
 * Context_Service: This is a sample class to reads sensor data (accelerometer). 
 * 
 * @author CS390MB
 * 
 */
public class Context_Service extends Service implements SensorEventListener{

	/**
	 * Class to orient axis
	 */
	private ReorientAxis orienter = null;
	/**
	 * Feature extractor
	 */
	private ActivityFeatureExtractor extractor = null;

	/**
	 * Notification manager to display notifications
	 */
	private NotificationManager nm;

	/**
	 * SensorManager
	 */
	private SensorManager mSensorManager;
	/**
	 * Accelerometer Sensor
	 */
	private Sensor mAccelerometer;

	// List of bound clients/activities to this service
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	// Message codes sent and received by the service
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_ACTIVITY_STATUS = 3;
	static final int MSG_STEP_COUNTER = 4;
	static final int MSG_ACCEL_VALUES = 5;
	static final int MSG_START_ACCELEROMETER = 6;
	static final int MSG_STOP_ACCELEROMETER = 7;
	static final int MSG_ACCELEROMETER_STARTED = 8;
	static final int MSG_ACCELEROMETER_STOPPED = 9;

	static Context_Service sInstance = null;
	private static boolean isRunning = false;
	private static boolean isAccelRunning = false;
	
	
	private static final int NOTIFICATION_ID = 777;
	
	public static List<Integer> selected;

	/**
	 * Filter class required to filter noise from accelerometer
	 */
	private Filter filter = null;
	/**
	 * Step count to be displayed in UI
	 */
	private int stepCount = 0;


	// Messenger used by clients
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Handler to handle incoming messages
	 */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_START_ACCELEROMETER: {
				isAccelRunning = true;
				mSensorManager.registerListener(sInstance, mAccelerometer,
						SensorManager.SENSOR_DELAY_GAME);
				sendMessageToUI(MSG_ACCELEROMETER_STARTED);
				showNotification();
				// Set up filter
				// Following sets up smoothing filter from mcrowdviz
				int SMOOTH_FACTOR = 10;
				filter = new Filter(SMOOTH_FACTOR);
				// OR Use Butterworth filter from mcrowdviz
				// double CUTOFF_FREQUENCY = 0.3;
				// filter = new Filter(CUTOFF_FREQUENCY);
				stepCount = 0;
				// Set up orienter
				orienter = new ReorientAxis();
				long WINDOW_IN_MILLISECONDS = 5000; // 5seconds
				// Set up a feature extractor that extracts features every 5
				// seconds
				extractor = new ActivityFeatureExtractor(5000);
				break;
			}
			case MSG_STOP_ACCELEROMETER: {
				isAccelRunning = false;
				mSensorManager.unregisterListener(sInstance);
				sendMessageToUI(MSG_ACCELEROMETER_STOPPED);
				showNotification();
				// Free filter and step detector
				filter = null;
				orienter = null;
				extractor = null;
				break;
			}
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void sendMessageToUI(int message) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				// Send message value
				mClients.get(i).send(Message.obtain(null, message));
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

	private void sendAccelValuesToUI(float accX, float accY, float accZ) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {

				// Send Accel Values
				Bundle b = new Bundle();
				b.putFloat("accx", accX);
				b.putFloat("accy", accY);
				b.putFloat("accz", accZ);
				Message msg = Message.obtain(null, MSG_ACCEL_VALUES);
				msg.setData(b);
				mClients.get(i).send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}



	private void sendUpdatedStepCountToUI() {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				// Send Step Count
				Message msg = Message.obtain(null, MSG_STEP_COUNTER, stepCount,
						0);
				mClients.get(i).send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

	private void sendActivityToUI(String activity) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				// Send Activity
				Bundle b = new Bundle();
				b.putString("activity", activity);
				Message msg = Message.obtain(null, MSG_ACTIVITY_STATUS);
				msg.setData(b);
				mClients.get(i).send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

	/**
	 * On Binding, return a binder
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	// Start service automatically if we reboot the phone
	public static class Context_BGReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent bootUp = new Intent(context, Context_Service.class);
			context.startService(bootUp);
		}
	}

	@SuppressWarnings("deprecation")
	private void showNotification() {
		// Cancel previous notification
		if (nm != null)
			nm.cancel(NOTIFICATION_ID);
		else
			nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		// Use the commented block of code if your target environment is
		// Android-16 or higher
		/*
		 * Notification notification = new Notification.Builder(this)
		 * .setContentTitle("Context Service")
		 * .setContentText("Running").setSmallIcon(R.drawable.icon)
		 * .setContentIntent(contentIntent) .build();
		 * 
		 * nm.notify(NOTIFICATION_ID, notification);
		 */

		// For lower versions of Android, the following code should work
		Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		notification.tickerText = getString(R.string.app_name);
		notification.contentIntent = contentIntent;
		notification.when = System.currentTimeMillis();
		if (isAccelerometerRunning())
			notification.setLatestEventInfo(getApplicationContext(),
					getString(R.string.app_name), "Accelerometer Running",
					contentIntent);
		else
			notification.setLatestEventInfo(getApplicationContext(),
					getString(R.string.app_name), "Accelerometer Not Started",
					contentIntent);

		// Send the notification.
		nm.notify(NOTIFICATION_ID, notification);
	}

	/* getInstance() and isRunning() are required by the */
	static Context_Service getInstance() {
		return sInstance;
	}

	protected static boolean isRunning() {
		return isRunning;
	}

	protected static boolean isAccelerometerRunning() {
		return isAccelRunning;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		showNotification();
		isRunning = true;
		sInstance = this;
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		nm.cancel(NOTIFICATION_ID); // Cancel the persistent notification.
		isRunning = false;
		// Don't let Context_Service die!
		Intent mobilityIntent = new Intent(this, Context_Service.class);
		startService(mobilityIntent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY; // run until explicitly stopped.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onAccuracyChanged(android.hardware
	 * .Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onSensorChanged(android.hardware
	 * .SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			float accel[] = event.values;
			sendAccelValuesToUI(accel[0], accel[1], accel[2]);

			// Add the following
			long time = event.timestamp / 1000000; // convert time to
			// milliseconds from
			// nanoseconds
			// Orient accelerometer
			double ortAcc[] = orienter.getReorientedValues(accel[0], accel[1],
					accel[2]);

			// Extract Features now
			Double features[] = extractor.extractFeatures(time, ortAcc[0],
					ortAcc[1], ortAcc[2], accel[0], accel[1], accel[2]);

			if (features != null) {
				// Classify
				try {
					double classId = ActivityClassifier.classify(features);

					// TODO: 1. The activity labels below will depend on
					// activities in your data set
					String activity = null;
					if (classId == 0.0){
						activity = "walking";
						System.out.println("Walking");
					}
					else if (classId == 1.0){
						activity = "stationary";
						System.out.println("Stationary");
					}
					else if (classId == 2.0){
						activity = "jumping";
						System.out.println("Jumping");
					}

					sendActivityToUI(activity);

					// TODO: 2. Send new activity label to UI
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			/**
			 * TODO: Step Detection
			 */
			// First, Get filtered values
			double filtAcc[] = filter.getFilteredValues(accel[0], accel[1],
					accel[2]);
			// Now, increment 'stepCount' variable if you detect any steps here
			stepCount += detectSteps(filtAcc[0], filtAcc[1], filtAcc[2]);
			// detectSteps() is not implemented
			sendUpdatedStepCountToUI();

		}

	}

	/**
	 * This should return number of steps detected.
	 * 
	 * @param filt_acc_x
	 * @param filt_acc_y
	 * @param filt_ac
	 *            c_z
	 * @return
	 */

	ArrayList<Double> xarray = new ArrayList<Double>();
	ArrayList<Double> yarray = new ArrayList<Double>();
	ArrayList<Double> zarray = new ArrayList<Double>();
	double xmin = 0;
	double xmax = 0;
	double xthreshold = 0;
	
	double ymin = 0;
	double ymax = 0;
	double ythreshold = 0;
	
	double zmin = 0;
	double zmax = 0;
	double zthreshold = 0;
	
	double domThreshold = 0;
	
	/*
	 * 
	 * THIS IS JUNK IS INCOMPLETE AND DOES NOT WORK
	 * Need to do this
	 * https://docs.google.com/document/d/1SwVUFpxVqIgVm6_Ao4Ol3Rg8M6P5YvlGUK0mrNFeIxw/pub
	 * 
	 */

	public int detectSteps(double filt_acc_x, double filt_acc_y,
			double filt_acc_z) {

		if (xarray.size() < 50){
			xarray.add(filt_acc_x);
			yarray.add(filt_acc_y);
			zarray.add(filt_acc_z);
			System.out.println("adding");
		}
		else{
			double xtempMin = xarray.get(0);
			double xtempMax = xarray.get(0);
			
			double ytempMin = yarray.get(0);
			double ytempMax = yarray.get(0);
			
			double ztempMin = zarray.get(0);
			double ztempMax = zarray.get(0);
			for (int i = 0; i < xarray.size(); i++) {
				if (xarray.get(i) < xtempMin){
					xtempMin = xarray.get(i);
				}
				if (xarray.get(i) > xtempMax){
					xtempMax = xarray.get(i);
				}
			}
			xthreshold = (xtempMax+xtempMin/2);
			
			for (int i = 0; i < yarray.size(); i++) {
				if (yarray.get(i) < ytempMin){
					ytempMin = yarray.get(i);
				}
				if (yarray.get(i) > ytempMax){
					ytempMax = yarray.get(i);
				}
			}
			ythreshold = (ytempMax+ytempMax/2);
			
			for (int i = 0; i < zarray.size(); i++) {
				if (zarray.get(i) < ztempMin){
					ztempMin = zarray.get(i);
				}
				if (zarray.get(i) > ztempMax){
					ztempMax = zarray.get(i);
				}
			}
			
			zthreshold = (ztempMax+ztempMax/2);
			
			if(xthreshold > zthreshold && xthreshold > ythreshold){
				System.out.println("x is dom");
				domThreshold = xthreshold;
			}
			else if(ythreshold > zthreshold && ythreshold > zthreshold){
				System.out.println(" y is dom");
				domThreshold = ythreshold;
			}
			else{
				System.out.println("z is dom");
				domThreshold = zthreshold;
			}
			
		}
		
				
		if (xarray.size() > 49 && filt_acc_x > domThreshold) {
			System.out.println("a step?");
			return 1;
		}
		
		return 0;
		

	}



	public Integer getIntVal(String s){
		return Integer.parseInt(s);
	}

	public Long getLongVal(String s){
		return Long.parseLong(s);
	}

	BufferedReader br;
	String csvFile ;
	String line = "";
	String cvsSplitBy = ",";
	String emaFile;
	String[] timeVals=new String[4];
	int timeCount=0;

	public ArrayList<String[]> parseData() {


		ArrayList<String[]> list = new ArrayList<String[]>();
		int window=0;
		String[] data=null;

		try{

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null&&window<1000) {

				// use comma as separator
				data = line.split(cvsSplitBy);
				list.add(data);
				window++;
			}
			BufferedReader br2;
			String line2 = "";
			br2 = new BufferedReader(new FileReader(emaFile));
			while ((line2 = br2.readLine()) != null) {
				String[]temp=line2.split(cvsSplitBy);
				// use comma as separator
				if(timeCount<4){
					timeVals[timeCount] = temp[0];
					timeCount++;
				}

			}
			br2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;//return an array list of arrays containing a reading and a timestamp
	}


	public File rrIntervalCalculator(String fileName)
	{

		ArrayList<String[]> data= new ArrayList<String[]>();

		// read the ecg file for time stamps and heart rate readings
		data = parseData();

		// read the ecg file for time stamps and heart rate readings
		ArrayList<Integer> ecgReadings = new ArrayList<Integer>();
		ArrayList<String> timeStamps = new ArrayList<String>();

		int mean=0;
		double max=0, min=0;

		for(int i=0;i<data.size();i++){
			String[] d = data.get(i);

			timeStamps.add(d[0]);//collect data timestamps
			ecgReadings.add(getIntVal(d[1]));//collect readings
			//  System.out.println(d[0]+", "+d[1]);
			mean=mean+getIntVal(d[1]);//add to mean calculation

		}

		mean = mean/data.size();//calculate mean
		max = mean*(1.2*1.02);//calculate max threshold as anything greater than 20% of mean times some margin of error
		min = mean*(0.8*1.02);//calculate min threshold as anything less than 20% of mean times some margin of error

		ArrayList<Integer> peaks = new ArrayList<Integer>();// then add all the peak values to the array peaks
		ArrayList<Integer> valleys=new ArrayList<Integer>();;
		ArrayList<String> times=new ArrayList<String>();;

		for(int i=0;i<ecgReadings.size();i++){
			// if readings fall
			if(ecgReadings.get(i)>=mean&&ecgReadings.get(i)>ecgReadings.get(i-1)&&ecgReadings.get(i)>ecgReadings.get(i+1)){
				peaks.add(ecgReadings.get(i));
				times.add(timeStamps.get(i));

				// System.out.println("here");
			}
			else if(ecgReadings.get(i)<=mean){
				valleys.add(ecgReadings.get(i));
				//  System.out.println("here");
			}
			else{
				//	  System.out.println("here");
			}
		}

		int[] RRInterval = new int[peaks.size()];//store distance between 2 consecutive peaks here
		ArrayList<String> newData=new  ArrayList<String>();

		for(int i = 1;i<times.size()-1;i++){


			Long t = Long.parseLong(times.get(i));
			//  System.out.println(t);
			Long prev = Long.parseLong(times.get(i-1));
			//  System.out.println(prev);
			RRInterval[i-1]= t.intValue() - prev.intValue();

			Long b = Long.parseLong(timeVals[1]);
			Long b2 = Long.parseLong(timeVals[0]);

			if(t<=b&&t>=b2){
				newData.add(RRInterval[i-1]+","+ " baseline");
				System.out.println(RRInterval[i-1]+","+ " baseline");
			}
			else if(t<=getLongVal(timeVals[3])&&t>=getLongVal(timeVals[2])){
				newData.add(RRInterval[i-1]+","+ " exercise");
				System.out.println(RRInterval[i-1]+","+ " exercise");
			}
			else{
				;
			}

		}


		PrintWriter writer;
		try {
			writer = new PrintWriter("newData.txt", "UTF-8");
			for(int i=0;i<RRInterval.length;i++){
				writer.println(RRInterval[i]);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("printed");
		return new File("newData.txt");


	}

}
