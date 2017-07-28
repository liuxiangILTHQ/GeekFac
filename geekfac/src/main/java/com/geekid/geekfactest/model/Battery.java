package com.geekid.geekfactest.model;

import java.io.Serializable;

public class Battery implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	private int id;
	private int capacity;
	private long time;
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getCapacity()
	{
		return capacity;
	}
	public void setCapacity(int capacity)
	{
		this.capacity = capacity;
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
