package com.fzj.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fzj.bbo.BBOStrategy;
import com.fzj.bbo.Habitat;
import com.fzj.ga.GAStrategy;
import com.fzj.ga.Region;
import com.fzj.ga.Solution;
import com.fzj.model.PatrolModel;
import com.fzj.pso.Particle;
import com.fzj.pso.ParticleStrategy;
import com.fzj.strategy.Strategy;
import com.fzj.utils.FileUtils;
import com.fzj.wwo.PWWOStrategy;
import com.fzj.wwo.WWOStrategy;
import com.fzj.wwo.Wave;

/** 
 * @author Fan Zhengjie 
 * @date 2016年10月2日 下午1:04:52 
 * @version 1.0 
 * @description
 */
public class PatrolThread {
	
	

	public static void main(String[] args) {
		
	//	Thread t1 = new GAThread();
		Thread t2 = new PSOThread();
		Thread t3 = new BBOThread();
		Thread t4 = new WWOThread();
		Thread t5 = new PWWOThread();
		Thread t6 = new DNSPSOThread();
		Thread t7 = new FADEThread();
		
		
		//t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		t7.start();
	}

}
