package com.bw.application.action;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.bw.application.config.AppConfig;
import com.bw.application.manager.channel.ChannelManager;
import com.bw.application.manager.user.IUserManager;
import com.bw.application.message.UserLoginInfo.UserLoginRequest;
import com.bw.application.message.UserLoginInfo.UserLoginResponse;
import com.bw.application.utils.CommonMethod;
import com.bw.baseJar.errorCode.ErrorCodeInterface;
import com.bw.baseJar.vo.BwAreaVO;
import com.bw.baseJar.vo.BwGameChannleVO;
import com.bw.cache.vo.BwBlacklistVO;
import com.bw.cache.vo.BwPlantUserVO;
import com.bw.dao.BwBlacklistDAO;
import com.bw.dao.CommonDAO;
import com.commonSocket.net.action.Action;
import com.commonSocket.net.action.Request;
import com.commonSocket.net.action.Response;

public class UserLoginAction implements Action {
	private Logger logger = Logger.getLogger(getClass());
	private ChannelManager channelManager;
	private AppConfig appConfig;
	private IUserManager userManager;
	private CommonDAO commonCacheDAOImpl;
	private BwBlacklistDAO bwBlacklistCacheDAOImpl;

	@Override
	public String execute(Request request, Response paramResponse)
			throws Exception {
		UserLoginRequest reqMsg = UserLoginRequest.parseFrom(request.getMessage());
		UserLoginResponse.Builder builder = UserLoginResponse.newBuilder();
		String mailAddress = reqMsg.getMailAddress();
		String machineNum = reqMsg.getMachineNum();
		logger.info("用户登录的账户:" + mailAddress);
		BwPlantUserVO puvo=new BwPlantUserVO();
		try {
			
			//要登录的游戏服务器实例号
			int instance_id = reqMsg.getInstanceId();
			//大区ID
			int area_id=reqMsg.getAreaId();
			//第三方登陆ID
			String loginId=reqMsg.getLoginId();
			//第三方类型1 新浪
			int thirdPartyType=reqMsg.getThirdPartyType();
			//登陆类型 0默认登陆 1切换账号登陆
			int login_type=reqMsg.getLoginType();
			String password=reqMsg.getPassword();
			
			//增加停服维护
            if(commonCacheDAOImpl.getStopServerMaintain()==ErrorCodeInterface.SERVER_MAINTENACE){
				builder.setInfo("server maintain !");
				builder.setResult(ErrorCodeInterface.SERVER_MAINTENACE);
				paramResponse.write(builder.build());
				return null;           	
            }
			if(machineNum==null||machineNum.equalsIgnoreCase("")){
				builder.setUpdateAble(0);
				builder.setInfo("mac address is not null!");
				builder.setResult(ErrorCodeInterface.EXIST_NOT_FOR_USER);
				paramResponse.write(builder.build());
				return null;
			}else if ((null == mailAddress || mailAddress.equalsIgnoreCase(""))&&(null == loginId || loginId.equalsIgnoreCase(""))){
				// 默认初始化用户
				//BwUserVO uservo = new BwUserVO();
				
				puvo.setMailaddress(machineNum);
				puvo.setMacaddress(machineNum);
				//uservo.setMailaddress(machineNum);
				puvo.setNickname("player");
				puvo.setPassword("");
				puvo.setPlatformtype(0);
				puvo.setAreaId(area_id);
				
				userManager.initPlantUserInforSender(puvo);
				builder.setTempMailAddress(puvo.getBoweiid());
				//uservo=null;
			  //第三方登录,如果用户不存在则初始化用户数据
			}else if(login_type==0&&thirdPartyType!=0&&null!=loginId&&!"".equalsIgnoreCase(loginId)){
				puvo.setBoweiid(loginId);
				puvo.setAreaId(area_id);
				puvo.setPlatformtype(thirdPartyType);
				puvo.setNickname("player");
				//过滤封掉的用户
				if(denyLogin(loginId)){
					builder.setResult(ErrorCodeInterface.DENY_LOGIN);
					builder.setInfo("拒绝登陆");
					paramResponse.write(builder.build());						
					return null;
				}
				userManager.initAndLoginForThirdParty(puvo);
			}else{//本平台用户登陆  需要根据用户名和密码来获取用户信息
				puvo.setMailaddress(mailAddress);
				puvo.setPassword(password);
				puvo.setAreaId(area_id);

				int result = userManager.isExistForUser(puvo);
				if (result==ErrorCodeInterface.USER_NAME_OR_PASSWORD_NO_RIGHT) {
					builder.setUpdateAble(0);
					builder.setInfo("用户名或者密码不对");
					builder.setResult(result);
					paramResponse.write(builder.build());
					return null;
				}else{
					//过滤封掉的用户
					if(denyLogin(puvo.getBoweiid())){
						builder.setResult(ErrorCodeInterface.DENY_LOGIN);
						builder.setInfo("拒绝登陆");
						paramResponse.write(builder.build());						
						return null;
					}
					//查询用户和宝石信息
//					userManager.queryUserInforAndGem(puvo);
					builder.setUpdateAble(0);
					builder.setInfo("成功");
					builder.setResult(ErrorCodeInterface.SUCESS);
					builder.setTempMailAddress(puvo.getBoweiid());
					//paramResponse.write(builder.build());				
				}
				
			}
			selectServerAndArea(builder,instance_id,area_id);

		} catch (Exception e) {
			e.printStackTrace();
			builder.setResult(1);
			builder.setInfo(e.getMessage());
			logger.error(e);
			System.out.println("登录失败!");
		} finally {
			puvo=null;
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

	public AppConfig getAppConfig() {
		return appConfig;
	}

	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}
 public void selectServerAndArea(UserLoginResponse.Builder builder,int instance_id,int areaId){
	 ConcurrentHashMap<Long, BwAreaVO> bwAreaVOMap= channelManager.getBwAreaVOMap();
	    //默认下发所有的分区信息
	 Collection<BwAreaVO> c=bwAreaVOMap.values();
	 Iterator<BwAreaVO> i=c.iterator();
	 while(i.hasNext()){
		 UserLoginResponse.AreaData.Builder ad=UserLoginResponse.AreaData.newBuilder();
		 BwAreaVO bv=i.next();
		 ad.setAreaId((int)bv.getAreaId());
		 ad.setAreaName(bv.getAreaName());
		 builder.addAreaDataList(ad.build());
	 }
	
	    //分配一个区 
	    if(areaId==0){//没有选择分区 默认选最后一个
	     int areaCount=	bwAreaVOMap.size();
	     areaId=areaCount;
	     builder.setAreaId(areaId);
	     builder.setAreaName(bwAreaVOMap.get((long)areaId).getAreaName());
	    }
		// 一个游戏服务器
	    //获取区下面的游戏服务器列表
	    ConcurrentHashMap<Long,BwGameChannleVO> gameChannleHashMap=bwAreaVOMap.get((long)areaId).getGameChannleHashMap();
		if (null != channelManager
				&& channelManager.getChannelActiveMap().size() > 0) {
			// 判断是否有可用的游戏服务器
			if (channelManager.getChannelActiveMap().size() >= 1) {
				
				if (instance_id > 0&&channelManager.getChannelActiveMap().containsKey(new Integer(instance_id))&&gameChannleHashMap.containsKey(
								(long)instance_id)) {
					////指定的游戏服务器 下架或者停服维护中
					BwGameChannleVO cchvo =gameChannleHashMap.get((long)(instance_id));
					if(cchvo.getStatus()==2){//停服维护中,清稍后重试
						builder.setGameServerAddress(cchvo.getAddress());
						builder.setResult(ErrorCodeInterface.SERVER_MAINTENACE_PART);
					}else{
						builder.setGameServerAddress(cchvo.getAddress());
						builder.setResult(ErrorCodeInterface.SUCESS);
					}
					
				} else if(instance_id == 0){//没有指定游戏服务器
					BwGameChannleVO cchvo = gameChannleHashMap.get(gameChannleHashMap.size()-1);
					builder.setGameServerAddress(cchvo.getAddress());
					builder.setResult(ErrorCodeInterface.SUCESS);
				}
				builder.setFileServerAddress(channelManager.getFileServerMap().get(0).getRul());
				builder.setVerifyCode(CommonMethod.creatVerifyCode());
				builder.setUpdateAble(0);
				System.out.println("登录成功");
				builder.setInfo("用户成功登陆");

//				builder.setBankServer("192.168.0.66:7775");
				builder.setBankServer(appConfig.getBankServerAddress());
			}
		}
 }
public IUserManager getUserManager() {
	return userManager;
}

public void setUserManager(IUserManager userManager) {
	this.userManager = userManager;
}

public CommonDAO getCommonCacheDAOImpl() {
	return commonCacheDAOImpl;
}

public void setCommonCacheDAOImpl(CommonDAO commonCacheDAOImpl) {
	this.commonCacheDAOImpl = commonCacheDAOImpl;
}
//登录过滤的功能
public boolean denyLogin(String boweiId){
	List<BwBlacklistVO> boweiIdList=bwBlacklistCacheDAOImpl.selectAll();
	if(null!=boweiIdList&&boweiIdList.size()>0){
		for(BwBlacklistVO boweiIdDeny:boweiIdList){
			java.util.Date d=new java.util.Date();
			if(boweiIdDeny.getBoweiId().equalsIgnoreCase(boweiId)&&d.after(boweiIdDeny.getStartTime())&&d.before(boweiIdDeny.getEndTime())){
				return true;
			}
		}
	}
	return false;
}

public BwBlacklistDAO getBwBlacklistCacheDAOImpl() {
	return bwBlacklistCacheDAOImpl;
}

public void setBwBlacklistCacheDAOImpl(BwBlacklistDAO bwBlacklistCacheDAOImpl) {
	this.bwBlacklistCacheDAOImpl = bwBlacklistCacheDAOImpl;
}

}
