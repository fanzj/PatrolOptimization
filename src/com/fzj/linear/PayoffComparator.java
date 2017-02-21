package com.fzj.linear;

import java.util.Comparator;

/** 
 * @author Fan Zhengjie 
 * @date 2016年10月25日 下午8:37:51 
 * @version 1.0 
 * @description
 */
public class PayoffComparator implements Comparator{

	@Override
	public int compare(Object o1, Object o2) {
		Pos x = (Pos)o1;
		Pos y = (Pos)o2;
		
		if(x.getPayoff()>y.getPayoff())
			return -1;
		else
			return 1;
	}

	
}
