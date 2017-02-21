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
 * @date 2016��7��28�� ����7:14:20 
 * @version 1.0 
 * @description �������ѧ�Ż��㷨���
 */
public class BBOStrategy extends Strategy{
		
	private int Smax;//�����������
	private double I;//���Ǩ����
	private double E;//���Ǩ����
	private double Pmod;//Ǩ����
	private double Mmax;//��������
	private int z;//��Ӣ��������Ӣ����������
	private double Pmax;//���ڼ��������
	private double Ps[];//��Ϣ��i����Si��������Ⱥ�ĸ��ʣ�Ps[i]��ʾ����i������ĸ���
	private double Im[];//��Ϣ��i��Ǩ����,����Si�����ֵ���Ϣ�ص�Ǩ����
	private double Em[];//��Ϣ��i��Ǩ����
	private double Mu[];//��Ϣ�صı����ʣ�index��ʾ��������
	public Habitat population[];//��Ⱥ
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
     * ������ʼ��
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
     * ��ʼ����Ⱥ
     */
    protected void initPopulation(){
    	int i,j,k,road;
		int[] SIV_Allocation;
		int tempPatrolNum;
		
		for(k=0;k<n;k++){
			SIV_Allocation = new int[D];
			tempPatrolNum = patrolUnitNum;
			//1.��֤ÿ��·��������1��Ѳ�ߵ�λ
			for(i=0;i<D;i++){//����ÿ������
				SIV_Allocation[i] = 1;
				tempPatrolNum --;
			}
			
			while(tempPatrolNum!=0){
				for(i=0;i<D;i++){//����ÿһά
					if(tempPatrolNum==0)
						break;
					int r = random.nextInt(2);
					SIV_Allocation[i] += r;//r=0��1�����Ǹ�·���Ƿ����Ѳ�ߵ�λ
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
     * ����ÿ����Ϣ�ص�HSI��������ÿ����Ϣ�ص����˶�
     */
    private void calHSI(Habitat habitat){
    	double HSI = 0.0;//��Ӧ��ֵ
    	int i,j,k,l;
    	double P[][][] = new double[crimeNum][regionNum][];//���ظ���
    	int periodLen = TimeEffect.length;

    	//2.�������ظ���
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
    				P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//��Ϊ��������km,ת��Ϊm
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
    				P[i][j][k] = temp/(temp+RoadLength[j][k]*1000);//��Ϊ��������km,ת��Ϊm
    				
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
     * �����ۻ����ʣ���Ϊ���̶�ѡ���һ����
     * ǰ�����Ѿ������HSI
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
     * ����Ⱥ��HSI�Ӵ�С����
     */
    private void sortPopulation(){
    	HSIComparator hsiComparator = new HSIComparator();//HSI�Ƚ���
    	Arrays.sort(population,hsiComparator);
    }
    
    /**
     * ���������Ϣ��iӦ�Ե���������Si�������������㷽��Ϊ����˼򵥴ֱ����Ľ�����
     * Ǩ����im
     * Ǩ����em
     */
    private void calS2MigrationRate(){

    	for(int k=0;k<=n;k++){//k��ʾ��Ⱥ����
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
     * Ǩ�Ʋ���
     */
    private void migration(){
    	double ran,ran2;
    	Habitat habitat;
    	int SIV_1[],SIV_2[] = null;
    	int i,j,k;
    	for(i=0;i<n;i++){
    		ran = random.nextDouble();
    		if(ran < Pmod){//��Ϣ��i��ȷ������Ǩ�����
    			habitat = population[i];
    			int patrolNum = habitat.getCurrentPatrolUnits();
    			SIV_1 = habitat.getSIV();
    			for(j=0;j<D;j++){
    				ran = random.nextDouble();
    				//������Ϣ��i��Ǩ����im���жϣ������������Ƿ���Ǩ�����
    				if(ran < habitat.getIm()){//��Ϣ��i����������Xij��ȷ��
    					//����������Ϣ�ص�Ǩ���ʽ������̶�ѡ��
    					//ѡ����Ϣ��k�Ķ�Ӧλ���滻��Ϣ��i�Ķ�Ӧλ
    					ran2 = random.nextDouble();
    					for(k=0;k<n;k++){
    						if(ran2 < population[k].getEm()){
    							SIV_2 = population[k].getSIV();
    							
    							//��¼��ʱ��Ѳ�ߵ�λ��
    	    					patrolNum += (SIV_2[j] - SIV_1[j]);
    	    					
    	    					//��Ǩ������Ϣ��k�ĵ�jά --> Ǩ�����Ϣ��i�ĵ�jά
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
     * �������
     * ��ÿһ���Ǿ�Ӣ��Ϣ�ؽ���ͻ�����
     */
    private void mutation(){
    	double ran;
    	for(int i=0;i<n;i++){
    		Habitat habitat = population[i];
    		if(!habitat.isElite()){//ͻ��Ǿ�Ӣ����
    		//	System.out.println("i = "+i);
    			int patrolNum = habitat.getCurrentPatrolUnits();
        		int SIV[] = habitat.getSIV();
        		for(int j=0;j<D;j++){
        			ran = random.nextDouble();
        			if(ran < habitat.getMu()){//����ͻ��
        				int temp = SIV[j];
        				SIV[j] = 2 + SIV[j]/2 + random.nextInt(SIV[j]+1);//�����޸�
        				//SIV[j] += 3;
        				int cha = SIV[j] - temp;
        				patrolNum += cha;
        				//����󣬿���Υ��Լ��
        				//������Υ��Լ������Ϊ���ʽ������²���
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
     * Խ�����
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
     * ����Ǩ�ƺͱ�����������ܻ����������Լ���������Ϊ�ˣ���������²�����
     * �����ܵķ���Ѳ�ߵ�λΪ100����ô����������ܺ�Ӧ�����������
     * ����С��100�ģ��������λ�����м�1������
     * ���ڴ���100�ģ��������λ�����м�1������
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
    			pos = random.nextInt(D);//�������һ��λ��
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
     * ����Ǩ�ƺͱ�����������ܻ����������Լ���������Ϊ�ˣ���������²�����
     * �����ܵķ���Ѳ�ߵ�λΪ100����ô����������ܺ�Ӧ�����������
     * �������λ���������
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
    			//count = random.nextInt(Math.abs(cha))+1;//�������
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
     * ����ÿ����Ϣ�ص���Ⱥ��������P(Si)
     * ����Ϣ��i����Si��������Ⱥ�ĸ���
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
     * ����ÿ����Ϣ�ص�Ps(i)
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
     * ��ѡz����Ӣ����
     * ǰz��
     * ǰ�����Ѿ���HSI���й��Ӵ�С������
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
     * ����ÿ����Ϣ�ص�ͻ����
     * ͻ��ÿһ���Ǿ�Ӣ����Ϣ��
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
     * ������Ⱥ����Ϣ��������Ϊsize����Ϣ��
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
     * ��x��
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
	    	//ѡ��Ӣ����
	    	selectElite();
	    	countRate();
	    	//������� ��Ϣ����������Si,Ǩ����im,Ǩ����em
	    	calS2MigrationRate();
	    
	    	if(t==0){
	    		calPs();//���μ���Ps
	    	}
	    	migration();
	    	checkBounds();
	    	//Ǩ�ƺ����Υ��Լ�����������иò���
	    	modify2Constraint2();
		
	    	//���¼��������Ϣ�ص����˶�
	    	calHSI();
	    	
	    	//����Ps[i]
	    	updatePs();
	    	//����ÿ����Ϣ�ص�ͻ�����
	    	calMutationRate();
	    	//ͻ��ͻ��
	    	mutation();
	    	
	    	checkBounds();
	    	//ͻ��������
	    	modify2Constraint2();
	    	
	    	//���¼���HSI
	    	//calHSI();
	    	
	    	if(t%50==0 || t==(max_t-1)){//������10000�ε�ʱ���൱��ÿ��100������һ�����Ž�
				Fitness f = new Fitness(t, population[0].getHSI());
				list.add(f);
			}

    	}*/
    	while(t<max_t){
    		if(t==0){
	    		calPs();//���μ���Ps
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
	    	//ѡ��Ӣ����
	    	selectElite();
	    	countRate();
	    	//������� ��Ϣ����������Si,Ǩ����im,Ǩ����em
	    	calS2MigrationRate();
	    
	    	
	    	migration();
	    	checkBounds();
	    	//Ǩ�ƺ����Υ��Լ�����������иò���
	    	modify2Constraint2();
		
	    	//���¼��������Ϣ�ص����˶�
	    	for(int i=0;i<n;i++){
	    		if(t%50==0 || t==(max_t-1)){
    				Fitness f = new Fitness(t, population[0].getHSI());
    				list.add(f);
    			}
    			Habitat habitat = population[i];
    			calHSI(habitat);
    			t++;
    			
    		}
	    	
	    	//����Ps[i]
	    	updatePs();
	    	//����ÿ����Ϣ�ص�ͻ�����
	    	calMutationRate();
	    	//ͻ��ͻ��
	    	mutation();
	    	
	    	checkBounds();
	    	//ͻ��������
	    	modify2Constraint2();
	    	

    	}
    	
    	
    			
    	sortPopulation();
    	String result = printBest(p);
    	FileUtils.saveResult(result,TYPE_NAME,RESULT_NAME,FileUtils.RESULT_PATH,PATH);
    	
    }
    
    /**
     * ��ӡ���Ž�
     */
    private String printBest(int p){
    	String result = "";
    	String alloc = "";//���ŷ��䷽��
    	result += "===============��"+p+"�����Ž�����===============\n";
    	result += "�������ֵΪ��\n";
    	result += population[0].getHSI()+"\n";
    	result += "���Ǩ�ƴ���Ϊ��\n";
    	result += population[0].getCurrent_t()+"\n";
    	result += "��ѷ�������Ϊ��\n";
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
    	
    	//�������ŷ��䷽��
    //	FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
    	return result;
    }
    
    /**
     * ��Ⱥ��ӡ
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
    	System.out.println("��ʼ���Ѳ������,��ȴ�...");
		long startTime = System.currentTimeMillis();// ��ʼʱ��
		PatrolModel patrolModel = new PatrolModel(PATH);
		long readFileEndTime = System.currentTimeMillis();
		long time = (readFileEndTime - startTime) / 1000;// ת��Ϊ��
		System.out.println("��ȡ�����ļ�����ʱ�䣺" + time / 3600 + "Сʱ��" + (time % 3600) / 60 + "���ӣ�" + (time % 60) + "��");
		
		BBOStrategy bbo,best = null;
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Habitat> results = new ArrayList<>();
		List<Fitness> minBest = new ArrayList<>();
		for(int i=0;i<runs;i++){//����100��
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
		//���������̱��浽excel��
    	new ExcelUtil().writeExcel(minBest, PATH2+best.TYPE_NAME+"\\bbo_50_100D.xlsx", best.TYPE_NAME);
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// ת��Ϊ��
		System.out.println(
				"WWO�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��");
		
		String result = "";
		result += "�����Ӧ�ȣ�"+fmax+"\n��С��Ӧ�ȣ�"+fmin+"\nƽ����Ӧ�ȣ�"+sum/runs+"\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Habitat habitat = results.get(i);
			std += (habitat.getHSI() - mean) * (habitat.getHSI() - mean);
		}
		result += "���Ʊ�׼��ֵ��" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "��ѷ��䣺\n";
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
		result += "\nBBO�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��\n";
		FileUtils.saveResult(result,best.TYPE_NAME, best.RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		System.out.println("Ѳ��������������");
	}
}
