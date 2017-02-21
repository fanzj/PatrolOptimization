package com.fzj.pso;

/**
 * @author Fan Zhengjie
 * @date 2016年7月26日 下午4:03:59
 * @version 1.0
 * @description 一个粒子，表示一个解，其位移就是巡逻单位分配方案
 */
public class Particle implements Cloneable {

	private int dimen;// 维度
	private double fitness;// 适应度值
	private int x[];// 位移向量，表示分配策略
	private int v[];// 速度向量
	private int current_t;// 当前进化代数

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
