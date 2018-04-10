package com.lhyone.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DbUtil {
    private static DbUtil _dbUtil;
    
    
    public static DbUtil getInstance(){
    	if(null==_dbUtil){
    		_dbUtil=new DbUtil();
    	}
    	
    	return _dbUtil;
    }
    
    
    /**   
     * 建立数据库连接   
     * @return 数据库连接   
     * @throws Exception   
     */    
    public  Connection getConnection() throws Exception {  
    	Connection connnection =null;
        try {    
            // 获取连接    
            connnection =DataSourceUtil.getDbSource().getConnection();  
        } catch (SQLException e) {    
        	e.printStackTrace();
        }    
        return connnection;    
    }    
    
    /**   
     * insert update delete SQL语句的执行的统一方法   
     * @param sql SQL语句   
     * @param params 参数数组，若没有参数则为null   
     * @return 受影响的行数   
     * @throws Exception   
     */    
    public  int executeUpdate(String sql, Object[] params){    
        // 受影响的行数    
    	Connection connnection =null;
        int affectedLine = 0;    
        PreparedStatement preparedStatement = null;        
        try {    
            // 获得连接    
            connnection = this.getConnection();   
            // 调用SQL     
            preparedStatement = connnection.prepareStatement(sql);    
            // 参数赋值    
            if (params != null) {    
                for (int i = 0; i < params.length; i++) {    
                    preparedStatement.setObject(i + 1, params[i]);    
                }    
            }    
            // 执行    
            affectedLine = preparedStatement.executeUpdate();    
        } catch (Exception e) {    
            e.printStackTrace();  
        } finally {    
            // 释放资源    
        	closeAll(preparedStatement,null,null,connnection );  
        }    
        return affectedLine;    
    }    
  
    /**
     * 获取单条记录
     * @param sql
     * @param params
     * @return
     */
    public Map<String,Object> getOne(String sql, Object[] params) { 
    	 List<Map<String,Object>> list= getList(sql, params);
    	 if(list.size()>0){
    		 return list.get(0);
    	 }
    	 return null;
    }
    
    /**
     * 获取单条记录
     * @param clasz
     * @param sql
     * @param params
     * @return
     */
    public <T> T getOne(Class<T> clasz ,String sql, Object[] params) { 
   	 List<T> list= getList(clasz,sql, params);
   	 if(list.size()>0){
   		 return list.get(0);
   	 }
   	 return null;
   }
   
    /**   
     * 获取结果集，并将结果放在List中   
     *    
     * @param sql   
     *            SQL语句   
     * @return List   
     *                       结果集   
     * @throws Exception   
     */    
    public List<Map<String,Object>> getList(String sql, Object[] params) {    
    	
    	Connection connnection =null;
    	PreparedStatement preparedStatement = null;
   	 	ResultSet resultSet = null;    
        // 创建ResultSetMetaData对象    
        ResultSetMetaData rsmd = null;    
            
        // 结果集列数    
        int columnCount = 0;    
        try {    
        	 // 获得连接    
            connnection = this.getConnection();    
            // 调用SQL    
            preparedStatement = connnection.prepareStatement(sql);    
            // 参数赋值    
            if (params != null) {    
                for (int i = 0; i < params.length; i++) {    
                    preparedStatement.setObject(i + 1, params[i]);    
                }    
            }    
            // 执行    
            resultSet = preparedStatement.executeQuery();  
            
        	rsmd = resultSet.getMetaData();    
                
            // 获得结果集列数    
            columnCount = rsmd.getColumnCount();    
        } catch (Exception e) {    
        	e.printStackTrace();
        }    
    
        // 创建List    
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();    
    
        try {    
            // 将ResultSet的结果保存到List中    
            while (resultSet.next()) {    
                Map<String, Object> map = new HashMap<String, Object>();    
                for (int i = 1; i <= columnCount; i++) {    
                    map.put(rsmd.getColumnLabel(i), resultSet.getObject(i));    
                }    
                list.add(map);    
            }    
          return list;
            
        } catch (SQLException e) {    
        	e.printStackTrace();
        }catch (Exception e) {    
        	e.printStackTrace();
        }  finally {    
            // 关闭所有资源    
        	closeAll(preparedStatement, null, resultSet, connnection); 
        }    
    
        return list;    
    }    
    
    
    // 返回某条记录的某一个字段的值 或 一个统计的值(一共有多少条记录等.)  
    public <E> E getValue(String sql, Object... args) {  
    	 PreparedStatement preparedStatement = null;   
    	 ResultSet resultSet = null;  
    	 Connection connnection=null;
        try {   
        	// 获得连接    
            connnection = this.getConnection();  
            //1. 得到结果集  
            preparedStatement = connnection.prepareStatement(sql);  
  
            for (int i = 0; i < args.length; i++) {  
                preparedStatement.setObject(i + 1, args[i]);  
            }  
  
            resultSet = preparedStatement.executeQuery();  
            if(resultSet.next()){  
                return (E) resultSet.getObject(1);  
            }  
        } catch (SQLException e) {    
        	e.printStackTrace();
        }catch(Exception ex){  
            ex.printStackTrace();  
        } finally{ 
        	closeAll(preparedStatement,null,resultSet,connnection);
        }  
        //2. 取得结果  
          
        return null;  
    }  
  
    
    
    /**   
     * 获取结果集，并将结果放在List中   
     *    
     * @param sql   
     *            SQL语句   
     * @return List   
     *                       结果集   
     * @throws Exception   
     */    
    public <T> List<T> getList(Class<T> clazz ,String sql, Object[] params) {
    	Connection connnection =null;
    	PreparedStatement preparedStatement = null;
   	 	ResultSet resultSet = null;    
        // 创建ResultSetMetaData对象   
    	List<T> listClasz=new ArrayList<T>();
        // 执行SQL获得结果集    
        // 创建ResultSetMetaData对象    
        ResultSetMetaData rsmd = null;    
            
        // 结果集列数    
        int columnCount = 0;    
        try {    
        	 // 获得连接    
            connnection = this.getConnection();    
            // 调用SQL    
            preparedStatement = connnection.prepareStatement(sql);    
            // 参数赋值    
            if (params != null) {    
                for (int i = 0; i < params.length; i++) {    
                    preparedStatement.setObject(i + 1, params[i]);    
                }    
            }    
            // 执行    
            resultSet = preparedStatement.executeQuery(); 
        	
            rsmd = resultSet.getMetaData();    
                
            // 获得结果集列数    
            columnCount = rsmd.getColumnCount();    
        } catch (Exception e) {    
             e.printStackTrace();
        }    
    
        // 创建List    
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();    
    
        try {    
            // 将ResultSet的结果保存到List中    
            while (resultSet.next()) {    
                Map<String, Object> map = new HashMap<String, Object>();    
                for (int i = 1; i <= columnCount; i++) {    
                    map.put(rsmd.getColumnLabel(i), resultSet.getObject(i));    
                }    
                list.add(map);    
            }    
            listClasz=  transfterMapListToBeanList(clazz, list);
            
        } catch (SQLException e) {    
           e.printStackTrace(); 
        }catch (Exception e) {    
        	e.printStackTrace();
        }finally{
        	// 关闭所有资源    
        	closeAll(preparedStatement, null, resultSet, connnection);
        }    
    
        return listClasz;    
    }    
        
    /**   
     * 存储过程带有一个输出参数的方法   
     * @param sql 存储过程语句   
     * @param params 参数数组   
     * @param outParamPos 输出参数位置   
     * @param SqlType 输出参数类型   
     * @return 输出参数的值   
     * @throws Exception   
     */    
    public Object excuteQuery(String sql, Object[] params,int outParamPos, int SqlType){  
    	 CallableStatement callableStatement = null;  
    	 Connection connnection =null;
        Object object = null;  
        try {   
            connnection = this.getConnection();    
         
            // 调用存储过程    
            callableStatement = connnection.prepareCall(sql);    
                
            // 给参数赋值    
            if(params != null) {    
                for(int i = 0; i < params.length; i++) {    
                    callableStatement.setObject(i + 1, params[i]);    
                }    
            }    
            // 注册输出参数    
            callableStatement.registerOutParameter(outParamPos, SqlType);    
                
            // 执行    
            callableStatement.execute();    
                
            // 得到输出参数    
            object = callableStatement.getObject(outParamPos);  
            
            
                
        } catch (Exception e) {    
            e.printStackTrace();
        } finally {    
            // 释放资源    
        	closeAll(null,callableStatement,null, connnection);    
        }    
            
        return object;    
    }    
    
    /**   
     * 关闭所有资源   
     */    
    private void closeAll(PreparedStatement preparedStatement,CallableStatement callableStatement,ResultSet resultSet,Connection connnection ) {    
        // 关闭结果集对象    
        if (resultSet != null) {    
            try {    
                resultSet.close();    
            } catch (SQLException e) {    
            	e.printStackTrace();
            }    
        }    
    
        // 关闭PreparedStatement对象    
        if (preparedStatement != null) {    
            try {    
                preparedStatement.close();    
            } catch (SQLException e) {    
            	e.printStackTrace();
            }    
        }    
            
        // 关闭CallableStatement 对象    
        if (callableStatement != null) {    
            try {    
                callableStatement.close();    
            } catch (SQLException e) {    
            	e.printStackTrace();
            }    
        }    
    
        // 关闭Connection 对象    
        if (connnection != null) {    
            try {    
                connnection.close();    
            } catch (SQLException e) {    
               e.printStackTrace();
            }    
        }       
    }   
    
    
    /** 
     * 转换List<Map> 为  List<T> 
     * @param clazz 
     * @param values 
     * @return 
     * @throws InstantiationException 
     * @throws IllegalAccessException 
     * @throws InvocationTargetException 
     */  
    public <T> List<T> transfterMapListToBeanList(Class<T> clazz,  
            List<Map<String, Object>> values) throws Exception {  
  
        List<T> result = new ArrayList<T>();  
  
        T bean = null;  
  
        if (values.size() > 0) {  
            for (Map<String, Object> m : values) {  
                //通过反射创建一个其他类的对象  
                bean = clazz.newInstance();  
                  
                for (Map.Entry<String, Object> entry : m.entrySet()) {  
                    String propertyName = entry.getKey();  
                    Object value = entry.getValue();  
                      
                    Field f;
					try {
						f = bean.getClass().getDeclaredField(processField(propertyName) );
						 if(f!=null){
	                    	 f.setAccessible(true);  
	                         f.set(bean, value);  
	                    }
					} catch (NoSuchFieldException e) {
						 
						e.printStackTrace();
					} catch (SecurityException e) {
						 
						e.printStackTrace();
					}  
                   
                   
                      
                    //BeanUtils.setProperty(bean, propertyName, value);  
                }  
                // 13. 把 Object 对象放入到 list 中.  
                result.add(bean);  
            }  
        }  
  
        return result;  
    }  
  
    private String processField( String field ) {
        StringBuffer sb = new StringBuffer(field.length());
        //field = field.toLowerCase();
//        System.out.println("field:"+field);
        String[] fields = field.split("_");
        String temp = null;
        sb.append(fields[0]);
        for ( int i = 1 ; i < fields.length ; i++ ) {
            temp = fields[i].trim();
            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
        }
        return sb.toString();
    }
    
    
    public static void main(String[] args) {
    	for(int i=0;i<100;i++){
    		long m=System.currentTimeMillis();
        	List<Map<String,Object>> list=DbUtil.getInstance().getList("SELECT * FROM lhy_club_gold_record t", new Object[]{});
        	long n=System.currentTimeMillis();
        	System.out.println(n-m);
    	}
    	
    	
//    	for(int i=0;i<100;i++){
//    		Long l=DbUtil.getInstance().getValue("SELECT t.club_gold FROM lhy_club_gold_record t WHERE t.id=2284", new Object[]{});
//    		System.out.println(l);
//    		DbUtil.getInstance().executeUpdate("UPDATE lhy_club_gold_record t SET t.club_gold=t.club_gold+? WHERE t.id=2284",new Object[]{l});
//    	}
//		for(int i=0;i<30;i++){
//    		new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					Long l=DbUtil.getInstance().getValue("SELECT t.club_gold FROM lhy_club_gold_record t WHERE t.id=2284", new Object[]{});
//					System.out.println(l);
//				}
//			}).start();
//    	}
     }
    
}
