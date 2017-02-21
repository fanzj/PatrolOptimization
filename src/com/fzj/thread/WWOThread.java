package com.fzj.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fzj.bean.Fitness;
import com.fzj.ga.Region;
import com.fzj.model.PatrolModel;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;
import com.fzj.wwo.WWOStrategy;
import com.fzj.wwo.Wave;

/** 
 * @author Fan Zhengjie 
 * @date 2016��10��2�� ����3:03:12 
 * @version 1.0 
 * @description
 */
public class WWOThread extends Thread{
	@Override
	public void run() {
		System.out.println("WWO�㷨��ʼ���Ѳ������,��ȴ�...");
		PatrolModel patrolModel=null;
		try {
			patrolModel = new PatrolModel(Strategy.PATH);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		WWOStrategy wwo, best = null;
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Wave> results = new ArrayList<>();
		List<Fitness> minBest = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// ����60��
			wwo = new WWOStrategy(50, Strategy.MAX_NFE, 20 * patrolModel.getRegionNum(), patrolModel);
			try {
				wwo.solve(1 + i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sum += wwo.best.getFitness();
			if (fmin > wwo.best.getFitness()) {
				fmin = wwo.best.getFitness();
				minBest.clear();
				minBest.addAll(wwo.list);
			}
			if (fmax < wwo.best.getFitness()) {
				fmax = wwo.best.getFitness();
				best = wwo;
			}
			results.add((Wave) wwo.best.clone());// ���ÿ�����еĽ����������Ʒ���ֵ
		}
		//���������̱��浽excel��
		try {
			new ExcelUtil().writeExcel(minBest, Strategy.PATH2+best.TYPE_NAME+"\\wwo_50_100D.xlsx", best.TYPE_NAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// ת��Ϊ��

		String result = "";
		result += "���Ž�������Ӧ�ȣ�" + fmax + "\n���Ž����С��Ӧ�ȣ�" + fmin + "\n���Ž��ƽ����Ӧ�ȣ�" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Wave wave = (Wave) results.get(i);
			std += (wave.getFitness() - mean) * (wave.getFitness() - mean);
		}
		result += "���Ʊ�׼��ֵ��" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "���Ž����ѷ��䣺\n";
		Region[] regions = best.best.getPatrol_allocation();
		int realPatrol = 0;
		for (int i = 0; i < best.regionNum; i++) {
			int road = best.roadNum[i];
			int[] allocation = regions[i].getRegion_allocation();
			for (int j = 0; j < road; j++) {
				result += allocation[j] + " ";
				realPatrol += allocation[j];
			}
			result += "|";
		}
		result += best.best.getCurrentPatrolUnits();
		result += "|realPatrol = " + realPatrol;
	
		result += "\nWWO�㷨�������Ѳ�ߵ��������ʱ��" + time2 / 3600 + "Сʱ��" + (time2 % 3600) / 60 + "���ӣ�" + (time2 % 60) + "��\n";
		FileUtils.saveResult(result, best.TYPE_NAME, best.RESULT_NAME, FileUtils.RESULT_PATH, Strategy.PATH);
		System.out.println("Ѳ������WWO��������");
	}
}