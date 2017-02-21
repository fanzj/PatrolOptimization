package com.fzj.dnspso;

/**
 * @author Fan Zhengjie
 * @date 2016��11��11�� ����3:49:58
 * @version 1.0
 * @description
 */
public class DnsParticle implements Cloneable {

	private int D;// ά��
	private double fitness;// ��Ӧ��ֵ
	private int x[];// λ����������ʾ�������
	private int v[];// �ٶ�����
	private int ct;// ��ǰ��������
	
	public DnsParticle() {
		// TODO Auto-generated constructor stub
	}
	
	public DnsParticle(int D){
		this.D = D;
		this.x = new int[D];
		this.v = new int[D];
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

	public int[] getV() {
		return v;
	}

	public void setV(int[] v) {
		this.v = v;
	}

	public int getCt() {
		return ct;
	}

	public void setCt(int ct) {
		this.ct = ct;
	}

	public Object clone() {
		DnsParticle dParticle = null;
		try {
			dParticle = (DnsParticle) super.clone();
			dParticle.x = this.x.clone();
			dParticle.v = this.v.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return dParticle;

	}

}
