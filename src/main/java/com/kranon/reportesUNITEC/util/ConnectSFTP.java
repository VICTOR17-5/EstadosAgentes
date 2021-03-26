package com.kranon.reportesUNITEC.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class ConnectSFTP {
	private String vsUser = "guestuser";
	private String vsPwd = "Kranon2020#";
	private String vsIP = "kranondemos.com";
	private int viPort = 22;
	private String vsPathReports = "/incoming/";
	private String[] confi = {"user","password","ip","port","path"};
	private Utilerias voUti;
	
	public ConnectSFTP() {
		voUti = new Utilerias();
		try {
			if (voUti.getProperty(confi,"connectSFTP.properties")) {
				vsUser = confi[0];
				vsPwd = confi[1];
				vsIP = confi[2];
				viPort = Integer.parseInt(confi[3]);
				vsPathReports = confi[4];
			}
		} catch(Exception e) {
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][ERROR] ---> " + e);
	    }
	}
	
	public boolean connect(String vsRutaCSV) {
		if(vsUser.equals("")) {
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][ERROR] ---> USER INVALID");
			return false;
		}
		if(vsPwd.equals("")) {
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][ERROR] ---> PASSWORD INVALID");
			return false;
		}
		if(vsIP.equals("")) {
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][ERROR] ---> IP INVALID");
			return false;
		}
		if(viPort == 0) {
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][ERROR] ---> PORT INVALID");
			return false;
		}
		String[] splited = vsRutaCSV.split("/");
		String vsNameCSV = splited[splited.length-1];
		JSch voJSCH = new JSch();
		try {
			Session session = voJSCH.getSession(vsUser, vsIP, viPort);
			session.setConfig("PreferredAuthentications", "password");
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(vsPwd);
			session.connect();
			Channel channel = session.openChannel("sftp");
			ChannelSftp sftp = (ChannelSftp) channel;
			sftp.connect();
			sftp.put(vsRutaCSV, vsPathReports + vsNameCSV);
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][INFO] ---> UPLOADED FILE");
			
			@SuppressWarnings("unchecked")
			List<LsEntry> vlFiles = new ArrayList<>((Vector<LsEntry>) sftp.ls(vsPathReports));
			vlFiles.sort(Comparator.comparing((LsEntry veFile) -> veFile.getAttrs().getMTime()).thenComparing(LsEntry::getFilename));
			Thread.sleep(1000);
			
			Map<String,String> vmMapFiles = new HashMap<>();
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][INFO] ---> CHECKING FILE");
			for(LsEntry voEntry : vlFiles){
				String vsFileName = voEntry.getFilename();
				if(vsFileName.equals(".") || vsFileName.equals("..")) continue;
				else
					if(vmMapFiles.containsKey(vsFileName)) continue;
					else vmMapFiles.put(vsFileName, voEntry.getLongname());
				vmMapFiles.put(voEntry.getFilename(), voEntry.getLongname());
			}
			
			if(vmMapFiles.containsKey(vsNameCSV)){
				Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][INFO] ---> FILE UPLOADED SUCCESSFULLY");
				return true;
			}
			
			sftp.disconnect();
			channel.disconnect();
			session.disconnect();
			
		} catch (JSchException e) {
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][ERROR] ---> " + e.getMessage());
		}catch (SftpException e) {
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][ERROR] ---> " + e.getMessage());
		}catch(Exception e){
			Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][ERROR] ---> " + e.getMessage());
		}
		Log.GuardaLog("[" + new Date() + "][CONNECT_SFTP][INFO] ---> FILE NOT LOADED");
		return false;
	}

}
