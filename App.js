var GaryMysql = require('./GaryMysql');
GaryMysql.setDB('localhost','root','root','lbw');
global.GaryMysql=GaryMysql;
var express = require('express');
var io = require('socket.io');
 
 //处理post表单
var bodyParser = require('body-parser');
//新的express实例
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));

//设定网页文件位置 
app.use(express.static(__dirname+"/html"));
app.post('/sendAdvert',  function(req, res){
			var type=req.body.type;
			var content=req.body.content;
			room.sendAdvert(type,content);
			res.send("ok");
	}); 
app.post('/sendAdmin',  function(req, res){
			var type=req.body.type;
			var content=req.body.content;
			room.sendAdmin(type,content);
			res.send("ok");
	});	
app.post('/sendNotice',  function(req, res){
			var type=req.body.type;
			var content=req.body.content;
			room.sendNotice(type,content);
			res.send("ok");
	});	
app.post('/setChange',  function(req, res){
			var k=req.body.k;
			var v=req.body.v;
			room.setChange(k,v);
			res.send("ok");
	});	
app.post('/forbidAllUser',  function(req, res){
			var set=req.body.set; 
			room.forbidAllUser(set);
			res.send("ok");
	});		
app.post('/forbidUser',  function(req, res){
			 var set=req.body.set;
			 var socketid=req.body.socketid;			 
			 room.forbidUser(set,socketid);
			res.send("ok");
	});
app.post('/getUserList',  function(req, res){ 
			res.send(room.getUserList());
	});	
//启动http服务
var port=8888;
var httpServer = app.listen(port);
//启动socket服务
var socketServer = io.listen(httpServer);
 
//聊天室服务
var Room=require('./Room');
var room=new Room(socketServer.of('/room')); 
console.log("run in port "+port);