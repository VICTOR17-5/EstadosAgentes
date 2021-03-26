package com.kranon.reportesUNITEC.util;

public class Contador {
	public static Integer viContador = 0;
	
	public synchronized static Integer Increase() {
		viContador++;
		return viContador;
	}
	
	
}
