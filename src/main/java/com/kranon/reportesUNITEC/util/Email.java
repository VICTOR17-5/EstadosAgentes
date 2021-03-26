package com.kranon.reportesUNITEC.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {
    
    private String vsUsername;
    private String vsPassword;
    private String vsDestinatario;
    private String vsCopia;
    private Session session;
    private MimeMessage msg;
    private MimeMultipart multiParte;
    
    private String vsMessageHTML = "";
    private String vsColorLineaHTML = "GREEN";
    private String vsTamLineaHTML = "2";
    private String vsTipoLetraLineaHTML = "Arial";
    
    
    public Email(String vsUser, String vsPassword, String vsDestinatario, String vsCC) {
    	this.vsUsername = vsUser;
    	this.vsPassword = vsPassword;
    	this.vsDestinatario = vsDestinatario;
    	this.vsCopia = vsCC;
    	session = null;
    	msg = null;
    	multiParte = null;
    }
    
    public boolean UpLoadEmail(String vsSubject) {
    	Log.GuardaLog("[" + new Date() + "][UpLoadEmail][INFO] ---> INITIATING EMAIL SEND");
    	Properties props = System.getProperties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.user", vsUsername);
        props.put("mail.smtp.clave", vsPassword);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");
        try {
		    Log.GuardaLog("[" + new Date() + "][UpLoadEmail][INFO] ---> FROM[" + vsUsername + "]");
		    session = Session.getDefaultInstance(props);
		    msg = new MimeMessage(session);
		    multiParte = new MimeMultipart();
		    msg.setFrom(new InternetAddress(vsUsername));
		    Log.GuardaLog("[" + new Date() + "][UpLoadEmail][INFO] ---> TO[" + vsDestinatario + "], CC[" + vsCopia + "]");
		    msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(vsDestinatario));
		    if(!vsCopia.equals("")) msg.addRecipients(Message.RecipientType.CC, vsCopia);
		    Log.GuardaLog("[" + new Date() + "][UpLoadEmail][INFO] ---> EMAIL SUBJECT [" + vsSubject + "]");
            msg.setSubject(vsSubject);
            return true;
        } catch (MessagingException e) {
        	Log.GuardaLog("[" + new Date() + "][UpLoadEmail][ERROR] ---> " + e);
        	return false;
        }
    }
    
    public void StartHTML() {
		vsMessageHTML = "<HTML><HEAD></HEAD><BODY>\n";
    }
    
    public void FormatHTML(String vsColor, String vsTam, String vsTipoLetra) {
    	this.vsColorLineaHTML = vsColor;
    	this.vsTamLineaHTML = vsTam;
    	this.vsTipoLetraLineaHTML = vsTipoLetra;
    	
    }

    public void AddParagraphHTML(String vsBody) {
    	String[] vaLineasBody = vsBody.split("\n");
    	vsMessageHTML += "<p>";
    	for(String vsLinea : vaLineasBody)
    		vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">" + vsLinea + "</font>\n";
    	
    	vsMessageHTML += "</p>";
    }
    
    public void EndHTML() {
    	vsMessageHTML += "</BODY></HTML>";
    }
    
    public boolean UpLoadBodyHTML() {
        try {
        	BodyPart voHTML = new MimeBodyPart();
			voHTML.setContent(vsMessageHTML, "text/html");
			multiParte.addBodyPart(voHTML);
	        msg.setContent(multiParte);
	        Log.GuardaLog("[" + new Date() + "][UpLoadBodyHTML][INFO] ---> ADD BODY TO EMAIL");
	        return true;
		} catch (MessagingException e) {
			Log.GuardaLog("[" + new Date() + "][UpLoadBodyHTML][ERROR] ---> " + e);
			return false;
		}
    }
    
    public void ProcessResult(String vsProcess, String vsFHInicio, String vsFHFin, String vsTipo, String vsSFTP) {
    	if(!vsProcess.equals("EJECUCION")) {
        	vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">RESULTADO PROCESO " + vsProcess + ": </font><br>\n";
        	vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">FECHA/HORA INICIO = " + vsFHInicio + "</font><br>\n";
        	vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">FECHA/HORA FIN = " + vsFHFin + "</font><br>\n";
        	vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">" + vsTipo + "</font><br>\n";
    		vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">" + vsSFTP + "</font>\n";
    		return;
    	}
    	BufferedReader b;
    	try {
	    	b = new BufferedReader(new FileReader(Log.vsRutaLog + Log.ArchLog));
	    	String vsCadena, vsError = "";
	    	Boolean vbError = false;
	    	while((vsCadena = b.readLine())!=null) {
	    		if(vsCadena.contains("ERROR")) {
	    			vsError += vsCadena + "\n";
	    			vbError = true;
	    			vsColorLineaHTML = "RED";
	    		}
	    	}
	    	vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">RESULTADO PROCESO " + vsProcess + ": </font><br>\n";
	    	vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">FECHA/HORA INICIO = " + vsFHInicio + "</font><br>\n";
	    	vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">FECHA/HORA FIN = " + vsFHFin + "</font><br>\n";
	    	vsMessageHTML += "<font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">" + vsTipo + "</font>\n";
	    	if(!vbError)
	        	vsMessageHTML += "<br><font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">ESTATUS FINAL = NO SE ENCONTRARON ERRORES DE EJECUCION</font>\n";
	    	else {
	    		vsMessageHTML += "<br><font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">ESTATUS FINAL = </font>\n";
	    		String[] vaSplitsLogs = vsError.split("\n");
	    		for(String vsLinea : vaSplitsLogs) 
	    			vsMessageHTML += "<br><font COLOR=\"" + vsColorLineaHTML + "\" SIZE=" + vsTamLineaHTML + " FACE=\"" + vsTipoLetraLineaHTML + "\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + vsLinea + "</font>\n";
	    	}
    	} catch(IOException ex) {
    		Log.GuardaLog("[" + new Date() + "][ProcessResult][ERROR] ---> " + ex);
    	}
    }

    public boolean TextFileUpLoad(String vsRutaArchivo) {
    	String vsTextHTML = "<HTML><HEAD></HEAD><BODY>\n";
    	String vsCadena;
        BufferedReader b;
		try {
			b = new BufferedReader(new FileReader(vsRutaArchivo));
			while((vsCadena = b.readLine())!=null) {
				vsTextHTML += "<font>" + vsCadena + "</font><br>\n";
			}
	        b.close();
	        vsTextHTML += "</BODY></HTML>";
	        BodyPart voHTML = new MimeBodyPart();
			voHTML.setContent(vsTextHTML, "text/html");
			multiParte.addBodyPart(voHTML);
	        msg.setContent(multiParte);
	        Log.GuardaLog("[" + new Date() + "][UpLoadFile][INFO] ---> ADD FILE [" + vsRutaArchivo + "] TO EMAIL");
	        return true;
		} catch (IOException e) {
			Log.GuardaLog("[" + new Date() + "][UpLoadFile][ERROR] ---> " + e);
			return false;
		} catch (MessagingException e) {
			Log.GuardaLog("[" + new Date() + "][UpLoadFile][ERROR] ---> " + e);
			return false;
		}
    }
    
    public boolean FileUpLoad(String vsRuta, String vsNameCSV) {
    	String splited[]= vsNameCSV.split("/");
        vsNameCSV=splited[splited.length-1];
        BodyPart adjunto = null;
        try {
        	adjunto = new MimeBodyPart();
			adjunto.setDataHandler(new DataHandler(new FileDataSource(vsRuta + vsNameCSV)));
			adjunto.setFileName(vsNameCSV);
	        multiParte.addBodyPart(adjunto);
            msg.setContent(multiParte);
            Log.GuardaLog("[" + new Date() + "][FileUpLoad][INFO] ---> FILE [" + vsNameCSV + "] UPLOAD SUCCESSFULLY");
	    	return true;
		} catch (MessagingException e) {
			Log.GuardaLog("[" + new Date() + "][FileUpLoad][ERROR] ---> " + e);
		}
    	return false;
    }
    
    public boolean SendEmail() {
        try {
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", vsUsername, vsPassword);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
            Log.GuardaLog("[" + new Date() + "][SendEmail][INFO] ---> EMAIL SEND");
            return true;
        } catch (MessagingException e) {
        	Log.GuardaLog("[" + new Date() + "][SendEmail][ERROR] ---> " + e);
        	return false;
        }
    }
}
