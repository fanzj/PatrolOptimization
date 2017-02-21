package com.fzj.wwo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import com.fzj.bean.Fitness;
import com.fzj.ga.Region;
import com.fzj.model.PatrolModel;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;

/**
 * @author Fan Zhengjie
 * @date 2016��9��26�� ����10:58:17
 * @version 1.0
 * @description
 */
public class WWOStrategy extends Strategy {

	private double epsilon = 0.0001;
	private int Hmax;// ��󲨸�
	private double a;// ��������ϵ��
	private double b;// ����ϵ��
	private double bmax;
	private double bmin;
	private int nmax;
	private int nmin;
	private int Kmax;
	public Wave best;// ��ǰ��ú����Ľ�
	private Wave worst;
	private double range[];// ��dά�����ռ�ĳ��ȣ��˴�һ��
	

	private Wave population[];// ��Ⱥ
	public List<Fitness> list;//��ŵ�������

	public WWOStrategy() {

	}

	public WWOStrategy(int n, int max_t, int patrolUnits, PatrolModel patrolModel) {
		super(n, max_t, patrolUnits, patrolModel);
		this.nmax = n;
		this.nmin = 3;
	}

	protected void init2() throws IOException {
		this.init();
		this.Hmax = 12;
		this.a = 1.0026;
		this.bmax = 0.25;
		this.bmin = 0.001;
		this.b = bmax;

		population = new Wave[n];
		Kmax = Math.min(12, D / 2);
		

		range = new double[D];
		for (int i = 0; i < D; i++) {
			range[i] = upperPatrol - 1;
		}

		best = new Wave(regionNum);
		worst = new Wave(regionNum);
		best.setFitness(Double.MIN_VALUE);
		worst.setFitness(Double.MAX_VALUE);

		this.TYPE_NAME = "WWO";
		this.RESULT_NAME = "wwo_60_100D.txt";
		
		this.list = new ArrayList<>();
	}

	/**
	 * ��ʼ����Ⱥ
	 */
	protected void initPopulation() {
		int i, j, k, road;
		Region[] patrol_allocation;//��������
		int[] road_allocation;//����·�η����Ѳ�ߵ�λ
		int tempPatrolNum;

		for (k = 0; k < n; k++) {
			patrol_allocation = new Region[regionNum];
			tempPatrolNum = patrolUnitNum;
			// 1.��֤ÿ��·��������1��Ѳ�ߵ�λ
			for (i = 0; i < regionNum; i++) {// ����ÿ������
				road = roadNum[i];//�������·����
				patrol_allocation[i] = new Region(road);
				road_allocation = new int[road];
				for(j=0;j<road;j++){
					road_allocation[j] = 1;
					tempPatrolNum --;
				}
				patrol_allocation[i].setRegion_allocation(road_allocation);
			}

			while (tempPatrolNum != 0) {
				for (i = 0; i < regionNum; i++) {// ����ÿ������
					if (tempPatrolNum == 0)
						break;
					road = roadNum[i];//�������·����
					road_allocation = patrol_allocation[i].getRegion_allocation();
					for(j=0;j<road;j++){
						if(tempPatrolNum==0){
							break;
						}
						int r = random.nextInt(2);//r=0��1�����Ǹ�·���Ƿ����Ѳ�ߵ�λ
						road_allocation[j] += r;
						tempPatrolNum -= r;
					}
					patrol_allocation[i].setRegion_allocation(road_allocation);
				}
			}

			Wave wave = new Wave(regionNum);
			wave.setPatrol_allocation(patrol_allocation);
			wave.setCurrentPatrolUnits(patrolUnitNum);
			wave.setW(0.5);// ��ʼ����Ϊ0.5
			wave.setH(Hmax);// ��ʼ����ΪHmax
			wave.setCurrent_t(0);
			
			calFitness(wave);
			
			population[k] = wave;

			if (best.getFitness() < wave.getFitness()) {
				best = (Wave) wave.clone();
			}
			if (worst.getFitness() > wave.getFitness()) {
				worst = (Wave) wave.clone();
			}

		}
	}
	
	

	/**
	 * WWO��Ӧ��ֵ����
	 */
	protected void calFitness(Wave wave) {
		double fitness = 0.0;//��Ӧ��ֵ
		int i,j,k,l;
		double P[][][] = new double[crimeNum][regionNum][];//���ظ���
		int periodLen = TimeEffect.length;
		
		//2.�������ظ���
		/*Region[] regions = wave.getPatrol_allocation();
		for(i=0;i<crimeNum;i++){
			for(j=0;j<regionNum;j++){
				int roadnum = roadNum[j];
				int[] allocation = regions[j].getRegion_allocation();
				P[i][j] = new double[roadnum];
				for(k=0;k<roadnum;k++){
					double temp = allocation[k]*V*TD[i]*CD[i];
					P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//��Ϊ��������km,ת��Ϊm
				}
			}
		}
		
		for(i=0;i<crimeNum;i++){
			for(j=0;j<regionNum;j++){
				for(k=0;k<roadNum[j];k++){
					for(l=0;l<periodLen;l++){
							fitness += W[i]*F[i][j][k]*P[i][j][k]*T[j][k][l];
					}
				}
			}
		}*/
		
		Region[] regions = wave.getPatrol_allocation();
		for(i=0;i<crimeNum;i++){
			for(j=0;j<regionNum;j++){
				int roadnum = roadNum[j];
				int[] allocation = regions[j].getRegion_allocation();
				P[i][j] = new double[roadnum];
				for(k=0;k<roadnum;k++){
					double temp = allocation[k]*V*TD[i]*CD[i];
					P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//��Ϊ��������km,ת��Ϊm
					
					double temp2 = W[i]*F[i][j][k]*P[i][j][k];
					for(l=0;l<periodLen;l++){
						fitness += temp2*T[j][k][l];
				}
				}
			}
		}
		
		
		wave.setFitness(fitness);
		wave.setCurrent_t(t);
		
	}

	public void solve(int p) throws Exception {
		init2();
		initPopulation();
		
		/*for (t = 0; t < max_t; t++) {
			evolve();
			//����Ӧ�ȴӴ�С����
			sortPopulation();
			//���²���
			updateW();
			//����ȫ�����ź����
			updateBest();
			//��������ϵ��
			updateB();
			//������Ⱥ��ģ
			updateN();
			
			if(t%50==0 || t==(max_t-1)){//������10000�ε�ʱ���൱��ÿ��100������һ�����Ž�
				Fitness f = new Fitness(t, best.getFitness());
				list.add(f);
			}
		}*/
		
		while(t < max_t) {
			evolve();
			//����Ӧ�ȴӴ�С����
			sortPopulation();
			//���²���
			updateW();
			//����ȫ�����ź����
			updateBest();
			//��������ϵ��
			updateB();
			//������Ⱥ��ģ
			updateN();
			
		}
		
		

		String result = "��" + p + "��BestWave: \n";
		String alloc = "";//���ŷ��䷽��
		Region[] regions = best.getPatrol_allocation();
		int realPatrol = 0;
		for (int i = 0; i < regionNum; i++) {
			int road = roadNum[i];
			int[] allocation = regions[i].getRegion_allocation();
			for (int j = 0; j < road; j++) {
				result += allocation[j] + " ";
				realPatrol += allocation[j];
				alloc += allocation[j] +"\n";
			}
			result += "|";
		}
		result += "realPatrol = "+realPatrol+"|";
		result += "fitness = " + best.getFitness() + "\n\n";

		FileUtils.saveResult(result, TYPE_NAME, RESULT_NAME, FileUtils.RESULT_PATH, PATH);
		
		//�������ŷ��䷽��
		//FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
	}
	
	/**
	 * ��ÿ��wave����Ӧ�ȴӴ�С����
	 */
	private void sortPopulation(){
		WWOComparator wwoComparator = new WWOComparator();//�Ƚ���
		Arrays.sort(population,wwoComparator);
	}

	/**
	 * ������Ⱥ��ģ
	 */
	private void updateN() {
		double temp = (nmax - nmin) * ((double) t / (double) max_t);
		n = nmax - (int) Math.round(temp);
	}

	/**
	 * ��������ϵ��
	 */
	private void updateB() {
		b = bmax - (bmax - bmin) * t / (double) max_t;
	}

	/**
	 * ����
	 */
	private void evolve() {
		for (int i = 0; i < n; i++) {
			Wave wave = population[i];// �����������޸�
			Wave temp = (Wave) wave.clone();// ���룬��Ϊ����

			propagate(temp);// ����
			Wave newWave = (Wave) temp.clone();// ���������ɵ��²�
			if (newWave.getFitness() > wave.getFitness()) {
				newWave.setH(Hmax);
				wave = (Wave) newWave.clone();
				if (newWave.getFitness() > best.getFitness()) {
					// ����
					best = (Wave) newWave.clone();
					breaking(newWave);//��߿����е�����(ò���ѽ��)
				}
				
			} else {
				int h = wave.getH();
				h--;
				wave.setH(h);
				if (h == 0) {
					// ����
					refract(wave);
				}
			}
			population[i] = (Wave) wave.clone();
			// population[i] = (Wave) newWave.clone();
		}
	}

	/**
	 * ���²���
	 */
	private void updateW() {
		// ���²���
		for (int i = 0; i < n; i++) {
			Wave wave = population[i];
			double w = wave.getW();// ����
			w *= Math.pow(a, -(wave.getFitness() - worst.getFitness() + epsilon)
					/ (best.getFitness() - worst.getFitness() + epsilon));
			wave.setW(w);
		}
	}

	private void updateBest() {
		for (int i = 0; i < n; i++) {
			Wave wave = population[i];
			if (best.getFitness() < wave.getFitness()) {
				best = (Wave) wave.clone();
			}
			if (worst.getFitness() > wave.getFitness()) {
				worst = (Wave) wave.clone();
			}
		}
	}

	/**
	 * ������������ֹΥ��Լ��
	 * 
	 * @param wave
	 */
	private void modify2Constraint(Wave wave) {
		int posRegion,posRoad;
		int i, j, k;
		int r;

		Region regions[] = wave.getPatrol_allocation();
		int patrolNum = wave.getCurrentPatrolUnits();

		while (patrolNum < patrolUnitNum) {
			posRegion = random.nextInt(regionNum);//���һ������
			int allocation[] = regions[posRegion].getRegion_allocation();
			int road = roadNum[posRegion];//�������·����
			posRoad = random.nextInt(road);//����������һ��·��
			if(allocation[posRoad]<upperPatrol)
			{
				allocation[posRoad]++;
				patrolNum++;
			}
			
		}

		while (patrolNum > patrolUnitNum) {
			posRegion = random.nextInt(regionNum);//���һ������
			int allocation[] = regions[posRegion].getRegion_allocation();
			int road = roadNum[posRegion];//�������·����
			posRoad = random.nextInt(road);//����������һ��·��
			if(allocation[posRoad]>1){
				allocation[posRoad]--;
				patrolNum--;
			}
		}
		
		wave.setCurrentPatrolUnits(patrolNum);
	}

	/**
	 * ����
	 * 
	 * @param wave
	 */
	private void propagate(Wave wave) {
		
		Region regions[] = wave.getPatrol_allocation();
		int patrolNum = wave.getCurrentPatrolUnits();
		double w = wave.getW();
		int z = 0;
		for(int j=0;j<regionNum;j++)//��ÿ������
		{
			int road = roadNum[j];//ÿ�������·����
			int allocation[] = regions[j].getRegion_allocation();
	
			for(int k=0;k<road;k++){
				int xd = allocation[k];
				double r = (-1.0+2.0*random.nextDouble());
				double y = allocation[k]*1.0+Math.ceil(r*w*range[z++]);
				int temp = new Double(y).intValue();
				if(temp<1 || temp> upperPatrol){
					temp = 1 + random.nextInt(upperPatrol);
				}
				allocation[k] = temp;
				patrolNum += (allocation[k]-xd);
			}
			regions[j].setRegion_allocation(allocation);
		}
		wave.setPatrol_allocation(regions);

		wave.setCurrentPatrolUnits(patrolNum);
		// newX����Υ��Լ�������д���
		modify2Constraint(wave);
		
		if(t%50==0 || t==(max_t-1)){
			Fitness f = new Fitness(t, best.getFitness());
			list.add(f);
		}
		calFitness(wave);
		t++;
		
	}



	/**
	 * ����
	 * 
	 * @param wave
	 */
	private void refract(Wave wave) {
		Wave wave2 = (Wave) wave.clone();
		Region[] regionsX1 = wave2.getPatrol_allocation();
		Region[] regionsX2 = best.getPatrol_allocation();
		
		double f1 = wave2.getFitness();
		double w1 = wave2.getW();
		int patrolNum = wave2.getCurrentPatrolUnits();
		
		for(int j=0;j<regionNum;j++){
			int road = roadNum[j];//·����
			int[] allocation1 = regionsX1[j].getRegion_allocation();
			int[] allocation2 = regionsX2[j].getRegion_allocation();
			for(int k=0;k<road;k++){
				int xd = allocation1[k];
				//��˹�ֲ�N(u,g)
				double u = (allocation1[k]+allocation2[k])/2.0;
				double g = Math.abs(allocation2[k]-allocation1[k])/2.0;
				//double r = g * random.nextGaussian() + u;
				double r = Math.sqrt(g)*random.nextGaussian()+u;
				int temp = new Double(Math.ceil(r)).intValue();
				if(temp < 1 || temp > upperPatrol){
					temp = 1 + random.nextInt(upperPatrol);
				}
				allocation1[k] = temp;
				patrolNum += (allocation1[k] - xd);
			}
			regionsX1[j].setRegion_allocation(allocation1);
		}
		wave2.setPatrol_allocation(regionsX1);
		wave2.setCurrentPatrolUnits(patrolNum);
		modify2Constraint(wave2);
		calFitness(wave2);
		wave2.setH(Hmax);
		
		double r = wave.getFitness()/wave2.getFitness();
		if(r<1){//����󣬲�����С��˵������ţ�����
			double w = wave.getW() * r;
			wave2.setW(w);
			wave = (Wave) wave2.clone();
		}
		else {//����ԭ�н�
			//��������
		}
		
		if(t%50==0 || t==(max_t-1)){
			Fitness f = new Fitness(t, best.getFitness());
			list.add(f);
		}
		t++;
		
		
	}

	/**
	 * ����
	 * 
	 * @param wave
	 */
	private void breaking(Wave wave) {
		Wave bestWave = new Wave(regionNum);
		bestWave.setFitness(Double.MIN_VALUE);

		int k = 1 + random.nextInt(Kmax);
		
	
		for (int i = 0; i < k; i++) {
			Wave newWave = (Wave) wave.clone();
			Region[] regions = newWave.getPatrol_allocation();
			int patrolNum = newWave.getCurrentPatrolUnits();
			int posRegion = random.nextInt(regionNum);//���һ������
			int road = roadNum[posRegion];//�������·����
			int d = random.nextInt(road);//����������һ��·��
			int[] allocation = regions[posRegion].getRegion_allocation();
			int num = allocation[d];
			double y = allocation[d] * 1.0 + random.nextGaussian() * b * range[d];//ʵ��Ӧ��range[d]��ʾ
			int temp = new Double(Math.ceil(y)).intValue();
			if (temp < 1 || temp > upperPatrol) {
				temp = 1 + random.nextInt(upperPatrol);
			}
			allocation[d] = temp;
			patrolNum += (allocation[d] - num);
//			regions[posRegion].setRegion_allocation(allocation);
//			newWave.setPatrol_allocation(regions);
			newWave.setCurrentPatrolUnits(patrolNum);
			modify2Constraint(newWave);
			calFitness(newWave);

			if (newWave.getFitness() > bestWave.getFitness()) {
				bestWave = (Wave) newWave.clone();
			}
		}
		// ���ŵĶ����������ŵĲ���
		if (bestWave.getFitness() > best.getFitness()) {
			best = (Wave) wave.clone();
		}

		if(t%50==0 || t==(max_t-1)){
			Fitness f = new Fitness(t, best.getFitness());
			list.add(f);
		}
		t++;
		
	}

	/**
	 * ģ������
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("��ʼ���Ѳ������,��ȴ�...");
		long startTime = System.currentTimeMillis();// ��ʼʱ��
		PatrolModel patrolModel = new PatrolModel(PATH);
		long readFileEndTime = System.currentTimeMillis();
		long time = (readFileEndTime - startTime) / 1000;// ת��Ϊ��
		System.out.println("��ȡ�����ļ�����ʱ�䣺" + time / 3600 + "Сʱ��" + (time % 3600) / 60 + "���ӣ�" + (time % 60) + "��");

		WWOStrategy wwo, best = null;
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Wave> results = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// ����60��
			wwo = new WWOStrategy(50, 200*patrolModel.getD(), 20 * patrolModel.getRegionNum(), patrolModel);
			wwo.solve(1 + i);
			sum += wwo.best.getFitness();
			if (fmin > wwo.best.getFitness()) {
				fmin = wwo.best.getFitness();
			}
			if (fmax < wwo.best.getFitness()) {
				fmax = wwo.best.getFitness();
				best = wwo;
			}
			results.add((Wave) wwo.best.clone());// ���ÿ�����еĽ����������Ʒ���ֵ
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// ת��Ϊ��
		System.out.println("WWO�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��");

		String result = "";
		result += "���Ž�������Ӧ�ȣ�" + fmax + "\n���Ž����С��Ӧ�ȣ�" + fmin + "\n���Ž��ƽ����Ӧ�ȣ�" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Wave wave = (Wave) results.get(i);
			std += (wave.getFitness() - mean) * (wave.getFitness() - mean);
		}
		result += "���Ʊ�׼��ֵ��" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "���Ž����ѷ��䣺\n";
		Region[] regions = best.best.getPatrol_allocation();
		int realPatrol = 0;
		for (int i = 0; i < best.regionNum; i++) {
			int road = best.roadNum[i];
			int[] allocation = regions[i].getRegion_allocation();
			for (int j = 0; j < road; j++) {
				result += allocation[j] + " ";
				realPatrol += allocation[j];
			}
			result += "|";
		}
		result += best.best.getCurrentPatrolUnits();
		result += "|realPatrol = " + realPatrol;
		// System.out.println(result);
		result += "\nWWO�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��\n";
		FileUtils.saveResult(result, best.TYPE_NAME, best.RESULT_NAME, FileUtils.RESULT_PATH, PATH);
		System.out.println("Ѳ��������������");
	}
}
