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
 * @date 2016��7��22�� ����4:32:45
 * @version 1.0
 * @description Ѳ��ģ�͵����ò���
 */
public class PatrolModel {

	private int crimeNum;// ����������
	private int regionNum;// ������
	private int roadNum[];// ���������·����

	private double V;// Ѳ���ٶ�
	private double W[];// ����Ȩ��
	private double TD[];// �������ʱ��
	private double CD[];// ���ﱻ��⵽�ĸ���
	private double PositionEffect[][];// ��ͬλ�õĽ�ͨӵ�³̶�
	private double RoadLength[][];// ��ͬ·�εĳ��ȣ�km
	private double TimeEffect[];// ��ͬʱ��Ľ�ͨӵ�³̶�
	private double F[][][];// ��ͬ�����ڲ�ͬ����ķ��﷢��Ƶ��F[i][j][k],����i������j·��k�ķ���Ƶ��
	private HashMap<String, List<Integer>> map;// ����������������1.��������2.��������3.���������·����
	
	private int D;

	public PatrolModel(String path) throws IOException {
		this.V = FileUtils.readV(RandomGenerateData.V_NAME, path);// ��ȡѲ���ٶ�
		this.D = 0;
		this.map = FileUtils.readBasic(RandomGenerateData.BASIC_NAME, path);// ��������
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
		System.out.println("1.Ѳ���ٶȣ�");
		System.out.println("V = " + patrolModel.V);

		System.out.println("2.����������");
		System.out.println("������ = " + patrolModel.crimeNum);
		System.out.println("������ = " + patrolModel.regionNum);
		System.out.print("��������·���� = [");
		for (int i = 0; i < patrolModel.regionNum; i++) {
			System.out.print(patrolModel.roadNum[i]);
			if (i < patrolModel.regionNum - 1)
				System.out.print(",");
			else
				System.out.println("]");
		}

		System.out.println("3.����Ȩ�أ�");
		System.out.print("[");
		for (int i = 0; i < patrolModel.crimeNum; i++) {
			System.out.print(patrolModel.W[i]);
			if (i < patrolModel.crimeNum - 1)
				System.out.print(",");
			else
				System.out.println("]");
		}

		System.out.println("4.�������ʱ�䣺");
		System.out.print("[");
		for (int i = 0; i < patrolModel.crimeNum; i++) {
			System.out.print(patrolModel.TD[i]);
			if (i < patrolModel.crimeNum - 1)
				System.out.print(",");
			else
				System.out.println("]");
		}

		System.out.println("5.���ﱻ��⵽�ĸ��ʣ�");
		System.out.print("[");
		for (int i = 0; i < patrolModel.crimeNum; i++) {
			System.out.print(patrolModel.CD[i]);
			if (i < patrolModel.crimeNum - 1)
				System.out.print(",");
			else
				System.out.println("]");
		}

		System.out.println("6.��ͬλ�õĽ�ͨӵ���̶ȣ�");
		for (int i = 0; i < patrolModel.regionNum; i++) {
			for (int j = 0; j < patrolModel.roadNum[i]; j++) {
				System.out.print(patrolModel.PositionEffect[i][j] + " ");
			}
			System.out.println();
		}

		System.out.println("7.��ͬ�����·�γ��ȣ�");
		for (int i = 0; i < patrolModel.regionNum; i++) {
			for (int j = 0; j < patrolModel.roadNum[i]; j++) {
				System.out.print(patrolModel.RoadLength[i][j] + " ");
			}
			System.out.println();
		}

		System.out.println("8.��ͬʱ��Ľ�ͨӵ���̶ȣ�");
		for (int i = 0; i < 3; i++) {
			System.out.print(patrolModel.TimeEffect[i] + " ");
		}
		System.out.println();

		System.out.println("9.��ͬ�����ڲ�ͬ����ͬ·�εķ���Ƶ�ʣ�");
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
