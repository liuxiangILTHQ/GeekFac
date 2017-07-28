/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA. Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. This heading must NOT be removed from the file.
 ******************************************************************************/
package com.geekid.geekfactest.dfu;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geekid.geekfactest.AppContext;
import com.geekid.geekfactest.R;

import java.io.File;

import no.nordicsemi.android.error.GattError;

/**
 * DfuActivity is the main DFU activity It implements DFUManagerCallbacks to
 * receive callbacks from DFUManager class It implements
 * DeviceScannerFragment.OnDeviceSelectedListener callback to receive callback
 * when device is selected from scanning dialog The activity supports portrait
 * and landscape orientations
 */
public class AutoDfuActivity extends Activity implements LoaderCallbacks<Cursor>
{
	private static final String TAG = "DfuActivity";

	private static final String PREFS_SAMPLES_VERSION = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_SAMPLES_VERSION";
	private static final int CURRENT_SAMPLES_VERSION = 3;

	private static final String PREFS_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_DEVICE_NAME";
	private static final String PREFS_FILE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_NAME";
	private static final String PREFS_FILE_TYPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_TYPE";
	private static final String PREFS_FILE_SIZE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SIZE";

	private static final String DATA_DEVICE = "device";
	private static final String DATA_FILE_TYPE = "file_type";
	private static final String DATA_FILE_TYPE_TMP = "file_type_tmp";
	private static final String DATA_FILE_PATH = "file_path";
	private static final String DATA_FILE_STREAM = "file_stream";
	private static final String DATA_INIT_FILE_PATH = "init_file_path";
	private static final String DATA_INIT_FILE_STREAM = "init_file_stream";
	private static final String DATA_STATUS = "status";

	private static final String EXTRA_URI = "uri";

	private static final int ENABLE_BT_REQ = 0;
	private static final int SELECT_FILE_REQ = 1;
	private static final int SELECT_INIT_FILE_REQ = 2;

	private TextView mDeviceNameView;
	private TextView mFileNameView;
	private TextView mFileSizeView;
	private TextView mFileStatusView;
	private TextView mTextPercentage;
	private TextView mTextUploading;
	private ProgressBar mProgressBar;

	// private Button mSelectFileButton, mUploadButton, mConnectButton;

	private Button mUploadButton;
	private BluetoothDevice mSelectedDevice;
	private String mFilePath;
	private Uri mFileStreamUri;
	private String mInitFilePath;
	private Uri mInitFileStreamUri;
	private boolean mStatusOk;

	private String devAdd;

	private final BroadcastReceiver mDfuUpdateReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(final Context context, final Intent intent)
		{
			// DFU is in progress or an error occurred
			final String action = intent.getAction();

			if (DfuService.BROADCAST_PROGRESS.equals(action))
			{
				final int progress = intent.getIntExtra(DfuService.EXTRA_DATA, 0);
				updateProgressBar(progress, false);
			} else if (DfuService.BROADCAST_ERROR.equals(action))
			{
				final int error = intent.getIntExtra(DfuService.EXTRA_DATA, 0);
				updateProgressBar(error, true);

				// We have to wait a bit before canceling notification. This is
				// called before DfuService creates the last notification.
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						// if this activity is still open and upload process was
						// completed, cancel the notification
						final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						manager.cancel(DfuService.NOTIFICATION_ID);
					}
				}, 200);
			}
		}
	};

	@Override
	protected void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(DATA_FILE_PATH, mFilePath);
		outState.putParcelable(DATA_FILE_STREAM, mFileStreamUri);
		outState.putParcelable(DATA_DEVICE, mSelectedDevice);
		outState.putBoolean(DATA_STATUS, mStatusOk);
	}

	private void updateProgressBar(final int progress, final boolean error)
	{
		switch (progress) {
		case DfuService.PROGRESS_CONNECTING:
			mProgressBar.setIndeterminate(true);
			mTextPercentage.setText(R.string.dfu_status_connecting);
			break;
		case DfuService.PROGRESS_STARTING:
			mProgressBar.setIndeterminate(true);
			mTextPercentage.setText(R.string.dfu_status_starting);
			break;
		case DfuService.PROGRESS_VALIDATING:
			mProgressBar.setIndeterminate(true);
			mTextPercentage.setText(R.string.dfu_status_validating);
			break;
		case DfuService.PROGRESS_DISCONNECTING:
			mProgressBar.setIndeterminate(true);
			mTextPercentage.setText(R.string.dfu_status_disconnecting);
			break;
		case DfuService.PROGRESS_COMPLETED:
			mTextPercentage.setText(R.string.dfu_status_completed);
			// let's wait a bit until we cancel the notification. When canceled
			// immediately it will be recreated by service again.
			new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					AppContext.logInfo("update success");
					showFileTransferSuccessMessage();

					// if this activity is still open and upload process was
					// completed, cancel the notification
					final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					manager.cancel(DfuService.NOTIFICATION_ID);
				}
			}, 200);
			break;
		default:
			//mProgressBar.setIndeterminate(false);
			if (error)
			{
                mProgressBar.setIndeterminate(true);
				showErrorMessage(progress);
			} else
			{
                mProgressBar.setIndeterminate(false);
				isUploading = true;
				mProgressBar.setProgress(progress);
				mTextPercentage.setText(getString(R.string.progress, progress));
			}
			break;
		}
	}

	private boolean isUploading = false;
	private int errorNum = 0;

	private void showFileTransferSuccessMessage()
	{
		isUploading = false;
		clearUI(true);
		mUploadButton.setEnabled(false);

		showToast("升级成功！");
		AlertDialog a = new AlertDialog.Builder(AutoDfuActivity.this).setMessage("升级成功！").setPositiveButton("确定", new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				AutoDfuActivity.this.finish();
			}
		}).create();
		a.show();
	}

	private void showErrorMessage(final int code)
	{
		isUploading = false;
		// clearUI(false);
		// mUploadButton.setEnabled(true);
		AppContext.logInfo("update fail" + ": " + GattError.parse(code) + " (" + (code & ~(DfuService.ERROR_MASK | DfuService.ERROR_REMOTE_MASK))
				+ ")");
		// showToast("升级失败!");
		if (errorNum > 0)
		{
			mUploadButton.setEnabled(true);
			showToast("升级失败!");
		}else{
			errorNum++;
			onSelectFileClicked();
			onUploadClicked();			
		}
		// showToast("升级失败: " + GattError.parse(code) + " ("
		// + (code & ~(DfuService.ERROR_MASK | DfuService.ERROR_REMOTE_MASK)) +
		// ")");
	}

	private void clearUI(final boolean clearDevice)
	{
		mProgressBar.setVisibility(View.INVISIBLE);
		mTextPercentage.setVisibility(View.INVISIBLE);
		mTextUploading.setVisibility(View.INVISIBLE);
		// mConnectButton.setEnabled(true);
		// mSelectFileButton.setEnabled(true);
		// mUploadButton.setEnabled(false);
		// mUploadButton.setText(R.string.dfu_action_upload);
		if (clearDevice)
		{
			mSelectedDevice = null;
			mDeviceNameView.setText(R.string.dfu_default_name);
		}
		// Application may have lost the right to these files if Activity was
		// closed during upload (grant uri permission). Clear file related
		// values.
		mFileNameView.setText(null);
		mFileSizeView.setText(null);
		mFileStatusView.setText(R.string.dfu_file_status_no_file);
		mFilePath = null;
		mFileStreamUri = null;
		mInitFilePath = null;
		mInitFileStreamUri = null;
		mStatusOk = false;
	}

	private void showToast(final int messageResId)
	{
		Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
	}

	private void showToast(final String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (isUploading)
			{
				AlertDialog a = new AlertDialog.Builder(AutoDfuActivity.this).setMessage("正在升级之中，要退出升级吗？")
						.setPositiveButton("确定", new OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								AutoDfuActivity.this.finish();
							}
						}).setNegativeButton("取消", new OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
							}
						}).create();
				a.show();
				return true;
			} else
			{
				return super.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feature_dfu_auto);

		isBLESupported();
		if (!isBLEEnabled())
		{
			showBLEDialog();
		}
		setGUI();

		// ensureSamplesExist();

		// restore saved state
		// mFileType = DfuService.TYPE_AUTO; // Default
		if (savedInstanceState != null)
		{
			mFilePath = savedInstanceState.getString(DATA_FILE_PATH);
			mFileStreamUri = savedInstanceState.getParcelable(DATA_FILE_STREAM);
			mSelectedDevice = savedInstanceState.getParcelable(DATA_DEVICE);
			mStatusOk = mStatusOk || savedInstanceState.getBoolean(DATA_STATUS);
			// mUploadButton.setEnabled(mSelectedDevice != null && mStatusOk);
		}
		mUploadButton.setEnabled(false);
		onSelectFileClicked();
		onConnectClicked();

		upload();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// We are using LocalBroadcastReceiver instead of normal
		// BroadcastReceiver for optimization purposes
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
		broadcastManager.registerReceiver(mDfuUpdateReceiver, makeDfuUpdateIntentFilter());
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		// final LocalBroadcastManager broadcastManager =
		// LocalBroadcastManager.getInstance(this);
		// broadcastManager.unregisterReceiver(mDfuUpdateReceiver);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
		broadcastManager.unregisterReceiver(mDfuUpdateReceiver);
	}

	private static IntentFilter makeDfuUpdateIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DfuService.BROADCAST_PROGRESS);
		intentFilter.addAction(DfuService.BROADCAST_ERROR);
		intentFilter.addAction(DfuService.BROADCAST_LOG);
		return intentFilter;
	}

	private void setGUI()
	{
		// final ActionBar actionBar = getSupportActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);

		mDeviceNameView = (TextView) findViewById(R.id.device_name);
		mFileNameView = (TextView) findViewById(R.id.file_name);
		mFileSizeView = (TextView) findViewById(R.id.file_size);
		mFileStatusView = (TextView) findViewById(R.id.file_status);
		// mSelectFileButton = (Button) findViewById(R.id.action_select_file);

		mUploadButton = (Button) findViewById(R.id.action_upload);
		// mUploadButton.setEnabled(false);
		mUploadButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onSelectFileClicked();
				onUploadClicked();
			}
		});
		// mConnectButton = (Button) findViewById(R.id.action_connect);
		mTextPercentage = (TextView) findViewById(R.id.textviewProgress);
		mTextUploading = (TextView) findViewById(R.id.textviewUploading);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar_file);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (isDfuServiceRunning())
		{
			// Restore image file information
			mDeviceNameView.setText(preferences.getString(PREFS_DEVICE_NAME, ""));
			mFileNameView.setText(preferences.getString(PREFS_FILE_NAME, ""));
			mFileSizeView.setText(preferences.getString(PREFS_FILE_SIZE, ""));
			mFileStatusView.setText(R.string.dfu_file_status_ok);
			mStatusOk = true;
			showProgressBar();
		}
	}

	private boolean isDfuServiceRunning()
	{
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if (DfuService.class.getName().equals(service.service.getClassName()))
			{
				return true;
			}
		}
		return false;
	}

	private void showProgressBar()
	{
		mProgressBar.setVisibility(View.VISIBLE);
		mTextPercentage.setVisibility(View.VISIBLE);
		mTextPercentage.setText(null);
		mTextUploading.setText(R.string.dfu_status_uploading);
		mTextUploading.setVisibility(View.VISIBLE);
		// mConnectButton.setEnabled(false);
		// mSelectFileButton.setEnabled(false);
		// mUploadButton.setEnabled(true);
		// mUploadButton.setText(R.string.dfu_action_upload_cancel);
	}

	private void isBLESupported()
	{
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
		{
			showToast(R.string.no_ble);
			finish();
		}
	}

	private boolean isBLEEnabled()
	{
		final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		final BluetoothAdapter adapter = manager.getAdapter();
		return adapter != null && adapter.isEnabled();
	}

	private void showBLEDialog()
	{
		final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, ENABLE_BT_REQ);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		final Uri uri = args.getParcelable(EXTRA_URI);
		return new CursorLoader(this, uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		if (data != null && data.moveToNext())
		{
			/*
			 * Here we have to check the column indexes by name as we have
			 * requested for all. The order may be different.
			 */
			final String fileName = data.getString(data.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)/*
																											 * 0
																											 * DISPLAY_NAME
																											 */);
			final int fileSize = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.SIZE) /*
																								 * 1
																								 * SIZE
																								 */);
			String filePath = null;
			final int dataIndex = data.getColumnIndex(MediaStore.MediaColumns.DATA);
			if (dataIndex != -1)
				filePath = data.getString(dataIndex /* 2 DATA */);
			if (!TextUtils.isEmpty(filePath))
				mFilePath = filePath;

			updateFileInfo(fileName, fileSize);
		} else
		{
			mFileNameView.setText(null);
			mFileSizeView.setText(null);
			mFilePath = null;
			mFileStreamUri = null;
			mFileStatusView.setText(R.string.dfu_file_status_error);
			mStatusOk = false;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		mFileNameView.setText(null);
		mFileSizeView.setText(null);
		mFilePath = null;
		mFileStreamUri = null;
		mStatusOk = false;
	}

	private void updateFileInfo(final String fileName, final long fileSize)
	{
		mFileNameView.setText(fileName);

		mFileSizeView.setText(getString(R.string.dfu_file_size_text, fileSize));
		final boolean isHexFile = mStatusOk = MimeTypeMap.getFileExtensionFromUrl(mFilePath).equalsIgnoreCase("HEX");
		mFileStatusView.setText(isHexFile ? R.string.dfu_file_status_ok : R.string.dfu_file_status_invalid);
		// mUploadButton.setEnabled(mSelectedDevice != null && isHexFile);
	}

	public void onSelectFileClicked()
	{
		mFilePath = getIntent().getStringExtra("path");
		devAdd = getIntent().getStringExtra("devAdd");
		AppContext.logInfo("mFilePath " + mFilePath);
		File file = new File(mFilePath);
		if (!file.exists())
		{
			showToast("固件文件不存在");
			finish();
		}
		updateFileInfo(file.getName(), file.length());
	}

	public void onUploadClicked()
	{
		isUploading = true;
		mUploadButton.setEnabled(false);
		AppContext.logInfo("onUploadClicked ");
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		//final boolean dfuInProgress = preferences.getBoolean(DfuService.DFU_IN_PROGRESS, false);
		// if (dfuInProgress) {
		// AppContext.logInfo("dfu In Progress ");
		// return;
		// }

		// check whether the selected file is a HEX file (we are just checking
		// the extension)
		if (!mStatusOk)
		{
			Toast.makeText(this, R.string.dfu_file_status_invalid_message, Toast.LENGTH_LONG).show();
			return;
		}

		// Save current state in order to restore it if user quit the Activity
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFS_DEVICE_NAME, mSelectedDevice.getName());
		editor.putString(PREFS_FILE_NAME, mFileNameView.getText().toString());
		editor.putString(PREFS_FILE_SIZE, mFileSizeView.getText().toString());
		editor.commit();

		showProgressBar();
		Intent service = new Intent(this, DfuService.class);
		service.putExtra(DfuService.EXTRA_DEVICE_ADDRESS, mSelectedDevice.getAddress());
		service.putExtra(DfuService.EXTRA_DEVICE_NAME, mSelectedDevice.getName());
		service.putExtra(DfuService.EXTRA_FILE_PATH, mFilePath);
		service.putExtra(DfuService.EXTRA_FILE_URI, mFileStreamUri);

		AppContext.logInfo("EXTRA_DEVICE_ADDRESS:" + mSelectedDevice.getAddress());
		AppContext.logInfo("start DFUService");
		startService(service);
	}

	BluetoothAdapter mBluetoothAdapter;

	public void onConnectClicked()
	{
		if (isBLEEnabled())
		{
			AppContext.logInfo("startLeScan");

			final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			mBluetoothAdapter = manager.getAdapter();
			if (!isScan)
			{
				isScan = true;
				mBluetoothAdapter.startLeScan(mLEScanCallback);
			}

		} else
		{
			showBLEDialog();
		}
	}

	boolean isScan = false;
    boolean isFound=false;
	private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback()
	{
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
		{
			if (device != null)
			{
				try
				{
					if (device.getAddress().equals(devAdd)&&!isFound)
					{
                        isFound=true;
						AppContext.logInfo("found dev " + device.getName()+" "+devAdd);
						mSelectedDevice = device;
						// mUploadButton.setEnabled(mStatusOk);
						mDeviceNameView.setText(device.getName());
                        //AppContext.isConnected=false;
						isScan = false;

						mBluetoothAdapter.stopLeScan(mLEScanCallback);
						mBluetoothAdapter = null;
						// upload();
						return;

					}

				} catch (Exception e)
				{
					// DebugLogger.e(TAG,
					// "Invalid data in Advertisement packet " + e.toString());
				}

			}
		}
	};

	Handler mHandler = new Handler();
	Runnable start = new Runnable()
	{
		@Override
		public void run()
		{
			onSelectFileClicked();
			onUploadClicked();
		}
	};
	Runnable run = new Runnable()
	{
		@Override
		public void run()
		{
			AppContext.logInfo("Utils.isConnected:" + AppContext.isConnected);
			if (mSelectedDevice != null && !AppContext.isConnected)
			{
				mHandler.postDelayed(start, 1000);
			} else
			{
				mHandler.postDelayed(run, 3000);
			}

		}
	};

	private void upload()
	{
		isUploading = true;
		mProgressBar.setIndeterminate(true);
		mTextPercentage.setText("请稍候...");
		mHandler.postDelayed(run, 3000);
	}

}
