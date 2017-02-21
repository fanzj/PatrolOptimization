package com.fzj.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.xml.transform.stream.StreamResult;

/**
 * @author Fan Zhengjie
 * @date 2016年7月22日 下午4:28:51
 * @version 1.0
 * @description 文件类，进行文件的操作
 */
public class FileUtils {

	public static final String DATA_PATH = "experiment_datas\\";
	public static final String RESULT_PATH = "result2\\";

	/**
	 * 读取文件 
	 * 获得巡逻速度
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static double readV(String filename,String path) throws IOException {

		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_PATH+path+filename)));
		String str = data.readLine();
		double v = Double.parseDouble(str);
		return v;
	}
	
	/**
	 * 读取基本的参数
	 * 1.犯罪数
	 * 2.区域数
	 * 3.各个区域的路段数
	 * @param filename
	 * @return
	 * @throws IOException 
	 */
	public static HashMap<String, List<Integer>> readBasic(String filename,String path) throws IOException{
		HashMap<String, List<Integer>> map = new HashMap<>();
		List<Integer> list;
		String strbuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_PATH+path+filename)));
	    
		//1.犯罪数
		strbuff = data.readLine();
		list = new ArrayList<>();
		String strCrimeNum[] = strbuff.split(" = ");
		list.add(Integer.valueOf(strCrimeNum[1]));
		map.put("CrimeNum", list);
		
		//2.区域数
		strbuff = data.readLine();
		list = new ArrayList<>();
		String strRegionNum[] = strbuff.split(" = ");
		list.add(Integer.valueOf(strRegionNum[1]));
		map.put("RegionNum", list);
		
		//3.各个区域的路段数
		strbuff = data.readLine();
		list = new ArrayList<>();
		String str1[] = strbuff.split(" = ");
		String strRoadNum[] = str1[1].split(",");
		for(int i=0;i<strRoadNum.length;i++){
			list.add(Integer.valueOf(strRoadNum[i]));
		}
		map.put("RoadNum", list);
		
		
		return map;
	}
	
	/**
	 * 可以获取三类参数
	 * 1.犯罪权重
	 * 2.犯罪持续时间
	 * 3.犯罪被检测到的概率
	 * @param filename
	 * @param crimeNum
	 * @return
	 * @throws IOException
	 */
	public static double[] readCrimeData(String filename,int crimeNum,String path) throws IOException{
		String strbuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_PATH+path+filename)));
		double result[] = new double[crimeNum];
		strbuff = data.readLine();
		String strdata[] = strbuff.split(" ");
		for(int i=0;i<strdata.length;i++){
			result[i] = Double.valueOf(strdata[i]);
		}
		return result;
	}
	
	/**
	 * 可以获取两类数据
	 * 1.不同区域的路段 交通拥挤程度
	 * 2.不同区域的路段 长度
	 * @param filename
	 * @param regionNum
	 * @param roadNum
	 * @return
	 * @throws IOException 
	 */
	public static double[][] readRoad(String filename,int regionNum,String path) throws IOException{
		String strbuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_PATH+path+filename)));
		double result[][] = new double[regionNum][];
		for(int i=0;i<regionNum;i++){
			strbuff = data.readLine();
			String strReuslt[] = strbuff.split(" ");
			result[i] = new double[strReuslt.length];
			for(int j=0;j<strReuslt.length;j++){
				result[i][j] = Double.valueOf(strReuslt[j]);
			}
		}
		
		return result;
	}
	
	/**
	 * 读取不同时间的交通拥挤程度
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static double[] readTimeEffect(String filename,String path) throws IOException{
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_PATH+path+filename)));
		String strbuff;
		
		strbuff = data.readLine();
		String strResult[] = strbuff.split(" ");
		double result[] = new double[strResult.length];
		for(int i=0;i<strResult.length;i++){
				result[i] = Double.valueOf(strResult[i]);
		}
		return result;
	}
	
	/**
	 * 读取不同犯罪在不同区域不同路段的发生频率
	 * @param filename
	 * @param crimeNum
	 * @param regionNum
	 * @param roadNum
	 * @return
	 * @throws IOException
	 */
	public static double[][][] readF(String filename,int crimeNum,int regionNum,int[] roadNum,String path) throws IOException{
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_PATH+path+filename)));
		String strbuff;
		double result[][][] = new double[crimeNum][regionNum][];
		for(int i=0;i<crimeNum;i++){
			for(int j=0;j<regionNum;j++){
				strbuff = data.readLine();
				String strResult[] = strbuff.split(" ");
				result[i][j] = new double[strResult.length];
				for(int k=0;k<strResult.length;k++){
					
					result[i][j][k] = Double.valueOf(strResult[k]);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 将结果以追加的方式保存到文件
	 * @param result 保存内容
	 * @param type 算法类型 如BBO,GA,PSO,WWO
	 * @param result_name 保存的文件名.txt
	 * @param pathRoot 保存根路径
	 * @param path 保存子路径
	 */
	public static void saveResult(String result,String type,String result_name,String pathRoot,String path){
		try{
			File f = new File(pathRoot+path+type);
			if(!f.exists()){
				f.mkdirs();
			}
			FileWriter fileWriter = new FileWriter(pathRoot+path+type+"\\"+result_name, true);
			fileWriter.write(result);
			fileWriter.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		int a[] = new int[]{3,4,5,7,2,1,9};
		int b[] = a.clone();
		Arrays.sort(b);
		for(int i=0;i<a.length;i++){
			System.out.print(a[i]+" ");
		}
		System.out.println();
		for(int i=0;i<b.length;i++){
			System.out.print(b[i]+" ");
		}
		
	}
}
