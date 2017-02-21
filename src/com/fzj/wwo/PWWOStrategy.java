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
 * @date 2016年9月26日 上午10:58:17
 * @version 1.0
 * @description
 */
public class PWWOStrategy extends Strategy {

	private double epsilon = 0.0001;
	private int Hmax;// 最大波高
	private double a;// 波长减少系数
	private double b;// 碎浪系数
	private double bmax;
	private double bmin;
	private int nmax;
	private int nmin;
	private int Kmax;
	public Wave best;// 当前最好和最差的解
	private Wave worst;
	private double range[];// 第d维搜索空间的长度，此处一致
	
	private int vmax;//从PSO引入
	private double c1;//学习因子

	private Wave population[];// 种群
	public List<Fitness> list;

	public PWWOStrategy() {

	}

	public PWWOStrategy(int n, int max_t, int patrolUnits, PatrolModel patrolModel) {
		super(n, max_t, patrolUnits, patrolModel);
		this.nmax = n;
		this.nmin = 3;
	}

	protected void init2() throws IOException {
		this.init();
		this.Hmax = 12;
		this.a = 1.0026;
		this.bmax = 0.25;//////
		this.bmin = 0.001;
		this.b = bmax;

		population = new Wave[n];
		Kmax = Math.min(12, D / 2);
		//System.out.println("Kmax = "+Kmax);

		range = new double[D];
		for (int i = 0; i < D; i++) {
			range[i] = upperPatrol - 1;
		}

		best = new Wave(regionNum);
		worst = new Wave(regionNum);
		best.setFitness(Double.MIN_VALUE);
		worst.setFitness(Double.MAX_VALUE);

		this.TYPE_NAME = "PWWO";
		this.RESULT_NAME = "pwwo_60_100D.txt";
		
		this.c1 = 2;
		this.vmax = upperPatrol - 1;
		list = new ArrayList<>();

	}

	/**
	 * 初始化种群
	 */
	protected void initPopulation() {
		int i, j, k, road;
		Region[] patrol_allocation;//各个区域
		int[] road_allocation;//各个路段分配的巡逻单位
		int tempPatrolNum;

		for (k = 0; k < n; k++) {
			patrol_allocation = new Region[regionNum];
			tempPatrolNum = patrolUnitNum;
			// 1.保证每个路段至少有1个巡逻单位
			for (i = 0; i < regionNum; i++) {// 对于每个区域
				road = roadNum[i];//该区域的路段数
				patrol_allocation[i] = new Region(road);
				road_allocation = new int[road];
				for(j=0;j<road;j++){
					road_allocation[j] = 1;
					tempPatrolNum --;
				}
				patrol_allocation[i].setRegion_allocation(road_allocation);
				patrol_allocation[i].setV(road_allocation);
			}

			while (tempPatrolNum != 0) {
				for (i = 0; i < regionNum; i++) {// 对于每个区域
					if (tempPatrolNum == 0)
						break;
					road = roadNum[i];//该区域的路段数
					road_allocation = patrol_allocation[i].getRegion_allocation();
					for(j=0;j<road;j++){
						if(tempPatrolNum==0){
							break;
						}
						int r = random.nextInt(2);//r=0或1，考虑该路段是否分配巡逻单位
						road_allocation[j] += r;
						tempPatrolNum -= r;
					}
					patrol_allocation[i].setRegion_allocation(road_allocation);
					patrol_allocation[i].setV(road_allocation);
				}
			}

			Wave wave = new Wave(regionNum);
			wave.setPatrol_allocation(patrol_allocation);
			wave.setCurrentPatrolUnits(patrolUnitNum);
			wave.setW(0.5);// 初始波长为0.5
			wave.setH(Hmax);// 初始波高为Hmax
			wave.setCurrent_t(0);
			
			//long startTime = System.currentTimeMillis();
			calFitness(wave);
			//long endTime = System.currentTimeMillis();
			//System.out.println("计算一次适应度所需时间："+(endTime-startTime)/1000.0+"秒");
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
	 * WWO适应度值计算
	 */
	protected void calFitness(Wave wave) {
		double fitness = 0.0;//适应度值
		int i,j,k,l;
		double P[][][] = new double[crimeNum][regionNum][];//拦截概率
		int periodLen = TimeEffect.length;
		
		//2.计算拦截概率
		/*Region[] regions = wave.getPatrol_allocation();
		for(i=0;i<crimeNum;i++){
			for(j=0;j<regionNum;j++){
				int roadnum = roadNum[j];
				int[] allocation = regions[j].getRegion_allocation();
				P[i][j] = new double[roadnum];
				for(k=0;k<roadnum;k++){
					double temp = allocation[k]*V*TD[i]*CD[i];
					P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//因为数组中是km,转化为m
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
					P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//因为数组中是km,转化为m
					
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
			//按适应度从大到小排序
			sortPopulation();
			//更新波长
			updateW();
			//更新全局最优和最差
			updateBest();
			//更新碎浪系数
			updateB();
			//更新种群规模
			updateN();
			
			if(t%50==0 || t==(max_t-1)){//当迭代10000次的时候，相当于每隔100代保存一个最优解
				Fitness f = new Fitness(t, best.getFitness());
				list.add(f);
			}
		}*/
		
		while (t < max_t) {
			evolve();
			//按适应度从大到小排序
			sortPopulation();
			//更新波长
			updateW();
			//更新全局最优和最差
			updateBest();
			//更新碎浪系数
			updateB();
			//更新种群规模
			updateN();
			
		}
		
		

		String result = "第" + p + "个BestWave: \n";
		String alloc = "";//最优分配方案
		Region[] regions = best.getPatrol_allocation();
		int realPatrol = 0;
		for (int i = 0; i < regionNum; i++) {
			int road = roadNum[i];
			int[] allocation = regions[i].getRegion_allocation();
			for (int j = 0; j < road; j++) {
				result += allocation[j] + " ";
				realPatrol += allocation[j];
				alloc += allocation[j]+"\n";
			}
			result += "|";
		}
		result += "realPatrol = "+realPatrol+"|";
		result += "fitness = " + best.getFitness() + "\n\n";

		// System.out.println(result);
		FileUtils.saveResult(result, TYPE_NAME, RESULT_NAME, FileUtils.RESULT_PATH, PATH);
		
		//保存最优分配方案
		//FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
	}
	
	/**
	 * 按每个wave的适应度从大到小排序
	 */
	private void sortPopulation(){
		WWOComparator wwoComparator = new WWOComparator();//比较器
		Arrays.sort(population,wwoComparator);
	}

	/**
	 * 更新种群规模
	 */
	private void updateN() {
		double temp = (nmax - nmin) * ((double) t / (double) max_t);
		n = nmax - (int) Math.round(temp);
	}

	/**
	 * 更新碎浪系数
	 */
	private void updateB() {
		b = bmax - (bmax - bmin) * t / (double) max_t;
	}

	/**
	 * 进化
	 */
	private void evolve() {
		for (int i = 0; i < n; i++) {
			Wave wave = population[i];// 保留，不做修改
			Wave temp = (Wave) wave.clone();// 传入，作为参数

			propagate(temp);// 传播
			Wave newWave = (Wave) temp.clone();// 传播后生成的新波
			if (newWave.getFitness() > wave.getFitness()) {
				newWave.setH(Hmax);
				wave = (Wave) newWave.clone();
				if (newWave.getFitness() > best.getFitness()) {
					// 碎浪
					best = (Wave) newWave.clone();
					breaking(newWave);//这边可能有点问题(貌似已解决)
				}
				
			} else {
				int h = wave.getH();
				h--;
				wave.setH(h);
				if (h == 0) {
					// 折射
					refract(wave);
				}
			}
			population[i] = (Wave) wave.clone();
			// population[i] = (Wave) newWave.clone();
		}
	}

	/**
	 * 更新波长
	 */
	private void updateW() {
		// 更新波长
		for (int i = 0; i < n; i++) {
			Wave wave = population[i];
			double w = wave.getW();// 波长
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
	 * 波的修正，防止违反约束
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
			posRegion = random.nextInt(regionNum);//随机一个区域
			int allocation[] = regions[posRegion].getRegion_allocation();
			int road = roadNum[posRegion];//该区域的路段数
			posRoad = random.nextInt(road);//该区域的随机一个路段
			if(allocation[posRoad]<upperPatrol)
			{
				allocation[posRoad]++;
				patrolNum++;
			}
			
		}

		while (patrolNum > patrolUnitNum) {
			posRegion = random.nextInt(regionNum);//随机一个区域
			int allocation[] = regions[posRegion].getRegion_allocation();
			int road = roadNum[posRegion];//该区域的路段数
			posRoad = random.nextInt(road);//该区域的随机一个路段
			if(allocation[posRoad]>1){
				allocation[posRoad]--;
				patrolNum--;
			}
		}
		
		wave.setCurrentPatrolUnits(patrolNum);
	}

	/**
	 * 传播
	 * 
	 * @param wave
	 */
	private void propagate(Wave wave) {
		Region bestRegions[] = best.getPatrol_allocation();
		
		
		Region regions[] = wave.getPatrol_allocation();
		int patrolNum = wave.getCurrentPatrolUnits();
		double w = wave.getW();
		int z = 0;
		for(int j=0;j<regionNum;j++)//对每个区域
		{
			int bestX[] = bestRegions[j].getRegion_allocation();
			
			int road = roadNum[j];//每个区域的路段数
			int allocation[] = regions[j].getRegion_allocation();
			int v[] = regions[j].getV();
			
			double v2[] = new double[road];
			int iv2[] = new int[road];
			for(int k=0;k<road;k++){
				int xd = allocation[k];
				double r = (-1.0+2.0*random.nextDouble());
				///double y = allocation[k]*1.0+Math.ceil(r*w*range[z++]);
				v2[k]  = w*v[k]+c1*r*(bestX[k]-xd);
				iv2[k] = new Double(Math.ceil(v2[k])).intValue();
				if(iv2[k]>vmax)
				{
					iv2[k] = vmax;
				}
				int temp = xd + iv2[k];
				if(temp<1)
					temp = 1;
				else if(temp >upperPatrol)
					temp = upperPatrol;
				
				//int temp = new Double(y).intValue();
			/*	if(temp<1 || temp> upperPatrol){
					temp = 1 + random.nextInt(upperPatrol);
				}*/
				allocation[k] = temp;
				patrolNum += (allocation[k]-xd);
			}
			regions[j].setRegion_allocation(allocation);
			regions[j].setV(iv2);
		}
		wave.setPatrol_allocation(regions);

		wave.setCurrentPatrolUnits(patrolNum);
		// newX可能违反约束，进行处理
		modify2Constraint(wave);
		
		//long startTime = System.currentTimeMillis();
		calFitness(wave);
		//long endTime = System.currentTimeMillis();
		//System.out.println("单个水波求解适应度所需时间："+(endTime-startTime)+"秒");
	
		if(t%50==0 || t==(max_t-1)){
			Fitness f = new Fitness(t, best.getFitness());
			list.add(f);
		}
		t++;
	}



	/**
	 * 折射
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
			int road = roadNum[j];//路段数
			int[] allocation1 = regionsX1[j].getRegion_allocation();
			int[] allocation2 = regionsX2[j].getRegion_allocation();
			for(int k=0;k<road;k++){
				int xd = allocation1[k];
				//高斯分布N(u,g)
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
		if(r<1){//折射后，波长减小，说明解更优，更新
			double w = wave.getW() * r;
			wave2.setW(w);
			wave = (Wave) wave2.clone();
		}
		else {//保留原有解
			//不做操作
		}
		
		if(t%50==0 || t==(max_t-1)){
			Fitness f = new Fitness(t, best.getFitness());
			list.add(f);
		}
		t++;

	}

	/**
	 * 碎浪
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
			int posRegion = random.nextInt(regionNum);//随机一个区域
			int road = roadNum[posRegion];//该区域的路段数
			int d = random.nextInt(road);//随机该区域的一个路段
			int[] allocation = regions[posRegion].getRegion_allocation();
			int num = allocation[d];
			double y = allocation[d] * 1.0 + random.nextGaussian() * b * range[d];//实则不应用range[d]表示
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
		// 最优的独立波比最优的波好
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
	 * 模拟运行
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("开始求解巡逻问题,请等待...");
		long startTime = System.currentTimeMillis();// 起始时间
		PatrolModel patrolModel = new PatrolModel(PATH);
		long readFileEndTime = System.currentTimeMillis();
		long time = (readFileEndTime - startTime) / 1000;// 转化为秒
		System.out.println("读取数据文件所需时间：" + time / 3600 + "小时，" + (time % 3600) / 60 + "分钟，" + (time % 60) + "秒");

		PWWOStrategy wwo, best = null;
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Wave> results = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// 运行60次
			wwo = new PWWOStrategy(50, 200*patrolModel.getD(), 20 * patrolModel.getRegionNum(), patrolModel);
			wwo.solve(1 + i);
			sum += wwo.best.getFitness();
			if (fmin > wwo.best.getFitness()) {
				fmin = wwo.best.getFitness();
			}
			if (fmax < wwo.best.getFitness()) {
				fmax = wwo.best.getFitness();
				best = wwo;
			}
			results.add((Wave) wwo.best.clone());// 存放每次运行的结果，以求估计方差值
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// 转化为秒
		System.out.println("PWWO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒");

		String result = "";
		result += "最优解的最大适应度：" + fmax + "\n最优解的最小适应度：" + fmin + "\n最优解的平均适应度：" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Wave wave = (Wave) results.get(i);
			std += (wave.getFitness() - mean) * (wave.getFitness() - mean);
		}
		result += "估计标准差值：" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "最优解的最佳分配：\n";
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
		result += "\nPWWO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒\n";
		FileUtils.saveResult(result, best.TYPE_NAME, best.RESULT_NAME, FileUtils.RESULT_PATH, PATH);
		System.out.println("巡逻问题求解结束！");
	}
}
