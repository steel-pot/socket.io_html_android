var mysql = require('mysql');
var pool  = null;
//由于使用了一些类的属性,所以不支持在多线程情况下使用同一对象执行sql
module.exports = class GaryMysql{
	constructor(tabName, pk) { 
        this.tabName = tabName;//类中变量
        this.pk = typeof pk !=="undefined"?pk:'id';
		this.initSet();
    }
	static setDB(host,user,password,database)
	{ 
		if(pool==null)
		{
			pool=mysql.createPool({
					  connectionLimit : 10,
					  host            :  host ,
					  user            :  user ,
					  password        :  password ,
					  database        :  database 
					});
		}
	}
	query(sql,callback){ 
			var values=this.values;
			this.initSet();
			pool.getConnection(function(err,conn){ 
							if(err){
								callback(err,null,null);
							}else{
								conn.query(sql,values,function(qerr,vals,fields){
									//释放连接
									conn.release();
									//事件驱动回调
									callback(qerr,vals,fields);
								});
							}
						});
	}
	 
	 //strWhere  
	// 链式调用 
	where(whereSet,values)
	{
		this.values = typeof values !=="undefined"?values:[];
		if(typeof whereSet==='string')
		{
			this.strWhere=whereSet;
		}else if(typeof whereSet==='object')
		{
			var strWhere='';
			for(var key in whereSet)
			{
				if(strWhere!='')strWhere+=' AND '; 
				strWhere+=key+'=?';
			    this.values.push(whereSet[key]) ;  
			}
			this.strWhere=strWhere;
		}else{
			this.strWhere='';
		}
		if(this.strWhere!="")this.strWhere=" WHERE "+this.strWhere
		 
		return this;
	} 
	 
	 
	findCont(callback){  
		var sql='SELECT COUNT(1)C FROM '+this.tabName+this.strWhere; 
		this.query(sql,function(err, result){
			if(!err)
			{
				 result=result[0].C;
			}
			if(typeof callback =="function")
			{
				callback(err,result);
			} 
		}); 
	}
    find(callback,order,field)
	{ 
	
		this.findAll(function(err,rows,fields){
			 if(typeof callback =="function")
			 {
				rows=rows&&rows.length>0?rows[0]:null;
				callback(err,rows,fields);
			 }
		},order,field,1);
	}
	findAll(callback,order,field,limit){  
		order =order?' ORDER BY '+order:'';
		field =field?field:'*';
		limit =limit?' LIMIT '+limit:''; 
		
		var sql='SELECT '+field+' FROM '+this.tabName+this.strWhere+order+limit; 
		this.query(sql,function(err,rows,fields){
			if(typeof callback =="function")
			 {
				rows=rows&&rows.length>0?rows:null;
				callback(err,rows,fields);
			 }
		});  
	}
	
	//setData 需要在 setWhere前面
	data(dataSet){
		this.dataStr='';
		this.values = typeof values !=="undefined"?values:[];
		for(var key in dataSet)
		{
			if(this.dataStr!='')this.dataStr+=','; 
			this.dataStr+=key+'=?';
			this.values.push(dataSet[key]) ;
		}
		return this;
	}
	del(callback){
		
		var sql='DELETE FROM '+this.tabName+this.strWhere;   
		this.query(sql,function(err,result,fields){
			 if(typeof callback =="function")
			 {
				 if(!err)
				 {
					  result=result.affectedRows;
				 }
				 callback(err,result,fields);
			 }
		}); 
	}
	update(callback){  
		var sql='UPDATE '+this.tabName+' SET '+this.dataStr+this.strWhere;   
		this.query(sql,function(err,result,fields){
			 if(typeof callback =="function")
			 {
				 if(!err)
				 {
					 result=result.changedRows;
				 }
				 callback(err,result,fields);
			 }
		}); 
	}
	//
	create(dataSet,callback)
	{ 
		var fields="";
		var valuesStr=""; 
		for(var key in dataSet)
		{
			if(fields!="")fields+=",";
			if(valuesStr!="")valuesStr+=",";
			fields+=key;
			valuesStr+="?";
			this.values.push(dataSet[key]) ;
		}
		
		var sql="INSERT INTO "+this.tabName+"("+fields+")VALUES("+valuesStr+")"; 
		this.query(sql,function(err,result,fields){
			 if(typeof callback =="function")
			 {
				 if(!err)
				 {
					 result=result.insertId;
				 }
				 callback(err,result,fields);
			 }
		});
	}
 
	initSet(){
		this.values=[];
		this.dataStr='';
		this.strWhere='';
	} 
} 