package com.fzj.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fzj.ga.GAStrategy;
import com.fzj.ga.Region;
import com.fzj.ga.Solution;
import com.fzj.model.PatrolModel;
import com.fzj.strategy.Strategy;
import com.fzj.utils.FileUtils;

/** 
 * @author Fan Zhengjie 
 * @date 2016��10��2�� ����3:00:04 
 * @version 1.0 
 * @description
 */
public class GAThread extends Thread{

	@Override
	public void run() {
		System.out.println("GA�㷨��ʼ���Ѳ������,��ȴ�...");
		PatrolModel patrolModel = null;
		try {
			patrolModel = new PatrolModel(Strategy.PATH);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//��ȡģ�Ͳ���
		
		GAStrategy patrol,best = null;
		
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 1;
		List<Solution> results = new ArrayList<>();
		for(int i=0;i<runs;i++){
			patrol = new GAStrategy(50,patrolModel.getRegionNum(),20*patrolModel.getRegionNum(),0.8,0.01,10000,patrolModel);
			try {
				patrol.solve(i+1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		FileUtils.saveResult(result,best.TYPE_NAME, best.RESULT_NAME,FileUtils.RESULT_PATH,Strategy.PATH);
		System.out.println("Ѳ������GA��������");
	}
}
