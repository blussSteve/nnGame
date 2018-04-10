package com.lhyone.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSourceFactory;


/**
 * Created by Think on 2017/8/15.
 */
public class DataSourceUtil {
    private static String confile = "/druid.properties";
    public static Properties p = null;
    
    private static DataSource dbSource;
    static {
        p = new Properties();
        InputStream inputStream = null;
        try {
            try {
            	BufferedReader br = new BufferedReader(new InputStreamReader(DataSourceUtil.class.getResourceAsStream(confile)));  
                p.load(br);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public static final DataSource getDbSource() throws Exception {
    	if(dbSource==null){
    		dbSource = DruidDataSourceFactory.createDataSource(p);
    	}
    	return dbSource;
    }
    public static final DataSource getDataSource() throws Exception {
        DataSource dataSource = DruidDataSourceFactory.createDataSource(p);
        
        return dataSource;
    }

    public static void main(String[] args) throws Exception{
//        DataSource dataSource = DataSourceUtil.getDataSource();
//        Connection conn=null;
//        PreparedStatement stmt=null;
//        ResultSet rs=null;
//        try{
//             conn = dataSource.getConnection();
//
//            String sql = "SELECT * from buytemp";
//
//            stmt = conn.prepareStatement(sql);
//
//            rs = stmt.executeQuery();
//            JdbcUtils.printResultSet(rs);
//            rs.close();
//            stmt.close();
//
//            conn.close();
//        }finally {
//            if(rs!=null){
//                rs.close();
//            }
//            if(stmt!=null){
//                stmt.close();
//            }
//            if(conn!=null){
//                conn.close();
//            }
//        }
    }
}
