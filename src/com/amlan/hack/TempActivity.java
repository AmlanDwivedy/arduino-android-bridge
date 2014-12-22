package com.amlan.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TempActivity extends Activity implements SensorEventListener,OnClickListener {
	private TextView tempTv,msg,requiredTemp;
	private Sensor mSensor;
	private Button fanBtn,heaterBtn,setTempBtn;
	private SensorManager mSensorManager;
	private Context context;
	private EditText inputTemp;
	private Handler handler;
	private final static int INTERVAL = 1000 * 5;
	
	
	//Bluetooth Related stuff
	BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    
    public static int REQUIRED_TEMPERATURE = 20;
    private int roomTemp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.temp_layout);	
		
		
		context = TempActivity.this;
		tempTv = (TextView)findViewById(R.id.temp);
		msg = (TextView)findViewById(R.id.msg);
		requiredTemp = (TextView)findViewById(R.id.requiredTemp);
		fanBtn = (Button)findViewById(R.id.fan_btn);
		heaterBtn  = (Button)findViewById(R.id.heater_btn);
		setTempBtn = (Button)findViewById(R.id.setTempBtn);
		inputTemp = (EditText)findViewById(R.id.setTempEt);
		
		fanBtn.setOnClickListener(this);
		heaterBtn.setOnClickListener(this);
		setTempBtn.setOnClickListener(this);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		
		
		
	}
	 @Override
	  protected void onResume() {
	    // Register a listener for the sensor.
	    super.onResume();
	    mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  @Override
	  protected void onPause() {
	    // Be sure to unregister the sensor when the activity pauses.
	    super.onPause();
	    try {
			closeBT();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    mSensorManager.unregisterListener(this);
	  }

	@Override
	public void onSensorChanged(SensorEvent event) {
		float temp = event.values[0];		
		roomTemp = (int) temp;
		tempTv.setText(""+String.format("%.2f", temp)+" \u00B0"+"C");
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	@Override
	public void onClick(View v) {
		if(v.getId()  == R.id.fan_btn){
			
		}else if(v.getId() == R.id.heater_btn){
			
		}else if(v.getId() == R.id.setTempBtn){
			 findBT();
             try {
				openBT();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(inputTemp.getText().toString().trim() != null || inputTemp.getText().toString().trim().length() >0 
					||  (!inputTemp.getText().toString().trim().equals(""))){
				REQUIRED_TEMPERATURE = Integer.parseInt(inputTemp.getText().toString());
				/**
				 * I room temp is less than temp then start heater
				 */
				try {
					if(REQUIRED_TEMPERATURE > roomTemp){
						sendData("1");
						heaterBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
						fanBtn.setBackgroundColor(getResources().getColor(android.R.color.black));
					}else{
						sendData("0");
						heaterBtn.setBackgroundColor(getResources().getColor(android.R.color.black));
						fanBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				requiredTemp.setText(inputTemp.getText().toString()+" \u00B0"+"C");
			}
			inputTemp.getText().clear();
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputTemp.getWindowToken(), 0);
		}
	}
	// This will run in every 30 sec
	

	/**
	 * Bluetooth Related Methods starts here
	 */
	
	 void findBT()
	    {
	        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        if(mBluetoothAdapter == null)
	        {
	            Toast.makeText(context, "No bluetooth adapter available",Toast.LENGTH_SHORT).show();
	        }
	        
	        if(!mBluetoothAdapter.isEnabled())
	        {
	            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableBluetooth, 0);
	        }
	        
	        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
	        if(pairedDevices.size() > 0)
	        {
	            for(BluetoothDevice device : pairedDevices)
	            {
	                if(device.getName().equals("HC-05")) 
	                {
	                    mmDevice = device;
	                    break;
	                }
	            }
	        }
	        Toast.makeText(context,"Bluetooth Device Found",Toast.LENGTH_SHORT).show();
	    }
	    
	    void openBT() throws IOException
	    {
	        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
	        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        
	        mmSocket.connect();
	        mmOutputStream = mmSocket.getOutputStream();
	        mmInputStream = mmSocket.getInputStream();
	        
	        beginListenForData();
	        
	        msg.setText("Bluetooth Opened");
	    }
	    void beginListenForData()
	    {
	        final Handler handler = new Handler(); 
	        final byte delimiter = 10; //This is the ASCII code for a newline character
	        
	        stopWorker = false;
	        readBufferPosition = 0;
	        readBuffer = new byte[1024];
	        workerThread = new Thread(new Runnable()
	        {
	            public void run()
	            {                
	               while(!Thread.currentThread().isInterrupted() && !stopWorker)
	               {
	                    try 
	                    {
	                        int bytesAvailable = mmInputStream.available();                        
	                        if(bytesAvailable > 0)
	                        {
	                            byte[] packetBytes = new byte[bytesAvailable];
	                            mmInputStream.read(packetBytes);
	                            for(int i=0;i<bytesAvailable;i++)
	                            {
	                                byte b = packetBytes[i];
	                                if(b == delimiter)
	                                {
	                                    byte[]encodedBytes = new byte[readBufferPosition];
	                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
	                                    final String data = new String(encodedBytes, "US-ASCII");
	                                    readBufferPosition = 0;
	                                    
	                                    handler.post(new Runnable()
	                                    {
	                                        public void run()
	                                        {
	                                            msg.setText(data);
	                                        }
	                                    });
	                                }
	                                else
	                                {
	                                    readBuffer[readBufferPosition++] = b;
	                                }
	                            }
	                        }
	                    } 
	                    catch (IOException ex) 
	                    {
	                        stopWorker = true;
	                    }
	               }
	            }
	        });

	        workerThread.start();
	    }
	    
	    void sendData(String msgString) throws IOException
	    {
//	        String msg = myTextbox.getText().toString();
	        msgString += "\n";
	        mmOutputStream.write(msgString.getBytes());
//	        myTextbox.getText().clear();
	        msg.setText("Data Sent");
	    }
	    
	    void closeBT() throws IOException
	    {
	        stopWorker = true;
	        mmOutputStream.close();
	        mmInputStream.close();
	        mmSocket.close();
	        msg.setText("Bluetooth Closed");
	    }

	    Runnable mHandlerTask = new Runnable()
	    {
	         @Override 
	         public void run() {
	              try {
					checkTemperature();
				} catch (IOException e) {
					e.printStackTrace();
				}
	              handler.postDelayed(mHandlerTask, INTERVAL);
	         }
	    };

		protected void checkTemperature() throws IOException {
			if(REQUIRED_TEMPERATURE > roomTemp){
				sendData("1");
				heaterBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
				fanBtn.setBackgroundColor(getResources().getColor(android.R.color.black));
			}else{
				sendData("0");
				heaterBtn.setBackgroundColor(getResources().getColor(android.R.color.black));
				fanBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
			}
		}
}
