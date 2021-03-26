package com.kranon.reportesUNITEC.util;

import java.util.ArrayList;
import java.util.List;

public class PoolThreadManager {
	private List<Thread> th;
	private long wait_between_tread;
	
	public PoolThreadManager(List<Thread> th,long wait_between_thread) {
		this.th=th;
		this.wait_between_tread=wait_between_thread;
	}
	public PoolThreadManager(long wait_between_thread) {
		this.th=new ArrayList<Thread>();
		this.wait_between_tread=wait_between_thread;
	}
	public void addThread(Thread t) {
		th.add(t);
	}
	public void runWhenMaxThreads(int size) {
		if(th.size()>=size) {
			this.run();
		}
		
	}
	
	public void run() {
		try {
			for(Thread a_th:th) {
				Thread.sleep(wait_between_tread);
				a_th.start();
			}
		
			for(Thread a_th:th) {
				a_th.join();
			}
			
			this.flush();
		}catch(Exception e) {
		}
	}
	

	public void flush() {
		th.clear();
	}

}