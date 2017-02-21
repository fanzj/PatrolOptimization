package com.fzj.bbo;

import java.awt.RadialGradientPaint;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.fzj.bean.Fitness;
import com.fzj.ga.Region;
import com.fzj.ga.Solution;
import com.fzj.model.PatrolModel;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;
import com.fzj.wwo.Wave;

/** 
 * @author Fan Zhengjie 
 * @date 2016年7月28日 下午7:14:20 
 * @version 1.0 
 * @description 生物地理学优化算法求解
 */
public class BBOStrategy extends Strategy{
		
	private int Smax;//最大物种数量
	private double I;//最大迁入率
	private double E;//最大迁出率
	private double Pmod;//迁移率
	private double Mmax;//最大变异率
	private int z;//精英参数，精英个体留存数
	private double Pmax;//用于计算变异率
	private double Ps[];//栖息地i容纳Si种生物种群的概率；Ps[i]表示容纳i种生物的概率
	private double Im[];//栖息地i的迁入率,具有Si个物种的栖息地的迁入率
	private double Em[];//栖息地i的迁出率
	private double Mu[];//栖息地的变异率，index表示物种数量
	public Habitat population[];//种群
	public List<Fitness> list;

    
    public BBOStrategy() {
		// TODO Auto-generated constructor stub
	}
    
    public BBOStrategy(PatrolModel patrolModel,int max_t,int n,int patrolUnitNum) {
		super(n, max_t, patrolUnitNum, patrolModel);
		
		this.Smax = n;
		this.I = 1.0;
		this.E = 1.0;
		this.Pmod = 1.0;
		this.Mmax = 0.01;
		this.z = 2;
	}
    
    /**
     * 参数初始化
     * @throws IOException 
     */
    protected void init2() throws IOException{
    	this.init();
    	population = new Habitat[n];
    	
		Ps = new double[n+1];
		Im = new double[n+1];
		Em = new double[n+1];
		Mu = new double[n+1];
		list = new ArrayList<>();
		
		this.TYPE_NAME = "BBO";
		this.RESULT_NAME = "bbo_60_100D.txt";
    }
    
    /**
     * 初始化种群
     */
    protected void initPopulation(){
    	int i,j,k,road;
		int[] SIV_Allocation;
		int tempPatrolNum;
		
		for(k=0;k<n;k++){
			SIV_Allocation = new int[D];
			tempPatrolNum = patrolUnitNum;
			//1.保证每个路段至少有1个巡逻单位
			for(i=0;i<D;i++){//对于每个区域
				SIV_Allocation[i] = 1;
				tempPatrolNum --;
			}
			
			while(tempPatrolNum!=0){
				for(i=0;i<D;i++){//对于每一维
					if(tempPatrolNum==0)
						break;
					int r = random.nextInt(2);
					SIV_Allocation[i] += r;//r=0或1，考虑该路段是否分配巡逻单位
					tempPatrolNum -= r;
				}
			}
			
			
			Habitat H = new Habitat(D);
			H.setCurrent_t(0);
			H.setSIV(SIV_Allocation);
			H.setCurrentPatrolUnits(patrolUnitNum);
			population[k] = H;
			calHSI(population[k]);
		}
		
    }
    
    /**
     * 计算每个栖息地的HSI，即计算每个栖息地的适宜度
     */
    private void calHSI(Habitat habitat){
    	double HSI = 0.0;//适应度值
    	int i,j,k,l;
    	double P[][][] = new double[crimeNum][regionNum][];//拦截概率
    	int periodLen = TimeEffect.length;

    	//2.计算拦截概率
    /*	int SIV_Allocation[] = habitat.getSIV();
    	int y;
    	for(i=0;i<crimeNum;i++){
    		y = 0;
    		for(j=0;j<regionNum;j++){
    			int roadnum = roadNum[j];
    			int road_allo[] = new int[roadnum];
    			for(k=0;k<roadnum;k++){
    				road_allo[k] = SIV_Allocation[y];
    				y++;
    			}
    				
    			P[i][j] = new double[roadnum];
    			for(k=0;k<roadnum;k++){
    				double temp = road_allo[k]*V*TD[i]*CD[i];
    				P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//因为数组中是km,转化为m
    			}
    		}
    	}
    		
    	for(i=0;i<crimeNum;i++){
    		for(j=0;j<regionNum;j++){
    			for(k=0;k<roadNum[j];k++){
    				for(l=0;l<periodLen;l++){
    						HSI += W[i]*F[i][j][k]*P[i][j][k]*T[j][k][l];
    				}
    			}
    		}
    	}*/
    		
    	int SIV_Allocation[] = habitat.getSIV();
    	int y;
    	for(i=0;i<crimeNum;i++){
    		y = 0;
    		for(j=0;j<regionNum;j++){
    			int roadnum = roadNum[j];
    			int road_allo[] = new int[roadnum];
    				
    			P[i][j] = new double[roadnum];
    			for(k=0;k<roadnum;k++){
    				road_allo[k] = SIV_Allocation[y++];
    				double temp = road_allo[k]*V*TD[i]*CD[i];
    				P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//因为数组中是km,转化为m
    				
    				double temp2 =  W[i]*F[i][j][k]*P[i][j][k];
    				for(l=0;l<periodLen;l++){
						HSI += temp2*T[j][k][l];
    				}
    			}
    		}
    	}
    	
    	habitat.setHSI(HSI);
    	habitat.setCurrent_t(t);
    	
    }
    
    /**
     * 计算累积概率，作为轮盘赌选择的一部分
     * 前提是已经计算出HSI
     */
    private void countRate(){
    	double sumHSI = 0.0;
    	for(int i=0;i<n;i++){
    		sumHSI += population[i].getHSI();
    	}
    	
    	population[0].setPi(population[0].getHSI()/sumHSI);
    	for(int i=1;i<n;i++){
    		population[i].setPi(population[i].getHSI()/sumHSI + population[i-1].getPi());
    	}
    }
    
    /**
     * 将种群按HSI从大到小排序
     */
    private void sortPopulation(){
    	HSIComparator hsiComparator = new HSIComparator();//HSI比较器
    	Arrays.sort(population,hsiComparator);
    }
    
    /**
     * 计算各个栖息地i应对的物种数量Si（物种数量计算方法为何如此简单粗暴，改进？）
     * 迁入率im
     * 迁出率em
     */
    private void calS2MigrationRate(){

    	for(int k=0;k<=n;k++){//k表示种群数量
    		Im[k] = I*(1.0-k/(double)n);
    		Em[k] = E*k/(double)n;
    	}
    	
    	for(int i=0;i<n;i++){
    		Habitat habitat = population[i];
    		habitat.setS(Smax-1-i);
    		habitat.setIm(Im[Smax-i-1]);
    		habitat.setEm(Em[Smax-i-1]);
    	}
    }
    
    /**
     * 迁移操作
     */
    private void migration(){
    	double ran,ran2;
    	Habitat habitat;
    	int SIV_1[],SIV_2[] = null;
    	int i,j,k;
    	for(i=0;i<n;i++){
    		ran = random.nextDouble();
    		if(ran < Pmod){//栖息地i被确定发生迁入操作
    			habitat = population[i];
    			int patrolNum = habitat.getCurrentPatrolUnits();
    			SIV_1 = habitat.getSIV();
    			for(j=0;j<D;j++){
    				ran = random.nextDouble();
    				//利用栖息地i的迁入率im来判断，其特征分量是否发生迁入操作
    				if(ran < habitat.getIm()){//栖息地i的特征分量Xij被确定
    					//利用其它栖息地的迁出率进行轮盘赌选择
    					//选出栖息地k的对应位置替换栖息地i的对应位
    					ran2 = random.nextDouble();
    					for(k=0;k<n;k++){
    						if(ran2 < population[k].getEm()){
    							SIV_2 = population[k].getSIV();
    							
    							//记录此时的巡逻单位数
    	    					patrolNum += (SIV_2[j] - SIV_1[j]);
    	    					
    	    					//将迁出的栖息地k的第j维 --> 迁入的栖息地i的第j维
    	    					SIV_1[j] = SIV_2[j];
    	    			
    							break;
    						}
    					}
    				}
    			}
    			
    			habitat.setCurrentPatrolUnits(patrolNum);
    		}
    	}
    }
    
    /**
     * 变异操作
     * 对每一个非精英栖息地进行突变操作
     */
    private void mutation(){
    	double ran;
    	for(int i=0;i<n;i++){
    		Habitat habitat = population[i];
    		if(!habitat.isElite()){//突变非精英个体
    		//	System.out.println("i = "+i);
    			int patrolNum = habitat.getCurrentPatrolUnits();
        		int SIV[] = habitat.getSIV();
        		for(int j=0;j<D;j++){
        			ran = random.nextDouble();
        			if(ran < habitat.getMu()){//进行突变
        				int temp = SIV[j];
        				SIV[j] = 2 + SIV[j]/2 + random.nextInt(SIV[j]+1);//可以修改
        				//SIV[j] += 3;
        				int cha = SIV[j] - temp;
        				patrolNum += cha;
        				//变异后，可能违反约束
        				//将产生违反约束的行为，故进行如下操作
//    					int rPos = random.nextInt(D);
//    					while(rPos==j || (SIV[rPos]-cha) <1){
//    						rPos = random.nextInt(D);
//    					}
//    					SIV[rPos] -= cha;
        			}
        		}
        		habitat.setCurrentPatrolUnits(patrolNum);
    		}
    		
    	}
    }
    
    /**
     * 越界检验
     */
    private void checkBounds(){
    	for(int i=0;i<n;i++){
    		Habitat habitat = population[i];
    		int SIV[] = habitat.getSIV();
    		int patrol = habitat.getCurrentPatrolUnits();
    		for(int j=0;j<D;j++){
    			if(SIV[j]>upperPatrol){
    				patrol -= (SIV[j]-upperPatrol);
    				SIV[j] = upperPatrol;
    			}
    		}
    		habitat.setCurrentPatrolUnits(patrol);
    	}
    }
    
    /**
     * 经过迁移和变异操作，可能会产生不满足约束的情况。为此，需进行如下操作：
     * 假设总的分配巡逻单位为100。那么，分配策略总和应该满足该条件
     * 对于小于100的，生成随机位，进行加1操作；
     * 对于大于100的，生成随机位，进行减1操作；
     */
    private void modify2Constraint(){
    	int pos;
    	int i,j,k;
    	int r;
    	int sumPatrolUnits = 0;
    	for(i=0;i<n;i++){
    		Habitat habitat = population[i];
    		int SIV[] = habitat.getSIV();
    		int patrolNum = habitat.getCurrentPatrolUnits();
    		
    		boolean isAdd[] = new boolean[D];
    		for(j=0;j<D;j++){
    			isAdd[j] = false;
    		}
    		
    		while(patrolNum < patrolUnitNum){
    			pos = random.nextInt(D);//随机生成一个位置
    			if(!isAdd[pos]){
    				SIV[pos] ++;
            		patrolNum ++;
            		isAdd[pos] = true;
    			}
    		
    		}
    		
    		while(patrolNum>patrolUnitNum){
    			pos = random.nextInt(D);
    			if(SIV[pos]>1){
    				SIV[pos] --;
    				patrolNum --;
    			}
    			
    		}
    		
    		habitat.setCurrentPatrolUnits(patrolNum);
    	}
    }
    
    /**
     * 经过迁移和变异操作，可能会产生不满足约束的情况。为此，需进行如下操作：
     * 假设总的分配巡逻单位为100。那么，分配策略总和应该满足该条件
     * 生成随机位，进行求解
     */
    private void modify2Constraint2(){
    	int pos,count,avg;
    	int i,j;
   
    	for(i=0;i<n;i++){
    		Habitat habitat = population[i];
    		int SIV[] = habitat.getSIV();
    		int patrolNum = habitat.getCurrentPatrolUnits();
    		int cha = patrolNum - patrolUnitNum;
    		
    		if(cha !=0){
    			//count = random.nextInt(Math.abs(cha))+1;//分配次数
    			int r = cha>0?cha:-cha;
    			//count = random.nextInt(r)+1;
    			count = r/2 + 1;
    			avg = cha / count;
    			
        		while(count >0){
        			pos = random.nextInt(D);
        			if((SIV[pos] - avg) >= 1){
        				SIV[pos] -= avg;
        				cha -= avg;
        				patrolNum -= avg;
        				count--;
        			}	
        		}
        		
        	//	System.out.println(",cha = "+cha);
        		pos = random.nextInt(D);
        		if(SIV[pos]-cha >= 1){
        			SIV[pos] -= cha;
        			patrolNum -= cha;
        			cha -= cha;
        		}
        		
    		}
    		
    		
    		habitat.setCurrentPatrolUnits(patrolNum);
    	}
    }
    
    /**
     * 计算每个栖息地的种群数量概率P(Si)
     * 即栖息地i容纳Si种生物种群的概率
     */
    private void calPs(){
    	int mid = (int) Math.ceil((n+1)/2.0);
    	BigDecimal a,b,c;
    	int i;
    	double V[] = new double[n+1];
    	double sumV = 0;
    	for(i=0;i<mid;i++){
    		a = factorial(n);
    		b = factorial(n-2-i);
    		c = factorial(i);
    		V[i] = (double)a.intValue()/(double)(b.intValue()*c.intValue());
//    		System.out.println("V["+i+"] = "+V[i]);
    		sumV += V[i];
    	}
    	for(;i<n+1;i++){
    		V[i] = V[n-i];
//    		System.out.println("V["+i+"] = "+V[i]);
    		sumV += V[i];
    	}
//    	System.out.println("sumV = "+sumV);
    	
    	for(i=0;i<n+1;i++){
    		
    		Ps[i] = V[i]/sumV;
    	}
    	
    	for(i=n-1;i>=0;i--){
    		population[i].setPs(Ps[n-i-1]);
    	}
    	
    }
    
    /**
     * 更新每个栖息地的Ps(i)
     */
    private void updatePs(){
    	double newPs[] = new double[n+1];
    	Pmax = Double.MIN_VALUE;
    	int s;
    	for(int i=0;i<n;i++){
    		Habitat habitat = population[i];
    		s = habitat.getS();
    		if(s==0){
    			newPs[s] = -(habitat.getIm()+habitat.getEm())*Ps[s] + Em[s+1]*Ps[s+1];
    		}
    		else if(s >=1 && s <= Smax-1){
    			newPs[s] = -(habitat.getIm()+habitat.getEm())*habitat.getPs() + Im[s-1]*Ps[s-1] + Em[s+1]*Ps[s+1];
    		}
    		else if(s==Smax){
    			newPs[s] = -(habitat.getIm()+habitat.getEm())*habitat.getPs() + Im[s-1]*Ps[s-1];
    		}
    		habitat.setPs(newPs[s]);
    		if(newPs[s] > Pmax){
    			Pmax = newPs[s];
    		}
    	}
    	
    	s = Smax;
    	newPs[s] = -(Im[s]+Em[s])*Ps[s] + Im[s-1]*Ps[s-1];
    	if(newPs[s] > Pmax){
			Pmax = newPs[s];
		}
    	
    	for(int i=0;i<n+1;i++){
    		Ps[i] = newPs[i];
    	}
    }
    
    /**
     * 挑选z个精英个体
     * 前z个
     * 前提是已经按HSI进行过从大到小的排序
     */
    private void selectElite(){
    	int i;
    	for(i=0;i<z;i++){
    		Habitat habitat = population[i];
    		habitat.setElite(true);
    	}
    	
    	for(;i<n;i++){
    		Habitat habitat = population[i];
    		habitat.setElite(false);
    	}
    }
    
    /**
     * 计算每个栖息地的突变率
     * 突变每一个非精英的栖息地
     */
    private void calMutationRate(){
    	
    	for(int s=0;s<=n;s++){
    		Mu[s] = Mmax*((1-Ps[s])/Pmax);
    	}
    	for(int i=0;i<n;i++){
    		Habitat habitat = population[i];
    		habitat.setMu(Mu[habitat.getS()]);
    	}
    }
    
    /**
     * 返回种群中栖息地物种数为size的栖息地
     * @param size
     * @return
     */
    private Habitat getHabitat(int size){
    	Habitat habitat = null;
    	for(int i=0;i<n;i++){
    		if(population[i].getS()==size){
    			habitat = (Habitat) population[i].clone();
    			break;
    		}
    	}
    	return habitat;
    }
    
    /**
     * 求x！
     * @param x
     * @return
     */
    private BigDecimal factorial(int x){
    	BigDecimal result = new BigDecimal(1);
    	
    	if(x==0 || x==1)
    		return result;
    	
    	
    	BigDecimal a;
    	for(int i=2;i<=x;i++){
    		a = new BigDecimal(i);
    		result = result.multiply(a);
    	}
    	return result;
    }
    
    public void solve(int p) throws Exception{
    	init2();
    	initPopulation();
    	
    	
    	/*for(t=0;t<max_t;t++){
    		
	    	calHSI();
	    	sortPopulation();
	    	//选择精英个体
	    	selectElite();
	    	countRate();
	    	//计算各个 栖息地物种数量Si,迁入率im,迁出率em
	    	calS2MigrationRate();
	    
	    	if(t==0){
	    		calPs();//初次计算Ps
	    	}
	    	migration();
	    	checkBounds();
	    	//迁移后可能违反约束，因此需进行该操作
	    	modify2Constraint2();
		
	    	//重新计算各个栖息地的适宜度
	    	calHSI();
	    	
	    	//更新Ps[i]
	    	updatePs();
	    	//计算每个栖息地的突变概率
	    	calMutationRate();
	    	//突变突变
	    	mutation();
	    	
	    	checkBounds();
	    	//突变后的修正
	    	modify2Constraint2();
	    	
	    	//重新计算HSI
	    	//calHSI();
	    	
	    	if(t%50==0 || t==(max_t-1)){//当迭代10000次的时候，相当于每隔100代保存一个最优解
				Fitness f = new Fitness(t, population[0].getHSI());
				list.add(f);
			}

    	}*/
    	while(t<max_t){
    		if(t==0){
	    		calPs();//初次计算Ps
	    	}
    		for(int i=0;i<n;i++){
    			if(t%50==0 || t==(max_t-1)){
    				Fitness f = new Fitness(t, population[0].getHSI());
    				list.add(f);
    			}
    			Habitat habitat = population[i];
    			calHSI(habitat);
    			t++;
    			
    		}
	    	
	    	sortPopulation();
	    	//选择精英个体
	    	selectElite();
	    	countRate();
	    	//计算各个 栖息地物种数量Si,迁入率im,迁出率em
	    	calS2MigrationRate();
	    
	    	
	    	migration();
	    	checkBounds();
	    	//迁移后可能违反约束，因此需进行该操作
	    	modify2Constraint2();
		
	    	//重新计算各个栖息地的适宜度
	    	for(int i=0;i<n;i++){
	    		if(t%50==0 || t==(max_t-1)){
    				Fitness f = new Fitness(t, population[0].getHSI());
    				list.add(f);
    			}
    			Habitat habitat = population[i];
    			calHSI(habitat);
    			t++;
    			
    		}
	    	
	    	//更新Ps[i]
	    	updatePs();
	    	//计算每个栖息地的突变概率
	    	calMutationRate();
	    	//突变突变
	    	mutation();
	    	
	    	checkBounds();
	    	//突变后的修正
	    	modify2Constraint2();
	    	

    	}
    	
    	
    			
    	sortPopulation();
    	String result = printBest(p);
    	FileUtils.saveResult(result,TYPE_NAME,RESULT_NAME,FileUtils.RESULT_PATH,PATH);
    	
    }
    
    /**
     * 打印最优解
     */
    private String printBest(int p){
    	String result = "";
    	String alloc = "";//最优分配方案
    	result += "===============第"+p+"个最优解如下===============\n";
    	result += "最佳适宜值为：\n";
    	result += population[0].getHSI()+"\n";
    	result += "最佳迁移代数为：\n";
    	result += population[0].getCurrent_t()+"\n";
    	result += "最佳分配序列为：\n";
    	int SIV[] = population[0].getSIV();
    	int x = 0;
    	int num =0;
    	for(int i=0;i<regionNum;i++){
    		for(int j=0;j<roadNum[i];j++){
    			result += SIV[x]+" ";
    			num += SIV[x];
    			alloc += SIV[x]+"\n";
    			x++;
    		}
    		result += "|";
    	}
    	result += "num = "+num;
    	result += "\n\n";
    	//System.out.println(result);
    	
    	//保存最优分配方案
    //	FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
    	return result;
    }
    
    /**
     * 种群打印
     */
    private void printPopulation(){
    	for(int k=0;k<n;k++){
    		int x = 0;
    		int[] siv = population[k].getSIV();
    		for(int i=0;i<regionNum;i++){
    			for(int j=0;j<roadNum[i];j++){
    				System.out.print(siv[x++]+" ");
    			}
    			System.out.print("|");
    		}
    		System.out.println("HSI = "+population[k].getHSI()+"|Pi = "+population[k].getPi()+"|currentPatrolUnits = "+population[k].getCurrentPatrolUnits()+"|Si = "+population[k].getS()+"|im = "+population[k].getIm()+"|em = "+population[k].getEm());
    	}
    }
    
    
    
    public static void main(String[] args) throws Exception {
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    	System.out.println("开始求解巡逻问题,请等待...");
		long startTime = System.currentTimeMillis();// 起始时间
		PatrolModel patrolModel = new PatrolModel(PATH);
		long readFileEndTime = System.currentTimeMillis();
		long time = (readFileEndTime - startTime) / 1000;// 转化为秒
		System.out.println("读取数据文件所需时间：" + time / 3600 + "小时，" + (time % 3600) / 60 + "分钟，" + (time % 60) + "秒");
		
		BBOStrategy bbo,best = null;
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Habitat> results = new ArrayList<>();
		List<Fitness> minBest = new ArrayList<>();
		for(int i=0;i<runs;i++){//运行100次
			bbo = new BBOStrategy(patrolModel, 200*patrolModel.getD(), 50,20*patrolModel.getRegionNum());
			bbo.solve(i+1);
			sum += bbo.population[0].getHSI();
			if(fmin > bbo.population[0].getHSI()){
				fmin = bbo.population[0].getHSI();
				minBest.clear();
				minBest.addAll(bbo.list);
			}
			if(fmax < bbo.population[0].getHSI()){
				fmax = bbo.population[0].getHSI();
				best = bbo;
			}
			results.add((Habitat) bbo.population[0].clone());
		}
		//将迭代过程保存到excel中
    	new ExcelUtil().writeExcel(minBest, PATH2+best.TYPE_NAME+"\\bbo_50_100D.xlsx", best.TYPE_NAME);
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// 转化为秒
		System.out.println(
				"WWO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒");
		
		String result = "";
		result += "最大适应度："+fmax+"\n最小适应度："+fmin+"\n平均适应度："+sum/runs+"\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Habitat habitat = results.get(i);
			std += (habitat.getHSI() - mean) * (habitat.getHSI() - mean);
		}
		result += "估计标准差值：" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "最佳分配：\n";
		int k = 0;
		int alllocation[] = best.population[0].getSIV();
		int num = 0;
		for(int i=0;i<best.regionNum;i++){
			for(int j=0;j<best.roadNum[i];j++){
				num += alllocation[k];
				result += alllocation[k++]+" ";
			}
			result += "|";
		}
		result += "num = "+num;
		//System.out.println(result);
		result += "\nBBO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒\n";
		FileUtils.saveResult(result,best.TYPE_NAME, best.RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		System.out.println("巡逻问题求解结束！");
	}
}
