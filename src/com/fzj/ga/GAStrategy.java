package com.fzj.ga;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.xml.transform.Templates;

import com.fzj.bean.Fitness;
import com.fzj.model.PatrolModel;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;
import com.fzj.wwo.Wave;

/**
 * @author Fan Zhengjie
 * @date 2016年7月22日 下午3:48:31
 * @version 1.0
 * @description 巡逻单位分配GA求解
 */
public class GAStrategy extends Strategy{

	
	private double Pc;// 交叉概率
	private double Pm;// 变异概率
	public Solution beSolution;// 全局最优解
	private Solution[] oldPopulation;// 父种群
	private Solution[] newPopulation;// 子种群，从父种群中选取，进行交叉和变异
	

	public GAStrategy() {

	}

	/**
	 * 
	 * @param n
	 *            种群规模
	 * @param regionNum
	 *            区域数，即染色体长度
	 * @param patrolUnitNum
	 *            要分配的巡逻单位数
	 * @param Pc
	 *            交叉概率
	 * @param Pm
	 *            变异概率
	 * @param max_t
	 *            最大进化代数
	 */
	public GAStrategy(int n, int regionNum, int patrolUnitNum, double Pc, double Pm, int max_t,PatrolModel patrolModel) {
		super(n, max_t, patrolUnitNum, patrolModel);
		this.regionNum = regionNum;
		this.Pc = Pc;
		this.Pm = Pm;
	}
	
	/**
	 * 参数的一些初始化设置
	 * @throws IOException 
	 */
	public void init2() throws IOException{
		init();
		//全局最优解的初始化设置
		beSolution = new Solution(regionNum);
		beSolution.setCurrent_t(0);
		beSolution.setRegionNum(regionNum);
		beSolution.setFitness(Double.MIN_VALUE);
		
		oldPopulation = new Solution[n];
		newPopulation = new Solution[n];
		
		this.TYPE_NAME = "GA";
		this.RESULT_NAME = "ga_60_100D.txt";
		
		
	}
	
	
	
	/**
	 * 种群初始化
	 */
	protected void initPopulation(){
		int i,j,k,road;
		Region[] patrol_allocation;//各个区域
		int[] road_allocation;//各个路段分配的巡逻单位
		int tempPatrolNum;
		
		for(k=0;k<n;k++){
			patrol_allocation = new Region[regionNum];
			tempPatrolNum = patrolUnitNum;
			//1.保证每个路段至少有1个巡逻单位
			for(i=0;i<regionNum;i++){//对于每个区域
				road = roadNum[i];//该区域的路段数
				road_allocation = new int[road];
				patrol_allocation[i] = new Region(road);
				for(j=0;j<road;j++){
					road_allocation[j] = 1;
					tempPatrolNum --;
				}
				patrol_allocation[i].setRegion_allocation(road_allocation);
			}
			
			while(tempPatrolNum!=0){
				for(i=0;i<regionNum;i++){//对于每个区域
					if(tempPatrolNum==0)
						break;
					road = roadNum[i];//该区域的路段数
					road_allocation = patrol_allocation[i].getRegion_allocation();
					for(j=0;j<road;j++){
						if(tempPatrolNum==0){
							break;
						}
						int r = random.nextInt(2);
						if(road_allocation[j]<upperPatrol){
							road_allocation[j] += r;//r=0或1，考虑该路段是否分配巡逻单位
							tempPatrolNum -= r;
						}
						
					}
					patrol_allocation[i].setRegion_allocation(road_allocation);
				}
			}
			
			
			Solution s = new Solution(regionNum);
			s.setCurrent_t(0);
			s.setFitness(0);
			s.setPatrol_allocation(patrol_allocation);
			s.setPi(0);
			oldPopulation[k] = s;
			evaluate(oldPopulation[k]);
		}
		
		
	}
	
	/**
	 * 适应度值计算
	 * @param s 
	 */
	private void evaluate(Solution s){
		double fitness = 0.0;//适应度值
		int i,j,k,l;
		double P[][][] = new double[crimeNum][regionNum][];//拦截概率
		int periodLen = TimeEffect.length;
		
		Region region[] = s.getPatrol_allocation();
		for(i=0;i<crimeNum;i++){
			for(j=0;j<regionNum;j++){
				int road_allo[] = region[j].getRegion_allocation();
				int roadnum = roadNum[j];
				P[i][j] = new double[roadnum];
				for(k=0;k<roadnum;k++){
					double temp = road_allo[k]*V*TD[i]*CD[i];
					P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//因为数组中是km,转化为m
					double temp2 = W[i]*F[i][j][k]*P[i][j][k];
					for(l=0;l<periodLen;l++){
						fitness += temp2*T[j][k][l];
					}
				}
			}
		}
		s.setFitness(fitness);
	}
	
	/**
	 * 计算种群各个个体的累积概率，前提是已经计算出各个个体的适应度，
	 * 作为轮盘赌选择策略的一部分
	 */
	private void countRate(){
		double sumFitness = 0.0;
		for(int i=0;i<n;i++){
			sumFitness += oldPopulation[i].getFitness();
		}
		
		oldPopulation[0].setPi(oldPopulation[0].getFitness()/sumFitness);
		for(int i=1;i<n;i++){
			oldPopulation[i].setPi(oldPopulation[i].getFitness()/sumFitness + oldPopulation[i-1].getPi());
		}
	}
	
	/**
	 * 挑选某代种群中适应度最高的个体，直接复制到子代
	 */
	private void selectBest(){
		int maxid = 0;
		double maxFitness = oldPopulation[0].getFitness();
		
		for(int i=1;i<n;i++){
			if(oldPopulation[i].getFitness() > maxFitness){
				maxFitness = oldPopulation[i].getFitness();
				maxid = i;
			}
		}
		
		if(beSolution.getFitness() < maxFitness){
			beSolution.setFitness(maxFitness);
			beSolution.setCurrent_t(t);
			Solution temp = (Solution) oldPopulation[maxid].clone();
			beSolution.setPatrol_allocation(temp.getPatrol_allocation());
		}
		
		//将当代种群中适应度最高的染色体maxid复制到新种群中，排在第一位
		copySolution(0, maxid);
		
	}
	
	/**
	 * 复制染色体
	 * @param k1 表示新染色体在种群中的位置
	 * @param k2 表示旧染色体在种群中的位置
	 */
	private void copySolution(int k1,int k2){
		newPopulation[k1] = (Solution) oldPopulation[k2].clone();
	}
	
	/**
	 * 打印种群
	 */
	private void printPopulation(Solution[] population){
		
		for(int i=0;i<n;i++){
			Solution s =population[i];
			for(int j=0;j<regionNum;j++){
				Region[] regions = s.getPatrol_allocation();
				int road = roadNum[j];
				for(int k=0;k<road;k++)
				{
					int[] roads = regions[j].getRegion_allocation();
					System.out.print(roads[k]+" ");
				}
				System.out.print("|");
			}
			System.out.println("fitness = "+s.getFitness()+"|Pi = "+s.getPi());
		}
	}
	
	/**
	 * 轮盘赌选择策略挑选
	 */
	private void select(){
		int i,j,selectId;
		double r;
		
		for(i=1;i<n;i++){//最优的已经直接复制到子代，所以此处再选择n-1个就行
			r = (random.nextInt(65535) % 1000) / 1000.0;
			for(j=0;j<n;j++){
				if(r<=oldPopulation[j].getPi()){
					break;
				}
			}
			selectId = j;
			copySolution(i, selectId);
		}
	}
	
	/**
	 * 交叉算子
	 * @param k1
	 * @param k2
	 */
	private void cross(int k1,int k2){
		int i,j,k;
		int len = 0;//将解展开后的染色体长度，即不同区域共有多少条路段
		Solution s1 = newPopulation[k1];//待交叉染色体k1
		Solution s2 = newPopulation[k2];//待交叉染色体k2
		
		//计算len
		for(i=0;i<regionNum;i++){
			len += roadNum[i];
		}
		
		int totalRoad1[] = new int[len];
		int totalRoad2[] = new int[len];
		
		Region region1[] = s1.getPatrol_allocation();
		Region region2[] = s2.getPatrol_allocation();
		k = 0;
		int max = -1;//记录某路段分配的最多巡逻单位数
		for(i=0;i<regionNum;i++){
			int road1[] = region1[i].getRegion_allocation();
			int road2[] = region2[i].getRegion_allocation();
			for(j=0;j<roadNum[i];j++){
				if(max < road1[j])
					max = road1[j];
				if(max < road2[j])
					max = road2[j];
				
				totalRoad1[k] = road1[j];
				totalRoad2[k] = road2[j];
				k++;
			}
		}
		
		int a[] = new int[max+1];//记录染色体1中各个巡逻单位的数目，比如a[5],表示巡逻单位为5的有a[5]个
		int b[] = new int[max+1];
		for(i=0;i<len;i++){
			a[totalRoad1[i]]++;
			b[totalRoad2[i]]++;
		}
		
		int ran = random.nextInt(65535) % len;
		while(ran==0){//保证交叉断点有效
			ran = random.nextInt(65535) % len;
		}
		
		int temp;
		for(i=ran;i<len;i++){
			temp = totalRoad1[i];
			totalRoad1[i] = totalRoad2[i];
			totalRoad2[i] = temp;
		}
		
		int a2[] = new int[max+1];//简单交叉后的各个巡逻单位的个数
		int b2[] = new int[max+1];
		for(i=0;i<len;i++){
			a2[totalRoad1[i]]++;
			b2[totalRoad2[i]]++;
		}
			
		i = ran;
		j = ran;
		int v1,v2;
		while(i< len && j <len)
		{
			v1 = totalRoad1[i];
			v2 = totalRoad2[j];
			if(a2[v1]-a[v1]>0 && b2[v2]-b[v2]>0)//找a2[v1]-a[v1]>0的数v1
			{
				totalRoad1[i] = v2;
				totalRoad2[j] = v1;
				
				a2[v1] --;
				a2[v2] ++;
				b2[v2] --;
				b2[v1] ++;
			}
			else if(a2[v1]-a[v1]>0)
			{
				j++;
				continue;
			}
			else if(b2[v2]-b[v2]>0)
			{
				i++;
				continue;
			}
				
			i++;
			j++;
		}
		
		k = 0;
		for(i=0;i<regionNum;i++)
		{
			int rl = roadNum[i];
			int roadAllocation1[] = new int[rl]; 
			int roadAllocation2[] = new int[rl];
			for(j=0;j<rl;j++){
				roadAllocation1[j] = totalRoad1[k];
				roadAllocation2[j] = totalRoad2[k];
				k++;
			}
			region1[i].setRegion_allocation(roadAllocation1);
			region2[i].setRegion_allocation(roadAllocation2);
		}
		s1.setPatrol_allocation(region1);
		s2.setPatrol_allocation(region2);
	}
	
	/**
	 * 多次对换变异算子
	 * @param k 选择，交叉后，新种群中的第k条染色体
	 */
	private void variation(int k){
		int re1,re2;//交换区域
		int ro1,ro2;//交换区域的路段
		int count;//对换次数
		int temp;
		
		count = random.nextInt(regionNum)+1;//regionNum相当于染色体长度
		Solution solution = newPopulation[k];
		Region region[] = solution.getPatrol_allocation();
		int road1[],road2[];
		for(int i=0;i<count;i++){
			re1 = random.nextInt(65535) % regionNum;
			re2 = random.nextInt(65535) % regionNum;
			while(re1==re2){
				re2 = random.nextInt(65535) % regionNum;
			}
			
			road1 = region[re1].getRegion_allocation();
			road2 = region[re2].getRegion_allocation();
			
			ro1 = random.nextInt(65535) % road1.length;
			ro2 = random.nextInt(65535) % road2.length;
			
			temp = road1[ro1];
			road1[ro1] = road2[ro2];
			road2[ro2] = temp;
			
			
		}
	}
	
	/**
	 * 进化函数
	 */
	private void evolution(){
		int k;
		
		//1.选择
		selectBest();
		select();
		
		double r;
		int count = n;
		if(count % 2 !=0){
			count--;
		}
		for(k=0;k<count;k=k+2){
			r = random.nextDouble();
			if(r<Pc){
				cross(k,k+1);
//				variation(k);
			}
			else{
				r = random.nextDouble();
				if(r<Pm){
					variation(k);
				}
				
				r = random.nextDouble();
				if(r<Pm){
					variation(k+1);
				}
			}
		}
		if(n%2!=0){//若种群规模为奇数，最后一个不进行交叉
			r = random.nextDouble();
			if(r<Pm){
				variation(n-1);
			}
		}
	}
	
	/**
	 * 进化函数，保留最好染色体不进行交叉变异
	 */
	private void evolution2(){
		int k;
		selectBest();
		select();
		
		double r;
		int count = n;
		if(count%2!=0){
			count++;
		}
		for(k=1;k<count-1;k+=2){
			r = random.nextDouble();
			if(r<Pc){
				cross(k, k+1);
			}
			else {
				r = random.nextDouble();
				if(r<Pm){
					variation(k);
				}
				
				r = random.nextDouble();
				if(r<Pm){
					variation(k+1);
				}
			}
		}
		if(n%2==0){//剩最后一个染色体没有交叉
			r = random.nextDouble();
			if(r<Pm){
				variation(n-1);
			}
		}
	}
	
	public void solve(int p) throws Exception{
		init2();//参数初始化
		initPopulation();//种群初始化，包括适应度值的计算
		countRate();//计算初始化种群中各个个体的累积概率
		
		List<Fitness> list = new ArrayList<>();
		//进化过程
		/*for(t=0;t<max_t;t++){
			evolution2();
			//将新种群复制到旧种群中，准备下一代进化
			for(int k=0;k<n;k++){
				oldPopulation[k] = (Solution) newPopulation[k].clone();
			}
			
			//计算种群适应度
			for(int k=0;k<n;k++){
				evaluate(oldPopulation[k]);
			}
			
			//计算种群中各个个体的累积概率
			countRate();
			
			if(t%50==0 || t==(max_t-1)){//当迭代10000次的时候，相当于每隔50代保存一个最优解
				Fitness f = new Fitness(t, beSolution.getFitness());
				list.add(f);
			}
		}*/
		while(t<max_t){
			evolution2();
			//将新种群复制到旧种群中，准备下一代进化
			for(int k=0;k<n;k++){
				oldPopulation[k] = (Solution) newPopulation[k].clone();
			}
			
			//计算种群适应度
			for(int k=0;k<n;k++){
				evaluate(oldPopulation[k]);
				t++;
				if(t%50==0){
					Fitness f = new Fitness(t, beSolution.getFitness());
					list.add(f);
				}
			}
			
			//计算种群中各个个体的累积概率
			countRate();
			
			
		}
		
		//将迭代过程保存到excel中
		new ExcelUtil().writeExcel(list, PATH2+TYPE_NAME+"\\ga_50_100D.xlsx", TYPE_NAME);
		
		String result = printBest(p);
		FileUtils.saveResult(result,TYPE_NAME, RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		
	}
	
	private String printBest(int p){
		//最佳打印
		String result = "";
		String alloc = "";//最优分配方案
		result += "=============第"+p+"个最优解如下=============\n";
		result += "最大目标值出现代数：\n";
		result += beSolution.getCurrent_t()+"\n";
		result += "最大目标值为：\n";
		result += beSolution.getFitness()+"\n";
		result += "最佳分配方案：\n";
		Region region[] = beSolution.getPatrol_allocation();
		for(int i=0;i<regionNum;i++){
			int road[] = region[i].getRegion_allocation();
			for(int j=0;j<roadNum[i];j++){
				result += road[j]+" ";
				alloc += road[j] +"\n";
			}
			result += "|";
		}
		result += "\n\n";
		
		//System.out.println(result);
		//保存最优分配方案
		//FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
		return result;
	}
	
	/**
	 * 模拟运行 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("开始求解巡逻问题,请等待...");
		long startTime = System.currentTimeMillis();// 起始时间
		PatrolModel patrolModel = new PatrolModel(PATH);//获取模型参数
		long readFileEndTime = System.currentTimeMillis();
		long time = (readFileEndTime - startTime) / 1000;// 转化为秒
		System.out.println("读取数据文件所需时间：" + time / 3600 + "小时，" + (time % 3600) / 60 + "分钟，" + (time % 60) + "秒");
		GAStrategy patrol,best = null;
		
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Solution> results = new ArrayList<>();
		for(int i=0;i<runs;i++){
			patrol = new GAStrategy(50,patrolModel.getRegionNum(),20*patrolModel.getRegionNum(),0.8,0.01,100*patrolModel.getD(),patrolModel);
			patrol.solve(i+1);
			sum += patrol.beSolution.getFitness();
			if(fmin > patrol.beSolution.getFitness()){
				fmin = patrol.beSolution.getFitness();
			}
			if(fmax < patrol.beSolution.getFitness()){
				fmax = patrol.beSolution.getFitness();
				best = patrol;
			}
			results.add((Solution) patrol.beSolution.clone());
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// 转化为秒
		System.out.println(
				"GA算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒");
		
		String result = "";
		result += "最大适应度："+fmax+"\n最小适应度："+fmin+"\n平均适应度："+sum/runs+"\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Solution solution = results.get(i);
			std += (solution.getFitness() - mean) * (solution.getFitness() - mean);
		}
		result += "估计标准差值：" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "最佳分配：\n";
		
		
		Region regions[] = best.beSolution.getPatrol_allocation();
		int alllocation[];
		for(int i=0;i<best.regionNum;i++){
			Region region = regions[i];
			alllocation = region.getRegion_allocation();
			for(int j=0;j<best.roadNum[i];j++){
				result += alllocation[j]+" ";
			}
			result += "|";
		}
		//System.out.println(result);
		result += "\nGA算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒\n";
		FileUtils.saveResult(result,best.TYPE_NAME, best.RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		System.out.println("巡逻问题求解结束！");
	}
}
