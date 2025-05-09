package com.bw.application.jmsUpdate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.bw.cache.vo.BwUserCharacterVO;
import com.bw.jms.sender.SenderUtil;

/**
 * @author denny zhao
 *日期:2012-12-10
 *更新用户兵力信息
 */
public class BwUserCharacterVOUpdateJMS {
	private Logger logger = Logger.getLogger(BwUserCharacterVOUpdateJMS.class);
	private Map<String, BwUserCharacterVO> jmsdataMap = new ConcurrentHashMap<String, BwUserCharacterVO>();
	public SenderUtil senderUtil;
	private long period;
	public SenderUtil getSenderUtil() {
		return senderUtil;
	}
	public void setSenderUtil(SenderUtil senderUtil) {
		this.senderUtil = senderUtil;
	}
	public long getPeriod() {
		return period;
	}
	public void setPeriod(long period) {
		this.period = period;
	}
	private void doSend(){
		for(String k :jmsdataMap.keySet()){
			BwUserCharacterVO f=jmsdataMap.remove(k);
			if(f!=null){
				senderUtil.sendBwUserCharacterVO(f);
				logger.info("发送用户兵力信息成功");				
			}

		}
	}
	public void addBwUserCharacterVO(BwUserCharacterVO bwUserCharacterVO ){
		String key=bwUserCharacterVO.getMailaddress()+"_"+bwUserCharacterVO.getChararchterid();
		jmsdataMap.put(key, bwUserCharacterVO);
		logger.info("成功放入map");
	};
	/**
	 *  自动发送用户地图信息到jms服务器
	 */
	public void init(){
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				doSend();
			}
		}, 10, period, TimeUnit.SECONDS);
	}
	public Map<String, BwUserCharacterVO> getJmsdataMap() {
		return jmsdataMap;
	}
	public void setJmsdataMap(Map<String, BwUserCharacterVO> jmsdataMap) {
		this.jmsdataMap = jmsdataMap;
	}
	
}
