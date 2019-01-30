package com.datameans.tool.ssh;
// 6984

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshConnect {

@Override
	public String toString() {
		return "SshConnect [sshUser=" + sshUser + ", sshPassword="
				+ sshPassword + ", sshHost=" + sshHost + ", sshPort=" + sshPort
				+ ", sshRemoteHost=" + sshRemoteHost + ", sshDbUser="
				+ sshDbUser + ", sshDbPassword=" + sshDbPassword + ", conn="
				+ conn + ", DB_ADDRS=" + DB_ADDRS + ", DB_USER=" + DB_USER
				+ ", DB_PASS=" + DB_PASS + ", DB_HOST=" + DB_HOST
				+ ", DB_NAME=" + DB_NAME + "]";
	}
	//	private boolean ssh=false;
	/**SSH loging username */
	private  String sshUser;                // SSH loging username
	/**SSH login password */
	private  String sshPassword;                 // SSH login password
	/**hostname or ip or SSH server */
	private  String sshHost;          // hostname or ip or SSH server
	/**remote SSH host port number */
	private  int sshPort;                                   // remote SSH host port number
	/**hostname or ip of your database server same as  strSshHost*/
	private  String sshRemoteHost;  // hostname or ip of your database server
	/** the db user name on the ssh server if left null, the user of the default db will be used*/
	private String sshDbUser;;                    // database loging username
	/** the db user's password on the ssh server if left null, the user of the default db will be used*/
	private String sshDbPassword;                   // database login password
	
	
	private Connection conn = null;
private String DB_ADDRS = null;
	private String DB_USER = null;
	private String DB_PASS = null;
	private String DB_HOST = null;
	private String DB_NAME ;
	private Session session;
	 private int nLocalPort=3366; // local port number use to bind SSH tunnel

	 public boolean isSshConnected(){
		 if(session!=null&&session.isConnected()){
			 return true;
		 }else{
			 return false;
		 }
	 }
	 
	 public void sshConnect() throws JSchException{
		 int nRemotePort = 3306;        
		   final JSch jsch = new JSch();
		    session = jsch.getSession( sshUser, sshHost, sshPort );
		   session.setPassword( sshPassword );
		       
		   final Properties config = new Properties();
		   config.put( "StrictHostKeyChecking", "no" );
		   session.setConfig( config );
		      
		   session.connect();
		   
		   while(true){
			   try{
				   session.setPortForwardingL(nLocalPort, sshRemoteHost, nRemotePort);
				   break;
			   }catch(com.jcraft.jsch.JSchException e){
				   nLocalPort++;
			   }
		   }
	   
	 }
	 
	 public SshConnect sshConnectToDb() throws SQLException, JSchException{		
	  //    int nLocalPort = 3366;                                // local port number use to bind SSH tunnel
	   
		                       // remote port number of your database 
	    	   try {
	    		   if(!isSshConnected()){
	    			   sshConnect();
	    		   }
	    		 //  System.out.println(toString());
	    		   if(sshDbPassword==null){
	    			   sshDbPassword=DB_PASS;	    			   
	    		   }
	    		   if(sshDbUser==null){
	    			   sshDbUser=DB_USER;
	    		   }// &autoReconnect=true&failOverReadOnly=false&maxReconnects=10
	    		   
	    		   conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:"+nLocalPort+"/"+DB_NAME+"?useUnicode=true&noAccessToProcedureBodies=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", 
	    				   sshDbUser, sshDbPassword);
	    	   } catch (JSchException e1) {
	    		   e1.printStackTrace();
	    		   disconnect();	    		
	    		   throw e1;
	    	   }  catch (SQLException e1) {
	    		   e1.printStackTrace();
	    		//   disconnect();	    		
	    		   throw e1;
	    	   }   

	   		return this;
	}

	/**
	 * 
	 * @return true if it is connected to the simple mysql server. 
	 * 		   false if connected to the ssh server
	 * @throws SQLException
	 * @throws JSchException
	 */
	public  boolean connectToDB( )throws SQLException, JSchException{
		
//		 StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//	       for (int i = 1; i < elements.length; i++) {
//	    	    StackTraceElement s = elements[i];
//	    	    System.out.println("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
//	    	  }
				
				try{
					DriverManager.setLoginTimeout(20);
//System.out.println("!1/"+toString());

//System.out.println("jdbc:mysql://"+DB_HOST+"/"+DB_NAME+"?useUnicode=true&characterEncoding=UTF-8&noAccessToProcedureBodies=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");

					conn = DriverManager.getConnection(
							"jdbc:mysql://"+DB_HOST+"/"+DB_NAME+"?"
									+ "useUnicode=true&characterEncoding=UTF-8&"
									+ "noAccessToProcedureBodies=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
							DB_USER,DB_PASS);
					return false;
				}catch(SQLException e){
				//	e.printStackTrace();
					if(sshHost!=null){
						System.out.println("connect via SSH");
						sshConnectToDb();
						return true;
					}else{
						return false;
//						throw e;
					}					
				}catch(Exception ee){
				//	System.out.println("!@3");ee.printStackTrace();

					if(sshHost!=null){
						sshConnectToDb();
						return true;
					}else{
						return false;
//						throw e;
					}
					
				}		
	}
	
	
	public Connection getConnection(){
		return conn;
	}
	public void disconnect() throws SQLException{
		try{conn.close();}catch(Exception e){}
		try{session.disconnect();}catch(Exception e){}
	}

	public String getSshUser() {
		return sshUser;
	}

	public String getSshPassword() {
		return sshPassword;
	}

	public String getSshHost() {
		return sshHost;
	}

	public int getSshPort() {
		return sshPort;
	}

	public String getSshRemoteHost() {
		return sshRemoteHost;
	}

	public String getSshDbUser() {
		return sshDbUser;
	}

	public String getSshDbPassword() {
		return sshDbPassword;
	}

	public String getDB_USER() {
		return DB_USER;
	}

	public String getDB_PASS() {
		return DB_PASS;
	}

	public String getDB_NAME() {
		return DB_NAME;
	}
	public int getLocalPort(){
		return nLocalPort;
	}



	public SshConnect setSshUser(String sshUser) {
		this.sshUser = sshUser;
		return this;
	}

	public SshConnect setSshPassword(String sshPassword) {
		this.sshPassword = sshPassword;
		return this;
	}

	public SshConnect setSshHost(String sshHost) {
		this.sshHost = sshHost;
		return this;
	}

	public SshConnect setSshPort(int sshPort) {
		this.sshPort = sshPort;
		return this;
	}

	public SshConnect setSshRemoteHost(String sshRemoteHost) {
		this.sshRemoteHost = sshRemoteHost;
		return this;
	}

	public SshConnect setSshDbUser(String sshDbUser) {
		this.sshDbUser = sshDbUser;
		return this;
	}

	public SshConnect setSshDbPassword(String sshDbPassword) {
		this.sshDbPassword = sshDbPassword;
		return this;
	}

	public SshConnect setDB_USER(String dB_USER) {
		DB_USER = dB_USER;
		return this;
	}

	public SshConnect setDB_PASS(String dB_PASS) {
		DB_PASS = dB_PASS;
		return this;
	}

	public SshConnect setDB_NAME(String dB_NAME) {
		DB_NAME = dB_NAME;
		return this;
	}

	public String getDB_ADDRS() {
		return DB_ADDRS;
	}

	public SshConnect setDB_ADDRS(String dB_ADDRS) {
		DB_ADDRS = dB_ADDRS;
		return this;
	}
	public SshConnect setDB_Host(String dB_Host) {
		this.DB_HOST=dB_Host;
		return this;
	}
	


	
}
