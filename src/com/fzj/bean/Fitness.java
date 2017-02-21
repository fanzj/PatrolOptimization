package com.fzj.bean;
/** 
 * @author Fan Zhengjie 
 * @date 2016年10月23日 下午1:42:45 
 * @version 1.0 
 * @description
 */
public class Fitness {

	private int id;//第id次迭代
	private double fitness;//当前代数的最优适应度值
	
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
