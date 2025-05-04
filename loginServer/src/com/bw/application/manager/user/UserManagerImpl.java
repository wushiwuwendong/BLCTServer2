package com.bw.application.manager.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.mina.core.session.IoSession;

import com.bw.application.client.FindUUIDClient;
import com.bw.application.exception.ManagerServerException;
import com.bw.application.manager.channel.ChannelManager;
import com.bw.application.message.UserLoginInfo.UserLoginResponse;
import com.bw.baseJar.common.CommonGameData;
import com.bw.baseJar.errorCode.ErrorCodeInterface;
import com.bw.baseJar.vo.BwBuildingPropertiesLevelVO;
import com.bw.baseJar.vo.BwInitUserVO;
import com.bw.baseJar.vo.StopServerMessageVO;
import com.bw.cache.vo.BwMineCollectorAllVO;
import com.bw.cache.vo.BwMineCollectorVO;
import com.bw.cache.vo.BwPlantUserVO;
import com.bw.cache.vo.BwUserBankVO;
import com.bw.cache.vo.BwUserBattleStatisticsVO;
import com.bw.cache.vo.BwUserMapDataVO;
import com.bw.cache.vo.BwUserVO;
import com.bw.dao.BwMineCollectorAllDAO;
import com.bw.dao.BwMineCollectorDAO;
import com.bw.dao.BwPlantUserDAO;
import com.bw.dao.BwUserBankDAO;
import com.bw.dao.BwUserBattleStatisticsDAO;
import com.bw.dao.BwUserDAO;
import com.bw.dao.BwUserMapDataDAO;
import com.bw.exception.CacheDaoException;
import com.bw.resourceManager.ResGlobal;
import com.commonSocket.net.Application;
import com.commonSocket.net.action.Response;
import com.commonSocket.net.action.support.DefaultResponse;

public class UserManagerImpl implements IUserManager {
	public BwUserDAO bwUserCacheDAOImpl;
	public DateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	public BwUserMapDataDAO bwUserMapDataCacheDAOImpl;
	public BwMineCollectorDAO bwMineCollectorCacheDAOImpl;
	public BwMineCollectorAllDAO bwMineCollectorAllCacheDAOImpl;
	//初始化战斗统计信息
	public BwUserBattleStatisticsDAO bwUserBattleStatisticsDaoImpl;
	//平台用户
	public BwPlantUserDAO bwPlantUserCacheDAOImpl;
	public FindUUIDClient findUUIDClient;
	public BwUserBankDAO bwUserBankCacheDAOImpl;
	private ArrayBlockingQueue<Long>  arrayBQ=new ArrayBlockingQueue<Long>(5000);
	@Override
	public int isExistForUser(BwPlantUserVO bwPlantUserVO) throws ManagerServerException {
		String bowei_id=bwPlantUserCacheDAOImpl.queryBwPlantUserVOByMailAddress(bwPlantUserVO);
		if(bowei_id!=null){//
			bwPlantUserVO.setBoweiid(bowei_id);
			BwPlantUserVO t=bwPlantUserCacheDAOImpl.queryBwPlantUserVOById(bwPlantUserVO);
			if((t.getPassword()==null&&bwPlantUserVO.getPassword()==null)){
				return ErrorCodeInterface.SUCESS;
			}else if(!t.getPassword().equals(bwPlantUserVO.getPassword())){
				return  	ErrorCodeInterface.USER_NAME_OR_PASSWORD_NO_RIGHT;
			}
		}else{
		 return  	ErrorCodeInterface.USER_NAME_OR_PASSWORD_NO_RIGHT;
		}
		return ErrorCodeInterface.SUCESS;
		
//		BwUserVO uservo=new BwUserVO();
//  		uservo.setMailaddress(bwPlantUserVO.getMailaddress());
//  		uservo.setAreaId(bwPlantUserVO.getAreaId());
//		BwUserVO uservoTemp =bwUserCacheDAOImpl.queryBwUserVOById(uservo);
//		if (null == uservoTemp) {
//			return false;
//		} else {
//			return true;
//		}
	}

	@Override
	public BwUserVO getUserVOByMailAddress(BwUserVO uservo) throws ManagerServerException {
		return bwUserCacheDAOImpl.queryBwUserVOById(uservo);
	}

	/**
	 * 用户注册
	 */
	public int UserRegister(BwPlantUserVO bwPlantUserVO) throws ManagerServerException {
            //先查找在更新
		BwPlantUserVO t =bwPlantUserCacheDAOImpl.queryBwPlantUserVOById(bwPlantUserVO);
		if(null!=t){
			t.setMailaddress(bwPlantUserVO.getMailaddress());
			t.setNickname(bwPlantUserVO.getNickname());
			t.setPassword(bwPlantUserVO.getPassword());
			bwPlantUserCacheDAOImpl.update(t);
			t=null;
			return ErrorCodeInterface.SUCESS;
		}
    return ErrorCodeInterface.ERROR;

	}

	public BwUserDAO getBwUserCacheDAOImpl() {
		return bwUserCacheDAOImpl;
	}

	public void setBwUserCacheDAOImpl(BwUserDAO bwUserCacheDAOImpl) {
		this.bwUserCacheDAOImpl = bwUserCacheDAOImpl;
	}

	/**
	 * @param uservo
	 * @throws ManagerServerExceptio
	 * 初始化用户信息 包括 用户、大厅,数目,石头的基本信息
	 */
	@Override
	public void initUserInfor(BwUserVO uservo) throws ManagerServerException {
		try {
			int maxGolden=0;
			int maxElixir=0;
			BwInitUserVO bwInitUserVO=ResGlobal.getInstance().bwInitUserVO;
			uservo.setGoldencount(bwInitUserVO.getGoldenCount());
			uservo.setElixircount(bwInitUserVO.getElixirCount());
			uservo.setExp(bwInitUserVO.getExp());
			//上线以后这个地方要去掉
			BwUserBankVO userBankVO=new BwUserBankVO();
			userBankVO.setGemtotalcount(bwInitUserVO.getGemCount());
			userBankVO.setLastupdatetime(sdf.format(new java.util.Date()));
			userBankVO.setMailaddress(uservo.getMacAddress());
			userBankVO.setBoweiId(uservo.getMailaddress());
			userBankVO.setPayTotalMoney(0);
			bwUserBankCacheDAOImpl.save(userBankVO);
			uservo.setLastlogintime(sdf.format(new java.util.Date()));
			uservo.setLevel(1);
			uservo.setNickname(uservo.getMailaddress());
			uservo.setPvpmark(0);
			uservo.setUserBankVO(userBankVO);
			//保存完用户信息，还需要保存其它的比如: 大厅位置,数目,石头,基本信息
			//大厅和树石头的uuid在1~200之间
			BwUserMapDataVO bwusermapdatavoTemp=new BwUserMapDataVO();
			bwusermapdatavoTemp.setMailaddress(uservo.getMailaddress());
			bwusermapdatavoTemp.setMapindexx(20);
			bwusermapdatavoTemp.setMapindexy(20);
			bwusermapdatavoTemp.setStatus(1);
			bwusermapdatavoTemp.setUniquenessbuildid(CommonGameData.TOWN_HALL_UUID_COMMON_GAME_DATA);
			bwusermapdatavoTemp.setBuildid(CommonGameData.TOWN_HALL_ID_COMMON_GAME_DATA);
			bwusermapdatavoTemp.setBuildlevel(1);
			bwusermapdatavoTemp.setUpgradefinishtime("");
			String buildingPropertiesKey=CommonGameData.TOWN_HALL_ID_COMMON_GAME_DATA+"_"+1;
			BwBuildingPropertiesLevelVO 	bbplvo=ResGlobal.getInstance().bwBuildingPropertiesLevelVOMap.get(buildingPropertiesKey);
			maxGolden+=bbplvo.getMaxstoredgold();
			maxElixir+=bbplvo.getMaxstoredelixir();
			//保存大厅信息
			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoTemp);
//			bwminecollectorallvo=new 
			BwMineCollectorAllVO bwminecollectorallvo=new BwMineCollectorAllVO();
			bwminecollectorallvo.setCollectcount(1000);
			bwminecollectorallvo.setSecondElixirCount(1000);
			bwminecollectorallvo.setMailAddress(uservo.getMailaddress());
			bwminecollectorallvo.setUserbuildingdataid(bwusermapdatavoTemp.getId());
			bwMineCollectorAllCacheDAOImpl.save(bwminecollectorallvo);
			//一个 金矿,一个营地,一个农民屋
			
			//一个金矿
			BwUserMapDataVO bwusermapdatavoGolden=new BwUserMapDataVO();
			bwusermapdatavoGolden.setMailaddress(uservo.getMailaddress());
			bwusermapdatavoGolden.setMapindexx(30);
			bwusermapdatavoGolden.setMapindexy(30);
			bwusermapdatavoGolden.setStatus(1);
			bwusermapdatavoGolden.setUniquenessbuildid(2);
			bwusermapdatavoGolden.setBuildid(202);
			bwusermapdatavoGolden.setBuildlevel(1);
			bwusermapdatavoGolden.setUpgradefinishtime("");
			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoGolden);
			BwMineCollectorVO bwminecollectorvo=new BwMineCollectorVO();
			bwminecollectorvo.setHarveststarttime(sdf.format(new Date()));
			bwminecollectorvo.setUserbuildingdataid(bwusermapdatavoGolden.getId());
			bwMineCollectorCacheDAOImpl.save(bwminecollectorvo);

//			 buildingPropertiesKey=202+"_"+1;
//			 	bbplvo=ResGlobal.getInstance().bwBuildingPropertiesLevelVOMap.get(buildingPropertiesKey);
//			maxGolden+=bbplvo.getMaxstoredgold();
//			maxElixir+=bbplvo.getMaxstoredelixir();
			uservo.setMaxGoldenCount(maxGolden);
			uservo.setMaxElixirCount(maxElixir);
			//一个营地
			BwUserMapDataVO bwusermapdatavoTroop=new BwUserMapDataVO();
			bwusermapdatavoTroop.setMailaddress(uservo.getMailaddress());
			bwusermapdatavoTroop.setMapindexx(15);
			bwusermapdatavoTroop.setMapindexy(15);
			bwusermapdatavoTroop.setStatus(1);
			bwusermapdatavoTroop.setUniquenessbuildid(3);
			bwusermapdatavoTroop.setBuildid(1);
			bwusermapdatavoTroop.setBuildlevel(1);
			bwusermapdatavoTroop.setUpgradefinishtime("");
			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoTroop);		
			//一个农民屋
			BwUserMapDataVO bwusermapdatavoWorker=new BwUserMapDataVO();
			bwusermapdatavoWorker.setMailaddress(uservo.getMailaddress());
			bwusermapdatavoWorker.setMapindexx(25);
			bwusermapdatavoWorker.setMapindexy(25);
			bwusermapdatavoWorker.setStatus(1);
			bwusermapdatavoWorker.setUniquenessbuildid(4);
			bwusermapdatavoWorker.setBuildid(500);
			bwusermapdatavoWorker.setBuildlevel(1);
			bwusermapdatavoWorker.setUpgradefinishtime("");
			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorker);
			//保存用户信息
			uservo.setWorkCount(1);
			bwUserCacheDAOImpl.save(uservo);
//			//批量生成墙信息 开始
//			int uuid=202;
//			for(int x=16;x>=10;x--){
//				BwUserMapDataVO bwusermapdatavoWorkerWall=new BwUserMapDataVO();
//				bwusermapdatavoWorkerWall.setMailaddress(uservo.getMailaddress());
//				bwusermapdatavoWorkerWall.setMapindexx(x);
//				bwusermapdatavoWorkerWall.setMapindexy(10);
//				bwusermapdatavoWorkerWall.setStatus(1);
//				bwusermapdatavoWorkerWall.setUniquenessbuildid(uuid);
//				bwusermapdatavoWorkerWall.setBuildid(400);
//				bwusermapdatavoWorkerWall.setBuildlevel(1);
//				bwusermapdatavoWorkerWall.setUpgradefinishtime("");
//				bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall);					
//				uuid++;
//			}
//			uuid=209;
//            for(int y=11;y<=17;y++){
//            	BwUserMapDataVO bwusermapdatavoWorkerWall2=new BwUserMapDataVO();
//    			bwusermapdatavoWorkerWall2.setMailaddress(uservo.getMailaddress());
//    			bwusermapdatavoWorkerWall2.setMapindexx(10);
//    			bwusermapdatavoWorkerWall2.setMapindexy(y);
//    			bwusermapdatavoWorkerWall2.setStatus(1);
//    			bwusermapdatavoWorkerWall2.setUniquenessbuildid(uuid);
//    			bwusermapdatavoWorkerWall2.setBuildid(400);
//    			bwusermapdatavoWorkerWall2.setBuildlevel(1);
//    			bwusermapdatavoWorkerWall2.setUpgradefinishtime("");
//    			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall2);	
//    			uuid++;
//            }
//			
//			
//			
//			BwUserMapDataVO bwusermapdatavoWorkerWall12=new BwUserMapDataVO();
//			bwusermapdatavoWorkerWall12.setMailaddress(uservo.getMailaddress());
//			bwusermapdatavoWorkerWall12.setMapindexx(16);
//			bwusermapdatavoWorkerWall12.setMapindexy(12);
//			bwusermapdatavoWorkerWall12.setStatus(1);
//			bwusermapdatavoWorkerWall12.setUniquenessbuildid(216);
//			bwusermapdatavoWorkerWall12.setBuildid(400);
//			bwusermapdatavoWorkerWall12.setBuildlevel(1);
//			bwusermapdatavoWorkerWall12.setUpgradefinishtime("");
//			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall12);		
//			
//			BwUserMapDataVO bwusermapdatavoWorkerWall1=new BwUserMapDataVO();
//			bwusermapdatavoWorkerWall1.setMailaddress(uservo.getMailaddress());
//			bwusermapdatavoWorkerWall1.setMapindexx(16);
//			bwusermapdatavoWorkerWall1.setMapindexy(11);
//			bwusermapdatavoWorkerWall1.setStatus(1);
//			bwusermapdatavoWorkerWall1.setUniquenessbuildid(217);
//			bwusermapdatavoWorkerWall1.setBuildid(400);
//			bwusermapdatavoWorkerWall1.setBuildlevel(1);
//			bwusermapdatavoWorkerWall1.setUpgradefinishtime("");
//			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall1);	
//			uuid=219;
//			for(int x=11;x<=13;x++){
//            	BwUserMapDataVO bwusermapdatavoWorkerWall2=new BwUserMapDataVO();
//    			bwusermapdatavoWorkerWall2.setMailaddress(uservo.getMailaddress());
//    			bwusermapdatavoWorkerWall2.setMapindexx(x);
//    			bwusermapdatavoWorkerWall2.setMapindexy(17);
//    			bwusermapdatavoWorkerWall2.setStatus(1);
//    			bwusermapdatavoWorkerWall2.setUniquenessbuildid(uuid);
//    			bwusermapdatavoWorkerWall2.setBuildid(400);
//    			bwusermapdatavoWorkerWall2.setBuildlevel(1);
//    			bwusermapdatavoWorkerWall2.setUpgradefinishtime("");
//    			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall2);	
//    			uuid++;			
//			}
//			uuid=222;
//			for(int y=18;y<=21;y++){
//            	BwUserMapDataVO bwusermapdatavoWorkerWall2=new BwUserMapDataVO();
//    			bwusermapdatavoWorkerWall2.setMailaddress(uservo.getMailaddress());
//    			bwusermapdatavoWorkerWall2.setMapindexx(13);
//    			bwusermapdatavoWorkerWall2.setMapindexy(y);
//    			bwusermapdatavoWorkerWall2.setStatus(1);
//    			bwusermapdatavoWorkerWall2.setUniquenessbuildid(uuid);
//    			bwusermapdatavoWorkerWall2.setBuildid(400);
//    			bwusermapdatavoWorkerWall2.setBuildlevel(1);
//    			bwusermapdatavoWorkerWall2.setUpgradefinishtime("");
//    			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall2);	
//    			uuid++;					
//			}
//			uuid=226;
//			for(int x=14;x<=21;x++){
//            	BwUserMapDataVO bwusermapdatavoWorkerWall2=new BwUserMapDataVO();
//    			bwusermapdatavoWorkerWall2.setMailaddress(uservo.getMailaddress());
//    			bwusermapdatavoWorkerWall2.setMapindexx(x);
//    			bwusermapdatavoWorkerWall2.setMapindexy(21);
//    			bwusermapdatavoWorkerWall2.setStatus(1);
//    			bwusermapdatavoWorkerWall2.setUniquenessbuildid(uuid);
//    			bwusermapdatavoWorkerWall2.setBuildid(400);
//    			bwusermapdatavoWorkerWall2.setBuildlevel(1);
//    			bwusermapdatavoWorkerWall2.setUpgradefinishtime("");
//    			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall2);	
//    			uuid++;					
//			}
//			uuid=234;
//			for(int y=20;y>=14;y--){
//            	BwUserMapDataVO bwusermapdatavoWorkerWall2=new BwUserMapDataVO();
//    			bwusermapdatavoWorkerWall2.setMailaddress(uservo.getMailaddress());
//    			bwusermapdatavoWorkerWall2.setMapindexx(21);
//    			bwusermapdatavoWorkerWall2.setMapindexy(y);
//    			bwusermapdatavoWorkerWall2.setStatus(1);
//    			bwusermapdatavoWorkerWall2.setUniquenessbuildid(uuid);
//    			bwusermapdatavoWorkerWall2.setBuildid(400);
//    			bwusermapdatavoWorkerWall2.setBuildlevel(1);
//    			bwusermapdatavoWorkerWall2.setUpgradefinishtime("");
//    			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall2);	
//    			uuid++;					
//			}
//			uuid=241;
//			for(int y=12;y<=14;y++){
//            	BwUserMapDataVO bwusermapdatavoWorkerWall2=new BwUserMapDataVO();
//    			bwusermapdatavoWorkerWall2.setMailaddress(uservo.getMailaddress());
//    			bwusermapdatavoWorkerWall2.setMapindexx(17);
//    			bwusermapdatavoWorkerWall2.setMapindexy(y);
//    			bwusermapdatavoWorkerWall2.setStatus(1);
//    			bwusermapdatavoWorkerWall2.setUniquenessbuildid(uuid);
//    			bwusermapdatavoWorkerWall2.setBuildid(400);
//    			bwusermapdatavoWorkerWall2.setBuildlevel(1);
//    			bwusermapdatavoWorkerWall2.setUpgradefinishtime("");
//    			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall2);	
//    			uuid++;					
//			}
//			uuid=244;
//			for(int x=18;x<=20;x++){
//            	BwUserMapDataVO bwusermapdatavoWorkerWall2=new BwUserMapDataVO();
//    			bwusermapdatavoWorkerWall2.setMailaddress(uservo.getMailaddress());
//    			bwusermapdatavoWorkerWall2.setMapindexx(x);
//    			bwusermapdatavoWorkerWall2.setMapindexy(14);
//    			bwusermapdatavoWorkerWall2.setStatus(1);
//    			bwusermapdatavoWorkerWall2.setUniquenessbuildid(uuid);
//    			bwusermapdatavoWorkerWall2.setBuildid(400);
//    			bwusermapdatavoWorkerWall2.setBuildlevel(1);
//    			bwusermapdatavoWorkerWall2.setUpgradefinishtime("");
//    			bwUserMapDataCacheDAOImpl.save(bwusermapdatavoWorkerWall2);	
//    			uuid++;					
//			}
			//批量生成墙信息 结束

			//保存用户默认 cache数据
//		1	Troop Housing
//		2	Barrack
//		3	Laboratory
//		4	Alliance Castle
//		5	Spell Forge
//		100	Town Hall
//		200	Elixir Pump
//		201	Elixir Storage
//		202	Gold Mine
//		203	Gold Storage
//		300	Cannon
//		301	Archer Tower
//		302	Wizard Tower
//		303	Air Defense
//		304	Mortar
//		305	Tesla Tower
//		400	Wall
//		500	Worker Building
//		600	Communications mast
//		601	Goblin main building
//		602	Goblin hut		
//		for(int x=0;x<21;x++){
//			
//		}
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 1, 1);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 2, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 3, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 4, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 5, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 100, 1);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 200, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 201, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 202, 1);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 203, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 300, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 301, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 302, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 303, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 304, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 305, 0);
//			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 400, 44);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 500, 1);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 600, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 601, 0);
			bwUserMapDataCacheDAOImpl.updateUserBuildCount(uservo.getMailaddress(), 602, 0);
			bwusermapdatavoTemp=null;
			//初始化战斗统计信息
			BwUserBattleStatisticsVO bwuserbattlestatisticsvo=new BwUserBattleStatisticsVO();
			bwuserbattlestatisticsvo.setMailaddress(uservo.getMailaddress());
			bwuserbattlestatisticsvo.setClansid(0);
			bwuserbattlestatisticsvo.setFailtimes(0);
			bwuserbattlestatisticsvo.setGetelixircount(0);
			bwuserbattlestatisticsvo.setGetgoldencount(0);
			bwuserbattlestatisticsvo.setMaxpvpmark(0);
			bwuserbattlestatisticsvo.setWintimes(0);
			bwUserBattleStatisticsDaoImpl.save(bwuserbattlestatisticsvo);
		} catch (CacheDaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//清楚用户注册的信息
			throw new ManagerServerException(e);
		}
		
	}

	public BwUserMapDataDAO getBwUserMapDataCacheDAOImpl() {
		return bwUserMapDataCacheDAOImpl;
	}

	public void setBwUserMapDataCacheDAOImpl(
			BwUserMapDataDAO bwUserMapDataCacheDAOImpl) {
		this.bwUserMapDataCacheDAOImpl = bwUserMapDataCacheDAOImpl;
	}

	public BwUserBattleStatisticsDAO getBwUserBattleStatisticsDaoImpl() {
		return bwUserBattleStatisticsDaoImpl;
	}

	public void setBwUserBattleStatisticsDaoImpl(
			BwUserBattleStatisticsDAO bwUserBattleStatisticsDaoImpl) {
		this.bwUserBattleStatisticsDaoImpl = bwUserBattleStatisticsDaoImpl;
	}

	public BwMineCollectorDAO getBwMineCollectorCacheDAOImpl() {
		return bwMineCollectorCacheDAOImpl;
	}

	public void setBwMineCollectorCacheDAOImpl(
			BwMineCollectorDAO bwMineCollectorCacheDAOImpl) {
		this.bwMineCollectorCacheDAOImpl = bwMineCollectorCacheDAOImpl;
	}

	public BwPlantUserDAO getBwPlantUserCacheDAOImpl() {
		return bwPlantUserCacheDAOImpl;
	}

	public void setBwPlantUserCacheDAOImpl(BwPlantUserDAO bwPlantUserCacheDAOImpl) {
		this.bwPlantUserCacheDAOImpl = bwPlantUserCacheDAOImpl;
	}


	@Override
	public void initPlantUserInforSender(BwPlantUserVO bwPlantUserVO)
			throws ManagerServerException {
		//mina  客户端发出获取uuid的请求
		try {
			//先判断一下改用户是否存在 以mac地址为条件
			String bowei_id=bwPlantUserCacheDAOImpl.queryBwPlantUserVOByMailAddress(bwPlantUserVO);
			if(bowei_id!=null){
				bwPlantUserVO.setBoweiid(bowei_id);
				return ;
			}
			findUUIDClient.getNewUUIDSendRequest(bwPlantUserVO);
			//bowei_id 用第三方服务器来生成
			long boweiId=arrayBQ.take();
			bwPlantUserVO.setBoweiid(String.valueOf(boweiId));
			bwPlantUserCacheDAOImpl.save(bwPlantUserVO);
			BwUserVO uservo = new BwUserVO();
			uservo.setMailaddress(bwPlantUserVO.getBoweiid());
			uservo.setMacAddress(bwPlantUserVO.getMacaddress());
			this.initUserInfor(uservo);
		} catch (CacheDaoException e) {
			e.printStackTrace();
			//删除注册的平台用户
			bwPlantUserCacheDAOImpl.delete(bwPlantUserVO);
			throw new ManagerServerException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new ManagerServerException(e);
		}
	}
	@Override
	public void initPlantUserInfor(BwPlantUserVO bwPlantUserVO)
			throws ManagerServerException {
		arrayBQ.add(Long.parseLong(bwPlantUserVO.getBoweiid()));
	}
	@Override
	public int initAndLoginForThirdParty(BwPlantUserVO bwplantuservo)
			throws ManagerServerException {
		BwPlantUserVO	t=bwPlantUserCacheDAOImpl.queryBwPlantUserVOById(bwplantuservo);
		if(t==null){//初始化用户数据
			bwPlantUserCacheDAOImpl.save(bwplantuservo);
		}
		return ErrorCodeInterface.SUCESS;
	}

	public FindUUIDClient getFindUUIDClient() {
		return findUUIDClient;
	}

	public void setFindUUIDClient(FindUUIDClient findUUIDClient) {
		this.findUUIDClient = findUUIDClient;
	}

	public BwMineCollectorAllDAO getBwMineCollectorAllCacheDAOImpl() {
		return bwMineCollectorAllCacheDAOImpl;
	}

	public void setBwMineCollectorAllCacheDAOImpl(
			BwMineCollectorAllDAO bwMineCollectorAllCacheDAOImpl) {
		this.bwMineCollectorAllCacheDAOImpl = bwMineCollectorAllCacheDAOImpl;
	}

	@Override
	public StopServerMessageVO getServerStatus() throws ManagerServerException {
		return bwUserCacheDAOImpl.getServerStatus();
	}

	public BwUserBankDAO getBwUserBankCacheDAOImpl() {
		return bwUserBankCacheDAOImpl;
	}

	public void setBwUserBankCacheDAOImpl(BwUserBankDAO bwUserBankCacheDAOImpl) {
		this.bwUserBankCacheDAOImpl = bwUserBankCacheDAOImpl;
	}

	@Override
	public void queryUserInforAndGem(BwPlantUserVO bwPlantUserVO)
			throws ManagerServerException {
		String bowei_id=bwPlantUserCacheDAOImpl.queryBwPlantUserVOByMailAddress(bwPlantUserVO);
		BwUserVO bwuservo =new BwUserVO();
		bwuservo.setMailaddress(bowei_id);
		BwUserVO t=bwUserCacheDAOImpl.queryBwUserVOById(bwuservo);
//		userBankVO=
//		t.setUserBankVO(userBankVO)
	}


}
