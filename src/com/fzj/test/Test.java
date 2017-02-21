package com.fzj.test;

import com.fzj.ga.Region;
import com.fzj.wwo.Wave;

/** 
 * @author Fan Zhengjie 
 * @date 2016年9月29日 下午2:20:34 
 * @version 1.0 
 * @description
 */
public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int road[] = new int[]{2,3,4,3,2};
		Wave wave = new Wave(5);
		Region regions[] = new Region[5];
		for(int i=0;i<5;i++){
			Region region = new Region(road[i]);
			int []x = new int[road[i]];
			for(int j=0;j<road[i];j++){
				x[j] = j+1;
			}
			region.setRegion_allocation(x);
			regions[i]= region;
		}
		wave.setPatrol_allocation(regions);
		
		Region R[] = wave.getPatrol_allocation();
		for(int i=0;i<5;i++){
			Region r = R[i];
			int x[] = r.getRegion_allocation();
			for(int j=0;j<x.length;j++){
				if(j%2==0)
				{
					x[j] = -1;
				}
			}
		}
		
		//输出打印
		for(int i=0;i<5;i++){
			Region r = R[i];
			int x[] = r.getRegion_allocation();
			for(int j=0;j<x.length;j++){
				System.out.print(x[j]+" ");
			}
			System.out.print("|");
		}
		System.out.println();
	}

}
