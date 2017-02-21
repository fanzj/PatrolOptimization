package com.fzj.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fzj.bean.Fitness;
import com.fzj.dnspso.DnsParticle;
import com.fzj.dnspso.DnsParticleStrategy;
import com.fzj.fade.FireSpark;
import com.fzj.fade.FireSparkStrategy;
import com.fzj.model.PatrolModel;
import com.fzj.pso.Particle;
import com.fzj.pso.ParticleStrategy;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;

/** 
 * @author Fan Zhengjie 
 * @date 2016��10��2�� ����3:02:00 
 * @version 1.0 
 * @description
 */
public class FADEThread extends Thread{
	
	@Override
	public void run() {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		System.out.println("FADE�㷨��ʼ���Ѳ������,��ȴ�...");
		PatrolModel patrolModel = null;
		try {
			patrolModel = new PatrolModel(Strategy.PATH);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		FireSparkStrategy fade, g = null;
		double sum = 0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<FireSpark> results = new ArrayList<>();
		List<Fitness> minBest = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// �㷨����100��
			fade = new FireSparkStrategy(patrolModel, Strategy.MAX_NFE, 50, 20*patrolModel.getRegionNum());
			try {
				fade.solve(i + 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sum += fade.bestFireSpark.getFitness();
			if (fmin > fade.bestFireSpark.getFitness()) {
				fmin = fade.bestFireSpark.getFitness();
				minBest.clear();
				minBest.addAll(fade.list);
			}
			if (fmax < fade.bestFireSpark.getFitness()) {
				fmax = fade.bestFireSpark.getFitness();
				g = fade;
			}
			results.add((FireSpark) fade.bestFireSpark.clone());
		}
		// ���������̱��浽excel��
		try {
			new ExcelUtil().writeExcel(minBest, Strategy.PATH2 + g.TYPE_NAME + "\\fade_50_100D.xlsx", g.TYPE_NAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// ת��Ϊ��
		
		String result = "";
		result += "�����Ӧ�ȣ�" + fmax + "\n��С��Ӧ�ȣ�" + fmin + "\nƽ����Ӧ�ȣ�" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			FireSpark spark = results.get(i);
			std += (spark.getFitness() - mean) * (spark.getFitness() - mean);
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
	
		result += "\nFADE�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��\n";
		FileUtils.saveResult(result, g.TYPE_NAME, g.RESULT_NAME,FileUtils.RESULT_PATH,Strategy.PATH);
		System.out.println("Ѳ������FADE��������");
	}
	

}