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
import com.fzj.wwo.PWWOStrategy;
import com.fzj.wwo.Wave;

/** 
 * @author Fan Zhengjie 
 * @date 2016年10月2日 下午3:03:54 
 * @version 1.0 
 * @description
 */
public class PWWOThread extends Thread{
	@Override
	public void run() {
		System.out.println("PWWO开始求解巡逻问题,请等待...");
		PatrolModel patrolModel=null;
		try {
			patrolModel = new PatrolModel(Strategy.PATH);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		PWWOStrategy wwo, best = null;
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Wave> results = new ArrayList<>();
		List<Fitness> minBest = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// 运行60次
			wwo = new PWWOStrategy(50, Strategy.MAX_NFE, 20 * patrolModel.getRegionNum(), patrolModel);
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
			results.add((Wave) wwo.best.clone());// 存放每次运行的结果，以求估计方差值
		}
		//将迭代过程保存到excel中
		try {
			new ExcelUtil().writeExcel(minBest, Strategy.PATH2+best.TYPE_NAME+"\\pwwo_50_100D.xlsx", best.TYPE_NAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// 转化为秒

		String result = "";
		result += "最优解的最大适应度：" + fmax + "\n最优解的最小适应度：" + fmin + "\n最优解的平均适应度：" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Wave wave = (Wave) results.get(i);
			std += (wave.getFitness() - mean) * (wave.getFitness() - mean);
		}
		result += "估计标准差值：" + Math.sqrt(std / (runs - 1)) + "\n";
		result += "最优解的最佳分配：\n";
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
		// System.out.println(result);
		result += "\nPWWO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒\n";
		FileUtils.saveResult(result, best.TYPE_NAME, best.RESULT_NAME, FileUtils.RESULT_PATH, Strategy.PATH);
		System.out.println("巡逻问题PWWO求解结束！");
	}
}