package com.example.touchscreen_fw_upgrade;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final static String PATH_IC_INFO = "/sys/bus/ttsp4/devices/main_ttsp_core.cyttsp4_i2c_adapter/ic_ver";
	private final static String PATH_MANUAL_UPGRADE = "/sys/bus/ttsp4/devices/cyttsp4_loader.main_ttsp_core/manual_upgrade";
	private final static String PATH_FIRMWARE_DATA = "/sys/class/firmware/cyttsp4_loader.main_ttsp_core/data";
	private final static String PATH_FIRMWARE_LOADING = "/sys/class/firmware/cyttsp4_loader.main_ttsp_core/loading";
	private final static String PATH_FW = "/mnt/sdcard/fw/";
	private final static String TAG = "TouchScreen";
	
	//private EditText readEdt;
	private TextView tpInfoView;
	private TextView fwInfoView;
	private Button fwUpgradeButton;
	
	
	private void MyWrite(String context,String file) throws IOException{
		FileOutputStream fos = null;
		try{
			fos = openFileOutput(file,MODE_APPEND);
			fos.write(context.getBytes());
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			fos.close();
		}
	}
	
	private void loadFirmware(String srcFile, String desFile) throws IOException{
		try{
			byte[] data = new byte[1];
			File sf = new File(srcFile);
			File df = new File(desFile);
			
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(sf));
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(df));
			
			System.out.println("copy file " + sf.length() + "bytes");
			while (bufferedInputStream.read(data) != -1) {
				bufferedOutputStream.write(data);
			}
			bufferedOutputStream.flush();
			System.out.println("copy finished");
			
			bufferedInputStream = new  BufferedInputStream(new FileInputStream(df));
			while (bufferedInputStream.read(data) != -1) {
				String str = new String(data);
				System.out.println(str);
			}
			
			bufferedInputStream.close();
			bufferedOutputStream.close();
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("using: java useFileStream src des");
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	/*private void saveToSdcard(String string) {
	
		String filename = "touch_info";
		FileOutputStream outputStream;
		File file = new File(this.getFileDir(),filename);
		
		try {
			outputStream = openFileOutput();
		}
	}
	*/
	private void showInfo(String string) {
		tpInfoView.setText(string);
	}


	/*private void saveToSdcar(String string) {
	
		String filename = "touch_info";
		FileOutputStream outputStream;
		File file = new File(this.getFileDir(),filename);
		
		try {
			outputStream = openFileOutput();
		}
	}
	*/

	
	
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
				if (temp != null)
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
				//save to sdcard
				//saveToSdcard(ret);
			}
		}.start();
	}
	
	// find touchscreen firmware in sdcard, if exist return true, else return false;
	private boolean findFw(){
		try {
			File fw = new File(PATH_FW + "cyttsp4.bin");
			if (!fw.exists())
				return false;
		}catch (Exception e) {
			//TODO
			return false;
		}
		
		return true;
	}
	
	private void getFw(){
		if (!findFw()) {;
			fwInfoView.setText(PATH_FW + "cyttsp4.bin" + " not exist!");
			fwUpgradeButton.setText("NO FW FOUND!");
		}else {
			fwInfoView.setText("Found firmware: " + PATH_FW + "cyttsp4.bin");
			fwUpgradeButton.setText("FW Upgrade");
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tpInfoView = (TextView)findViewById(R.id.tpInfoView);
		fwInfoView = (TextView)findViewById(R.id.fwInfoView);
		fwUpgradeButton = (Button)findViewById(R.id.fwUpgradeButton);
	
		/*get touchscreen ic info and show it to me*/
		getTpInfo();
		/*find the fw in sdcard*/
		getFw();
		
		//start load firmware
    	try {
			loadFirmware("/sdcard/src.txt", "/sdcard/dsc.txt");//for test
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 /** Called when the user clicks the confirm button*/ 
    public void FwUpgrade(View view){
		if (!findFw()){
			fwUpgradeButton.setText("NO FW FOUND!");
			return;
		}else if(fwUpgradeButton.getText() == "upgrade success!") {
			fwUpgradeButton.setText("try again?");
			return;
		}else if (fwUpgradeButton.getText() == "try again?") {
			fwUpgradeButton.setText("fw upgrading...");
		}
		else {
			fwUpgradeButton.setText("fw upgrading...");
		}
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
		    	//start load firmware
		    	try {
					loadFirmware(PATH_FW + "cyttsp4.bin", PATH_FIRMWARE_LOADING);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		    	try {
					writeFile(PATH_FIRMWARE_DATA,"0");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
			}
		}.start();
		fwUpgradeButton.setText("upgrade success!");
    }
}
