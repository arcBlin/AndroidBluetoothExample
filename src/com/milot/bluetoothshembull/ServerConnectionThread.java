package com.milot.bluetoothshembull;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class ServerConnectionThread extends Thread {
	private BluetoothSocket bluetoothSocket;
	private final BluetoothDevice device;
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private final Handler handler;

	public ServerConnectionThread(String deviceID, Handler handler) {
		device = bluetoothAdapter.getRemoteDevice(deviceID);
		this.handler = handler;
		try {
			bluetoothSocket = device
					.createRfcommSocketToServiceRecord(MainActivity.APP_UUID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		bluetoothAdapter.cancelDiscovery();
		try {
			bluetoothSocket.connect();
			manageConnectedSocket();
		} catch (IOException connectException) {
			try {
				bluetoothSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void manageConnectedSocket() {
		DataTransferThread conn = new DataTransferThread(
				bluetoothSocket, handler);
		handler.obtainMessage(
				MainActivity.SOCKET_CONNECTED, conn)
				.sendToTarget();
		conn.start();
	}
	
	public void cancel() {
		try {
			bluetoothSocket.close();
		} catch (IOException e) {
		}
	}

}
