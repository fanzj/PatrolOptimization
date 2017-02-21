package com.fzj.ga;

import java.util.Arrays;

/**
 * @author Fan Zhengjie
 * @date 2016年7月22日 下午3:15:53
 * @version 1.0
 * @description
 */
public class Region implements Cloneable {

	private int roadNum;// 该区域的路段数
	private int region_allocation[];// 该区域的巡逻单位分配策略，region_allocation[i]表示路段i分配的巡逻单位数

	private int v[];
	
	public Region(int roadNum) {
		this.roadNum = roadNum;
		region_allocation = new int[roadNum];
		v = new int[roadNum];
	}
	
	public int[] getV() {
		return v;
	}
	
	public void setV(int[] v) {
		this.v = v;
	}

	public int getRoadNum() {
		return roadNum;
	}

	public void setRoadNum(int roadNum) {
		this.roadNum = roadNum;
	}

	public int[] getRegion_allocation() {
		return region_allocation;
	}

	public void setRegion_allocation(int[] region_allocation) {
		this.region_allocation = region_allocation;
	}

	@Override
	public Object clone() {
		Region region = null;
		try {
			region = (Region) super.clone();
			region.region_allocation = region_allocation.clone();
			region.v = v.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return region;
	}

	@Override
	public String toString() {
		return "Region [allocation = " + Arrays.toString(region_allocation) + ", roadNum = " + roadNum + "]";
	}

	public static void main(String[] args) {
		// 测试clone
		Region region = new Region(3);
		region.setRegion_allocation(new int[] { 1, 2, 3 });
		System.out.println("region1: " + region);

		Region region2 = (Region) region.clone();
		region2.setRoadNum(5);
		int allocation[] = region2.getRegion_allocation();
		allocation[0] = 10;
		System.out.println("region2: " + region2);
		System.out.println("region1: " + region);
	}
}
