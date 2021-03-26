package com.kranon.reportesUNITEC.util;

import java.io.FileWriter;
import java.io.PrintWriter;

public class Log {
	public static String vsRutaLog = "";
	public static String ArchLog = "";
	public static String vsFecha = "";
	
	
	public synchronized static void GuardaLog(String cadena) {
        ArchLog = "Reporte_" + vsFecha + ".log";
        if (ArchLog != null) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new FileWriter(vsRutaLog + ArchLog, true));
                pw.println(cadena);
                pw.close();
            } catch (Exception e) {
                ArchLog = "Errores.log";
                 System.out.println("Error en la creacion de logs, no se pudo guardar la cadena: " + cadena);
            } finally {
				pw.close();
			}
        }
    }

}
