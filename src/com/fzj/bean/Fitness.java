package com.fzj.bean;
/** 
 * @author Fan Zhengjie 
 * @date 2016��10��23�� ����1:42:45 
 * @version 1.0 
 * @description
 */
public class Fitness {

	private int id;//��id�ε���
	private double fitness;//��ǰ������������Ӧ��ֵ
	
	public Fitness(){}
	
	public Fitness(int id,double fitness){
		this.id = id;
		this.fitness = fitness;
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public double getFitness() {
		return fitness;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
