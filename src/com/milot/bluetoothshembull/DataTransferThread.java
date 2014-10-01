package com.milot.bluetoothshembull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class DataTransferThread extends Thread {
	BluetoothSocket bluetoothSocket;
	private final Handler handler;
	private InputStream mInStream;
	private OutputStream mOutStream;

	DataTransferThread(BluetoothSocket socket, Handler handler){
		super();
		bluetoothSocket = socket;
		this.handler = handler;
		try {
			mInStream = bluetoothSocket.getInputStream();
			mOutStream = bluetoothSocket.getOutputStream();
		} catch (IOException e) {
		}
	}

	@Override
	public void run() {
			byte[] buffer = new byte[1024];
			int bytes;
		while (true) {
			try {
				bytes = mInStream.read(buffer);
				String data = new String(buffer, 0, bytes);
				handler.obtainMessage(
						MainActivity.DATA_RECEIVED,
						data).sendToTarget();
			} catch (IOException e) {
				break;
			}
		}
	}

	public void write(byte[] bytes) {
		try {
			mOutStream.write(bytes);
		} catch (IOException e) {
		}
	}
}
