package com.fzj.ga;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fan Zhengjie
 * @date 2016��7��22�� ����2:55:50
 * @version 1.0
 * @description һ��Ⱦɫ�壬��ʾһ���⣬�����˸�������Ѳ�ߵ�λ�ķ������
 */
public class Solution implements Cloneable {

	private double fitness; // ��Ӧ��ֵ��ģ�͵�Ŀ��ֵ
	private int current_t; // ��ǰ��������
	private Region patrol_allocation[]; // �������,patrol_allcation[i]������i�ķ������
	private int regionNum; // ������Ŀ����ʾȾɫ�峤��
	private double Pi; // �ۼƽ�������

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
