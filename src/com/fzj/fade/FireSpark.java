package com.fzj.fade;

/**
 * @author Fan Zhengjie
 * @date 2016年11月11日 下午3:49:58
 * @version 1.0
 * @description 烟花、火星，表示一个可行解
 */
public class FireSpark implements Cloneable {

	private int D;// 维度
	private double fitness;// 适应度值
	private int x[];// 位移向量，表示分配策略
	private double Pi;// 累积概率
	private int ct;// 当前进化代数
	


	public FireSpark() {
		// TODO Auto-generated constructor stub
	}

	public FireSpark(int D) {
		this.D = D;
		this.x = new int[D];
	}


	

	
	public int getD() {
		return D;
	}

	public void setD(int d) {
		D = d;
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

	public double getPi() {
		return Pi;
	}

	public void setPi(double pi) {
		Pi = pi;
	}

	public int getCt() {
		return ct;
	}

	public void setCt(int ct) {
		this.ct = ct;
	}

	public Object clone() {
		FireSpark fireSpark = null;
		try {
			fireSpark = (FireSpark) super.clone();
			fireSpark.x = this.x.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return fireSpark;

	}

}
