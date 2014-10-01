package com.milot.bluetoothshembull;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

class ServerThread extends Thread {
	private BluetoothServerSocket serverSocket;
	private BluetoothSocket bluetoothSocket = null;
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private final Handler handler;

	public ServerThread(Handler handler) {
		this.handler = handler;
		try {
			serverSocket = bluetoothAdapter
					.listenUsingRfcommWithServiceRecord("Bluetooth Demo",
							MainActivity.APP_UUID);
		} catch (IOException e) {
		}
	}

	public void run() {
		while (true) {
			try {
				bluetoothSocket = serverSocket.accept();
				manageConnectedSocket();
				serverSocket.close();
				break;
			} catch (IOException e1) {
				if (bluetoothSocket != null) {
					try {
						serverSocket.close();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				}
			}
		}
	}

	private void manageConnectedSocket() {
		DataTransferThread conn = new DataTransferThread(bluetoothSocket, handler);
		handler.obtainMessage(MainActivity.SOCKET_CONNECTED, conn)
				.sendToTarget();
		conn.start();
	}

	public void cancel() {
		try {
			if (null != serverSocket)
				serverSocket.close();
		} catch (IOException e) {
		}
	}
}
