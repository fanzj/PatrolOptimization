package com.fzj.ga;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fan Zhengjie
 * @date 2016年7月22日 下午2:55:50
 * @version 1.0
 * @description 一条染色体，表示一个解，代表了各个区，巡逻单位的分配情况
 */
public class Solution implements Cloneable {

	private double fitness; // 适应度值，模型的目标值
	private int current_t; // 当前进化代数
	private Region patrol_allocation[]; // 分配策略,patrol_allcation[i]，区域i的分配策略
	private int regionNum; // 区域数目，表示染色体长度
	private double Pi; // 累计进化概率

	public Solution() {
	}

	public Solution(int regionNum) {
		this.regionNum = regionNum;
		patrol_allocation = new Region[regionNum];
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int getCurrent_t() {
		return current_t;
	}

	public void setCurrent_t(int current_t) {
		this.current_t = current_t;
	}

	public Region[] getPatrol_allocation() {
		return patrol_allocation;
	}

	public void setPatrol_allocation(Region[] patrol_allocation) {
		this.patrol_allocation = patrol_allocation;
	}

	public int getRegionNum() {
		return regionNum;
	}

	public void setRegionNum(int regionNum) {
		this.regionNum = regionNum;
	}

	public double getPi() {
		return Pi;
	}

	public void setPi(double pi) {
		Pi = pi;
	}

	@Override
	public Object clone() {
		Solution solution = null;
		try {
			solution = (Solution) super.clone();
			solution.patrol_allocation = new Region[regionNum];
			for(int i=0;i<regionNum;i++)
			{
				solution.patrol_allocation[i] = (Region) patrol_allocation[i].clone();
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return solution;
	}

}
