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
 * @date 2016��11��11�� ����3:58:28
 * @version 1.0
 * @description
 */
public class DnsParticleStrategy extends Strategy {

	private double c1;// ѧϰ����
	private double c2;// ѧϰ����
	private int vmax;// ����ٶ�
	private int xmax;// λ������
	private double w;// ����Ȩ��
	private double wmin;// ��СȨ��ϵ��
	private double wmax;// ���Ȩ��ϵ��

	public DnsParticle gBest;// ȫ����������
	private DnsParticle pBest[];// ��ʷ��������

	private DnsParticle population[];// ��Ⱥ
	public List<Fitness> list;
	
	//DNSPSO���Ӳ���
	private double Pr;//���ڶ��������ӻ���
	private double Pns;//����������������
	private int K;
	private double r1,r2,r3,r4,r5,r6;//���������������ɵ������

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
	 * ����Ȩ�ظ���
	 */
	private void updateW() {
		w = wmax - ((double) t / (double) max_t) * (wmax - wmin);
	}

	/**
	 * ��Ⱥ��ʼ��
	 */
	@Override
	protected void initPopulation() {
		// TODO Auto-generated method stub
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
					if (allocation[i] + r > xmax) {
						continue;
					}
					allocation[i] += r;// r=0��1�����Ǹ�·���Ƿ����Ѳ�ߵ�λ
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
	 * ��Ӧ�ȼ���
	 * 
	 * @param p
	 */
	private void evaluate(DnsParticle p) {
		double fitness = 0.0;// ��Ӧ��ֵ
		int i, j, k, l,m;
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
					P[i][j][k] = temp / (temp + RoadLength[j][k] * 1000);// ��Ϊ��������km,ת��Ϊm
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
	 * ����pBest��gBest
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
	 * �������ӵ�λ�ú��ٶ�
	 * @param particle ���µ�����
	 * @param index ���ӵ�����
	 */
	private void updateXV(DnsParticle particle,int index) {
		double r1, r2;
		int xp[] = pBest[index].getX();
		int xg[] = gBest.getX();
		
		int m = 0;
		int x1[] = particle.getX();//���ӵ�ԭʼλ��
		int v1[] = particle.getV();//���ӵ�ԭʼ

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
	 * ���ӵ���������Ϊ�����ٶȺ�λ�ƺ����Υ��Լ��
	 */
	private void modifyParticle(DnsParticle p) {
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

		
		while(t<max_t){
			
			//��һ�׶�
			for(int i=0;i<n;i++){//����ÿ������
				if(t%50==0){
					Fitness f = new Fitness(t, gBest.getFitness());
					list.add(f);
				}
				DnsParticle Pit = population[i];
				//��������Pi(t)
				updateXV(Pit, i);//��������Pi���ٶȺ�λ��	
				modifyParticle(Pit);//��������			
				evaluate(Pit);//��������Pi����Ӧ��ֵ
				t++;
				//��������Pi(t+1)
				DnsParticle Pit2 = (DnsParticle) Pit.clone();
				updateXV(Pit2, i);
				modifyParticle(Pit2);
				evaluate(Pit2);
				updateW();
				
				
				//��ʼ��������߻���
				DnsParticle TPi2 = dem(Pit, Pit2);
				//��Pit2��TPi2��ѡ��Ϻõ�һ����ΪPit2
				if(TPi2.getFitness() > Pit2.getFitness()){
					Pit2 = TPi2;
				}
				population[i] = Pit2;//����
				updateBest(i);
				
			}
			//System.out.println("1 t = "+t);
			//�ڶ��׶�
			randomR();
			for(int i=0;i<n;i++){
				
				double randi = random.nextDouble();
				if(randi<=Pns){
					DnsParticle Pi = population[i];
					DnsParticle Li = LNS(Pi, i);//�ֲ���������
					DnsParticle Gi = GNS(Pi, i);//ȫ����������
					modifyParticle(Li);
					evaluate(Li);
					modifyParticle(Gi);
					evaluate(Gi);
					//���� 
					if(t%50==0){
						Fitness f = new Fitness(t, gBest.getFitness());
						list.add(f);
					}
					t++;
					updateW();
					//���� 
					if(t%50==0){
						Fitness f = new Fitness(t, gBest.getFitness());
						list.add(f);
					}
					t++;
					updateW();
					population[i] = selectFittest(Pi, Li, Gi);
				}
				//����pbesti��gbest
				updateBest(i);
			}
			//System.out.println("2 t = "+t);
		
		}
		
		
		String result = printBest(p);
		FileUtils.saveResult(result, TYPE_NAME, RESULT_NAME,FileUtils.RESULT_PATH,PATH);
	}
	
	/**
	 * ��������߻���
	 */
	private DnsParticle dem(DnsParticle Pit,DnsParticle Pit2){
		//�����µ�����TPi2
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
	 * ÿ���������ϵ��
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
	 * �ֲ���������
	 * ����Li
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
	 * ȫ��������������
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
	 * ��a,b,c������������ѡ����ѵ�һ������
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
	 * ��ӡpBest��gBest
	 */
	private String printBest(int p) {
		String result = "";
		String alloc = "";//���ŷ��䷽��
		int i, j, k;
		result += "===============��" + p + "��ȫ�����Ž�================\n��Ѵ�����\n" + gBest.getCt() + "\n";
		int gx[] = gBest.getX();
		k = 0;
		// System.out.print("x = ");
		result += "��ѷ��䣺\n";
		for (i = 0; i < regionNum; i++) {
			for (j = 0; j < roadNum[i]; j++) {
				result += gx[k] + " ";
				alloc += gx[k]+"\n";
				k++;
			}
			result += "|";
		}
		result += "\n�����Ӧ�ȣ�\n" + gBest.getFitness() + "\n\n";
		//System.out.println(result);

		
		//�������ŷ��䷽��
		//FileUtils.saveResult(alloc, TYPE_NAME, "allocation.txt", FileUtils.RESULT_PATH, PATH);
		return result;
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
		
		DnsParticleStrategy dnspso, g = null;
		double sum = 0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<DnsParticle> results = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// �㷨����100��
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
		long time2 = (readFileEndTime2 - startTime2) / 1000;// ת��Ϊ��
		System.out.println(
				"DNSPSO�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��");
		
		String result = "";
		result += "�����Ӧ�ȣ�" + fmax + "\n��С��Ӧ�ȣ�" + fmin + "\nƽ����Ӧ�ȣ�" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			DnsParticle particle = results.get(i);
			std += (particle.getFitness() - mean) * (particle.getFitness() - mean);
		}
		result += "���Ʊ�׼��ֵ��" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "��ѷ��䣺\n";
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
		System.out.println("num = "+num);//����
		//System.out.println(result);
		result += "\nDNSPSO�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��\n";
		FileUtils.saveResult(result, g.TYPE_NAME, g.RESULT_NAME,FileUtils.RESULT_PATH,PATH);
		System.out.println("Ѳ��������������");

	}

}
