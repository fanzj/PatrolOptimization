package com.fzj.bbo;
/** 
 * @author Fan Zhengjie 
 * @date 2016年7月28日 下午7:02:29 
 * @version 1.0 
 * @description 栖息地，代表一个可行解
 */
public class Habitat implements Cloneable{

	private int SIV[];//适宜度指数变量
	private int s;//该栖息地的物种数量
	private double HSI;//栖息地适宜度指数
	private double im;//该栖息地的物种迁入率
	private double em;//该栖息地的物种迁出率
	private int current_t;//当前进化代数
	private int dimen;//解的维度
	private double mu;//该栖息地的物种变异率
	private double Pi;//累积概率，用于轮盘赌选择（迁移操作）
	private int currentPatrolUnits;//当前的巡逻单位数
	private double Ps;//栖息地i容纳Si种生物种群的概率
	private boolean isElite;//是否为精英个体
	
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
