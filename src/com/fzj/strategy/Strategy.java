package com.fzj.strategy;

import java.io.IOException;
import java.util.Random;

import com.fzj.model.PatrolModel;
import com.fzj.utils.FileUtils;
import com.fzj.wwo.Wave;

/**
 * @author Fan Zhengjie
 * @date 2016年9月25日 上午11:21:33
 * @version 1.0
 * @description 抽象策略类
 */
public abstract class Strategy {

	public String TYPE_NAME;// 算法类型名，如“WWO”,“BBO”
	public String RESULT_NAME;// 保存的结果名，如“1.txt”
	public static final String PATH = "data_05\\";
	public static String PATH2 = FileUtils.RESULT_PATH + PATH;
	public static int MAX_NFE = 400000;

	protected int n;// 种群规模
	protected int max_t;// 最大进化代数
	protected int patrolUnitNum;// 巡逻单位数
	protected int t;// 当前进化代数
	protected int D;// 问题的维度

	protected int upperPatrol;// 每个路段分配的巡逻单位的上界
	protected PatrolModel patrolModel;// 模型参数
	protected Random random;

	// 模型参数值，由构造传入
	protected int crimeNum;// 犯罪类型数
	public int regionNum;// 区域数，染色体长度
	public int roadNum[];// 各个区域的路段数

	protected double V;// 巡逻速度
	protected double W[];// 犯罪权重
	protected double TD[];// 犯罪持续时间
	protected double CD[];// 犯罪被检测到的概率
	protected double PositionEffect[][];// 不同位置的交通拥堵程度
	protected double RoadLength[][];// 不同路段的长度，km
	protected double TimeEffect[];// 不同时间的交通拥堵程度
	protected double F[][][];// 不同犯罪在不同区域的犯罪发生频率F[i][j][k],犯罪i在区域j路段k的发生频率
	
	//计算值
	protected double T[][][];//不同时间，不同路段的交通拥挤程度

	public Strategy() {
		// TODO Auto-generated constructor stub
	}

	public Strategy(int n, int max_t, int patrolUnits, PatrolModel patrolModel) {
		this.n = n;
		this.max_t = max_t;
		this.patrolModel = patrolModel;
		this.patrolUnitNum = patrolUnits;
	}

	/**
	 * 公共参数初始化
	 * @throws IOException 
	 */
	protected void init() throws IOException {
		random = new Random(System.currentTimeMillis());
		this.t = 0;
		this.regionNum = patrolModel.getRegionNum();

		// 模型参数赋值
		this.V = patrolModel.getV();
		this.W = patrolModel.getW();
		this.F = patrolModel.getF();
		this.TD = patrolModel.getTD();
		this.CD = patrolModel.getCD();
		this.PositionEffect = patrolModel.getPositionEffect();
		this.RoadLength = patrolModel.getRoadLength();
		this.TimeEffect = patrolModel.getTimeEffect();
		this.crimeNum = patrolModel.getCrimeNum();
		this.roadNum = patrolModel.getRoadNum();

		this.D = 0;
		for (int i = 0; i < regionNum; i++) {
			D += roadNum[i];
		}
		System.out.println("D = "+D);

		upperPatrol = (patrolUnitNum / D) + 5;
		T = new double[regionNum][][];
		calTrafficCongestion();//计算交通拥挤程度
		System.out.println("Upper = "+upperPatrol);
	}
	
	/**
	 * 计算交通拥挤程度
	 */
	protected void calTrafficCongestion()
	{
		double r1,r2;//[0,1]之间的随机数，r1+r2=1
		int j,k,l;
		int periodLen = TimeEffect.length;
		
		//1.计算交通拥挤程度
		for(j=0;j<regionNum;j++){
			int road = roadNum[j];//该区的路段数
			T[j] = new double[road][];
			for(k=0;k<road;k++){
				
				T[j][k] = new double[periodLen];
				
				for(l=0;l<periodLen;l++){
						r1 = random.nextDouble();
						r2 = 1 - r1;
						T[j][k][l] = r1*TimeEffect[l] + r2*PositionEffect[j][k];
				}
			}
		}
	}

	/**
	 * 私有参数初始化
	 * @throws IOException 
	 */
	protected abstract void init2() throws IOException;
	
	/**
	 * 种群初始化
	 */
	protected abstract void initPopulation();
	
	

}
