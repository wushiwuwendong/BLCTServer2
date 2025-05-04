package com.bw.application.action;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.bw.application.manager.channel.ChannelManager;
import com.bw.application.message.AreaList.AreaListRequest;
import com.bw.application.message.AreaList.AreaListResponse;
import com.bw.baseJar.vo.BwAreaVO;
import com.commonSocket.net.action.Action;
import com.commonSocket.net.action.Request;
import com.commonSocket.net.action.Response;

/**
 * @author denny zhao
 *
 */
public class AreaListAction implements Action {
	private ChannelManager channelManager;
	@Override
	public String execute(Request paramRequest, Response paramResponse)
			throws Exception {
		AreaListRequest request=AreaListRequest.parseFrom(paramRequest.getMessage());
		AreaListResponse.Builder builder=AreaListResponse.newBuilder();
		try {
			 ConcurrentHashMap<Long, BwAreaVO> bwAreaVOMap= channelManager.getBwAreaVOMap();
			    //默认下发所有的分区信息
			 Collection<BwAreaVO> c=bwAreaVOMap.values();
			 Iterator<BwAreaVO> i=c.iterator();
			 while(i.hasNext()){
				 AreaListResponse.AreaData.Builder ad=AreaListResponse.AreaData.newBuilder();
				 BwAreaVO bv=i.next();
				 ad.setAreaId((int)bv.getAreaId());
				 ad.setAreaName(bv.getAreaName());
				 builder.addAreaDataList(ad.build());
			 }
			builder.setResult(0);
		} catch (Exception e) {
			e.printStackTrace();
			builder.setResult(1);
			builder.setInfo(e.getMessage());
		} finally {
			
			paramResponse.write(builder.build());
		}
		return null;
	}
	public ChannelManager getChannelManager() {
		return channelManager;
	}
	public void setChannelManager(ChannelManager channelManager) {
		this.channelManager = channelManager;
	}
	

}
