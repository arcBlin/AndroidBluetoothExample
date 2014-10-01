package com.milot.bluetoothshembull;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int SELECT_SERVER = 1;
	public static final int DATA_RECEIVED = 3;
	public static final int SOCKET_CONNECTED = 4;

	public static final UUID APP_UUID = UUID
			.fromString("aeb9f938-a1a3-4947-ace2-9ebd0c67adf1");
	private Button serverButton, clientButton;
	private TextView tv = null;
	
	private BluetoothAdapter bluetoothAdapter = null;
	private DataTransferThread bluetoothDataTransferConnection = null;
	private String data;
	private boolean mServerMode;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Log.i("MainActivity", "Bluetooth not supported");
			finish();
		}
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.text_window);
		serverButton = (Button) findViewById(R.id.server_button);
		serverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startAsServer();
				mServerMode = true;
			}
		});

		clientButton = (Button) findViewById(R.id.client_button);
		clientButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectServer();
			}
		});

		if (!bluetoothAdapter.isEnabled()) {
			Intent enableBluetoothIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
		} else {
			setButtonsEnabled(true);
		}
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
			setButtonsEnabled(true);
		} else if (requestCode == SELECT_SERVER
				&& resultCode == RESULT_OK) {
			BluetoothDevice device = data
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			connectToBluetoothServer(device.getAddress());
		}
	}

	private void startAsServer() {
		setButtonsEnabled(false);
		new ServerThread(mHandler).start();
	}

	private void selectServer() {
		setButtonsEnabled(false);
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter
				.getBondedDevices();
		ArrayList<String> pairedDeviceStrings = new ArrayList<String>();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				pairedDeviceStrings.add(device.getName() + "\n"
						+ device.getAddress());
			}
		}
		Intent showDevicesIntent = new Intent(this, ClientActivity.class);
		showDevicesIntent.putStringArrayListExtra("devices", pairedDeviceStrings);
		startActivityForResult(showDevicesIntent, SELECT_SERVER);
	}

	private void connectToBluetoothServer(String id) {
		tv.setText("Connecting to Server...");
		new ServerConnectionThread(id, mHandler).start();
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SOCKET_CONNECTED: {
				bluetoothDataTransferConnection = (DataTransferThread) msg.obj;
				if (!mServerMode)
					bluetoothDataTransferConnection.write("this is a message".getBytes());
				break;
			}
			case DATA_RECEIVED: {
				data = (String) msg.obj;
				tv.setText(data);
				if (mServerMode)
					bluetoothDataTransferConnection.write(data.getBytes());
			}
			default:
				break;
			}
		}
	};

	private void setButtonsEnabled(boolean state) {
		serverButton.setEnabled(state);
		clientButton.setEnabled(state);
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
