package com.fzj.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fzj.data.RandomGenerateData;
import com.fzj.utils.FileUtils;

/**
 * @author Fan Zhengjie
 * @date 2016年7月22日 下午4:32:45
 * @version 1.0
 * @description 巡逻模型的配置参数
 */
public class PatrolModel {

	private int crimeNum;// 犯罪类型数
	private int regionNum;// 区域数
	private int roadNum[];// 各个区域的路段数

	private double V;// 巡逻速度
	private double W[];// 犯罪权重
	private double TD[];// 犯罪持续时间
	private double CD[];// 犯罪被检测到的概率
	private double PositionEffect[][];// 不同位置的交通拥堵程度
	private double RoadLength[][];// 不同路段的长度，km
	private double TimeEffect[];// 不同时间的交通拥堵程度
	private double F[][][];// 不同犯罪在不同区域的犯罪发生频率F[i][j][k],犯罪i在区域j路段k的发生频率
	private HashMap<String, List<Integer>> map;// 基本参数，包括：1.犯罪数；2.区域数；3.各个区域的路段数
	
	private int D;

	public PatrolModel(String path) throws IOException {
		this.V = FileUtils.readV(RandomGenerateData.V_NAME, path);// 读取巡逻速度
		this.D = 0;
		this.map = FileUtils.readBasic(RandomGenerateData.BASIC_NAME, path);// 基本参数
		this.crimeNum = map.get("CrimeNum").get(0);
		this.regionNum = map.get("RegionNum").get(0);
		this.roadNum = new int[regionNum];
		List<Integer> list = map.get("RoadNum");
		for (int i = 0; i < list.size(); i++) {
			this.roadNum[i] = Integer.valueOf(list.get(i));
			this.D += roadNum[i];
		}

		this.W = FileUtils.readCrimeData(RandomGenerateData.W_NAME, crimeNum, path);
		this.TD = FileUtils.readCrimeData(RandomGenerateData.TD_NAME, crimeNum, path);
		this.CD = FileUtils.readCrimeData(RandomGenerateData.CD_NAME, crimeNum, path);

		this.PositionEffect = FileUtils.readRoad(RandomGenerateData.POS_NAME, regionNum, path);
		this.RoadLength = FileUtils.readRoad(RandomGenerateData.RL_NAME, regionNum, path);

		this.TimeEffect = FileUtils.readTimeEffect(RandomGenerateData.TIME_NAME, path);

		this.F = FileUtils.readF(RandomGenerateData.F_NAME, crimeNum, regionNum, roadNum, path);

	}

	public int getCrimeNum() {
		return crimeNum;
	}

	public void setCrimeNum(int crimeNum) {
		this.crimeNum = crimeNum;
	}

	public int getRegionNum() {
		return regionNum;
	}

	public void setRegionNum(int regionNum) {
		this.regionNum = regionNum;
	}

	public int[] getRoadNum() {
		return roadNum;
	}

	public void setRoadNum(int[] roadNum) {
		this.roadNum = roadNum;
	}

	public double getV() {
		return V;
	}

	public void setV(double v) {
		V = v;
	}

	public double[] getW() {
		return W;
	}

	public void setW(double[] w) {
		W = w;
	}

	public double[] getTD() {
		return TD;
	}

	public void setTD(double[] tD) {
		TD = tD;
	}

	public double[] getCD() {
		return CD;
	}

	public void setCD(double[] cD) {
		CD = cD;
	}

	public double[][] getPositionEffect() {
		return PositionEffect;
	}

	public void setPositionEffect(double[][] positionEffect) {
		PositionEffect = positionEffect;
	}

	public double[][] getRoadLength() {
		return RoadLength;
	}

	public void setRoadLength(double[][] roadLength) {
		RoadLength = roadLength;
	}

	public double[][][] getF() {
		return F;
	}

	public void setF(double[][][] f) {
		F = f;
	}

	public HashMap<String, List<Integer>> getMap() {
		return map;
	}

	public void setMap(HashMap<String, List<Integer>> map) {
		this.map = map;
	}

	public double[] getTimeEffect() {
		return TimeEffect;
	}

	public void setTimeEffect(double[] timeEffect) {
		TimeEffect = timeEffect;
	}
	
	public int getD() {
		return D;
	}
	
	public void setD(int d) {
		D = d;
	}

	public static void main(String[] args) throws IOException {
		PatrolModel patrolModel = new PatrolModel("data_01\\");
		System.out.println("1.巡逻速度：");
		System.out.println("V = " + patrolModel.V);

		System.out.println("2.基本参数：");
		System.out.println("犯罪数 = " + patrolModel.crimeNum);
		System.out.println("区域数 = " + patrolModel.regionNum);
		System.out.print("各个区域路段数 = [");
		for (int i = 0; i < patrolModel.regionNum; i++) {
			System.out.print(patrolModel.roadNum[i]);
			if (i < patrolModel.regionNum - 1)
				System.out.print(",");
			else
				System.out.println("]");
		}

		System.out.println("3.犯罪权重：");
		System.out.print("[");
		for (int i = 0; i < patrolModel.crimeNum; i++) {
			System.out.print(patrolModel.W[i]);
			if (i < patrolModel.crimeNum - 1)
				System.out.print(",");
			else
				System.out.println("]");
		}

		System.out.println("4.犯罪持续时间：");
		System.out.print("[");
		for (int i = 0; i < patrolModel.crimeNum; i++) {
			System.out.print(patrolModel.TD[i]);
			if (i < patrolModel.crimeNum - 1)
				System.out.print(",");
			else
				System.out.println("]");
		}

		System.out.println("5.犯罪被检测到的概率：");
		System.out.print("[");
		for (int i = 0; i < patrolModel.crimeNum; i++) {
			System.out.print(patrolModel.CD[i]);
			if (i < patrolModel.crimeNum - 1)
				System.out.print(",");
			else
				System.out.println("]");
		}

		System.out.println("6.不同位置的交通拥挤程度：");
		for (int i = 0; i < patrolModel.regionNum; i++) {
			for (int j = 0; j < patrolModel.roadNum[i]; j++) {
				System.out.print(patrolModel.PositionEffect[i][j] + " ");
			}
			System.out.println();
		}

		System.out.println("7.不同区域的路段长度：");
		for (int i = 0; i < patrolModel.regionNum; i++) {
			for (int j = 0; j < patrolModel.roadNum[i]; j++) {
				System.out.print(patrolModel.RoadLength[i][j] + " ");
			}
			System.out.println();
		}

		System.out.println("8.不同时间的交通拥挤程度：");
		for (int i = 0; i < 3; i++) {
			System.out.print(patrolModel.TimeEffect[i] + " ");
		}
		System.out.println();

		System.out.println("9.不同犯罪在不同区域不同路段的发生频率：");
		for (int i = 0; i < patrolModel.crimeNum; i++) {
			for (int j = 0; j < patrolModel.regionNum; j++) {
				for (int k = 0; k < patrolModel.roadNum[j]; k++) {
					System.out.print(patrolModel.F[i][j][k] + " ");
				}
				System.out.println();
			}
			System.out.println();
		}
	}
}
