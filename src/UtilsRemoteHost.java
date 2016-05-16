

import com.jcraft.jsch.*;

import java.io.*;
import java.util.regex.Pattern;

public class UtilsRemoteHost {
	public static String eol = System.getProperty("line.separator");  

	static JSch jsch = new JSch();
	static Session session = null;
	private static String username = null;
	private static String password = null;
	private static  String hostname = null;

	public UtilsRemoteHost (String username,String password, String hostname)
	{
		this.setUsername(username);
		this.setPassword(password);
		this.setHostname(hostname);

		setupSession();
	}

	private static void setupSession ()
	{
		try {
			session = jsch.getSession(getUsername(), getHostname(), 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(getPassword());
			//	        Common.print(getPassword());
			session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
			session.connect(); 
			Common.print("Result:true - session is established");		
		} catch (JSchException e) {
			e.printStackTrace();
			Common.print("Result:false - session establisment failed");		
		}

	}

	private static ChannelSftp getChannelSftp ()
	{
		ChannelSftp sftpChannel = new ChannelSftp(); 
		Common.print("Trying to connect.....");
		try 
		{
			Common.print("session.isConnected(): " + session.isConnected());
			if (!session.isConnected())
			{
				setupSession();
			}

			Channel channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp) channel;
			Common.print("Result:true - SSH connection established");		
			Common.print(sftpChannel.pwd());
		}
		catch (JSchException | SftpException e) {
			Common.print("Result:false - SSH connection failed");		
			e.printStackTrace();
			sftpChannel = null;
		}
		return sftpChannel;
	}

	public String FileCopyTo(String copyTo, String copyFrom, String copyFileName)
	{
		ChannelSftp sftpChannel = new ChannelSftp();
		String result = "";
		Common.print("copyTo: " + copyTo);
		sftpChannel = getChannelSftp();
		try {
			//				Common.print(copyFileName+ "   :::   " + copyFrom);
			sftpChannel.cd(copyTo);
			Common.print(sftpChannel.pwd());
			//				sftpChannel.put(new FileInputStream(new File(copyFrom)), (new File(copyFrom).getName()), sftpChannel.OVERWRITE);
			sftpChannel.put(new FileInputStream(new File(copyFrom)), copyFileName, sftpChannel.OVERWRITE);
			//				sftpChannel.rename((new File(copyFrom).getName()), copyFileName);
			Common.print("Result:true - File is copied to remote host " + copyFileName);
			result = "Result:true - File is copied to remote host " + copyFileName;
			sftpChannel.exit();
		} catch (FileNotFoundException | SftpException | NullPointerException e) {
			e.printStackTrace();
			Common.print("Result:false - File is not copied to remote host");
			result = "Result:false - File is not copied to remote host. Check folder existence or other... ";
		}
		//            closeChannel();
		return result;
	}

	public String FileCopyFrom(String copyTo, String copyFrom)
	{
		ChannelSftp sftpChannel = new ChannelSftp();
		String result = "";
		Common.print("copyFrom: " + copyFrom);
		Common.print("copyTo: " + copyTo);
		sftpChannel = getChannelSftp();
		try {
			//            	Common.print(sftpChannel.pwd());
			sftpChannel.get(copyFrom, copyTo);
			Common.print("Result:true - File is copied from remote host");
			result = "Result:true - File is copied from remote host";
		} catch (SftpException e) {
			e.printStackTrace();
			Common.print("Result:false - File is not copied from remote host");
			result = "Result:false - File is not copied from remote host";
		}
		//            closeChannel();
		sftpChannel.exit();
		return result;
	}

	public String shellExecutorViaExecChannel (String command)
	{
		Channel channel;
		String result = "";
		try {
			Common.print("session.isConnected(): " + session.isConnected());
			if (!session.isConnected())
			{
				setupSession();
			}
			channel = session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec)channel).setErrStream(System.err);
			InputStream in=channel.getInputStream();
			channel.connect();

			byte[] tmp=new byte[1024];
			while(true)
			{
				while(in.available()>0)
				{
					int i=in.read(tmp, 0, 1024);
					if(i<0) break;
					result += new String(tmp, 0, i);// + eol;
				}
				if(channel.isClosed())
				{
					Common.print(command + eol + "exit-status: "+channel.getExitStatus());
					if (channel.getExitStatus() != 0)
					{
						result += "Result:false - exit-statusis not 0 but "+channel.getExitStatus();
					}
					result += "exit-status: "+channel.getExitStatus();
					break;
				}
			}
			//			    Common.print("shell result: " + result);
			channel.disconnect();
		}
		catch (JSchException | IOException e) {
			Common.print("Result:false - shell command execution failed");		
			e.printStackTrace();
			result = "Result:false - command failed to execute" + eol + e;;
		}
		//	        Common.print("Shell execution result: "+ result);
		return result;
	}

	public String shellExecutorViaShellChannel (String command)
	{
		Channel channel;
		StringBuilder result = new StringBuilder(); 
		command = commandFormatter(command);
		String finalResult = "";
		try {
			Common.print("session.isConnected(): " + session.isConnected());
			if (!session.isConnected())
			{
				setupSession();
			}
			channel=session.openChannel("shell"); //only shell  

			OutputStream inputstream_for_the_channel = channel.getOutputStream();
			PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
			InputStream outputstream_from_the_channel = channel.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(outputstream_from_the_channel));
			String line;

			channel.connect();
			Thread.sleep(5000);

			commander.println(command);
			while (true)
			{
				line = br.readLine();
				if (line.contains("Result:exit"))
				{
//					System.out.print(line+"\n");
					result.append(line+"\n");
					//	            	result.append(line.trim());
					break;
				}
				else if (line.contains("error")){
					System.out.print("ERROR ");
					System.out.print(line+"\n");
				}
//				System.out.print(line+"\n");
				result.append(line+"\n");
			}
			commander.println("exit");
			commander.close();
			channel.disconnect();      
			/* Test block for finding initial command and to exclude is from result
	        Pattern p = Pattern.compile("b=exit;.*\\$a\\$b;", Pattern.DOTALL);
	        Matcher m = p.matcher(result.toString());
	        boolean b = m.find();
	        Common.print("match found: " + b);
			 */
			finalResult = Pattern.compile("b=exit;.*\\$a\\$b;", Pattern.DOTALL).matcher(result.toString()).replaceAll("\ninitial command\n");	        
//			Common.print(finalResult);
		} catch (Exception e) { 
			System.err.println("ERROR: Connecting via shell");
			e.printStackTrace();
			Common.print("Result:false - shell command execution failed");		
			finalResult += eol + "Result:false - command failed to execute" + eol + e;
		}
		return finalResult;
	}

	/**
	 * Help method for formatting shell-command with adding anchors to the start and end. This helps in identification of actual execution end.
	 * @param command
	 * @return
	 */
	private String commandFormatter (String command)
	{		
		if (command.contains("exit;"))
		{
			//			command = command.replace("exit;", "a=Result:;b=exit;echo $a$b;");
			command = command.replace("exit;", "echo $a$b;");
		}
		else
			//			command += "a=Result:;b=exit;echo $a$b;";			
			command += "echo $a$b;";
		command = "a=Result:;b=exit;" + command;
		return command;
	}

	public String shellExecutorSilentMode (String command)
	{
		Channel channel;
		String result = "";
		try {
			Common.print("session.isConnected(): " + session.isConnected());
			if (!session.isConnected())
			{
				setupSession();
			}
			channel=session.openChannel("shell");  
			OutputStream inputstream_for_the_channel = channel.getOutputStream();
			PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
			channel.connect();
			Thread.sleep(3000);
			commander.println(command);
			Thread.sleep(10000);
			channel.disconnect();
		} catch (JSchException | IOException | InterruptedException e) {
			Common.print("Result:false - shell command execution failed");		
			e.printStackTrace();
			result = "Result:false - command failed to execute" + eol + e;;
		}
		return result;
	}

	/*	
	public String shellExecutorSilentMode (String command)
	{
		Channel channel;
		String result = "";
		try {
			channel = session.openChannel("exec");
			command = command.substring(0, command.length()-1)+" &";
			((ChannelExec)channel).setCommand(command);
			channel.setInputStream(null);
			InputStream in=channel.getInputStream();
			((ChannelExec)channel).setErrStream(System.err);
			Common.print("shell command: " + command);
			channel.connect();
			Thread.sleep(3000);
			channel.disconnect();
		} catch (JSchException | IOException | InterruptedException e) {
			Common.print("Result:false - shell command execution failed");		
			e.printStackTrace();
			result = "Result:false - command failed to execute" + eol + e;;
		}
		return result;
	}
	 */

	public void closeSession ()
	{
		session.disconnect();
	}
	/*
    public void closeChannel ()
	{
		sftpChannel.exit();
	}	

	 */

	public static  String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		UtilsRemoteHost.username = username;
	}

	public static  String getPassword() {
		return password;
	}

	public  void setPassword(String password) {
		UtilsRemoteHost.password = password;
	}

	public static  String getHostname() {
		return hostname;
	}

	public  void setHostname(String hostname) {
		UtilsRemoteHost.hostname = hostname;
	}
}
