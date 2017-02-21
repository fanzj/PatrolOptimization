package com.fzj.fade;

import java.util.Comparator;

/** 
 * @author Fan Zhengjie 
 * @date 2016年7月29日 上午10:31:49 
 * @version 1.0 
 * @description HSI比较器, 按HSI从大到小
 */
public class FComparator implements Comparator {

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		FireSpark FS1 = (FireSpark) o1;
		FireSpark FS2 = (FireSpark) o2;
		
		if(FS1.getFitness() > FS2.getFitness())
			return -1;
		else
			return 1;
	}

	
	

}
