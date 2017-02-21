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
 * @date 2016��7��22�� ����4:28:51
 * @version 1.0
 * @description �ļ��࣬�����ļ��Ĳ���
 */
public class FileUtils {

	public static final String DATA_PATH = "experiment_datas\\";
	public static final String RESULT_PATH = "result2\\";

	/**
	 * ��ȡ�ļ� 
	 * ���Ѳ���ٶ�
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
	 * ��ȡ�����Ĳ���
	 * 1.������
	 * 2.������
	 * 3.���������·����
	 * @param filename
	 * @return
	 * @throws IOException 
	 */
	public static HashMap<String, List<Integer>> readBasic(String filename,String path) throws IOException{
		HashMap<String, List<Integer>> map = new HashMap<>();
		List<Integer> list;
		String strbuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_PATH+path+filename)));
	    
		//1.������
		strbuff = data.readLine();
		list = new ArrayList<>();
		String strCrimeNum[] = strbuff.split(" = ");
		list.add(Integer.valueOf(strCrimeNum[1]));
		map.put("CrimeNum", list);
		
		//2.������
		strbuff = data.readLine();
		list = new ArrayList<>();
		String strRegionNum[] = strbuff.split(" = ");
		list.add(Integer.valueOf(strRegionNum[1]));
		map.put("RegionNum", list);
		
		//3.���������·����
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
	 * ���Ի�ȡ�������
	 * 1.����Ȩ��
	 * 2.�������ʱ��
	 * 3.���ﱻ��⵽�ĸ���
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
	 * ���Ի�ȡ��������
	 * 1.��ͬ�����·�� ��ͨӵ���̶�
	 * 2.��ͬ�����·�� ����
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
	 * ��ȡ��ͬʱ��Ľ�ͨӵ���̶�
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
	 * ��ȡ��ͬ�����ڲ�ͬ����ͬ·�εķ���Ƶ��
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
	 * �������׷�ӵķ�ʽ���浽�ļ�
	 * @param result ��������
	 * @param type �㷨���� ��BBO,GA,PSO,WWO
	 * @param result_name ������ļ���.txt
	 * @param pathRoot �����·��
	 * @param path ������·��
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
