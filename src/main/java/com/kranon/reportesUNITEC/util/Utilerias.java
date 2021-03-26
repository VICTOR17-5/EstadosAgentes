package com.kranon.reportesUNITEC.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Utilerias {
    
    public Utilerias() {
    }
    
    public boolean getProperty(String[] confi, String Archivo) {
    	String Ruta = "C:\\Appl\\UNITEC\\EstadosAgentes\\Configuraciones\\";
    	try {
            Properties p = new Properties();
            p.load(new FileReader(Ruta + Archivo));
            for (int i = 0; i < confi.length; i++) {
                String cadena = confi[i];
                confi[i] = p.getProperty(cadena);
                if (confi[i] == null) {
                    confi[i] = "";
                } else {
                    confi[i] = confi[i].trim();
                }
            }
            return true;
        } catch (Exception e) {
        	System.out.println("[" + new Date() + "] ERROR LEYENDO " + Ruta + Archivo);
        	Log.GuardaLog("[" + new Date() + "] ERROR LEYENDO " + Ruta + Archivo);
            return false;
        }
    }

    public Map<String, List<String>> GetFileContent(String vsPathFile, List<String> vlLogger) {
        Map<String, List<String>> vmEstadoAgente = new HashMap<>();
        List<String> vlEstados = new ArrayList<>();
        vlLogger.add(new Date() + " : GetFileContent()-->Inicio, Archivo Fuente=[" + vsPathFile + "].");
        BufferedReader b = null;
        int viLine = 0;
        try {
            FileReader f = new FileReader(vsPathFile);
            b = new BufferedReader(f);
            String vsCadena;
            while ((vsCadena = b.readLine()) != null) {
                viLine++;
                if (viLine == 1) {
                    continue;
                }
                vsCadena = vsCadena.trim();
                if (vsCadena.length() > 0) {
                    if (vsCadena.contains(",")) {
                        String[] vaValores = vsCadena.split(",");
                        String vsKey = vaValores[2];
                        if (vaValores.length == 24 || vaValores.length == 25 || vaValores.length == 26) {
                            if (vmEstadoAgente.containsKey(vsKey)) {
                                List<String> vlEstadoTemp = vmEstadoAgente.get(vsKey);
                                vlEstadoTemp.add(vsCadena);
                                vmEstadoAgente.put(vsKey, vlEstadoTemp);
                            } else {
                                vlEstados = new ArrayList<>();
                                vlEstados.add(vsCadena);
                                vmEstadoAgente.put(vsKey, vlEstados);
                            }
                        } else {
                            vlLogger.add(new Date() + " : GetFileContent()-->ERROR Linea[" + viLine + "]=Sin Longitud Correcta=[" + vaValores.length + "], Linea=[" + vsCadena + "].");
                        }
                    } else {
                        vlLogger.add(new Date() + " : GetFileContent()-->ERROR Linea[" + viLine + "]=Sin Comas=[" + vsCadena + "].");
                    }
                } else {
                    vlLogger.add(new Date() + " : GetFileContent()-->ERROR Linea[" + viLine + "]=Sin Información=[" + vsCadena + "].");
                }
            }
        } catch (IOException e) {
            vlLogger.add(new Date() + " : GetFileContent()-->ERROR IOException=[" + e.getMessage() + "].");
        }
        vlLogger.add(new Date() + " : GetFileContent()-->Fin, Lineas Leidas=[" + viLine + "], Mapa Retornado=[" + vmEstadoAgente.size() + "].");

        return vmEstadoAgente;
    }
}
