package com.geekid.geekfactest.model;

import java.io.Serializable;

/**
 * 尿布更换信息
 * @author Administrator
 *
 */
public class DiaperChangeInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id;
	//更换时间
	private long time;
	//煎熬时间(秒)
	private long suffering_time;
	public long getTime()
	{
		return time;
	}
	public void setTime(long time)
	{
		this.time = time;
	}
	public long getSuffering_time()
	{
		return suffering_time;
	}
	public void setSuffering_time(long suffering_time)
	{
		this.suffering_time = suffering_time;
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}

}
