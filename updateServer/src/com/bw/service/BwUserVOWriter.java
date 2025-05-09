package com.bw.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.bw.cache.vo.BwUserVO;
import com.bw.dao.BwUserDAO;

/**
 * @author denny zhao
 *日期:2012-12-10
 *更新用户信息
 */
public class BwUserVOWriter {
	private Logger log = Logger.getLogger(BwUserVOWriter.class);
    private BwUserDAO bwUserDaoImpl;
	public ArrayBlockingQueue<Collection<BwUserVO>> clq=new ArrayBlockingQueue<Collection<BwUserVO>>(100000);
	//批量更新的数量
	int beishu=3;
	//线程间隔时间
	private int periodSecond = 10;
	//开始时间
	private int startTime = 10000;
	//间隔线程每次更新的数量
    private int perRunUpdateCount=1000;
    
	public void batchUpdateUserInfor(){
		try {
			log.info("开始批量更新数据库用户信息....");
			long updateCount=0;
			while(!clq.isEmpty()&&updateCount<perRunUpdateCount){
				Collection<BwUserVO>  c=clq.poll();
				long allcount=c.size();
				if(allcount>0){
						//
						List<BwUserVO> listtime1 = new ArrayList<BwUserVO>();
						for(BwUserVO info:c){
							listtime1.add(info);
							updateCount++;
							if(listtime1.size()>=beishu){
								bwUserDaoImpl.batchUpdate(listtime1);
								listtime1.clear();
							}
							
						}
						if(!listtime1.isEmpty()){
							bwUserDaoImpl.batchUpdate(listtime1);
							listtime1.clear();
							listtime1=null;
						}
					}
					c.clear();
					c=null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("后台批量更新用户信息发生错误:",e);
		}
	}
	public void init(){
//		threadPoolExecutor = 
		ScheduledExecutorService  es=	Executors.newSingleThreadScheduledExecutor();
		es.scheduleAtFixedRate(new Runnable(){
			public void run(){
				batchUpdateUserInfor();
			}
			
		},  startTime, periodSecond, TimeUnit.MILLISECONDS);
	}	
	public int getBeishu() {
		return beishu;
	}
	public void setBeishu(int beishu) {
		this.beishu = beishu;
	}
	
	public ArrayBlockingQueue<Collection<BwUserVO>> getClq() {
		return clq;
	}
	public void setClq(ArrayBlockingQueue<Collection<BwUserVO>> clq) {
		this.clq = clq;
	}
	public int getPeriodSecond() {
		return periodSecond;
	}
	public void setPeriodSecond(int periodSecond) {
		this.periodSecond = periodSecond;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	public int getPerRunUpdateCount() {
		return perRunUpdateCount;
	}
	public void setPerRunUpdateCount(int perRunUpdateCount) {
		this.perRunUpdateCount = perRunUpdateCount;
	}
	public BwUserDAO getBwUserDaoImpl() {
		return bwUserDaoImpl;
	}
	public void setBwUserDaoImpl(BwUserDAO bwUserDaoImpl) {
		this.bwUserDaoImpl = bwUserDaoImpl;
	}
      
      
}
