package com.tom.grabdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class BootStrap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		wirteToFile("result1.csv","\ufeff");
		wirteToFile("result1.csv","医院id,	医院名,省份,城市,	等级,	电话,	地址,	网址,	简介");
		List<String> urls = getHospitalURLs(true);
		
		int index = 0;
		for(String url : urls) {
			//1833
			System.out.println("index = " + index++);
			String hospitalInfo = getHospitalInfo(url);
		    try {
		    	wirteToFile("result1.csv", hospitalInfo);
		    	Thread.sleep(1000 * 3);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		
	}

	private static String getHospitalInfo(String hospitalurl) {
		StringBuilder sb = new StringBuilder();
		try {
			Document doc = Jsoup.connect(hospitalurl).get();
			
			String id = doc.select("input[name=hospitalId]").val();
			sb.append(id).append(",");
			
			String hospitalName = doc.select("#g-breadcrumb span").text();
			sb.append(hospitalName).append(",");
			
			Elements cityAndProvince = doc.select("#g-breadcrumb a");
			String province = cityAndProvince.get(1).text();
			sb.append(province).append(",");
			
			String city = "";
			if(province.indexOf("北京") != -1 || province.indexOf("天津") != -1 ||province.indexOf("上海") != -1 
					||province.indexOf("重庆") != -1 ||province.indexOf("澳门") != -1 || province.indexOf("香港") != -1) {
				city = province;
			} else {
				if( cityAndProvince.size() > 2) {
					city = cityAndProvince.get(2).text();
				} else {
					int index = province.indexOf("市");
					if (index == -1) {
						city = province.substring(0,2);
					} else {
						city = province.substring(0,index);
					}
				}
			}
			
			sb.append(city).append(",");
			
			Elements hospitalInfo = doc.select(".hosp-info h1 em");
			String level = hospitalInfo.size() > 0 ? hospitalInfo.get(0).text() : "";
			sb.append(level).append(",");
			
			Elements morePhones = doc.select(".introduce-tel .more-content");
			String telephone = "";
			if (morePhones.size() > 0) {
				telephone = morePhones.text();
			} else {
				telephone = doc.select(".introduce-tel span").text();
			}
			telephone = telephone.replaceAll("电话：", "");
			sb.append(telephone).append(",");
			
			String address = doc.select(".introduce-ads span").get(0).ownText();
			sb.append(address).append(",");
			
			String webSite = doc.select(".introduce-word").get(0).previousElementSibling().text();
			if(webSite.indexOf("官网：") != -1) {
				webSite = webSite.replaceAll("官网：", "");
			} else {
				webSite = "";
			}
			sb.append(webSite).append(",");
			
			String descUrl = doc.select(".introduce-word a").attr("href");
			if (descUrl != null  && descUrl.trim().length() !=0) {
				String description = getHospitalDescription(descUrl);
				sb.append(description);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			sb.append("\n");
			return sb.toString();
		}
	}
	private static String getHospitalDescription (String hospitalURL) throws Exception {
		Document doc = Jsoup.connect(hospitalURL).get();
		String description = doc.select(".hospital-detail .info").text();
		return description;
	}
	
	private static void wirteToFile(String FileName , String content){
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(FileName, true),"UTF-8");
			osw.write(content);
			osw.flush();
			osw.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static List<String> getHospitalURLs(boolean fromFile) {
		String url = "http://www.guahao.com/hospital/areahospitals?sort=0&q=&pi=all&p=%E5%85%A8%E5%9B%BD&ci=all&c=%E4%B8%8D%E9%99%90&o=all&hl=all&ht=all&hk=&fg=0&ipIsShanghai=false";
		int totalPage = 0;
		List<String> list = new ArrayList<String>();
		if(fromFile){
			try {
				BufferedReader bf = new BufferedReader(new FileReader(new File("HospitalLink.txt")));
				String line;
				while((line = bf.readLine()) != null) {
					list.add(line);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return list;
		}
		try {
			Document doc = Jsoup.connect(url).cookie("_area_", "%7B%22provinceId%22%3A%22all%22%2C%22provinceName%22%3A%22%E5%85%A8%E5%9B%BD%22%2C%22cityId%22%3A%22all%22%2C%22cityName%22%3A%22%E4%B8%8D%E9%99%90%22%7D; path=/").get();
			String totalPageString = doc.select(".other-info .pd label").text();
			System.out.println("totalPageString = " + totalPageString);
			totalPage = Integer.valueOf(totalPageString);
			
			for (int i = 1; i <= totalPage ; i ++) {
				try {
					Document docPage = Jsoup.connect(url + "&pageNo=" + i).cookie("_area_", "%7B%22provinceId%22%3A%22all%22%2C%22provinceName%22%3A%22%E5%85%A8%E5%9B%BD%22%2C%22cityId%22%3A%22all%22%2C%22cityName%22%3A%22%E4%B8%8D%E9%99%90%22%7D; path=/").get();
					Elements hopitals = docPage.select("#J_HospitalList dt a");
					for(Element e : hopitals) {
						String hosiptalLink = e.attr("href");
						hosiptalLink = hosiptalLink.replaceAll("hosptopic", "hospital");
						list.add(hosiptalLink);
						wirteToFile("HospitalLink.txt", hosiptalLink + "\r\n");
					}
					System.out.print( i + ",");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Thread.sleep(1000*3);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("get hostpital link complele");
		return list;
	}
}
