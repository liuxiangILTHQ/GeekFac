package com.geekid.geekfactest.model;

import java.io.Serializable;

public class Alarminfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int type=-1;//报警类型:0为第一次报警   1为第二次报警
	private int temperature;
	private int humidity;
	private long time;
	
	private int temperature_min;
	private int humidity_min;
	private long time_min;
	
	public int getType()
	{
		return type;
	}
	public void setType(int type)
	{
		this.type = type;
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getTemperature()
	{
		return temperature;
	}
	public void setTemperature(int temperature)
	{
		this.temperature = temperature;
	}
	public int getHumidity()
	{
		return humidity;
	}
	public void setHumidity(int humidity)
	{
		this.humidity = humidity;
	}
	public long getTime()
	{
		return time;
	}
	public void setTime(long time)
	{
		this.time = time;
	}
	public int getTemperature_min()
	{
		return temperature_min;
	}
	public void setTemperature_min(int temperature_min)
	{
		this.temperature_min = temperature_min;
	}
	public int getHumidity_min()
	{
		return humidity_min;
	}
	public void setHumidity_min(int humidity_min)
	{
		this.humidity_min = humidity_min;
	}
	public long getTime_min()
	{
		return time_min;
	}
	public void setTime_min(long time_min)
	{
		this.time_min = time_min;
	}

}
