package com.fzj.fade;

/**
 * @author Fan Zhengjie
 * @date 2016��11��11�� ����3:49:58
 * @version 1.0
 * @description �̻������ǣ���ʾһ�����н�
 */
public class FireSpark implements Cloneable {

	private int D;// ά��
	private double fitness;// ��Ӧ��ֵ
	private int x[];// λ����������ʾ�������
	private double Pi;// �ۻ�����
	private int ct;// ��ǰ��������
	


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
