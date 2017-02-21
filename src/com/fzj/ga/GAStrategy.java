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
 * @date 2016��7��22�� ����3:48:31
 * @version 1.0
 * @description Ѳ�ߵ�λ����GA���
 */
public class GAStrategy extends Strategy{

	
	private double Pc;// �������
	private double Pm;// �������
	public Solution beSolution;// ȫ�����Ž�
	private Solution[] oldPopulation;// ����Ⱥ
	private Solution[] newPopulation;// ����Ⱥ���Ӹ���Ⱥ��ѡȡ�����н���ͱ���
	

	public GAStrategy() {

	}

	/**
	 * 
	 * @param n
	 *            ��Ⱥ��ģ
	 * @param regionNum
	 *            ����������Ⱦɫ�峤��
	 * @param patrolUnitNum
	 *            Ҫ�����Ѳ�ߵ�λ��
	 * @param Pc
	 *            �������
	 * @param Pm
	 *            �������
	 * @param max_t
	 *            ����������
	 */
	public GAStrategy(int n, int regionNum, int patrolUnitNum, double Pc, double Pm, int max_t,PatrolModel patrolModel) {
		super(n, max_t, patrolUnitNum, patrolModel);
		this.regionNum = regionNum;
		this.Pc = Pc;
		this.Pm = Pm;
	}
	
	/**
	 * ������һЩ��ʼ������
	 * @throws IOException 
	 */
	public void init2() throws IOException{
		init();
		//ȫ�����Ž�ĳ�ʼ������
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
	 * ��Ⱥ��ʼ��
	 */
	protected void initPopulation(){
		int i,j,k,road;
		Region[] patrol_allocation;//��������
		int[] road_allocation;//����·�η����Ѳ�ߵ�λ
		int tempPatrolNum;
		
		for(k=0;k<n;k++){
			patrol_allocation = new Region[regionNum];
			tempPatrolNum = patrolUnitNum;
			//1.��֤ÿ��·��������1��Ѳ�ߵ�λ
			for(i=0;i<regionNum;i++){//����ÿ������
				road = roadNum[i];//�������·����
				road_allocation = new int[road];
				patrol_allocation[i] = new Region(road);
				for(j=0;j<road;j++){
					road_allocation[j] = 1;
					tempPatrolNum --;
				}
				patrol_allocation[i].setRegion_allocation(road_allocation);
			}
			
			while(tempPatrolNum!=0){
				for(i=0;i<regionNum;i++){//����ÿ������
					if(tempPatrolNum==0)
						break;
					road = roadNum[i];//�������·����
					road_allocation = patrol_allocation[i].getRegion_allocation();
					for(j=0;j<road;j++){
						if(tempPatrolNum==0){
							break;
						}
						int r = random.nextInt(2);
						if(road_allocation[j]<upperPatrol){
							road_allocation[j] += r;//r=0��1�����Ǹ�·���Ƿ����Ѳ�ߵ�λ
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
	 * ��Ӧ��ֵ����
	 * @param s 
	 */
	private void evaluate(Solution s){
		double fitness = 0.0;//��Ӧ��ֵ
		int i,j,k,l;
		double P[][][] = new double[crimeNum][regionNum][];//���ظ���
		int periodLen = TimeEffect.length;
		
		Region region[] = s.getPatrol_allocation();
		for(i=0;i<crimeNum;i++){
			for(j=0;j<regionNum;j++){
				int road_allo[] = region[j].getRegion_allocation();
				int roadnum = roadNum[j];
				P[i][j] = new double[roadnum];
				for(k=0;k<roadnum;k++){
					double temp = road_allo[k]*V*TD[i]*CD[i];
					P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//��Ϊ��������km,ת��Ϊm
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
	 * ������Ⱥ����������ۻ����ʣ�ǰ�����Ѿ�����������������Ӧ�ȣ�
	 * ��Ϊ���̶�ѡ����Ե�һ����
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
	 * ��ѡĳ����Ⱥ����Ӧ����ߵĸ��壬ֱ�Ӹ��Ƶ��Ӵ�
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
		
		//��������Ⱥ����Ӧ����ߵ�Ⱦɫ��maxid���Ƶ�����Ⱥ�У����ڵ�һλ
		copySolution(0, maxid);
		
	}
	
	/**
	 * ����Ⱦɫ��
	 * @param k1 ��ʾ��Ⱦɫ������Ⱥ�е�λ��
	 * @param k2 ��ʾ��Ⱦɫ������Ⱥ�е�λ��
	 */
	private void copySolution(int k1,int k2){
		newPopulation[k1] = (Solution) oldPopulation[k2].clone();
	}
	
	/**
	 * ��ӡ��Ⱥ
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
	 * ���̶�ѡ�������ѡ
	 */
	private void select(){
		int i,j,selectId;
		double r;
		
		for(i=1;i<n;i++){//���ŵ��Ѿ�ֱ�Ӹ��Ƶ��Ӵ������Դ˴���ѡ��n-1������
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
	 * ��������
	 * @param k1
	 * @param k2
	 */
	private void cross(int k1,int k2){
		int i,j,k;
		int len = 0;//����չ�����Ⱦɫ�峤�ȣ�����ͬ�����ж�����·��
		Solution s1 = newPopulation[k1];//������Ⱦɫ��k1
		Solution s2 = newPopulation[k2];//������Ⱦɫ��k2
		
		//����len
		for(i=0;i<regionNum;i++){
			len += roadNum[i];
		}
		
		int totalRoad1[] = new int[len];
		int totalRoad2[] = new int[len];
		
		Region region1[] = s1.getPatrol_allocation();
		Region region2[] = s2.getPatrol_allocation();
		k = 0;
		int max = -1;//��¼ĳ·�η�������Ѳ�ߵ�λ��
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
		
		int a[] = new int[max+1];//��¼Ⱦɫ��1�и���Ѳ�ߵ�λ����Ŀ������a[5],��ʾѲ�ߵ�λΪ5����a[5]��
		int b[] = new int[max+1];
		for(i=0;i<len;i++){
			a[totalRoad1[i]]++;
			b[totalRoad2[i]]++;
		}
		
		int ran = random.nextInt(65535) % len;
		while(ran==0){//��֤����ϵ���Ч
			ran = random.nextInt(65535) % len;
		}
		
		int temp;
		for(i=ran;i<len;i++){
			temp = totalRoad1[i];
			totalRoad1[i] = totalRoad2[i];
			totalRoad2[i] = temp;
		}
		
		int a2[] = new int[max+1];//�򵥽����ĸ���Ѳ�ߵ�λ�ĸ���
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
			if(a2[v1]-a[v1]>0 && b2[v2]-b[v2]>0)//��a2[v1]-a[v1]>0����v1
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
	 * ��ζԻ���������
	 * @param k ѡ�񣬽��������Ⱥ�еĵ�k��Ⱦɫ��
	 */
	private void variation(int k){
		int re1,re2;//��������
		int ro1,ro2;//���������·��
		int count;//�Ի�����
		int temp;
		
		count = random.nextInt(regionNum)+1;//regionNum�൱��Ⱦɫ�峤��
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
	 * ��������
	 */
	private void evolution(){
		int k;
		
		//1.ѡ��
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
		if(n%2!=0){//����Ⱥ��ģΪ���������һ�������н���
			r = random.nextDouble();
			if(r<Pm){
				variation(n-1);
			}
		}
	}
	
	/**
	 * �����������������Ⱦɫ�岻���н������
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
		if(n%2==0){//ʣ���һ��Ⱦɫ��û�н���
			r = random.nextDouble();
			if(r<Pm){
				variation(n-1);
			}
		}
	}
	
	public void solve(int p) throws Exception{
		init2();//������ʼ��
		initPopulation();//��Ⱥ��ʼ����������Ӧ��ֵ�ļ���
		countRate();//�����ʼ����Ⱥ�и���������ۻ�����
		
		List<Fitness> list = new ArrayList<>();
		//��������
		/*for(t=0;t<max_t;t++){
			evolution2();
			//������Ⱥ���Ƶ�����Ⱥ�У�׼����һ������
			for(int k=0;k<n;k++){
				oldPopulation[k] = (Solution) newPopulation[k].clone();
			}
			
			//������Ⱥ��Ӧ��
			for(int k=0;k<n;k++){
				evaluate(oldPopulation[k]);
			}
			
			//������Ⱥ�и���������ۻ�����
			countRate();
			
			if(t%50==0 || t==(max_t-1)){//������10000�ε�ʱ���൱��ÿ��50������һ�����Ž�
				Fitness f = new Fitness(t, beSolution.getFitness());
				list.add(f);
			}
		}*/
		while(t<max_t){
			evolution2();
			//������Ⱥ���Ƶ�����Ⱥ�У�׼����һ������
			for(int k=0;k<n;k++){
				oldPopulation[k] = (Solution) newPopulation[k].clone();
			}
			
			//������Ⱥ��Ӧ��
			for(int k=0;k<n;k++){
				evaluate(oldPopulation[k]);
				t++;
				if(t%50==0){
					Fitness f = new Fitness(t, beSolution.getFitness());
					list.add(f);
				}
			}
			
			//������Ⱥ�и���������ۻ�����
			countRate();
			
			
		}
		
		//���������̱��浽excel��
		new ExcelUtil().writeExcel(list, PATH2+TYPE_NAME+"\\ga_50_100D.xlsx", TYPE_NAME);
		
		String result = printBest(p);
		FileUtils.saveResult(result,TYPE_NAME, RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		
	}
	
	private String printBest(int p){
		//��Ѵ�ӡ
		String result = "";
		String alloc = "";//���ŷ��䷽��
		result += "=============��"+p+"�����Ž�����=============\n";
		result += "���Ŀ��ֵ���ִ�����\n";
		result += beSolution.getCurrent_t()+"\n";
		result += "���Ŀ��ֵΪ��\n";
		result += beSolution.getFitness()+"\n";
		result += "��ѷ��䷽����\n";
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
		//�������ŷ��䷽��
		//FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
		return result;
	}
	
	/**
	 * ģ������ 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("��ʼ���Ѳ������,��ȴ�...");
		long startTime = System.currentTimeMillis();// ��ʼʱ��
		PatrolModel patrolModel = new PatrolModel(PATH);//��ȡģ�Ͳ���
		long readFileEndTime = System.currentTimeMillis();
		long time = (readFileEndTime - startTime) / 1000;// ת��Ϊ��
		System.out.println("��ȡ�����ļ�����ʱ�䣺" + time / 3600 + "Сʱ��" + (time % 3600) / 60 + "���ӣ�" + (time % 60) + "��");
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
		long time2 = (readFileEndTime2 - startTime2) / 1000;// ת��Ϊ��
		System.out.println(
				"GA�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��");
		
		String result = "";
		result += "�����Ӧ�ȣ�"+fmax+"\n��С��Ӧ�ȣ�"+fmin+"\nƽ����Ӧ�ȣ�"+sum/runs+"\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Solution solution = results.get(i);
			std += (solution.getFitness() - mean) * (solution.getFitness() - mean);
		}
		result += "���Ʊ�׼��ֵ��" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "��ѷ��䣺\n";
		
		
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
		result += "\nGA�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��\n";
		FileUtils.saveResult(result,best.TYPE_NAME, best.RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		System.out.println("Ѳ��������������");
	}
}
