package com.geecare.blelibrary.model;

public class BleDevice implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;//蓝牙设备id
	private String userId;//设备使用者的userId
	private String productType; // 产品类型
	private String model; // 型号
	private String sid; // 序列号
	private String bleMacAddr; // 蓝牙MAC地址
	private String bleName; // 蓝牙设备名称
	private String name; // 自定义名称 
	private String romVersion;//固件版本
	private String scene;//使用场景
	private String info;//简介
	private String bleBondState; // 蓝牙绑定状态
	private String bleDeviceType; // 蓝牙设备类型
	private String status; // 状态标记0未同步、1已同步
	private int stateOfCharge; // 剩余电量
	private int rssi;//蓝牙信号

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public int getStateOfCharge() {
		return stateOfCharge;
	}

	public void setStateOfCharge(int stateOfCharge) {
		this.stateOfCharge = stateOfCharge;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getBleMacAddr() {
		return bleMacAddr;
	}

	public void setBleMacAddr(String bleMacAddr) {
		this.bleMacAddr = bleMacAddr;
	}

	public String getBleName() {
		return bleName;
	}

	public void setBleName(String bleName) {
		this.bleName = bleName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRomVersion() {
		return romVersion;
	}

	public void setRomVersion(String romVersion) {
		this.romVersion = romVersion;
	}

	public String getScene() {
		return scene;
	}

	public void setScene(String scene) {
		this.scene = scene;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getBleBondState() {
		return bleBondState;
	}

	public void setBleBondState(String bleBondState) {
		this.bleBondState = bleBondState;
	}

	public String getBleDeviceType() {
		return bleDeviceType;
	}

	public void setBleDeviceType(String bleDeviceType) {
		this.bleDeviceType = bleDeviceType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}
}
