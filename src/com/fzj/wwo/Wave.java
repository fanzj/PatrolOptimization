package com.fzj.wwo;

import java.math.BigDecimal;
import java.util.Random;

import com.fzj.ga.Region;

/**
 * @author Fan Zhengjie
 * @date 2016��8��1�� ����8:12:34
 * @version 1.0
 * @description ˮ���Ż��㷨��һ��ˮ������һ����
 */
public class Wave implements Cloneable {

	private int h;// ����
	private double w;// ����
	private double fitness;// ��Ӧ��ֵ
	// private int x[];//��������
	private Region patrol_allocation[];
	// private int dimen;//���ά��
	private int regionNum;// ������
	private int current_t;// ��ǰ����
	private int currentPatrolUnits;// ��ǰ��Ѳ�ߵ�λ
	

	public Wave() {
		// TODO Auto-generated constructor stub
	}

	public Wave(int regionNum) {
		this.regionNum = regionNum;
		this.patrol_allocation = new Region[regionNum];
	}
	

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
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

	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
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

	public int getCurrentPatrolUnits() {
		return currentPatrolUnits;
	}

	public void setCurrentPatrolUnits(int currentPatrolUnits) {
		this.currentPatrolUnits = currentPatrolUnits;
	}

	@Override
	public Object clone() {
		Wave wave = null;
		try {
			wave = (Wave) super.clone();
			wave.patrol_allocation = new Region[regionNum];
			for(int i=0;i<regionNum;i++){
				wave.patrol_allocation[i] = (Region) patrol_allocation[i].clone();
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return wave;
	}

	public static void main(String[] args) {
		Random random = new Random();
		for (int i = 0; i < 1000000; i++) {
			double r = (-1 + 2 * random.nextDouble());
			double r2 = (-1 + 2 * random.nextDouble());
			double sum = r * r2;
			long a = Math.round(sum);
			System.out.println("sum" + i + " = " + a);
		}

	}
}
