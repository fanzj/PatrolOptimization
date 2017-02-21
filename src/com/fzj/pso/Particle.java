package com.fzj.pso;

/**
 * @author Fan Zhengjie
 * @date 2016��7��26�� ����4:03:59
 * @version 1.0
 * @description һ�����ӣ���ʾһ���⣬��λ�ƾ���Ѳ�ߵ�λ���䷽��
 */
public class Particle implements Cloneable {

	private int dimen;// ά��
	private double fitness;// ��Ӧ��ֵ
	private int x[];// λ����������ʾ�������
	private int v[];// �ٶ�����
	private int current_t;// ��ǰ��������

	public Particle() {
	}

	public Particle(int dimen) {
		this.dimen = dimen;
		this.x = new int[dimen];
		this.v = new int[dimen];
	}

	public int getDimen() {
		return dimen;
	}

	public void setDimen(int dimen) {
		this.dimen = dimen;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int[] getX() {
		return x;
	}

	public void setX(int[] x) {
		this.x = x;
	}

	public int[] getV() {
		return v;
	}

	public void setV(int[] v) {
		this.v = v;
	}

	public int getCurrent_t() {
		return current_t;
	}

	public void setCurrent_t(int current_t) {
		this.current_t = current_t;
	}

	@Override
	public Object clone() {
		Particle particle = null;
		try {
			particle = (Particle) super.clone();
			particle.v = this.v.clone();
			particle.x = this.x.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return particle;
	}
}
