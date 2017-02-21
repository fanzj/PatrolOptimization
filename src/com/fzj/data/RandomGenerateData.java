package com.fzj.data;

import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.Random;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import com.fzj.strategy.Strategy;
import com.fzj.utils.FileUtils;

/**
 * @author Fan Zhengjie
 * @date 2016��9��24�� ����1:10:51
 * @version 1.0
 * @description �������ʵ������
 */
public class RandomGenerateData {

	public static final String BASIC_NAME = "Basic.txt";
	public static final String V_NAME = "V.txt";
	public static final String W_NAME = "W.txt";
	public static final String TD_NAME = "TD.txt";
	public static final String CD_NAME = "CD.txt";
	public static final String POS_NAME = "PositionEffect.txt";
	public static final String RL_NAME = "RoadLength.txt";
	public static final String TIME_NAME = "TimeEffect.txt";
	public static final String F_NAME = "F.txt";

	private Random random;
	private int CrimeNum;// ����������
	private int RegionNum;// ������
	private int RoadNum[];

	public RandomGenerateData(int CrimeNum, int RegionNum) {
		this.CrimeNum = CrimeNum;
		this.RegionNum = RegionNum;
		this.RoadNum = new int[RegionNum];
		this.random = new Random(System.currentTimeMillis());
	}

	/**
	 * ����[a,b]֮����������
	 * 
	 * @param a
	 *            ����
	 * @param b
	 *            ����
	 * @return
	 */
	private int randIntA2B(int a, int b) {
		return a + random.nextInt(b - a + 1);
	}

	/**
	 * �������[a,b]֮ǰ�ĸ�����
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private double randDoubleA2B(double a, double b) {
		return a + (b - a) * random.nextDouble();
	}

	/**
	 * 1.���ɻ�������
	 */
	private void generateBasic(String path) {
		String content = "CrimeNum = " + CrimeNum + "\nRegionNum = " + RegionNum + "\nRoadNum = ";
		for (int i = 0; i < RoadNum.length; i++) {
			RoadNum[i] = randIntA2B(5, 20);
			content += RoadNum[i];
			if (i < RoadNum.length - 1)
				content += ",";
		}
		FileUtils.saveResult(content, "", BASIC_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 2.����Ѳ���ٶ�
	 */
	private void generateV(String path) {
		String content = "";
		content += randDoubleA2B(1, 20);
		FileUtils.saveResult(content, "", V_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 3.���ɷ���Ȩ��
	 */
	private void generateW(String path) {
		String content = "";
		for (int i = 0; i < CrimeNum; i++) {
			content += randDoubleA2B(0.01, 1);
			if (i < CrimeNum - 1)
				content += " ";
		}
		FileUtils.saveResult(content, "", W_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 4.���ɷ������ʱ��
	 */
	private void generateTD(String path) {
		String content = "";
		for (int i = 0; i < CrimeNum; i++) {
			content += randDoubleA2B(1, 60);
			if (i < CrimeNum - 1)
				content += " ";
		}
		FileUtils.saveResult(content, "", TD_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 5.���ɷ��ﱻ��⵽�ĸ���
	 */
	private void generateCD(String path) {
		String content = "";
		for (int i = 0; i < CrimeNum; i++) {
			content += randDoubleA2B(0.01, 0.3);
			if (i < CrimeNum - 1)
				content += " ";
		}
		FileUtils.saveResult(content, "", CD_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 6.���ɲ�ͬλ�õĽ�ͨӵ�³̶�
	 */
	private void generatePosEffect(String path) {
		String content = "";
		for (int i = 0; i < RegionNum; i++) {
			int len = RoadNum[i];
			for (int j = 0; j < len; j++) {
				content += randDoubleA2B(0.01, 1);
				if (j < len - 1)
					content += " ";
				else if (i < RegionNum - 1)
					content += "\n";
			}
		}
		FileUtils.saveResult(content, "", POS_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 7.���ɲ�ͬ·�εĳ���
	 */
	private void generateRoadLength(String path) {
		String content = "";
		for (int i = 0; i < RegionNum; i++) {
			int len = RoadNum[i];
			for (int j = 0; j < len; j++) {
				content += randDoubleA2B(0.5, 5);
				if (j < len - 1)
					content += " ";
				else if (i < RegionNum - 1)
					content += "\n";
			}
		}
		FileUtils.saveResult(content, "", RL_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 8.���ɲ�ͬʱ��Ľ�ͨӵ�³̶�
	 */
	private void generateTimeEffect(String path) {
		String content = "";
		int[] level = new int[] { 5, 5, 5, 5, 5, 5, 3, 1, 1, 2, 3, 3, 3, 3, 3, 3, 2, 1, 1, 1, 2, 3, 4, 4 };
		for (int i = 0; i < level.length; i++) {
			switch (level[i]) {
			case 1:
				content += randDoubleA2B(0.01, 0.2);
				break;
			case 2:
				content += randDoubleA2B(0.2, 0.4);
				break;
			case 3:
				content += randDoubleA2B(0.4, 0.6);
				break;
			case 4:
				content += randDoubleA2B(0.6, 0.8);
				break;
			case 5:
				content += randDoubleA2B(0.8, 1);
				break;
			}
			if (i < level.length - 1)
				content += " ";
		}
		FileUtils.saveResult(content, "", TIME_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 9.���ɲ�ͬ�����ڲ�ͬ����ķ��﷢������
	 */
	private void generateF(String path) {
		String content = "";
		for (int i = 0; i < CrimeNum; i++) {
			for (int j = 0; j < RegionNum; j++) {
				int len = RoadNum[j];
				for (int k = 0; k < len; k++) {
					content += randDoubleA2B(0.01, 0.2);
					if (k < len - 1)
						content += " ";
					else if(j<RegionNum-1)
						content+="\n";
				}
			}
			if(i<CrimeNum-1)
				content +="\n";
		}
		FileUtils.saveResult(content, "", F_NAME, FileUtils.DATA_PATH,path);
	}

	private void generateData(String path) {
		generateBasic(path);
		generateV(path);
		generateW(path);
		generateTD(path);
		generateCD(path);
		generatePosEffect(path);
		generateRoadLength(path);
		generateTimeEffect(path);
		generateF(path);
		System.out.println("ʵ�����������ɣ�");
	}

	/**
	 * �������ģ������
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();// ��ʼʱ��
		// 20�ַ��30������
		RandomGenerateData r = new RandomGenerateData(60, 120);
		r.generateData(Strategy.PATH);//�������ݱ���·��
		long endTime = System.currentTimeMillis();
		System.out.println("������������ʱ��Ϊ��" + (endTime - startTime) / 1000.0 + "��");

	}

}
