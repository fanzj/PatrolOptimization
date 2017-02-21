package com.fzj.bbo;
/** 
 * @author Fan Zhengjie 
 * @date 2016��7��28�� ����7:02:29 
 * @version 1.0 
 * @description ��Ϣ�أ�����һ�����н�
 */
public class Habitat implements Cloneable{

	private int SIV[];//���˶�ָ������
	private int s;//����Ϣ�ص���������
	private double HSI;//��Ϣ�����˶�ָ��
	private double im;//����Ϣ�ص�����Ǩ����
	private double em;//����Ϣ�ص�����Ǩ����
	private int current_t;//��ǰ��������
	private int dimen;//���ά��
	private double mu;//����Ϣ�ص����ֱ�����
	private double Pi;//�ۻ����ʣ��������̶�ѡ��Ǩ�Ʋ�����
	private int currentPatrolUnits;//��ǰ��Ѳ�ߵ�λ��
	private double Ps;//��Ϣ��i����Si��������Ⱥ�ĸ���
	private boolean isElite;//�Ƿ�Ϊ��Ӣ����
	
	public Habitat(){
		
	}
	
	public Habitat(int dimen){
		this.dimen = dimen;
		this.SIV = new int[dimen];
	}
	
	public void setElite(boolean isElite) {
		this.isElite = isElite;
	}
	
	
	
	
	public boolean isElite() {
		return isElite;
	}

	public void setPs(double ps) {
		Ps = ps;
	}
	
	public double getPs() {
		return Ps;
	}
	
	public int getCurrentPatrolUnits() {
		return currentPatrolUnits;
	}

	public void setCurrentPatrolUnits(int currentPatrolUnits) {
		this.currentPatrolUnits = currentPatrolUnits;
	}

	public double getPi() {
		return Pi;
	}

	public void setPi(double pi) {
		Pi = pi;
	}

	public int[] getSIV() {
		return SIV;
	}



	public void setSIV(int[] sIV) {
		SIV = sIV;
	}



	public int getS() {
		return s;
	}



	public void setS(int s) {
		this.s = s;
	}



	public double getHSI() {
		return HSI;
	}



	public void setHSI(double hSI) {
		HSI = hSI;
	}



	public double getIm() {
		return im;
	}



	public void setIm(double im) {
		this.im = im;
	}



	public double getEm() {
		return em;
	}



	public void setEm(double em) {
		this.em = em;
	}



	public int getCurrent_t() {
		return current_t;
	}



	public void setCurrent_t(int current_t) {
		this.current_t = current_t;
	}



	public int getDimen() {
		return dimen;
	}



	public void setDimen(int dimen) {
		this.dimen = dimen;
	}



	public double getMu() {
		return mu;
	}



	public void setMu(double mu) {
		this.mu = mu;
	}



	@Override
	public Object clone()  {
		Habitat habitat = null;
		try{
			habitat = (Habitat) super.clone();
			habitat.SIV = this.SIV.clone();
		}
		catch(CloneNotSupportedException e){
			e.printStackTrace();
		}
		return habitat;
	}

	
	
}
