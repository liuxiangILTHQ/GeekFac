package com.geekid.geekfactest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geecare.blelibrary.Constants;
import com.geecare.blelibrary.model.BleDevice;
import com.geekid.geekfactest.ble.BleConstants;
import com.geekid.geekfactest.ble.CommandManager;
import com.geekid.geekfactest.ble.TempService;
import com.geekid.geekfactest.dfu.AutoDfuActivity;
import com.geekid.geekfactest.model.DataInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TempActivity extends Activity implements OnClickListener
{
    private EditText editText1, editText2;
    private Button button1, button2, button3, button4, button5, button33, button44, button444, send_bt;
    private TextView textView, textView0, textView2, textView4, textView6, textView8, hex;

    private ListView listview;
    private DataInfoAdapter dataInfoAdapter;
    List<DataInfo> dataInfos;

    private Intent intent;
    private TempService tempService = null;

    private String hexName1 = "SmartTemp_V_1_10.hex";
    String addr;
    BleDevice mDevice;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp_test);
        init();
        mDevice=(BleDevice)getIntent().getSerializableExtra(Constants.DEVICE);
        addr= mDevice.getBleMacAddr();
        textView.setText(addr);
        intent = new Intent(this, TempService.class);
        //startService(intent);

        // 注册广播监听
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                AppContext.getTempServiceIntentFilter());
        // 绑定蓝牙服务
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }



    private ServiceConnection mConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
        {
            tempService = ((TempService.LocalBinder) rawBinder).getService();
            if(tempService.isConnected()){
                textView0.setText("蓝牙已连接");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName classname)
        {
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(BleConstants.TEMP_ACTION_DATA_UPDATED))
            {
                if (intent.getSerializableExtra(BleConstants.EXTRA_DATA) != null)
                {
                    DataInfo dataInfo = (DataInfo) intent.getSerializableExtra(BleConstants.EXTRA_DATA);
                    dataInfos.add(dataInfo);
                    dataInfoAdapter.notifyDataSetChanged();
                    //Log.d("lx",""+listview.getLastVisiblePosition()+","+listview.getCount());
                    if (listview.getLastVisiblePosition() == (listview.getCount() - 2))
                        listview.setSelection(listview.getCount()-1);
                }
            } else if (action.equals(BleConstants.TEMP_ACTION_DEVICE_CONNECTING))
            {
                textView0.setText("蓝牙正在连接...");
            } else if (action.equals(BleConstants.TEMP_ACTION_DEVICE_CONNECT_SUCCESS))
            {
                textView0.setText("蓝牙已连接");

                if (second_connect)
                {
                    textView8.setText((TempService.end_time - TempService.start_time) / 1000.0 + " 秒");
                } else
                {
                    textView4.setText((TempService.end_time - TempService.start_time)  / 1000.0 + " 秒");
                }
                // textView6.setText(mDevice.getRssi() + " db");
            } else if (action.equals(BleConstants.TEMP_ACTION_DEVICE_CONNECT_FAIL))
            {
                textView0.setText("蓝牙未连接");
            } else if (action.equals(BleConstants.ACTION_BLE_NOT_ENABLE))
            {
                textView0.setText("蓝牙已关闭");

            } else if (action.equals(BleConstants.TEMP_ACTION_DEVICE_SOC_UPDATED))
            {
                if (intent.getStringExtra("content") != null)
                {
                    String content = intent.getStringExtra("content");
                    String s = textView0.getText().toString();
                    if (!s.contains("电量:"))
                    {
                        textView0.setText(textView0.getText() + ";电量:" + content);
                    } else
                    {
                        int i = s.indexOf(":");
                        s = s.substring(0, i + 1);
                        textView0.setText(s + content);
                    }
                }
            }else if (action.equals(BleConstants.TEMP_ACTION_DATA_COMING))
            {
                if (intent.getStringExtra("content") != null)
                {
                    String content = intent.getStringExtra("content");
                    if (content.length() == 24)
                    {
                        hex.setTextColor(Color.BLUE);
                        hex.setText("序列号为:" + content.substring(7));
                        if (!sn.equals("") && content.equals(sn.toUpperCase()))
                        {
                            String from = AppContext.getSharedPreferencesStringKey(TempActivity.this, "from");
                            String to = AppContext.getSharedPreferencesStringKey(TempActivity.this, "to");
                            int f = Integer.parseInt(from);
                            int t = Integer.parseInt(to);
                            if (f >= t)
                            {
                                AlertDialog a = new AlertDialog.Builder(TempActivity.this).setMessage("所有序列号写入完毕，请重新进入设置序列号界面设置新序列号!").create();
                                a.show();
                                return;
                            }
                            f++;
                            AppContext.setSharedPreferencesStringKey(TempActivity.this, "from", AppContext.getString(String.valueOf(f), 5));
                            nextSn();
                            AlertDialog a = new AlertDialog.Builder(TempActivity.this).setMessage("序列号写入成功!").create();
                            a.show();
                        }
                    } else
                    {
                        hex.setText("固件版本号为:" + CommandManager.hexstringToChar(content));

                    }
                }

            }

        }
    };


    public void init()
    {
        hex = (TextView) findViewById(R.id.hex);
        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button44 = (Button) findViewById(R.id.button44);
        button444 = (Button) findViewById(R.id.button444);
        button5 = (Button) findViewById(R.id.button5);
        button33 = (Button) findViewById(R.id.button33);

        send_bt = (Button) findViewById(R.id.send_bt);
        textView = (TextView) findViewById(R.id.textView);
        textView0 = (TextView) findViewById(R.id.textView0);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView8 = (TextView) findViewById(R.id.textView8);
        listview = (ListView) findViewById(R.id.listView1);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button33.setOnClickListener(this);
        button44.setOnClickListener(this);
        button444.setOnClickListener(this);
        send_bt.setOnClickListener(this);
        dataInfos = new ArrayList<DataInfo>();
        dataInfoAdapter = new DataInfoAdapter(this, dataInfos);
        listview.setAdapter(dataInfoAdapter);
    }

    private boolean second_connect = false;
    String sn = "";

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    tempService.write("56020c" + sn);
                    handler.sendEmptyMessageDelayed(1, 1000);
                    break;
                case 1:
                    tempService.write("5603");
                    break;
            }
        }

    };

    private void upgrade(String hexName,boolean fromAsset)
    {
        if (!tempService.isConnected())
        {
            Toast.makeText(TempActivity.this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
            return;
        }
        String path = Environment.getExternalStorageDirectory() + "/" + hexName;
        if(fromAsset)
        {
            AppContext.copyFilesFassets(TempActivity.this, hexName, path);
        }
        File file = new File(path);
        if (!file.exists())
        {
            AlertDialog a = new AlertDialog.Builder(TempActivity.this).setMessage(path + "文件不存在!").create();
            a.show();
            return;
        }
        tempService.write("5604");
        tempService.removeHandler();
        Intent intent = new Intent(TempActivity.this, AutoDfuActivity.class);

        intent.putExtra("path", path);
        intent.putExtra("devAdd", addr);
        startActivity(intent);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.button1:
                dataInfos.clear();
                dataInfoAdapter.notifyDataSetChanged();
                tempService.connect(addr);
                second_connect = true;
                break;
            case R.id.button3://读sn
                tempService.write("5603");
                break;
            case R.id.button44:
                upgrade(hexName1,true);
                break;
            case R.id.button444:// 读固件ver
                tempService.write("5606");
                break;
            case R.id.button4:// 固件升级
                upgrade("SmartTemp.hex",false);
                break;
            case R.id.button5:
                if (sn.equals("0000000") || sn.length() != 24)
                {
                    Toast.makeText(TempActivity.this, "序列号需要为17位", Toast.LENGTH_SHORT).show();
                    return;
                }
                tempService.write("56020c" + sn);

                handler.sendEmptyMessageDelayed(1, 1000);
                break;
            case R.id.button33:
                Intent snintent = new Intent(TempActivity.this, SnSettingActivity.class);
                snintent.putExtra("type1",2);
                snintent.putExtra("type2",6);
                TempActivity.this.startActivity(snintent);
                break;
            case R.id.send_bt:
                String send_txt = editText2.getText().toString().trim();
                if (send_txt.equals(""))
                {
                    Toast.makeText(TempActivity.this, "发送不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                tempService.write(send_txt);
                break;
        }
    }

    private void nextSn()
    {
        String proType = AppContext.getSharedPreferencesStringKey(TempActivity.this, "proType");
        String customType = AppContext.getSharedPreferencesStringKey(TempActivity.this, "customType");
        String time = AppContext.getSharedPreferencesStringKey(TempActivity.this, "time");
        String from = AppContext.getSharedPreferencesStringKey(TempActivity.this, "from");
        String to = AppContext.getSharedPreferencesStringKey(TempActivity.this, "to");
        editText1.setText(proType + customType + time + from);
        sn = "0000000" + proType + customType + time + from;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        nextSn();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        tempService.disconnect();
        if (mReceiver != null)
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
        if (mConn != null)
        {
            unbindService(mConn);
        }
        if (intent != null)
        {
            stopService(intent);
        }
    }

    class DataInfoAdapter extends BaseAdapter
    {
        Context context;
        List<DataInfo> dataInfos;
        LayoutInflater inflater;

        public DataInfoAdapter(Context context, List<DataInfo> dataInfos)
        {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.dataInfos = dataInfos;
        }

        @Override
        public int getCount()
        {
            return dataInfos.size();
        }

        @Override
        public Object getItem(int position)
        {
            return dataInfos.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewGroup vg;
            vg = (ViewGroup) inflater.inflate(R.layout.data_info, null);
            DataInfo dataInfo = dataInfos.get(position);
            final TextView tv_temperature = ((TextView) vg.findViewById(R.id.temperature));
            final TextView tv_humidity = ((TextView) vg.findViewById(R.id.humidity));
            final TextView tv_time = ((TextView) vg.findViewById(R.id.time));

            tv_temperature.setText(dataInfo.getTemperature()/100f + "");
            tv_humidity.setText("");
            Date dt = new Date();
            dt.setTime(dataInfo.getTime());
            String sDateTime = AppContext.DATE_FORMAT.format(dt);
            tv_time.setText(sDateTime);

            return vg;
        }
    }
}
