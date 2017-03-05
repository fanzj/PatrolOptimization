package com.fzj.fade;

import java.awt.dnd.DnDConstants;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import org.w3c.dom.ls.LSInput;

import com.fzj.bean.Fitness;
import com.fzj.dnspso.DnsParticle;
import com.fzj.dnspso.DnsParticleStrategy;
import com.fzj.model.PatrolModel;
import com.fzj.pso.Particle;
import com.fzj.pso.ParticleStrategy;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;

/**
 * @author Fan Zhengjie
 * @date 2016��11��11�� ����3:58:28
 * @version 1.0
 * @description
 */
public class FireSparkStrategy extends Strategy {

	private double epsilon = 0.0001;
	private int Smin;
	private int Smax;
	private double Ax;
	private int mx;
	private int m;
	public FireSpark bestFireSpark;// S�����Ž�
	private FireSpark worstFireSpark;//S������
	private FireSpark best;//R�����Ž�

	private FireSpark S[];// ��ʼ��Ⱥ
	private List<FireSpark> R;// ����̻��������ɵĻ���
	private List<FireSpark> P;
	private int Si[];//�̻�i���ɵĻ�����
	private double Ai[];//�̻�i���Ƶ����
	private boolean isSelected[];
	
	private double f;//����DE�ı���
	private double Cr;//����DE�Ľ���
	
	public List<Fitness> list;//��Ž��


	public FireSparkStrategy() {

	}

	 public FireSparkStrategy(PatrolModel patrolModel,int max_t,int n,int patrolUnitNum) {
			super(n, max_t, patrolUnitNum, patrolModel);		
	}
	

	@Override
	protected void init2() throws IOException {
		// TODO Auto-generated method stub
		this.init();

		this.Smin = 2;
		this.Smax = 20;
		this.m = 25;
		this.mx = 5;
		this.Ax = (upperPatrol-1)/7.0;
		this.f = 0.5;
		this.Cr = 0.9;
		
		S = new FireSpark[n];
		R = new ArrayList<>();
		bestFireSpark = new FireSpark(D);
		bestFireSpark.setFitness(Double.MIN_VALUE);
		worstFireSpark = new FireSpark(D);
		worstFireSpark.setFitness(Double.MAX_VALUE);
		best = new FireSpark(D);
		best.setFitness(Double.MIN_VALUE);
		Si = new int[n];
		Ai = new double[n];
		isSelected = new boolean[n];
		TYPE_NAME = "FADE";
		RESULT_NAME = "fade_60_100D.txt";
		list = new ArrayList<>();
	}

	/**
	 * ��Ⱥ��ʼ��
	 */
	@Override
	protected void initPopulation() {
		int i, j, k;
		int[] allocation;// ����Ѳ�ߵ�λ�ķ���
		int tempPatrolNum;

		for (k = 0; k < n; k++) {
			allocation = new int[D];
			tempPatrolNum = patrolUnitNum;
			// 1.��֤ÿ��·��������1��Ѳ�ߵ�λ
			for (i = 0; i < D; i++) {
				allocation[i] = 1;
				tempPatrolNum--;
			}

			while (tempPatrolNum != 0) {
				for (i = 0; i < D; i++) {// ����ÿ������
					if (tempPatrolNum == 0)
						break;

					int r = random.nextInt(2);
					if (allocation[i] + r > upperPatrol) {
						continue;
					}
					allocation[i] += r;// r=0��1�����Ǹ�·���Ƿ����Ѳ�ߵ�λ
					tempPatrolNum -= r;
				}
			}

			FireSpark spark = new FireSpark(D);
			spark.setCt(0);
			spark.setFitness(0);
			spark.setX(allocation);
			spark.setPi(0);
			S[k] = spark;

			evaluate(S[k]);
			
			if(bestFireSpark.getFitness()<S[k].getFitness()){
				bestFireSpark = (FireSpark) S[k].clone();
			}
			if(worstFireSpark.getFitness()>S[k].getFitness()){
				worstFireSpark = (FireSpark) S[k].clone();
			}
		}
	}

	/**
	 * ��Ӧ�ȼ���
	 * 
	 * @param p
	 */
	private void evaluate(FireSpark p) {
		double fitness = 0.0;// ��Ӧ��ֵ
		int i, j, k, l, m;
		double P[][][] = new double[crimeNum][regionNum][];// ���ظ���
		int periodLen = TimeEffect.length;

		int[] allocation = p.getX();
		for (i = 0; i < crimeNum; i++) {
			m = 0;
			for (j = 0; j < regionNum; j++) {
				int roadnum = roadNum[j];
				P[i][j] = new double[roadnum];
				for (k = 0; k < roadnum; k++) {
					double temp = allocation[m++] * V * TD[i] * CD[i];
					P[i][j][k] = temp / (temp + RoadLength[j][k] * 1000);// ��Ϊ��������km,ת��Ϊm
					double r = (W[i] * F[i][j][k] * P[i][j][k]);
					for (l = 0; l < periodLen; l++) {
						fitness += r * T[j][k][l];
					}
				}
			}
		}

		p.setFitness(fitness);
	}

	/**
	 * ���ӵ���������Ϊ�����ٶȺ�λ�ƺ����Υ��Լ��
	 */
	private void modifyParticle(FireSpark p) {
		int patrolNum = 0;// ��ǰ��Ѳ�ߵ�λ��
		int x[] = p.getX();
		for (int i = 0; i < x.length; i++) {
			patrolNum += x[i];
		}

		int cha = patrolNum - patrolUnitNum;// ���ӻ���ٵĸ���
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
		
		
		while (t < max_t) {
			R.clear();//��Ż���
			double sumS = 0.0,sumA = 0.0;
			for(int i=0;i<n;i++){//����S�е�ÿ���̻�
				FireSpark fireSpark = S[i];
				sumS += (bestFireSpark.getFitness()-fireSpark.getFitness());//���ڼ���Si
				sumA += (fireSpark.getFitness()-worstFireSpark.getFitness());//���ڼ���Ai
			}
			
			for(int i=0;i<n;i++){//����S�е�ÿ���̻�
				FireSpark fireSpark = S[i];
				int xi[] = fireSpark.getX();
				//����Si��Ai
				Si[i] = (int) Math.round(m*((worstFireSpark.getFitness()-fireSpark.getFitness()-epsilon)/(sumS-epsilon)));
				Ai[i] = Ax*((fireSpark.getFitness()-bestFireSpark.getFitness()-epsilon)/(sumA-epsilon));
//				Si[i] = (int) Math.round(m*((bestFireSpark.getFitness()-fireSpark.getFitness()+epsilon)/(sumS+epsilon)));
//				Ai[i] = Ax*((fireSpark.getFitness()-worstFireSpark.getFitness()+epsilon)/(sumA+epsilon));
				if(Si[i]<Smin)
					Si[i] = Smin;
				else if(Si[i]>Smax)
					Si[i] = Smax;
				
				//���̻�i����fireSpark���б���1
				for(int j=0;j<Si[i];j++){
					FireSpark spark = (FireSpark) fireSpark.clone();//����һ������
					int xj[] = spark.getX();
					int z = (int) Math.round(D*random.nextDouble());//����z���������ɢ��
					for(int k=0;k<z;k++){//ÿ�������൱������ά�ȵ�ĳ��ά
						int d = random.nextInt(D);//�������һά
						xj[d] = (int) (xi[d] + Math.round(Ai[i]*rand(-1,1)));
						if(xj[d]>upperPatrol || xj[d]<1){
							if(xj[d]<0)
								xj[d] = 1;
							else
								xj[d] = 1 + xj[d]%(upperPatrol-1);
						}
						/*if(xj[d]>upperPatrol)
							xj[d] = upperPatrol;
						else if(xj[d] < 1)
							xj[d] = 1;*/
					}
					spark.setX(xj);
					modifyParticle(spark);
					evaluate(spark);
					if(t%50==0){
						Fitness f = new Fitness(t, bestFireSpark.getFitness());
						list.add(f);
					}
					t++;
					R.add(spark);
				}
			}
			
			//��S�����ѡ��mx���̻���Ϊ����P���б���2
			P = new ArrayList<>();
			for(int j=0;j<n;j++){
				isSelected[j] = false;
			}
			for(int j=0;j<mx;j++){
				int index = random.nextInt(n);
				while(isSelected[index]){
					index = random.nextInt(n);
				}
				P.add((FireSpark) S[index].clone());
				isSelected[index] = true;
			}
			
			//����P���ϵ�ÿһ���̻����б���2
			for(int i=0;i<P.size();i++){
				FireSpark s1 = P.get(i);
				int xi[] = s1.getX();
				FireSpark s2 = (FireSpark) s1.clone();
				int xj[] = s2.getX();
				int z = (int) Math.round(D*random.nextDouble());
				for(int k=0;k<z;k++){//ÿ�������൱������ά�ȵ�ĳ��ά
					int d = random.nextInt(D);//�������һά
					xj[d] = (int) Math.round((1 + random.nextGaussian())*xi[d]);
					
					if(xj[d]>upperPatrol || xj[d]<1){
						if(xj[d]<0)
							xj[d] = 1;
						else
							xj[d] = 1 + xj[d]%(upperPatrol-1);
					}
					/*if(xj[d]>upperPatrol)
						xj[d] = upperPatrol;
					else if(xj[d] < 1)
						xj[d] = 1;*/
				}
				s2.setX(xj);
				modifyParticle(s2);
				evaluate(s2);
				if(t%50==0){
					Fitness f = new Fitness(t, bestFireSpark.getFitness());
					list.add(f);
				}
				t++;
				R.add(s2);
			}
			
			//R = R��S
			for(int i=0;i<n;i++){
				R.add((FireSpark)S[i].clone());
			}
			
			//��ʼ���в�����Ӳ���
			 S = new FireSpark[n];
			 sortRByF();
			/* for(int i=0;i<R.size();i++){
				 FireSpark spark = R.get(i);
				 System.out.println("F = "+spark.getFitness());
			 }*/
//			 System.out.println("1 R�ĳ���Ϊ��"+R.size());
			 best = (FireSpark) R.get(0).clone();
			 while(R.size()>2*n){
				 R.remove(R.size()-1);
			 }
//			 System.out.println("2 R�ĳ���Ϊ��"+R.size());
			 
			 //��R�����ѡ��n���������Ǽ���S��ÿ��x��R����һ��ѡ�����
			 //����������̶�ѡ��
			 countRate(R);//�����ۻ�����
			 int k=0;
			 for(int i=0;i<n;i++){
				 double r = (random.nextInt(65535)%1000)/1000.0;
				 for(int j=0;j<R.size();j++){
					 FireSpark spark =R.get(j);
					 if(spark.getPi()>=r){
						 S[k++] = (FireSpark) spark.clone();
						 break;
					 }
				 }
			 }
			// System.out.println("k = "+k);
			 //����S�е�ÿ���⣬Ӧ��DE�ı��졢���桢ѡ�����
			 int r1,r2,r3;
			 for(int i=0;i<n;i++){
				 FireSpark spark = S[i];
				 int xi[] = spark.getX(); 
				 
				 //����
				 r1 = random.nextInt(n);
				 while(r1==i){
					 r1 = random.nextInt(n);
				 }
				 r2 = random.nextInt(n);
				 while(i==r2 || r1==r2){
					 r2 = random.nextInt(n);
				 }
				 r3 = random.nextInt(n);
				 while(i==r3 || r1==r3 || r2==r3){
					 r3 = random.nextInt(n);
				 }
				// System.out.println("r1 = "+r1+",r2 = "+r2+",r3 = "+r3);
				 
				 FireSpark spark1 = S[r1];
				 FireSpark spark2 = S[r2];
				 FireSpark spark3 = S[r3];
				 int xr1[] = spark1.getX();
				// System.out.println("xr1 len = "+xr1.length);
				 int xr2[] = spark2.getX();
				// System.out.println("xr2 len = "+xr2.length);
				 int xr3[] = spark3.getX();
				// System.out.println("xr3 len = "+xr3.length);
				 int vig2[] = new int[D];//����������м����
				 for(int j=0;j<D;j++){
					 vig2[j] = (int) (xr1[j] + Math.round(f*(xr2[j]-xr3[j])));
					 if(vig2[j]>upperPatrol){
						 vig2[j] = upperPatrol;
					 } 
					 else if(vig2[j] < 1){
						 vig2[j] = 1;
					 }
						 
				 }
				 
				 //����
				 int ri = random.nextInt(n);
				 int ui[] = new int[D];
				 for(int j=0;j<D;j++){
					 double r = random.nextDouble();
					 if(r<Cr || j==ri){
						 ui[j] = vig2[j];
					 }
					 else{
						 ui[j] = xi[j];
					 }
				 }
				 FireSpark fireSpark = (FireSpark) S[i].clone();
				 fireSpark.setX(ui);
				 modifyParticle(fireSpark);
				 evaluate(fireSpark);
				 if(t%50==0){
					Fitness f = new Fitness(t, bestFireSpark.getFitness());
					list.add(f);
				 }
				 t++;
				 if(fireSpark.getFitness()>=spark.getFitness()){
					 S[i] = fireSpark;		
				 }
			 }
			 
			 //��bestFireSpark��ΪS�е���ý�
			 for(int i=0;i<n;i++){
				 FireSpark s = S[i];
				 if(bestFireSpark.getFitness()<s.getFitness()){
					 bestFireSpark = (FireSpark) s.clone();
				 }
				 if(worstFireSpark.getFitness()>s.getFitness()){
					 worstFireSpark = (FireSpark) s.clone();
				 }
			 }
			 if(bestFireSpark.getFitness()<best.getFitness()){
				 int index = random.nextInt(n);
				 if(S[index].getFitness()<worstFireSpark.getFitness()){
					 worstFireSpark = (FireSpark) S[index].clone();
				 }
				 S[index] = (FireSpark) best.clone();
				 bestFireSpark = (FireSpark) best.clone();
			 }
		}
		
		String result = printBest(p);
		FileUtils.saveResult(result, TYPE_NAME, RESULT_NAME, FileUtils.RESULT_PATH, PATH);
	}
	
	private double rand(double lower,double upper){
		return lower+random.nextDouble()*(upper-lower);
	}
	
	private void sortRByF(){
		FComparator fComparator = new FComparator();
		Collections.sort(R, fComparator);
	}
	
	/**
	 * ������Ⱥ����������ۻ����ʣ�ǰ�����Ѿ�����������������Ӧ�ȣ�
	 * ��Ϊ���̶�ѡ����Ե�һ����
	 */
	private void countRate(List<FireSpark> list){
		double sumFitness = 0.0;
		for(int i=0;i<list.size();i++){
			FireSpark spark = list.get(i);
			sumFitness += spark.getFitness();
		}
		
		FireSpark spark = list.get(0);
		spark.setPi(spark.getFitness()/sumFitness);
		for(int i=1;i<list.size();i++){
			FireSpark spark2 = list.get(i);
			FireSpark spark3 = list.get(i-1);
			spark2.setPi(spark2.getFitness()/sumFitness+spark3.getPi());
		}
		
		/*for(int i=0;i<list.size();i++){
			System.out.println("Pi = "+list.get(i).getPi());
		}*/
	}

	/**
	 * ��ӡpBest��gBest
	 */
	private String printBest(int p) {
		String result = "";
		String alloc = "";// ���ŷ��䷽��
		int i, j, k;
		result += "===============��" + p + "��ȫ�����Ž�================\n��Ѵ�����\n" + bestFireSpark.getCt() + "\n";
		int gx[] = bestFireSpark.getX();
		k = 0;
		// System.out.print("x = ");
		result += "��ѷ��䣺\n";
		for (i = 0; i < regionNum; i++) {
			for (j = 0; j < roadNum[i]; j++) {
				result += gx[k] + " ";
				alloc += gx[k] + "\n";
				k++;
			}
			result += "|";
		}
		result += "\n�����Ӧ�ȣ�\n" + bestFireSpark.getFitness() + "\n\n";
		//System.out.println(result);
		// �������ŷ��䷽��
		// FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt",
		// FileUtils.RESULT_PATH, PATH);
		return result;
	}

	/**
	 * ģ������
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		System.out.println("��ʼ���Ѳ������,��ȴ�...");
		long startTime = System.currentTimeMillis();// ��ʼʱ��
		PatrolModel patrolModel = new PatrolModel(PATH);
		long readFileEndTime = System.currentTimeMillis();
		long time = (readFileEndTime - startTime) / 1000;// ת��Ϊ��
		System.out.println("��ȡ�����ļ�����ʱ�䣺" + time / 3600 + "Сʱ��" + (time % 3600) / 60 + "���ӣ�" + (time % 60) + "��");
		
		FireSparkStrategy dnspso, g = null;
		double sum = 0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<FireSpark> results = new ArrayList<>();
		List<Fitness> minBest = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// �㷨����100��
			dnspso = new FireSparkStrategy(patrolModel, 200*patrolModel.getD(), 50, 20*patrolModel.getRegionNum());
			dnspso.solve(i + 1);
			sum += dnspso.bestFireSpark.getFitness();
			if (fmin > dnspso.bestFireSpark.getFitness()) {
				fmin = dnspso.bestFireSpark.getFitness();
				minBest.clear();
				minBest.addAll(dnspso.list);
			}
			if (fmax < dnspso.bestFireSpark.getFitness()) {
				fmax = dnspso.bestFireSpark.getFitness();
				g = dnspso;
			}
			results.add((FireSpark) dnspso.bestFireSpark.clone());
		}
		// ���������̱��浽excel��
		new ExcelUtil().writeExcel(minBest, PATH2 + g.TYPE_NAME + "\\fade_50_100D.xlsx", g.TYPE_NAME);
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// ת��Ϊ��
		System.out.println(
				"FADE�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��");
		
		String result = "";
		result += "�����Ӧ�ȣ�" + fmax + "\n��С��Ӧ�ȣ�" + fmin + "\nƽ����Ӧ�ȣ�" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			FireSpark particle = results.get(i);
			std += (particle.getFitness() - mean) * (particle.getFitness() - mean);
		}
		result += "���Ʊ�׼��ֵ��" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "��ѷ��䣺\n";
		int k = 0;
		int alllocation[] = g.bestFireSpark.getX();
		int num = 0;
		for (int i = 0; i < g.regionNum; i++) {
			for (int j = 0; j < g.roadNum[i]; j++) {
				num += alllocation[k];
				result += alllocation[k++] + " ";
			}
			result += "|";
		}
		System.out.println("num = "+num);//����
		//System.out.println(result);
		result += "\nFADE�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��\n";
		FileUtils.saveResult(result, g.TYPE_NAME, g.RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		System.out.println("Ѳ��������������");


	}

}
