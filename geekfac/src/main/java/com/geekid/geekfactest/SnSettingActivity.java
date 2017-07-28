package com.geekid.geekfactest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class SnSettingActivity extends Activity implements OnClickListener
{
    private LinearLayout ll_other;
    private Spinner spinner1, spinner2;
    private TextView textView1, textView2;
    private EditText editText2, editText3, editText4, et_other;
    private Button button1;
    private String[] proType = {"智能嘘嘘扣2代", "智能嘘嘘扣1代", "智能体温计", "奶瓶宝", "奶瓶模组"};
    private String[] proId = {"012", "011", "021", "031", "041"};
    private String[] customType = {"帮贝怡", "知更鸟", "贝舒乐", "好之", "全面时代", "喜蓓", "极客宝贝",
            "重庆澳蓝朵BBG", "功夫龙", "德邦首护Baby", "乖婴乐", "大爱之都Idorbaby", "阿朵云豆",
            "巧乐萌", "芷御坊", "阿里巴巴ALIBABA", "潔環", "帕尔舒", "NautrueBabyCare","茵乐夏尔","手动输入客户"};
    private String[] customId = {"001", "002", "003", "004", "005", "006", "007", "008", "009",
            "010", "011", "012", "013", "014", "015", "016", "017", "018", "019", "020", "XXX"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sn_setting);
        ll_other = (LinearLayout) findViewById(R.id.ll_other);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        button1 = (Button) findViewById(R.id.button1);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);
        et_other = (EditText) findViewById(R.id.et_other);
        button1.setOnClickListener(this);


        ll_other.setVisibility(View.GONE);
        editText2.setText(AppContext.DATE_FORMAT_DAY_INT.format(new Date()).substring(2));

        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, proType);
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(arrayAdapter1);

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, customType);
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(arrayAdapter2);

        Intent intent = getIntent();
        if (intent != null)
        {
            int type1 = intent.getIntExtra("type1", 0);
            int type2 = intent.getIntExtra("type2", 0);
            spinner1.setSelection(type1);
            spinner2.setSelection(type2);
        }

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id)
            {
                String str = proId[position];
                textView1.setText(str);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id)
            {
                String str = customId[position];
                textView2.setText(str);
                if (customType.length - 1 == position)
                {
                    ll_other.setVisibility(View.VISIBLE);
                    et_other.requestFocus();
                } else
                {
                    ll_other.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button1:
                String proType = textView1.getText().toString();
                String customType;
                if (textView2.getText().toString().equals("XXX"))
                {
                    customType = et_other.getText().toString();
                } else
                {
                    customType = textView2.getText().toString();
                }
                if(customType.length()!=3){
                    Toast.makeText(SnSettingActivity.this, "请输入三位客户编码（例如020)", Toast.LENGTH_SHORT).show();
                    return;
                }

                String time = editText2.getText().toString();
                if(time.length()!=6){
                    Toast.makeText(SnSettingActivity.this, "生产日期必须是六位", Toast.LENGTH_SHORT).show();
                    return;
                }

                String from = AppContext.getString(editText3.getText().toString(), 5);
                String to = AppContext.getString(editText4.getText().toString(), 5);
                int f = Integer.parseInt(from);
                int t = Integer.parseInt(to);
                if (f > t)
                {
                    Toast.makeText(SnSettingActivity.this, "结束序号必须大于开始序号", Toast.LENGTH_SHORT).show();
                    return;
                }
                AppContext.saveSnInfo(SnSettingActivity.this, proType, customType, time, from, to);
                SnSettingActivity.this.finish();
                break;
        }

    }


}
