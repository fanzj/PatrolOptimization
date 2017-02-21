package com.fzj.linear;

/**
 * @author Fan Zhengjie
 * @date 2016��10��25�� ����6:23:44
 * @version 1.0
 * @description
 */
public class Pos implements Cloneable {

	private int num;//���
	private int region;// ����������
	private int road;// ������·��
	private int cur_patrol;// ��ǰ�������Ѳ�ߵ�λ��
	private double payoff;// �������棬�����������
	private int rank;// ����
	private double fitness;// Ŀ��ֵ
	private double cur_payoff;// ��ǰ����
	private double next_payoff;// ����һ����λʱ������

	public Pos() {
		// TODO Auto-generated constructor stub
	}
	
	public void setNum(int num) {
		this.num = num;
	}
	
	public int getNum() {
		return num;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public double getCur_payoff() {
		return cur_payoff;
	}

	public void setCur_payoff(double cur_payoff) {
		this.cur_payoff = cur_payoff;
	}

	public double getNext_payoff() {
		return next_payoff;
	}

	public void setNext_payoff(double next_payoff) {
		this.next_payoff = next_payoff;
	}

	public Pos(int region, int road) {
		this.region = region;
		this.road = road;
	}

	public int getRegion() {
		return region;
	}

	public void setRegion(int region) {
		this.region = region;
	}

	public int getRoad() {
		return road;
	}

	public void setRoad(int road) {
		this.road = road;
	}

	public int getCur_patrol() {
		return cur_patrol;
	}

	public void setCur_patrol(int cur_patrol) {
		this.cur_patrol = cur_patrol;
	}

	public double getPayoff() {
		return payoff;
	}

	public void setPayoff(double payoff) {
		this.payoff = payoff;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public Object clone() {
		Pos pos = null;
		try {
			pos = (Pos) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pos;
	}
}
