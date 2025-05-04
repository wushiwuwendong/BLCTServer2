
package com.bw.application.manager.channel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.bw.application.exception.ManagerServerException;
import com.bw.baseJar.vo.BwAreaVO;
import com.bw.baseJar.vo.BwFileServerVO;
import com.commonSocket.net.exception.OperationFailedException;




public interface ChannelManager {

	/**
	 * 注册频道信息
	 * 
	 * @throws OperationFailedException
	 * @throws Exception
	 */
	void registerChannelInfo() throws ManagerServerException;

	/**
	 * 注册频道状态
	 * 
	 * @param appId
	 * @param address
	 * @param currentRole
	 * @throws OperationFailedException
	 * @throws Exception
	 */
	void registerChannelActive(int appId,int currentRole) throws ManagerServerException;

	/**
	 * 重置频道存活状态
	 * 
	 * @param appId
	 * @throws OperationFailedException
	 * @throws Exception
	 */
	void resetChannelActive(int appId) throws ManagerServerException;

	/**
	 * @return
	 * 获取分区信息和对应的服务器列表
	 */
	ConcurrentHashMap<Long, BwAreaVO> getBwAreaVOMap();
	/**
	 * @param area
	 * @param bwAreaVO
	 * @return
	 * 设置分区和游戏服务器信息
	 */
	public boolean  setBwAreaVOMap(Long area, BwAreaVO bwAreaVO);
	

	ConcurrentSkipListMap<Integer, Boolean> getChannelActiveMap();
	public List<BwFileServerVO> getFileServerMap()throws ManagerServerException;

}
