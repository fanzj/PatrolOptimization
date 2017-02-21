package com.fzj.pso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fzj.bean.Fitness;
import com.fzj.model.PatrolModel;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;
import com.fzj.wwo.Wave;

/**
 * @author Fan Zhengjie
 * @date 2016年9月28日 上午9:57:47
 * @version 1.0
 * @description
 */
public class ParticleStrategy extends Strategy {

	private double c1;// 学习因子
	private double c2;// 学习因子
	private int vmax;// 最大速度
	private int xmax;// 位移上限
	private double w;// 惯性权重
	private double wmin;// 最小权重系数
	private double wmax;// 最大权重系数

	public Particle gBest;// 全局最优粒子
	private Particle pBest[];// 历史最优粒子

	private Particle population[];// 种群
	public List<Fitness> list;

	public ParticleStrategy() {

	}

	public ParticleStrategy(int n, int regionNum, int patrolUnitNum, double c1, double c2, double wmin, double wmax,
			int max_t, PatrolModel patrolModel) {
		super(n, max_t, patrolUnitNum, patrolModel);
		this.regionNum = regionNum;
		this.c1 = c1;
		this.c2 = c2;
		this.wmin = wmin;
		this.wmax = wmax;
	}

	/**
	 * 参数初始化
	 * 
	 * @throws IOException
	 */
	protected void init2() throws IOException {
		this.init();

		w = wmax;
		population = new Particle[n];

		// 粒子每一维的取值为[1,patrolUnitNum-D+1]
		//this.vmax = (int) Math.round(0.1 * (patrolUnitNum - D + 1));
		this.vmax = upperPatrol-1;
	//	System.out.println("vmax = "+vmax);

		this.xmax = upperPatrol;
		//System.out.println("xmax = "+xmax);

		pBest = new Particle[n];

		gBest = new Particle(D);
		gBest.setCurrent_t(0);
		gBest.setFitness(Double.MIN_VALUE);
		list = new ArrayList<>();
		
		TYPE_NAME = "PSO";
		RESULT_NAME = "pso_60_100D.txt";
	}

	/**
	 * 惯量权重更新
	 */
	private void updateW() {
		w = wmax - ((double) t / (double) max_t) * (wmax - wmin);
	}

	/**
	 * 种群初始化
	 */
	protected void initPopulation() {
		int i, j, k;
		int[] allocation;// 分配巡逻单位的方案
		int tempPatrolNum;

		for (k = 0; k < n; k++) {
			allocation = new int[D];
			tempPatrolNum = patrolUnitNum;
			// 1.保证每个路段至少有1个巡逻单位
			for (i = 0; i < D; i++) {
				allocation[i] = 1;
				tempPatrolNum--;
			}

			while (tempPatrolNum != 0) {
				for (i = 0; i < D; i++) {// 对于每个区域
					if (tempPatrolNum == 0)
						break;

					int r = random.nextInt(2);
					if (allocation[i] + r > xmax) {
						continue;
					}
					allocation[i] += r;// r=0或1，考虑该路段是否分配巡逻单位
					tempPatrolNum -= r;
				}
			}

			Particle p = new Particle(D);
			p.setCurrent_t(0);
			p.setFitness(0);
			p.setX(allocation);
			p.setV(allocation);
			population[k] = p;

			evaluate(population[k]);

		}

		for (k = 0; k < n; k++) {
			pBest[k] = (Particle) population[k].clone();
			if (gBest.getFitness() < population[k].getFitness()) {
				gBest = (Particle) pBest[k].clone();
			}
		}

	}

	/**
	 * 适应度计算
	 * 
	 * @param p
	 */
	private void evaluate(Particle p) {
		double fitness = 0.0;// 适应度值
		int i, j, k, l;
		double P[][][] = new double[crimeNum][regionNum][];// 拦截概率
		int periodLen = TimeEffect.length;

		
		// 2.计算拦截概率
		int m;
		int[] allocation = p.getX();
		for (i = 0; i < crimeNum; i++) {
			m = 0;
			for (j = 0; j < regionNum; j++) {
				int roadnum = roadNum[j];
				P[i][j] = new double[roadnum];
				for (k = 0; k < roadnum; k++) {
					double temp = allocation[m++] * V * TD[i] * CD[i];
					P[i][j][k] = temp / (temp + RoadLength[j][k] * 1000);// 因为数组中是km,转化为m
					double r = (W[i]*F[i][j][k]*P[i][j][k]);
					for (l = 0; l < periodLen; l++) {
						fitness += r * T[j][k][l];
					}
				}
			}
		}
		/*for (i = 0; i < crimeNum; i++) {
			m = 0;
			for (j = 0; j < regionNum; j++) {
				int roadnum = roadNum[j];
				P[i][j] = new double[roadnum];
				for (k = 0; k < roadnum; k++) {
					double temp = allocation[m++] * V * TD[i] * CD[i];
					P[i][j][k] = temp / (temp + RoadLength[j][k] * 1000);// 因为数组中是km,转化为m
				}
			}
		}

		for (i = 0; i < crimeNum; i++) {
			for (j = 0; j < regionNum; j++) {
				for (k = 0; k < roadNum[j]; k++) {
					for (l = 0; l < periodLen; l++) {
						fitness += W[i] * F[i][j][k] * P[i][j][k] * T[j][k][l];
					}
				}
			}
		}*/

		p.setFitness(fitness);
	}

	/**
	 * 更新pBest和gBest
	 */
	private void updateBest(int k) {
		if (pBest[k].getFitness() < population[k].getFitness()) {
			pBest[k] = (Particle) population[k].clone();
		}
		if (gBest.getFitness() < pBest[k].getFitness()) {
			gBest = (Particle) pBest[k].clone();
		}
	}

	/**
	 * 更新速度和位移
	 */
	private void updateXV(Particle particle,int index) {
		double r1, r2;
		int xp[] = pBest[index].getX();
		int xg[] = gBest.getX();
		int x1[] = particle.getX();
		int v1[] = particle.getV();
		int	x2[] = new int[D];
		int	v2[] = new int[D];
		for (int j = 0; j < D; j++) {
			r1 = random.nextDouble();
			r2 = random.nextDouble();
			double temp_v = w * v1[j] + c1 * r1 * (xp[j] - x1[j]) + c2 * r2 * (xg[j] - x1[j]);
			v2[j] = new Double(Math.ceil(temp_v)).intValue();
			if (v2[j] > vmax) {
				v2[j] = vmax;
			} else if (v2[j] < 0) {
				v2[j] = 0;
			}
			x2[j] = x1[j] + v2[j];
			if (x2[j] > xmax) {
				x2[j] = xmax;
			}
		}
		particle.setCurrent_t(t);
		particle.setX(x2);
		particle.setV(v2);
		
	}

	/**
	 * 粒子的修正，因为更新速度和位移后可能违反约束
	 */
	private void modifyParticle(Particle p) {
		int patrolNum = 0;// 当前的巡逻单位数
		int x[] = p.getX();
		for (int i = 0; i < x.length; i++) {
			patrolNum += x[i];
		}

		int cha = patrolNum - patrolUnitNum;// 增加或减少的个数
		while (cha > 0) {
			int pos = random.nextInt(D);
			if (x[pos] > 1) {
				x[pos]--;
				cha--;
			}
		}

		while (cha < 0) {
			int pos = random.nextInt(D);
			if(x[pos]<upperPatrol){
				x[pos]++;
				cha++;
			}
			
		}
	}

	public void solve(int p) throws Exception {
		init2();
		initPopulation();

	
		/*for (t = 0; t < max_t; t++) {
			updateXV();

			updateBest();
			updateW();
			// printBest();
			// System.out.println("W("+(t+1)+") = "+w);
			if(t%50==0 || t==(max_t-1)){//当迭代10000次的时候，相当于每隔100代保存一个最优解
				Fitness f = new Fitness(t, gBest.getFitness());
				list.add(f);
			}
		}*/
		while(t<max_t) {
			for(int i=0;i<n;i++){
				if(t%50==0 || t==(max_t-1)){
					Fitness f = new Fitness(t, gBest.getFitness());
					list.add(f);
				}
				Particle particle = population[i];
				updateXV(particle,i);
				modifyParticle(particle);
				evaluate(particle);
				t++;
				updateW();
				updateBest(i);
				
			}
			
			
		}
		
		

		String result = printBest(p);
		FileUtils.saveResult(result, TYPE_NAME, RESULT_NAME,FileUtils.RESULT_PATH,PATH);
	}

	/**
	 * 打印种群
	 */
	private void printPopulation(Particle[] population) {
		int l;
		for (int i = 0; i < n; i++) {
			l = 0;
			Particle p = population[i];
			int allocation[] = p.getX();
			System.out.print("x = ");
			for (int j = 0; j < regionNum; j++) {
				for (int k = 0; k < roadNum[j]; k++) {
					System.out.print(allocation[l++] + " ");
				}
				System.out.print("|");
			}
			System.out.println("fitness = " + p.getFitness());

			l = 0;
			int v[] = p.getV();
			System.out.print("v = ");
			for (int j = 0; j < regionNum; j++) {
				for (int k = 0; k < roadNum[j]; k++) {
					System.out.print(v[l++] + " ");
				}
				System.out.print("|");
			}
			System.out.println();
		}
	}

	/**
	 * 打印pBest和gBest
	 */
	private String printBest(int p) {
		String result = "";
		String alloc = "";//最优分配方案
		int i, j, k;
		result += "===============第" + p + "个全局最优解================\n最佳代数：\n" + gBest.getCurrent_t() + "\n";
		int gx[] = gBest.getX();
		k = 0;
		// System.out.print("x = ");
		result += "最佳分配：\n";
		for (i = 0; i < regionNum; i++) {
			for (j = 0; j < roadNum[i]; j++) {
				result += gx[k] + " ";
				alloc += gx[k]+"\n";
				k++;
			}
			result += "|";
		}
		result += "\n最佳适应度：\n" + gBest.getFitness() + "\n\n";
		//System.out.println(result);

		
		//保存最优分配方案
	//	FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
		return result;
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
		
		ParticleStrategy pso, g = null;
		double sum = 0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Particle> results = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// 算法运行100次
			pso = new ParticleStrategy(50, patrolModel.getRegionNum(), 20*patrolModel.getRegionNum(), 2, 2, 0.4, 0.9, 200*patrolModel.getD(), patrolModel);
			pso.solve(i + 1);
			sum += pso.gBest.getFitness();
			if (fmin > pso.gBest.getFitness()) {
				fmin = pso.gBest.getFitness();
			}
			if (fmax < pso.gBest.getFitness()) {
				fmax = pso.gBest.getFitness();
				g = pso;
			}
			results.add((Particle) pso.gBest.clone());
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// 转化为秒
		System.out.println(
				"PSO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒");
		
		String result = "";
		result += "最大适应度：" + fmax + "\n最小适应度：" + fmin + "\n平均适应度：" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Particle particle = results.get(i);
			std += (particle.getFitness() - mean) * (particle.getFitness() - mean);
		}
		result += "估计标准差值：" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "最佳分配：\n";
		int k = 0;
		int alllocation[] = g.gBest.getX();
		int num = 0;
		for (int i = 0; i < g.regionNum; i++) {
			for (int j = 0; j < g.roadNum[i]; j++) {
				num += alllocation[k];
				result += alllocation[k++] + " ";
			}
			result += "|";
		}
		System.out.println("num = "+num);//测试
		//System.out.println(result);
		result += "\nPSO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒\n";
		FileUtils.saveResult(result, g.TYPE_NAME, g.RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		System.out.println("巡逻问题求解结束！");

	}

}
