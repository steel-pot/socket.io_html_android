var cryptos=require("./cryptos"); 
var loggedGroup="room.logged";
var forbidAll=false;
var forbidList={};
module.exports=class{
	constructor(server) { 
		this.server=server;
		var self=this;
		server.on('connection', function (client) {  
			self.onConnection(client);
		});  
	}
	
	onConnection(client){
		var self=this;
		client.on('login', function(msg){
			 self.onLogin(client,msg);
		  });
		client.on('sendMsg', function(msg){
			 self.onSendMsg(client,msg);
		  });
		client.on('getNums', function(msg){
			 self.onGetNums(client);
		  });  
		client.on('disconnect', function(){
			self.onDisconnect(client);
		});		  
	} 
	onDisconnect(client){
		this.userChange(client,0);
	}
	userChange(client,isIn){ 
		client.broadcast.to(loggedGroup).emit('userChange',{"isIn":isIn,"userinfo":client.userinfo,"nums":this.getNums()});  
	} 
//监听
//---------LOGIN BEGIN--------------------------------------------------------------------------------------------------------------------
	onLogin(client,msg){
		  
		 var self=this;
		 var where; 
		 if(msg.token)
		 {
			 where={"token":msg.token}
		 }else if(msg.account&&msg.password){  
			  where={'account':msg.account,"password":msg.password};
		 }else{
			 self.loginBack(client,0, "登陆信息不全");
			 return;
		 } 
		 
		 var tab=new GaryMysql('lbw_userinfo'); 
		 tab.where(where).find(function(err,userinfo){
				  if(err){
					  self.loginBack(client,0, "查询用户失败,请与管理员联系");
					  return;
				  }
				  if(!userinfo)
				  {
					  self.loginBack(client,0,"登陆失败用户名或密码错误");
					  return;
				  }
				  client.userinfo=userinfo;
				  client.join(loggedGroup); 
				  
					  
				  self.loginBack(client,1,"登陆成功");
				  //查用户是否被禁言
				  if(forbidList.hasOwnProperty(userinfo.userID))
				  {
					  forbidList[userinfo.userID]=client.id;
					  //通知用户被禁言
					  self.sendNotice("txt","管理员禁止了你发言,想要解封请与客服联系",client); 
				  }
				  
				  self.userChange(client,1);
			  },null,'userID,name,avatar');
	}
	loginBack(client,status,info){ 
		 
		client.emit("loginBack",{"status":status,"info":info,"userinfo":status==1?client.userinfo:null});
	} 
	
//---------LOGIN END------------------------------------------------------------------------------------------------------------------------------
//---------GETNUMS BEGIN--------------------------------------------------------------------------------------------------------------------------
	onGetNums(client)
	{
		this.getNumsBack(client);
	}
	getNumsBack(client)
	{
		if(!client.userinfo)return ;
		client.emit("getNumsBack",{"nums":this.getNums()});
	}
//---------GETNUMS END-----------------------------------------------------------------------------------------------------------------------------


//----------SendMsg BEGIN--------------------------------------------------------------------------------------------------------------------------
	onSendMsg(client,msg)
	{ 
		//防止乱发消息,重构msg
		var type;
		var content;
		if(msg.type)
		{
			//这里需要对type进行判断,是否定义内的类型,现在只有txt所以不管
			type=msg.type;
		}else{
			return ;
		}
		
		if(msg.content)
		{
			content=msg.content;
		}else{
			return ;
		}
		this.messageComing(client,type,content);
	}
	/*
	* user  用户发过来的消息
	* advert 公告消息
	* notice 通知消息,比如被禁言,发送结果
	* admin  管理员消息
	* */
	messageComing(client,type,content)
	{
		//防止未登陆用户发消息
		if(!client.userinfo)return ;
		if(forbidAll)
		{
			this.sendNotice("txt","老铁,不好意思,你的发言未成功,管理员禁止了所有人发言",client); 
			return ;
		}
		
		if(forbidList.hasOwnProperty(client.userinfo.userID))
		{
			this.sendNotice("txt","你的发言未成功,管理员禁止了你发言,想要解封请面壁思考后与客服联系",client); 
			return ;
		}
		
		client.broadcast.to(loggedGroup).emit("messageComing",{"fromType":"user","type":type,"userinfo":client.userinfo,"content":content});
	}
//----------SendMsg END----------------------------------------------------------------------------------------------------------------------------

  
	//方法
	 getNums()
	 { 
		return Object.getOwnPropertyNames(this.server.in("room.logged").sockets).length;
	 }
	 
	 //发送公告
	 sendAdvert(type,content)
	 {
		  this.server.in(loggedGroup).emit("messageComing",{"fromType":"advert","type":type,"content":content});
	 }
	 //以管理员身份发言
	 sendAdmin(type,content)
	 {
		  this.server.in(loggedGroup).emit("messageComing",{"fromType":"admin","type":type,"content":content});
	 }
	 
	 //发送通知 也可以针对个人发通知
	 sendNotice(type,content,client)
	 {
		 if(client)
		 {
			 client.emit("messageComing",{"fromType":"notice","type":type,"content":content});
		 }else{
			 this.server.in(loggedGroup).emit("messageComing",{"fromType":"notice","type":type,"content":content});
		 }
		  
	 } 
	  //修改配置
	 setChange(k,v)
	 {
		  this.server.in(loggedGroup).emit("setChange",{"key":k,"value":v});
	 }
	 
	 //禁止所有用户发言 1禁言 0解除禁言
	 forbidAllUser(set)
	 {
		 forbidAll=set==1;
		 if(forbidAll)
		 {
			this.sendNotice("txt","管理员禁止了所有用户发言"); 
		 }else{
			this.sendNotice("txt","管理员解除了禁言,大家可以自由发言了");  
		 }
		 
	 }
	 forbidUser(set,socketid)
	 {
		 var client=this.server.in(loggedGroup).sockets[socketid];
		 if(!client)return;
		 if(set==1)
		 {  
			forbidList[client.userinfo.userID]=socketid;
			this.sendNotice("txt","您被管理员禁言了",client); 
		 }else{
			delete forbidList[client.userinfo.userID];
			this.sendNotice("txt","管理员解除了您的禁言",client); 
		 } 
	 }
	 
	 getUserList()
	 {
		 var rs={};
		 var sockets=this.server.in(loggedGroup).sockets;
		 for(var socketid in sockets)
		 { 
			 rs[socketid]=sockets[socketid].userinfo; 
		 }
		 return rs;
	 }
} 