package com.fzj.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fzj.bbo.BBOStrategy;
import com.fzj.bbo.Habitat;
import com.fzj.bean.Fitness;
import com.fzj.model.PatrolModel;
import com.fzj.strategy.Strategy;
import com.fzj.utils.ExcelUtil;
import com.fzj.utils.FileUtils;

/** 
 * @author Fan Zhengjie 
 * @date 2016年10月2日 下午3:02:37 
 * @version 1.0 
 * @description
 */
public class BBOThread extends Thread{
	@Override
	public void run() {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    	System.out.println("BBO算法开始求解巡逻问题,请等待...");
		PatrolModel patrolModel=null;
		try {
			patrolModel = new PatrolModel(Strategy.PATH);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		BBOStrategy bbo,best = null;
		double sum = 0.0;
		double fmin = Double.MAX_VALUE;
		double fmax = Double.MIN_VALUE;
		long startTime2 = System.currentTimeMillis();
		int runs = 60;
		List<Habitat> results = new ArrayList<>();
		List<Fitness> minBest = new ArrayList<>();
		for(int i=0;i<runs;i++){//运行100次
			bbo = new BBOStrategy(patrolModel, Strategy.MAX_NFE, 50,20*patrolModel.getRegionNum());
			try {
				bbo.solve(i+1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
    	try {
			new ExcelUtil().writeExcel(minBest, Strategy.PATH2+best.TYPE_NAME+"\\bbo_50_100D.xlsx", best.TYPE_NAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long readFileEndTime2 = System.currentTimeMillis();
		long time2 = (readFileEndTime2 - startTime2) / 1000;// 转化为秒
		
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
		result += "\nBBO算法运行求解巡逻调度问题耗时：" + time2 / 3600 + "小时，" + (time2 % 3600) / 60 + "分钟，" + (time2 % 60) + "秒\n";
		FileUtils.saveResult(result,best.TYPE_NAME, best.RESULT_NAME,FileUtils.RESULT_PATH,Strategy.PATH);
		System.out.println("巡逻问题BBO求解结束！");
	}
}