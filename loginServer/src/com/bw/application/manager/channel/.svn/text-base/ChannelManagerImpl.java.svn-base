package com.bw.application.manager.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.bw.application.exception.ManagerServerException;
import com.bw.baseJar.vo.BwAreaVO;
import com.bw.baseJar.vo.BwFileServerVO;
import com.bw.baseJar.vo.BwGameChannleVO;
import com.common.BaseInfor.DAO.DBBaseInforDAO;
import com.commonSocket.net.Service;

/**
 * @author zhaoqingyou
 *
 */
public class ChannelManagerImpl implements ChannelManager, Service {
	private Logger logger = Logger.getLogger(getClass());
	private AtomicBoolean started = new AtomicBoolean(false);

	/** 频道存活任务视图 */

	/** 频道存活状态 */
	private ConcurrentSkipListMap<Integer, Boolean> channelActiveMap;
	
	/** 频道基础信息 */
//	private ConcurrentSkipListMap<Integer, CityServerChannleVO> channelInfoMap;
	//文件服务器信息
	public  List<BwFileServerVO> fileServerMap;
//	public ConcurrentHashMap<Long,> bwHallBuildsRelationVOMap;
	/** 频道服务线程池 */
	private ExecutorService channelService;
	
    private DBBaseInforDAO dbBaseInforDAOImpl;
	//区域游戏服务器列表
	public ConcurrentHashMap<Long, BwAreaVO> bwAreaVOMap;
	/** 心跳间隔（毫秒为单位） */
	private int egisTime;
	/**
	 * 
	 */
	public ChannelManagerImpl() {
		// TODO Auto-generated constructor stub
	}
    
	public void start() {
		if (started.compareAndSet(false, true)) {
			this.channelService = Executors.newCachedThreadPool();
			this.channelActiveMap = new ConcurrentSkipListMap<Integer, Boolean>();
//			this.channelInfoMap = new ConcurrentSkipListMap<Integer, CityServerChannleVO>();
			fileServerMap=new ArrayList<BwFileServerVO>(); 
			bwAreaVOMap=new ConcurrentHashMap<Long, BwAreaVO>();
			this.initAreaList();
				this.registerChannelInfo();
		}
	}

	public void stop() {
		if (started.compareAndSet(true, false)) {
			this.channelActiveMap.clear();
			this.channelActiveMap = null;
			fileServerMap.clear();
			fileServerMap=null;
		}
	}

	public void registerChannelInfo() throws ManagerServerException {
		List<BwGameChannleVO> channleList =dbBaseInforDAOImpl.getGameServerChannleList();
		//此地方需要获取服务器列表
		if (channleList == null) {
			logger.error("此地方需要获取服务器列表");
		}
//		for (CityServerChannleVO cityServerChannleVO : channleList) {
//			this.channelInfoMap.put((int)cityServerChannleVO.getId(), cityServerChannleVO);
//			this.channelActiveMap.put((int)cityServerChannleVO.getId(), false);
//		}
		//重新注册游戏服务器列表
		Collection<BwAreaVO> c=bwAreaVOMap.values();
		Iterator<BwAreaVO> i=c.iterator();
		while(i.hasNext()){
			BwAreaVO t=i.next();
			if(!t.getGameChannleHashMap().isEmpty()){
			  Collection<BwGameChannleVO> cc=	t.getGameChannleHashMap().values();
			  Iterator<BwGameChannleVO> it=cc.iterator();
				while(it.hasNext()){
					BwGameChannleVO csv=it.next();
					this.channelActiveMap.put((int)csv.getId(), false);
				}
			}
			
		}
		
		
		//获取文件服务器列表
		List<BwFileServerVO> fileServerList=dbBaseInforDAOImpl.queryBwFileServerVO();
		if(fileServerList==null||fileServerList.size()==0){
			logger.error("此地方需要获取文件服务器列表");
		}
		for (BwFileServerVO fileServer : fileServerList) {
			this.fileServerMap.add(fileServer);
		}		
		//fileServerMap
	}

	@Override
	public void registerChannelActive(int appId,int currentRole) throws ManagerServerException {
		if (this.channelActiveMap.containsKey(new Integer(appId))) {
			this.resetChannelActive(appId);
//			CityServerChannleVO wGgameChannleVO = this.channelInfoMap.get(new Integer(appId));//重定向服务器地址
//			wGgameChannleVO.setUserCount(currentRole);
		}
	}

	@Override
	public void resetChannelActive(int appId) throws ManagerServerException {
																// =
		this.channelActiveMap.put(new Integer(appId), true);											// 3分05秒
	}
	public ConcurrentSkipListMap<Integer, Boolean> getChannelActiveMap() {
		return channelActiveMap;
	}

	public void setChannelActiveMap(ConcurrentSkipListMap<Integer, Boolean> channelActiveMap) {
		this.channelActiveMap = channelActiveMap;
	}



	public ExecutorService getChannelService() {
		return channelService;
	}

	public void setChannelService(ExecutorService channelService) {
		this.channelService = channelService;
	}



	public int getEgisTime() {
		return egisTime;
	}

	public void setEgisTime(int egisTime) {
		this.egisTime = egisTime;
	}

	public DBBaseInforDAO getDbBaseInforDAOImpl() {
		return dbBaseInforDAOImpl;
	}

	public void setDbBaseInforDAOImpl(DBBaseInforDAO dbBaseInforDAOImpl) {
		this.dbBaseInforDAOImpl = dbBaseInforDAOImpl;
	}

	public List<BwFileServerVO> getFileServerMap() {
		return fileServerMap;
	}

	public void setFileServerMap(List<BwFileServerVO> fileServerMap) {
		this.fileServerMap = fileServerMap;
	}
 public void initAreaList(){
		//初始化区域信息
		List<BwAreaVO> tempAreaVOList=dbBaseInforDAOImpl.queryBwAreaVOList();
		if(null!=tempAreaVOList&&tempAreaVOList.size()>0){
			for(BwAreaVO arvo:tempAreaVOList){
				//根据区域iD获取区域服务器列表
				List<BwGameChannleVO> cityServerChannleList=dbBaseInforDAOImpl.getGameServerChannleList(arvo.getAreaId());
				if(null!=cityServerChannleList&&cityServerChannleList.size()>0){
					ConcurrentHashMap<Long, BwGameChannleVO> gameChannleHashMap=new ConcurrentHashMap<Long, BwGameChannleVO>();
					for(BwGameChannleVO bgcvo:cityServerChannleList){
						gameChannleHashMap.put((long)bgcvo.getId(), bgcvo);
					}
					arvo.setGameChannleHashMap(gameChannleHashMap);
					//cityServerChannleList.clear();
					//cityServerChannleList=null;
				}
				bwAreaVOMap.put(arvo.getAreaId(), arvo);
			}
			//tempAreaVOList.clear();
			//tempAreaVOList=null;
		}
 }

@Override
public ConcurrentHashMap<Long, BwAreaVO> getBwAreaVOMap() {
	return this.bwAreaVOMap;
}

@Override
public boolean setBwAreaVOMap(Long area, BwAreaVO bwAreaVO) {
	// TODO Auto-generated method stub
	return false;
}

}
