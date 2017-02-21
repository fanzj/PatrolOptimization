package com.fzj.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fzj.bean.Fitness;
import com.fzj.model.PatrolModel;
import com.fzj.pso.Particle;
import com.fzj.pso.ParticleStrategy;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;

/** 
 * @author Fan Zhengjie 
 * @date 2016年10月2日 下午3:02:00 
 * @version 1.0 
 * @description
 */
public class PSOThread extends Thread{
	
	@Override
	public void run() {
		System.out.println("PSO算法开始求解巡逻问题,请等待...");
		PatrolModel patrolModel = null;
		try {
			patrolModel = new PatrolModel(Strategy.PATH);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ParticleStrategy pso, g = null;
		double sum = 0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Particle> results = new ArrayList<>();
		List<Fitness> minBest = new ArrayList<>();
		for (int i = 0; i < runs; i++) {// 算法运行100次
			pso = new ParticleStrategy(50, patrolModel.getRegionNum(), 20*patrolModel.getRegionNum(), 2, 2, 0.4, 0.9,Strategy.MAX_NFE, patrolModel);
			try {
				pso.solve(i + 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sum += pso.gBest.getFitness();
			if (fmin > pso.gBest.getFitness()) {
				fmin = pso.gBest.getFitness();
				minBest.clear();
				minBest.addAll(pso.list);
			}
			if (fmax < pso.gBest.getFitness()) {
				fmax = pso.gBest.getFitness();
				g = pso;
			}
			results.add((Particle) pso.gBest.clone());
		}
		//将迭代过程保存到excel中
		try {
			new ExcelUtil().writeExcel(minBest, Strategy.PATH2+g.TYPE_NAME+"\\pso_50_100D.xlsx", g.TYPE_NAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// 转化为秒
		
		
		String result = "";
		result += "最大适应度：" + fmax + "\n最小适应度：" + fmin + "\n平均适应度：" + sum / runs + "\n";
		double std = 0.0;
		double mean = sum / runs;
		for (int i = 0; i < runs; i++) {
			Particle particle = results.get(i);
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
	
		result += "\nPSO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒\n";
		FileUtils.saveResult(result, g.TYPE_NAME, g.RESULT_NAME,FileUtils.RESULT_PATH,Strategy.PATH);
		System.out.println("巡逻问题PSO求解结束！");
	}
	

}