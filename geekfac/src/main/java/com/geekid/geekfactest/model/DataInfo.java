package com.geekid.geekfactest.model;

import java.io.Serializable;

public class DataInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int temperature;
	private int humidity;
    private int status;

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    private long time;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
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
	
}
