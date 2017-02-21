package com.fzj.linear;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import com.fzj.ga.Region;
import com.fzj.model.PatrolModel;
import com.fzj.strategy.Strategy;
import com.fzj.utils.FileUtils;

/**
 * @author Fan Zhengjie
 * @date 2016年10月25日 下午6:04:58
 * @version 1.0
 * @description Chelst 论文中的方法
 */
public class LinearStrategy extends Strategy {

	private Pos[] allPos;// 存放解

	public LinearStrategy() {
		// TODO Auto-generated constructor stub
	}

	public LinearStrategy(int patrolUnits, PatrolModel patrolModel) {
		super(0, patrolUnits, patrolUnits, patrolModel);
	}

	@Override
	protected void init2() throws IOException {
		init();
		allPos = new Pos[D];

		this.TYPE_NAME = "Chelst";
		this.RESULT_NAME = "result.txt";
	}

	@Override
	protected void initPopulation() {
		int x = 0;
		for (int j = 0; j < regionNum; j++) {
			int len = roadNum[j];
			for (int k = 0; k < len; k++) {
				Pos pos = new Pos(j, k);
				pos.setCur_patrol(0);
				pos.setPayoff(0);
				pos.setCur_payoff(0);
				pos.setNext_payoff(0);
				pos.setFitness(0);
				pos.setRank(0);
				pos.setNum(x+1);
				allPos[x++] = pos;
			}
		}

	}

	private void calPayoff(Pos pos) {
		int j, k;

		int x = pos.getCur_patrol();// 当前所分配的巡逻单位数

		double P[] = new double[crimeNum];
		double P2[] = new double[crimeNum];

		int periodLen = TimeEffect.length;
		double payoff = 0, cur_payoff = 0, next_payoff = 0;
		j = pos.getRegion();// 所属区域
		k = pos.getRoad();// 所属路段
		for (int i = 0; i < crimeNum; i++) {
			double temp = x * V * TD[i] * CD[i];
			P[i] = temp / (temp + RoadLength[j][k] * 1000);// 因为数组中是km,转化为m

			temp = (x + 1) * V * TD[i] * CD[i];
			P2[i] = temp / (temp + RoadLength[j][k] * 1000);// 因为数组中是km,转化为m

			double r1 = W[i] * F[i][j][k] * (P[i]);
			double r2 = W[i] * F[i][j][k] * (P2[i]);
			for (int l = 0; l < periodLen; l++) {
				payoff += (r2 - r1) * T[j][k][l];
				cur_payoff += r1 * T[j][k][l];
				next_payoff += r2 * T[j][k][l];
			}
		}
		pos.setPayoff(payoff);
		pos.setCur_payoff(cur_payoff);
		pos.setNext_payoff(next_payoff);
		pos.setFitness(cur_payoff);
	}
	
	private double calFitness(){
	
		double fitness = 0.0;
		for(int d=0;d<D;d++){
			fitness += allPos[d].getFitness();
		}
		return fitness;
	}

	private void sortPosByPayoff() {
		PayoffComparator payoffComparator = new PayoffComparator();
		Arrays.sort(allPos, payoffComparator);
	}
	
	private void sortPosByFitness(){
		FitnessComparator fitnessComparator = new FitnessComparator();
		Arrays.sort(allPos, fitnessComparator);
	}
	
	private void sortPosByNum(){
		NumComparator numComparator = new NumComparator();
		Arrays.sort(allPos, numComparator);
	}

	private void solve() throws IOException {
		init2();
		initPopulation();

		for (t = 0; t < max_t; t++) {// 每次分配一个单位，一共patrolUnits次

			// 对每个路段计算增量收益
			for (int j = 0; j < D; j++)
				calPayoff(allPos[j]);

			// 按增量收益排序
			sortPosByPayoff();
			int x = allPos[0].getCur_patrol();
			allPos[0].setCur_patrol(++x);
			allPos[0].setFitness(allPos[0].getNext_payoff());
			for (int j = 1; j <= D; j++)
				allPos[j - 1].setRank(j);
			// printPos();
		}
		
		double fitness = calFitness();
		sortPosByFitness();
		String result = printPos();
		result += "fitness = "+fitness+"\n";
		FileUtils.saveResult(result, TYPE_NAME, RESULT_NAME, FileUtils.RESULT_PATH, PATH);

		sortPosByNum();
		String alloc = "";
		for(int i=0;i<D;i++){
			alloc += allPos[i].getCur_patrol()+"\n";
		}
		FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
	}

	/**
	 * 打印测试
	 */
	private String printPos() {
		String result = "";
		for (int j = 0; j < D; j++) {
			Pos pos = allPos[j];
			result += "j = " + pos.getRegion() + ",k = " + pos.getRoad() + ",rank = " + pos.getRank() + ",x = "
					+ pos.getCur_patrol() + ",payoff = " + pos.getPayoff()+"\n";
		}
		return result;
	}

	/*
	 * 模拟运行
	 */
	public static void main(String[] args) throws IOException {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		PatrolModel patrolModel = new PatrolModel(PATH);
		LinearStrategy strategy = new LinearStrategy(20 * patrolModel.getRegionNum(), patrolModel);
		strategy.solve();
	}

}
