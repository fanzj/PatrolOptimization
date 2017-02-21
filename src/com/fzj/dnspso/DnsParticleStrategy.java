package com.fzj.dnspso;

import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import org.w3c.dom.ls.LSInput;

import com.fzj.bean.Fitness;
import com.fzj.model.PatrolModel;
import com.fzj.pso.Particle;
import com.fzj.pso.ParticleStrategy;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;

/**
 * @author Fan Zhengjie
 * @date 2016年11月11日 下午3:58:28
 * @version 1.0
 * @description
 */
public class DnsParticleStrategy extends Strategy {

	private double c1;// 学习因子
	private double c2;// 学习因子
	private int vmax;// 最大速度
	private int xmax;// 位移上限
	private double w;// 惯性权重
	private double wmin;// 最小权重系数
	private double wmax;// 最大权重系数

	public DnsParticle gBest;// 全局最优粒子
	private DnsParticle pBest[];// 历史最优粒子

	private DnsParticle population[];// 种群
	public List<Fitness> list;
	
	//DNSPSO增加参数
	private double Pr;//用于多样性增加机制
	private double Pns;//用于邻域搜索策略
	private int K;
	private double r1,r2,r3,r4,r5,r6;//用于邻域搜索生成的随机数

	public DnsParticleStrategy() {

	}

	public DnsParticleStrategy(int n, int regionNum, int patrolUnitNum, double c1, double c2, double wmin, double wmax,
			int max_t,double Pr,double Pns ,int K,PatrolModel patrolModel) {
		super(n, max_t, patrolUnitNum, patrolModel);
		this.regionNum = regionNum;
		this.c1 = c1;
		this.c2 = c2;
		this.wmin = wmin;
		this.wmax = wmax;
		this.Pr = Pr;
		this.Pns = Pns;
		this.K = K;

	}

	@Override
	protected void init2() throws IOException {
		// TODO Auto-generated method stub
		this.init();
		w = wmax;
		//this.w = 0.7298;
		population = new DnsParticle[n];
		this.vmax = upperPatrol-1;
		this.xmax = upperPatrol;
		pBest = new DnsParticle[n];
		gBest = new DnsParticle(D);
		gBest.setCt(0);
		gBest.setFitness(Double.MIN_VALUE);
		list = new ArrayList<>();
		
		TYPE_NAME = "DNSPSO";
		RESULT_NAME = "dnspso_60_100D.txt";
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
	@Override
	protected void initPopulation() {
		// TODO Auto-generated method stub
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

			DnsParticle p = new DnsParticle(D);
			p.setCt(0);
			p.setFitness(0);
			p.setX(allocation);
			p.setV(allocation);
			population[k] = p;

			evaluate(population[k]);

		}

		for (k = 0; k < n; k++) {
			pBest[k] = (DnsParticle) population[k].clone();
			if (gBest.getFitness() < population[k].getFitness()) {
				gBest = (DnsParticle) pBest[k].clone();
			}
		}

	}
	
	/**
	 * 适应度计算
	 * 
	 * @param p
	 */
	private void evaluate(DnsParticle p) {
		double fitness = 0.0;// 适应度值
		int i, j, k, l,m;
		double P[][][] = new double[crimeNum][regionNum][];// 拦截概率
		int periodLen = TimeEffect.length;

	
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
		}
*/
		p.setFitness(fitness);
	}

	/**
	 * 更新pBest和gBest
	 */
	private void updateBest(int k) {
		if (pBest[k].getFitness() < population[k].getFitness()) {
			pBest[k] = (DnsParticle) population[k].clone();
		}

		if (gBest.getFitness() < pBest[k].getFitness()) {
			gBest = (DnsParticle) pBest[k].clone();
		}

	}

	/**
	 * 更新粒子的位置和速度
	 * @param particle 更新的粒子
	 * @param index 粒子的索引
	 */
	private void updateXV(DnsParticle particle,int index) {
		double r1, r2;
		int xp[] = pBest[index].getX();
		int xg[] = gBest.getX();
		
		int m = 0;
		int x1[] = particle.getX();//粒子的原始位移
		int v1[] = particle.getV();//粒子的原始

		int x2[] = new int[D];
		int v2[] = new int[D];
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
		particle.setCt(t);
		particle.setX(x2);
		particle.setV(v2);
			
	}

	/**
	 * 粒子的修正，因为更新速度和位移后可能违反约束
	 */
	private void modifyParticle(DnsParticle p) {
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

		
		while(t<max_t){
			
			//第一阶段
			for(int i=0;i<n;i++){//遍历每个粒子
				if(t%50==0){
					Fitness f = new Fitness(t, gBest.getFitness());
					list.add(f);
				}
				DnsParticle Pit = population[i];
				//生成粒子Pi(t)
				updateXV(Pit, i);//计算粒子Pi的速度和位移	
				modifyParticle(Pit);//修正粒子			
				evaluate(Pit);//计算粒子Pi的适应度值
				t++;
				//生成粒子Pi(t+1)
				DnsParticle Pit2 = (DnsParticle) Pit.clone();
				updateXV(Pit2, i);
				modifyParticle(Pit2);
				evaluate(Pit2);
				updateW();
				
				
				//开始多样性提高机制
				DnsParticle TPi2 = dem(Pit, Pit2);
				//从Pit2和TPi2中选择较好的一个作为Pit2
				if(TPi2.getFitness() > Pit2.getFitness()){
					Pit2 = TPi2;
				}
				population[i] = Pit2;//保存
				updateBest(i);
				
			}
			//System.out.println("1 t = "+t);
			//第二阶段
			randomR();
			for(int i=0;i<n;i++){
				
				double randi = random.nextDouble();
				if(randi<=Pns){
					DnsParticle Pi = population[i];
					DnsParticle Li = LNS(Pi, i);//局部邻域搜索
					DnsParticle Gi = GNS(Pi, i);//全局邻域搜索
					modifyParticle(Li);
					evaluate(Li);
					modifyParticle(Gi);
					evaluate(Gi);
					//保存 
					if(t%50==0){
						Fitness f = new Fitness(t, gBest.getFitness());
						list.add(f);
					}
					t++;
					updateW();
					//保存 
					if(t%50==0){
						Fitness f = new Fitness(t, gBest.getFitness());
						list.add(f);
					}
					t++;
					updateW();
					population[i] = selectFittest(Pi, Li, Gi);
				}
				//更新pbesti和gbest
				updateBest(i);
			}
			//System.out.println("2 t = "+t);
		
		}
		
		
		String result = printBest(p);
		FileUtils.saveResult(result, TYPE_NAME, RESULT_NAME,FileUtils.RESULT_PATH,PATH);
	}
	
	/**
	 * 多样性提高机制
	 */
	private DnsParticle dem(DnsParticle Pit,DnsParticle Pit2){
		//生成新的粒子TPi2
		DnsParticle TPi2 = (DnsParticle) Pit2.clone();
		int txi2[] = new int[D];
		int tvi2[] = new int[D];
		
		int xit[] = Pit.getX();
		int xit2[] = Pit2.getX();
		int vit2[] = Pit2.getV();
		
		double randj = 0;
		for(int j=0;j<D;j++){
			randj = random.nextDouble();
			if(randj < Pr){
				txi2[j] = xit2[j];
			}
			else {
				txi2[j] = xit[j];
			}
			tvi2[j] = vit2[j];
		}
		TPi2.setX(xit2);
		TPi2.setV(vit2);
		modifyParticle(TPi2);
		evaluate(TPi2);
		t++;
		return TPi2;
	}
	
	/**
	 * 每代生成随机系数
	 */
	private void randomR(){
		r1 = random.nextDouble();
		r2 = random.nextDouble();
		while(r1+r2>1){
			r2 = random.nextDouble();
		}
		r3 = 1 - r1 - r2;
		r4 = random.nextDouble();
		r5 = random.nextDouble();
		while(r4+r5>1){
			r5 = random.nextDouble();
		}
		r6 = 1 - r4 - r5;
		
	}
	
	
	/**
	 * 局部搜索策略
	 * 生成Li
	 * @param Pi
	 * @return
	 */
	private DnsParticle LNS(DnsParticle Pi,int index){
		DnsParticle Li = (DnsParticle) Pi.clone();
		int lxi[] = new int[D];
		int lvi[] = new int[D];
		int pbestxi[] = pBest[index].getX();
		int xi[] = Pi.getX();
		int vi[] = Pi.getV();
		
		List<Integer> list = new ArrayList<>();
		for(int i=1;i<=K;i++)
		{
			list.add((index+i)%n);
			list.add((index-i+n)%n);
		}
		
		int x = random.nextInt(list.size());
		int y = random.nextInt(list.size());
		while(x==y){
			y = random.nextInt(list.size());
		}
		int c = list.get(x);
		int d = list.get(y);
		DnsParticle Pc = population[c];
		int xc[] = Pc.getX();
		DnsParticle Pd = population[d];
		int xd[] = Pd.getX();
		
		for(int j=0;j<D;j++){
			double temp_x = r1 * xi[j] + r2 * pbestxi[j] + r3 * (xc[j]-xd[j]);
			lxi[j] = (int) new Double(Math.ceil(temp_x)).intValue();
			lvi[j] = vi[j];
			
			if (lxi[j] > xmax) {
				lxi[j] = xmax;
			}else if(lxi[j]<1){
				lxi[j] = 1;
			}
		}
		Li.setX(lxi);
		Li.setV(lvi);
		
		return Li;
	}
	
	/**
	 * 全局邻域搜索策略
	 * @param Pi
	 * @param index
	 * @return
	 */
	private DnsParticle GNS(DnsParticle Pi,int index){
		DnsParticle Gi = (DnsParticle) Pi.clone();
		int gxi[] = new int[D];
		int gvi[] = new int[D];
		int gbestxi[] = gBest.getX();
		int xi[] = Pi.getX();
		int vi[] = Pi.getV();
		
		int e = random.nextInt(n);
		int f = random.nextInt(n);
		while(e==index){
			e = random.nextInt(n);
		}
		while(e==f || f==index){
			f = random.nextInt(n);
		}
		
		
		DnsParticle Pe = population[e];
		int xe[] = Pe.getX();
		DnsParticle Pf = population[f];
		int xf[] = Pf.getX();
		
		for(int j=0;j<D;j++){
			double temp_x = r4 * xi[j] + r5 * gbestxi[j] + r6 * (xe[j]-xf[j]);
			gxi[j] = (int) new Double(Math.ceil(temp_x)).intValue();
			gvi[j] = vi[j];
			if (gxi[j] > xmax) {
				gxi[j] = xmax;
			}else if(gxi[j]<1){
				gxi[j] = 1;
			}
		}
		Gi.setX(gxi);
		Gi.setV(gvi);
		return Gi;
	}
	
	/**
	 * 从a,b,c这三个粒子中选择最佳的一个返回
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private DnsParticle selectFittest(DnsParticle a,DnsParticle b,DnsParticle c){
		if(a.getFitness()>b.getFitness()){
			if(a.getFitness()>c.getFitness())
				return a;
			else
				return c;
		}
		else {
			if(b.getFitness()>c.getFitness())
				return b;
			else
				return c;
		}
	}

	
	/**
	 * 打印pBest和gBest
	 */
	private String printBest(int p) {
		String result = "";
		String alloc = "";//最优分配方案
		int i, j, k;
		result += "===============第" + p + "个全局最优解================\n最佳代数：\n" + gBest.getCt() + "\n";
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
		//FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
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
		
		DnsParticleStrategy dnspso, g = null;
		double sum = 0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<DnsParticle> results = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// 算法运行100次
			dnspso = new DnsParticleStrategy(50, patrolModel.getRegionNum(), 20*patrolModel.getRegionNum(), 1.49618, 1.49618, 0.4, 0.9, 200*patrolModel.getD(),0.9,0.6,2, patrolModel);
			dnspso.solve(i + 1);
			sum += dnspso.gBest.getFitness();
			if (fmin > dnspso.gBest.getFitness()) {
				fmin = dnspso.gBest.getFitness();
			}
			if (fmax < dnspso.gBest.getFitness()) {
				fmax = dnspso.gBest.getFitness();
				g = dnspso;
			}
			results.add((DnsParticle) dnspso.gBest.clone());
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// 转化为秒
		System.out.println(
				"DNSPSO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒");
		
		String result = "";
		result += "最大适应度：" + fmax + "\n最小适应度：" + fmin + "\n平均适应度：" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			DnsParticle particle = results.get(i);
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
		result += "\nDNSPSO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒\n";
		FileUtils.saveResult(result, g.TYPE_NAME, g.RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		System.out.println("巡逻问题求解结束！");

	}

}
