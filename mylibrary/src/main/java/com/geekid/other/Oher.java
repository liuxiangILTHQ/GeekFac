package com.geekid.other;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/7/1.
 */

public class Oher extends Activity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TextView textView=new TextView(this);
        textView.setText("Other");
        setContentView(textView);
    }
}
