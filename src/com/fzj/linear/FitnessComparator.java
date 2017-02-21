package com.fzj.linear;

import java.util.Comparator;

/** 
 * @author Fan Zhengjie 
 * @date 2016年10月25日 下午9:16:33 
 * @version 1.0 
 * @description
 */
public class FitnessComparator implements Comparator{

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		Pos x = (Pos)o1;
		Pos y = (Pos)o2;
		if(x.getFitness()>y.getFitness())
			return -1;
		else
			return 1;
	}

}
