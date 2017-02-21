package com.fzj.data;

import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.Random;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import com.fzj.strategy.Strategy;
import com.fzj.utils.FileUtils;

/**
 * @author Fan Zhengjie
 * @date 2016年9月24日 下午1:10:51
 * @version 1.0
 * @description 随机生成实验数据
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
	private int CrimeNum;// 犯罪类型数
	private int RegionNum;// 区域数
	private int RoadNum[];

	public RandomGenerateData(int CrimeNum, int RegionNum) {
		this.CrimeNum = CrimeNum;
		this.RegionNum = RegionNum;
		this.RoadNum = new int[RegionNum];
		this.random = new Random(System.currentTimeMillis());
	}

	/**
	 * 生成[a,b]之间的随机整数
	 * 
	 * @param a
	 *            下限
	 * @param b
	 *            上限
	 * @return
	 */
	private int randIntA2B(int a, int b) {
		return a + random.nextInt(b - a + 1);
	}

	/**
	 * 随机生成[a,b]之前的浮点数
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private double randDoubleA2B(double a, double b) {
		return a + (b - a) * random.nextDouble();
	}

	/**
	 * 1.生成基本参数
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
	 * 2.生成巡逻速度
	 */
	private void generateV(String path) {
		String content = "";
		content += randDoubleA2B(1, 20);
		FileUtils.saveResult(content, "", V_NAME, FileUtils.DATA_PATH,path);
	}

	/**
	 * 3.生成犯罪权重
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
	 * 4.生成犯罪持续时间
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
	 * 5.生成犯罪被检测到的概率
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
	 * 6.生成不同位置的交通拥堵程度
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
	 * 7.生成不同路段的长度
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
	 * 8.生成不同时间的交通拥堵程度
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
	 * 9.生成不同犯罪在不同区域的犯罪发生概率
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
		System.out.println("实验数据已生成！");
	}

	/**
	 * 随机数据模拟生成
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();// 开始时间
		// 20种犯罪，30个区域
		RandomGenerateData r = new RandomGenerateData(60, 120);
		r.generateData(Strategy.PATH);//生成数据保存路径
		long endTime = System.currentTimeMillis();
		System.out.println("生成数据所需时间为：" + (endTime - startTime) / 1000.0 + "秒");

	}

}
