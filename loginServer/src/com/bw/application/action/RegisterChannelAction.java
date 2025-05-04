package com.bw.application.action;

import org.apache.log4j.Logger;

import com.bw.application.manager.channel.ChannelManager;
import com.bw.application.message.RegisterChannel.RegisterChannelRequest;
import com.commonSocket.net.action.Action;
import com.commonSocket.net.action.Request;
import com.commonSocket.net.action.Response;


public class RegisterChannelAction implements Action {
	private Logger logger = Logger.getLogger(getClass());
	private ChannelManager channelManager;


	public ChannelManager getChannelManager() {
		return channelManager;
	}

	public void setChannelManager(ChannelManager channelManager) {
		this.channelManager = channelManager;
	}

	@Override
	public String execute(Request paramRequest, Response paramResponse)
			throws Exception {
		
		RegisterChannelRequest reqMsg = RegisterChannelRequest.parseFrom(paramRequest.getMessage());
		channelManager.registerChannelActive(reqMsg.getAppId(), reqMsg.getRoleCount());
		
		
		
		logger.debug(reqMsg);
		return null;
	}

}
