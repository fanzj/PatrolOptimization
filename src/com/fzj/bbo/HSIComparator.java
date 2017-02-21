package com.fzj.bbo;

import java.util.Comparator;

/** 
 * @author Fan Zhengjie 
 * @date 2016年7月29日 上午10:31:49 
 * @version 1.0 
 * @description HSI比较器, 按HSI从大到小
 */
public class HSIComparator implements Comparator {

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		Habitat H1 = (Habitat) o1;
		Habitat H2 = (Habitat) o2;
		
		if(H1.getHSI() > H2.getHSI())
			return -1;
		else
			return 1;
	}

	
	

}
