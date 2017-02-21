package com.fzj.linear;

import java.util.Comparator;

import com.fzj.thread.PSOThread;

/** 
 * @author Fan Zhengjie 
 * @date 2016年10月26日 下午1:21:25 
 * @version 1.0 
 * @description
 */
public class NumComparator implements Comparator{

	@Override
	public int compare(Object o1, Object o2) {
		Pos x = (Pos)o1;
		Pos y = (Pos)o2;
		if(x.getNum() < y.getNum())
			return -1;
		else 
			return 1;
	}

	
}
