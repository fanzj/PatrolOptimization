package com.fzj.strategy;

import java.io.IOException;
import java.util.Random;

import com.fzj.model.PatrolModel;
import com.fzj.utils.FileUtils;
import com.fzj.wwo.Wave;

/**
 * @author Fan Zhengjie
 * @date 2016��9��25�� ����11:21:33
 * @version 1.0
 * @description ���������
 */
public abstract class Strategy {

	public String TYPE_NAME;// �㷨���������硰WWO��,��BBO��
	public String RESULT_NAME;// ����Ľ�������硰1.txt��
	public static final String PATH = "data_05\\";
	public static String PATH2 = FileUtils.RESULT_PATH + PATH;
	public static int MAX_NFE = 400000;

	protected int n;// ��Ⱥ��ģ
	protected int max_t;// ����������
	protected int patrolUnitNum;// Ѳ�ߵ�λ��
	protected int t;// ��ǰ��������
	protected int D;// �����ά��

	protected int upperPatrol;// ÿ��·�η����Ѳ�ߵ�λ���Ͻ�
	protected PatrolModel patrolModel;// ģ�Ͳ���
	protected Random random;

	// ģ�Ͳ���ֵ���ɹ��촫��
	protected int crimeNum;// ����������
	public int regionNum;// ��������Ⱦɫ�峤��
	public int roadNum[];// ���������·����

	protected double V;// Ѳ���ٶ�
	protected double W[];// ����Ȩ��
	protected double TD[];// �������ʱ��
	protected double CD[];// ���ﱻ��⵽�ĸ���
	protected double PositionEffect[][];// ��ͬλ�õĽ�ͨӵ�³̶�
	protected double RoadLength[][];// ��ͬ·�εĳ��ȣ�km
	protected double TimeEffect[];// ��ͬʱ��Ľ�ͨӵ�³̶�
	protected double F[][][];// ��ͬ�����ڲ�ͬ����ķ��﷢��Ƶ��F[i][j][k],����i������j·��k�ķ���Ƶ��
	
	//����ֵ
	protected double T[][][];//��ͬʱ�䣬��ͬ·�εĽ�ͨӵ���̶�

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
	 * ����������ʼ��
	 * @throws IOException 
	 */
	protected void init() throws IOException {
		random = new Random(System.currentTimeMillis());
		this.t = 0;
		this.regionNum = patrolModel.getRegionNum();

		// ģ�Ͳ�����ֵ
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
		calTrafficCongestion();//���㽻ͨӵ���̶�
		System.out.println("Upper = "+upperPatrol);
	}
	
	/**
	 * ���㽻ͨӵ���̶�
	 */
	protected void calTrafficCongestion()
	{
		double r1,r2;//[0,1]֮����������r1+r2=1
		int j,k,l;
		int periodLen = TimeEffect.length;
		
		//1.���㽻ͨӵ���̶�
		for(j=0;j<regionNum;j++){
			int road = roadNum[j];//������·����
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
	 * ˽�в�����ʼ��
	 * @throws IOException 
	 */
	protected abstract void init2() throws IOException;
	
	/**
	 * ��Ⱥ��ʼ��
	 */
	protected abstract void initPopulation();
	
	

}
