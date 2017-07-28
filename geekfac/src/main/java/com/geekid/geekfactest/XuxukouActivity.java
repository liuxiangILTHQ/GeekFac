package com.geekid.geekfactest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

import com.geecare.blelibrary.BleTools;
import com.geecare.blelibrary.Constants;
import com.geecare.blelibrary.callback.IBleComm;
import com.geecare.blelibrary.callback.IBleConn;
import com.geecare.blelibrary.callback.IDataResult;
import com.geecare.blelibrary.callback.IDisconnect;
import com.geecare.blelibrary.model.BleDevice;
import com.geekid.geekfactest.ble.CommandManager;
import com.geekid.geekfactest.dfu.AutoDfuActivity;
import com.geekid.geekfactest.model.DataInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.geekid.geekfactest.R.id.humidity;
import static com.geekid.geekfactest.R.id.temperature;

public class XuxukouActivity extends Activity implements OnClickListener
{
    private EditText editText1, editText2;
    private Button buttonm,button,button1, button2, button3, button4, button5, button33, button44, button444, send_bt;
    private TextView textView, textView0, textView2, textView4, textView6, textView8, hex;

    private ListView listview;
    private DataInfoAdapter dataInfoAdapter;
    List<DataInfo> dataInfos;

    //二代升级
    private String hexName2 = "Xuxukou233.hex";
    private String hexName1 = "Xuxukou122.hex";
    String addr;
    BleDevice mDevice;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pee_test);
        init();
        mDevice=(BleDevice)getIntent().getSerializableExtra(Constants.DEVICE);
        addr = mDevice.getBleMacAddr();
        textView.setText(addr);

        textView2.setText(mDevice.getRssi()+" DB");

        BleTools.getInstantce().setBleConnCallback(new IBleConn()
        {
            @Override
            public void connectStaus(int staus)
            {
                if(staus== Constants.PEE_ACTION_DEVICE_CONNECT_SUCCESS){
                    updateView(textView0,"蓝牙已连接");
                }else if(staus== Constants.PEE_ACTION_DEVICE_CONNECTING){
                    updateView(textView0,"蓝牙正在连接...");
                }else{
                    AppContext.isConnected=false;
                    updateView(textView0,"蓝牙未连接");
                }
            }
        });
        BleTools.getInstantce().register(this);
        if(BleTools.getInstantce().isBleEnabled()){
            BleTools.getInstantce().connect(mDevice.getBleMacAddr());
        }else{
            Toast.makeText(this,"蓝牙不可用",Toast.LENGTH_SHORT).show();
        }

        BleTools.getInstantce().setBleCommCallback(new IBleComm()
        {
            @Override
            public void OnRecvData(byte[] data)
            {
                CommandManager commandManager=new CommandManager(XuxukouActivity.this);
                commandManager.setCommandParse(iXuxukouData);
                commandManager.onRecvXuxukouData(data);
            }
        });

        // 注册广播监听
        //LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
         //       AppContext.getPeeServiceIntentFilter());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        BleTools.getInstantce().unregister();
        BleTools.getInstantce().setAutoConnect(false);
        BleTools.getInstantce().disconnect(null);
        BleTools.getInstantce().release();
    }

    private void updateView(final View view,final String text){
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if(view instanceof TextView){
                    ((TextView) view).setText(text);
                }
            }
        });
    }

    CommandManager.IXuxukouData iXuxukouData=new CommandManager.IXuxukouData(){

        @Override
        public void temphumReceive(final int temperature,final int humidity)
        {
            DataInfo dataInfo = new DataInfo();
            dataInfo.setTemperature(temperature);
            dataInfo.setHumidity(humidity);
            dataInfo.setTime(System.currentTimeMillis());
            dataInfos.add(dataInfo);
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    int h=humidity;
                    int t=temperature;
                    //Log.d("lx","t:"+t+",h:"+h);
                    if(h>1000||h<200||t>1000||t<10)
                    {
                        if(a!=null&&a.isShowing()){
                        }else{
                            a = new AlertDialog.Builder(XuxukouActivity.this).setMessage("传感器异常，此设备需要维修!").create();
                            a.show();
                        }
                    }
                    dataInfoAdapter.notifyDataSetChanged();
                    if (listview.getLastVisiblePosition() == (listview.getCount() - 2))
                        listview.setSelection(listview.getCount()-1);
                }
            });

        }


        @Override
        public void batteryReceive(final int batt)
        {
            XuxukouActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    String s = textView0.getText().toString();
                    if (!s.contains("电量:"))
                    {
                        textView0.setText(textView0.getText() + ";电量:" + batt);
                    } else
                    {
                        int i = s.indexOf(":");
                        s = s.substring(0, i + 1);
                        textView0.setText(s + batt);
                    }
                }
            });

        }

        @Override
        public void verReceive(String ver)
        {
            updateView(hex,"固件版本号为:" + ver);
        }

        @Override
        public void snReceive(final String content)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    hex.setTextColor(Color.BLUE);
                    hex.setText("序列号为:" + content.substring(7));
                    if (!sn.equals("") && content.equals(sn.toUpperCase()))
                    {
                        String from = AppContext.getSharedPreferencesStringKey(XuxukouActivity.this, "from");
                        String to = AppContext.getSharedPreferencesStringKey(XuxukouActivity.this, "to");
                        int f = Integer.parseInt(from);
                        int t = Integer.parseInt(to);
                        if (f >= t)
                        {
                            AppContext.showTip(XuxukouActivity.this,"所有序列号写入完毕，请重新进入设置序列号界面设置新序列号!");
                            return;
                        }
                        f++;
                        AppContext.setSharedPreferencesStringKey(XuxukouActivity.this, "from", AppContext.getString(String.valueOf(f), 5));
                        nextSn();
                        AppContext.showTip(XuxukouActivity.this,"序列号写入成功!");
                    }
                }
            });
        }

        @Override
        public void otherReceive(String s)
        {
            long a = Long.valueOf(s, 16).longValue();
            //int a = Integer.valueOf(content, 16).intValue();
            //int a =Integer.parseInt(s, 16);
            if (a >= 0x00203398 && a <= 0x10203398)
            {
                updateView(hex,"为新传感器id:" + s);
            }else
            {
                updateView(hex,"为旧传感器id:" + s);
            }
        }
    };

    AlertDialog a;


    public void init()
    {
        hex = (TextView) findViewById(R.id.hex);
        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        button = (Button) findViewById(R.id.button);
        button1 = (Button) findViewById(R.id.button1);
        button2= (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button44 = (Button) findViewById(R.id.button44);
        button444 = (Button) findViewById(R.id.button444);
        button5 = (Button) findViewById(R.id.button5);
        button33 = (Button) findViewById(R.id.button33);

        buttonm= (Button) findViewById(R.id.buttonm);

        send_bt = (Button) findViewById(R.id.send_bt);
        textView = (TextView) findViewById(R.id.textView);
        textView0 = (TextView) findViewById(R.id.textView0);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView8 = (TextView) findViewById(R.id.textView8);
        listview = (ListView) findViewById(R.id.listView1);
        buttonm.setOnClickListener(this);
        button.setOnClickListener(this);
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

    String sn = "";

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    BleTools.getInstantce().sendDataToBle("55020c" + sn);
                    handler.sendEmptyMessageDelayed(1, 1000);
                    break;
                case 1:
                    BleTools.getInstantce().sendDataToBle("5503");
                    break;
            }
        }

    };

    private void upgrade(String hexName,boolean fromAsset)
    {
        if (!BleTools.getInstantce().isConnected())
        {
            Toast.makeText(this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
            return;
        }
        String path = Environment.getExternalStorageDirectory() + "/" + hexName;
        if(fromAsset)
        {
            AppContext.copyFilesFassets(this, hexName, path);
        }
        File file = new File(path);
        if (!file.exists())
        {
            AlertDialog a = new AlertDialog.Builder(this).setMessage(path + "文件不存在!").create();
            a.show();
            return;
        }
        BleTools.getInstantce().sendDataToBle("5504");
        //BleTools.getInstantce().removeHandler();
        Intent intent = new Intent(this, AutoDfuActivity.class);

        intent.putExtra("path", path);
        intent.putExtra("devAdd", addr);
        startActivity(intent);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.button://获取信号
                textView2.setText("...");
                BleTools.getInstantce().readRssi(new IDataResult()
                {
                    @Override
                    public void getResult(String result)
                    {
                        textView2.setText(result+"DB");
                    }
                });
                break;
            case R.id.buttonm:
                dataInfos.clear();
                dataInfoAdapter.notifyDataSetChanged();
                BleTools.getInstantce().setAutoConnect(false);
                BleTools.getInstantce().disconnect(null);
                break;
            case R.id.button1:
                dataInfos.clear();
                dataInfoAdapter.notifyDataSetChanged();
                BleTools.getInstantce().setAutoConnect(false);
                BleTools.getInstantce().disconnect(new IDisconnect()
                {
                    @Override
                    public void disconnect(boolean b)
                    {
                        if(b)//断开成功之后，再去连接
                        {
                            BleTools.getInstantce().scanAndConnect(mDevice.getBleMacAddr());
                        }
                    }
                });
                break;
            case R.id.button2:
                upgrade("xuxukou.hex",false);
                break;
            case R.id.button3://读sn
                BleTools.getInstantce().sendDataToBle("5503");
                break;
            case R.id.button44:
                upgrade(hexName1,true);
                break;
            case R.id.button444:// 读固件ver
                BleTools.getInstantce().sendDataToBle("5506");
                break;
            case R.id.button4:// 固件升级
                upgrade(hexName2,true);
                break;
            case R.id.button5://写序列号
                if (sn.equals("0000000") || sn.length() != 24)
                {
                    Toast.makeText(XuxukouActivity.this, "序列号需要为17位", Toast.LENGTH_SHORT).show();
                    return;
                }
                BleTools.getInstantce().sendDataToBle("55020c" + sn);
                //mBleService.sendDataToBle("550d");

                handler.sendEmptyMessageDelayed(0, 500);
                break;
            case R.id.button33:
                Intent snintent = new Intent(XuxukouActivity.this, SnSettingActivity.class);
                snintent.putExtra("type1",0);
                snintent.putExtra("type2",6);
                XuxukouActivity.this.startActivity(snintent);
                break;
            case R.id.send_bt:
                String send_txt = editText2.getText().toString().trim();
                if (send_txt.equals(""))
                {
                    Toast.makeText(XuxukouActivity.this, "发送不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                BleTools.getInstantce().sendDataToBle(send_txt);
                break;
        }
    }

    private void nextSn()
    {
        String proType = AppContext.getSharedPreferencesStringKey(XuxukouActivity.this, "proType");
        String customType = AppContext.getSharedPreferencesStringKey(XuxukouActivity.this, "customType");
        String time = AppContext.getSharedPreferencesStringKey(XuxukouActivity.this, "time");
        String from = AppContext.getSharedPreferencesStringKey(XuxukouActivity.this, "from");
        String to = AppContext.getSharedPreferencesStringKey(XuxukouActivity.this, "to");
        editText1.setText(proType + customType + time + from);
        sn = "0000000" + proType + customType + time + from;
        //editText1.clearFocus();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        nextSn();
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
            final TextView tv_temperature = ((TextView) vg.findViewById(temperature));
            final TextView tv_humidity = ((TextView) vg.findViewById(humidity));
            final TextView tv_time = ((TextView) vg.findViewById(R.id.time));

            tv_temperature.setText(dataInfo.getTemperature()/10f + "");
            tv_humidity.setText(dataInfo.getHumidity()/10f + "");
            Date dt = new Date();
            dt.setTime(dataInfo.getTime());
            String sDateTime = AppContext.DATE_FORMAT.format(dt);
            tv_time.setText(sDateTime);

            return vg;
        }
    }



}
