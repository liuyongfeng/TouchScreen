package com.example.touchscreen_fw_upgrade;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final static String PATH_IC_INFO = "/sys/bus/ttsp4/devices/main_ttsp_core.cyttsp4_i2c_adapter/ic_ver";
	private final static String PATH_MANUAL_UPGRADE = "/sys/bus/ttsp4/devices/cyttsp4_loader.main_ttsp_core/manual_upgrade";
	private final static String PATH_FIRMWARE_DATA = "/sys/class/firmware/cyttsp4_loader.main_ttsp_core/data";
	private final static String PATH_FIRMWARE_LOADING = "/sys/class/firmware/cyttsp4_loader.main_ttsp_core/loading";
	private final static String TAG = "TouchScreen";

	
	private void showInfo(String string) {
		//Create the ic info text
		TextView infoView = new TextView(this);
		infoView.setTextSize(15);
		infoView.setText(string);
		
		//Set the text view as the activity layout
		setContentView(infoView);
	}
	
	
	private String readLine(String filename) throws IOException{
		BufferedReader reader = null;
		String info = "";
		try {
			reader = new BufferedReader(new FileReader(filename),256);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try{
			info = reader.readLine();
			System.out.println(info);
			return info;
		}finally {
			reader.close();
		}
	}
	
	//need to chmod 777 /sys/bus/ttsp4/devices/cyttsp4_loader.main_ttsp_core/manual_upgrade first
	private void writeFile(String filename, String string) throws IOException {
		BufferedWriter writer = null;
		String ret = "";
		try {
			writer = new BufferedWriter(new FileWriter(filename));
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			writer.write(string);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			writer.close();
		}
	}

	private String readFile(String filename) throws IOException{
		BufferedReader reader = null;
		String touchInfo = "";
		String temp = "";
		try {
			reader = new BufferedReader(new FileReader(filename),256);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try{
			do {
				temp =  reader.readLine();
				touchInfo += temp;
				touchInfo += "\n";
				System.out.println(touchInfo);
			}while(temp != null);

			System.out.println(touchInfo);
			return touchInfo;
		}finally {
			reader.close();
		}
	}
	
	private void getTpInfo(){
		new Thread() {
			String ret = "";
			public void run() {		
				try {
					//ret = readLine(PATH_IC_INFO);
					ret = readFile(PATH_IC_INFO);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				showInfo(ret);
			}
		}.start();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/*get touchscreen ic info and show it to me*/
		getTpInfo();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 /** Called when the user clicks the confirm button*/ 
    public void sendMessage(View view){
		new Thread() {
			public void run() {
				//request firmware upgrade from user space
		    	try {
					writeFile(PATH_MANUAL_UPGRADE,"1");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//inform kernel to load the firmware
		    	try {
					writeFile(PATH_FIRMWARE_DATA,"1");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		    	//start load firmware into kernel
		    	
			}
		}.start();
    }
}
