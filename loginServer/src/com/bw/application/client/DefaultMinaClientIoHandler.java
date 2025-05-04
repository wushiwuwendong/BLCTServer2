 package com.bw.application.client;
 
 import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.bw.application.manager.user.IUserManager;
import com.bw.application.message.UUIDsSelfBuilder;
import com.bw.cache.vo.BwPlantUserVO;
import com.commonSocket.net.IMessage;
 
 public class DefaultMinaClientIoHandler extends IoHandlerAdapter
 {
	 private IUserManager userManagerImpl;
	 private Logger logger = Logger.getLogger(DefaultMinaClientIoHandler.class);
   public void sessionCreated(IoSession session)
     throws Exception
   {
     //super.sessionCreated(session);
	   logger.info("create session@@@@@@@");
   }
 
   public void sessionOpened(IoSession session)
     throws Exception
   {
     //super.sessionOpened(session);
	   logger.info("create session@@@@@@@");
   }
 
   public void sessionClosed(IoSession session)
     throws Exception
   {
     //super.sessionClosed(session);
	   logger.info("sessionClosed@@@@@@@");
   }
 
   public void sessionIdle(IoSession session, IdleStatus status)
     throws Exception
   {
	   logger.info("sessionIdle@@@@@@@");
     //super.sessionIdle(session, status);
   }
 
   public void exceptionCaught(IoSession session, Throwable cause)
     throws Exception
   {
	   logger.info("exceptionCaught @@@@@@@",cause);
//     super.exceptionCaught(session, cause);
   }
 
   public void messageReceived(IoSession session, Object message)
     throws Exception
   {
	      IMessage.PNPCMessage pMessage = (IMessage.PNPCMessage)message;
	      IMessage.Head head = IMessage.Head.parseFrom(pMessage.getMsgHead());
	      System.out.println("good ideal :"+head.getCommandId());
//	       Request request = new DefaultRequest(ioSession, head.getCommandId(), head.getSequence(), pMessage.getMsgBody(), ioMode);
//	   if(message instanceof ){
//		   
//	   }
	      int commondId=head.getCommandId();
	      if(commondId==41600){
		       UUIDsSelfBuilder.CreateUUIDResponse response= UUIDsSelfBuilder.CreateUUIDResponse.parseFrom(pMessage.getMsgBody());
			      long uuid= response.getUuid();
			      BwPlantUserVO puvo=new BwPlantUserVO();
					puvo.setBoweiid(String.valueOf(uuid));
			      System.out.println("new uuid:"+uuid);
			      userManagerImpl.initPlantUserInfor(puvo);	    	  
	      }
   }
 
   public void messageSent(IoSession session, Object message)
     throws Exception
   {
   // super.messageSent(session, message);
	   logger.info("messageSent @@@@@@@");
   }

public IUserManager getUserManagerImpl() {
	return userManagerImpl;
}

public void setUserManagerImpl(IUserManager userManagerImpl) {
	this.userManagerImpl = userManagerImpl;
}
   
 }