package com.fzj.wwo;

import java.util.Comparator;

/** 
 * @author Fan Zhengjie 
 * @date 2016年9月29日 下午1:07:48 
 * @version 1.0 
 * @description Wave按适应度从大到小排序
 */
public class WWOComparator implements Comparator{

	@Override
	public int compare(Object o1, Object o2) {
		Wave wave1 = (Wave) o1;
		Wave wave2 = (Wave) o2;
		
		if(wave1.getFitness() > wave2.getFitness()){
			return -1;
		}
		else
			return 1;
	}

}
