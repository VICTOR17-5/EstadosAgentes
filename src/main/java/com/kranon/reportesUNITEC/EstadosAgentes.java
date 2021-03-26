package com.kranon.reportesUNITEC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.kranon.reportesUNITEC.util.GenerateExcel;
import com.kranon.reportesUNITEC.util.HttpRequest;
import com.kranon.reportesUNITEC.util.Log;
import com.kranon.reportesUNITEC.util.PoolThreadManager;
import com.kranon.reportesUNITEC.util.ConnectSFTP;
import com.kranon.reportesUNITEC.util.Contador;
import com.kranon.reportesUNITEC.util.Email;
import com.kranon.reportesUNITEC.util.Utilerias;

public class EstadosAgentes implements Job {

	private static Utilerias voUti;
	private static HashMap<String, Object> users = new HashMap<String, Object>();
	private static HashMap<String, Object> header = new HashMap<String, Object>();
	public static List<HashMap<String, Object>> content = new ArrayList<HashMap<String, Object>>();
	private static HashMap<String, String[]> sub_status = new HashMap<String, String[]>();
	private static String day_;
	private static int page = 0;
	private static int list_pages_size = 0;
	private static String id_agent;
	private static int order = 0;
	private static Integer substract = 6;
	private String client_id = null;
	private String client_secret = null;
	private String path_file = null;
	private String vsHorario_verano = null;
	private String vsNombreCSV = "";
	private String vsHoraFechaInicio;
	private String vsHoraFechaFin;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("INICIANDO TAREA PROGRAMADA [" + new Date() + "]");
		String[] confi = {"date","client_id","client_secret","horario_verano","CSVOut","to","cc","Username","Password","Log"};
		voUti = new Utilerias();
		try {
			if (voUti.getProperty(confi,"conf.properties")) {
				try {
					day_= confi[0];
					if(day_.equals("")) {
						Calendar c = Calendar.getInstance();
						c.add(Calendar.DATE, -1);
						day_ = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
					}
					vsNombreCSV = day_ + "_reportesdetalles.csv";
					client_id = confi[1];
					client_secret = confi[2];
					path_file = confi[4] + vsNombreCSV;
					vsHorario_verano = confi[3];
					Log.vsRutaLog = confi[9];
				} catch (Exception e) {
					Log.GuardaLog("[" + new Date() + "][ERROR] Archivo de configuracion mal armado: " + e);
				}
			}
		} catch(Exception e) {
			Log.GuardaLog("[" + new Date() + "][ERROR] ---> " + e);
	    }
		Log.vsFecha = day_;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "ES"));
		Log.GuardaLog("[" + new Date() + "] *********************************** INICIANDO ***********************************");
		
		vsHoraFechaInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		if (client_id == null || client_id.equals("")) client_id = "18b497ab-ef3c-46a5-b6eb-a0bd2ae13ca8";
		if (client_secret == null || client_secret.equals("")) client_secret = "n9YSI653hn-32VjE2XgjHYlT0_wGmV5TcDSEaeysGxo";
		
		try {
			Date date = format.parse(day_);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, 1);
			Date day_after_dt = cal.getTime();
			String day_after = format.format(day_after_dt);
			if (vsHorario_verano != null && !vsHorario_verano.equals("")) {
				if (vsHorario_verano.contentEquals("true")) {
					HttpRequest.date_range = day_ + "T05:00:00.000Z/" + day_after + "T05:00:00.000Z";
					substract = 5;
				} else
					HttpRequest.date_range = day_ + "T06:00:00.000Z/" + day_after + "T06:00:00.000Z";
			} else
				HttpRequest.date_range = day_ + "T06:00:00.000Z/" + day_after + "T06:00:00.000Z";
			
			Log.GuardaLog("[" + new Date() + "][MAIN][INFO] ---> horario_verano=[" + vsHorario_verano + "]");
			Log.GuardaLog("[" + new Date() + "][MAIN][INFO] ---> Horario de verano=[" + HttpRequest.date_range + "]");
		} catch (ParseException e) {
			Log.GuardaLog("[" + new Date() + "][MAIN][ERROR] ---> Bad expression in date, must be yyyy-MM-dd");
			Log.GuardaLog("[" + new Date() + "][MAIN][ERROR] ---> " + e.getMessage());
			System.exit(1);
		}

		header.put("INTERVALO", order++);
		header.put("id", order++);
		header.put("name", order++);
		header.put("email", order++);
		header.put("INICIO_SESION_FECHA", order++);
		header.put("FIN_SESION_FECHA", order++);
		header.put("INICIO_SESION_HORA", order++);
		header.put("FIN_SESION_HORA", order++);
		header.put("OFFLINE", order++);
		header.put("AVAILABLE", order++);
		header.put("ON QUEUE", order++);
		header.put("MEAL", order++);
		header.put("BUSY", order++);
		header.put("SEGUIMIENTO", order++);
		header.put("BREAK", order++);
		header.put("MEETING", order++);
		header.put("SUPERVISOR", order++);
		header.put("CALIDAD", order++);
		header.put("AWAY", order++);
		header.put("AUXILIAR NO PERMITIDO", order++);
		header.put("TRAINING", order++);
		header.put("RETRO SUPERVISOR", order++);
		header.put("CURSO", order++);
		header.put("IDLE", order++);
		header.put("SIN_LOGOFF_DIAACTUAL", order++);
		header.put("SIN_LOGOFF_DIA(S)PREVIO(S)", order++);
		try {
			Log.GuardaLog("[" + new Date() + "][MAIN][INFO] ---> TOKEN SUCCESSFULLY");
			HttpRequest.loadToken(client_id, client_secret);
		} catch (Exception e) {
			Log.GuardaLog("[" + new Date() + "][MAIN][ERROR] ---> NOT CREATE TOKEN [" + e.getMessage() + "]");
			e.printStackTrace();
		}
		getUsers();
		getPresence();
		Map<String, Object> o_users = new TreeMap<String, Object>(users);
		ExecutorService es = Executors.newFixedThreadPool(10);
		List<Runnable> tasks = new ArrayList<Runnable>();
		for (Entry<String, Object> entry : o_users.entrySet()) {
			id_agent = entry.getKey();
			Thread th = new Thread(new Runnable() {
				String id = id_agent;
				public void run() {
					getTimeStatics(id);
				}
			});
			tasks.add(th);
		}

		CompletableFuture<?>[] futures = tasks.stream().map(task -> CompletableFuture.runAsync(task, es)) .toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(futures).join();
		es.shutdown();
		Log.GuardaLog("[" + new Date() + "][MAIN][INFO] ---> TOTAL AGENTS LOADED [" + Contador.viContador + "]");
		File voCSVActiguo = new File(path_file);
		if (voCSVActiguo.delete()) Log.GuardaLog("[" + new Date() + "][MAIN][INFO] ---> EXISTING FILE [" + voCSVActiguo.getName() + "], DELETED SUCCESSFULLY");
		Log.GuardaLog("[" + new Date() + "][MAIN][INFO] ---> WRITING TO CSV FILE IN [" + voCSVActiguo.getParent() + "]");
		GenerateExcel.writeWithColumnAsociationCSV(path_file, header, content);
		vsHoraFechaFin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

		Email voEmail = new Email(confi[7], confi[8], confi[5], confi[6]);
		if(voEmail.UpLoadEmail("[INFORMATIVO][UNITEC] REPORTE ESTADOS DE AGENTE DEL DIA : " + day_)) {
			voEmail.FileUpLoad(confi[9], Log.ArchLog);
			voEmail.FormatHTML("GREEN", "2", "Arial");
			voEmail.StartHTML();
			voEmail.ProcessResult("EJECUCION", vsHoraFechaInicio, vsHoraFechaFin, "NÚMERO AGENTES PROCESADOS = " + Contador.viContador,"");
			voEmail.EndHTML();
			voEmail.UpLoadBodyHTML();
			if(voEmail.SendEmail()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
				}
				ConnectSFTP connect = new ConnectSFTP();
				vsHoraFechaInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				Email voEmailWarning = new Email(confi[7], confi[8], confi[5], confi[6]);
				if(!connect.connect(path_file.replace("xlsx", "csv"))) {
					if(voEmailWarning.UpLoadEmail("[INFORMATIVO][UNITEC] REPORTE ESTADOS DE AGENTE DEL DIA : " + day_)) {
						vsHoraFechaFin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
						voEmailWarning.FormatHTML("RED", "2", "Arial");
						voEmailWarning.StartHTML();
						voEmailWarning.ProcessResult("TRANSMISION SFTP", vsHoraFechaInicio, vsHoraFechaFin, "ARCHIVO TRANSMITIDO = " + vsNombreCSV, ", ESTATUS FINAL = FALLO TRANSMISION DE ARCHIVO");
						voEmailWarning.EndHTML();
					}
				} else {
					try {
						String vsRutaCSV = path_file;
						String vsRutaBackup = confi[4] + "backup/" + vsNombreCSV; 
						File voInitialFile = new File(vsRutaCSV);
						File voFinalFile = new File(vsRutaBackup);
						if(voInitialFile.exists()) 
							Files.copy(Paths.get(voInitialFile.getAbsolutePath()),
									Paths.get(voFinalFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						Log.GuardaLog("[" + new Date() + "][MAIN][ERROR] ---> " + e.getMessage());
					}	
					if(voEmailWarning.UpLoadEmail("[INFORMATIVO][UNITEC] REPORTE ESTADOS DE AGENTE DEL DIA : " + day_)) {
						vsHoraFechaFin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
						voEmailWarning.FormatHTML("GREEN", "2", "Arial");
						voEmailWarning.StartHTML();
						voEmailWarning.ProcessResult("TRANSMISION SFTP", vsHoraFechaInicio, vsHoraFechaFin, "ARCHIVO TRANSMITIDO = " + vsNombreCSV, "ESTATUS FINAL = ARCHIVO TRANSMITIDO EXITOSAMENTE");
						voEmailWarning.EndHTML();
					}
				}
				voEmailWarning.UpLoadBodyHTML();
				voEmailWarning.SendEmail();
			}
		}		
		Log.GuardaLog("[" + new Date() + "][MAIN][INFO] *********************************** TERMINO ***********************************\n");
		System.out.println("TERMINANDO TAREA PROGAMADA [" + new Date() + "]");
	}

	private void getUsers() {
		try {
			PoolThreadManager pool = new PoolThreadManager(250);
			do {
				Thread th = new Thread(new Runnable() {
					int local_page = ++page;

					public void run() {
						try {
							String response_page = HttpRequest.getPageAgents(local_page);
							ObjectMapper mapper = new ObjectMapper();
							Map<String, Object> req_map = mapper.readValue(response_page,new TypeReference<Map<String, Object>>(){});
							if (list_pages_size == 0)
								list_pages_size = (Integer) req_map.get("pageCount") + 1;
							@SuppressWarnings("unchecked")
							List<HashMap<String, Object>> map_pages = (List<HashMap<String, Object>>) req_map.get("entities");
							if (map_pages != null) {
								for (HashMap<String, Object> map_page : map_pages) {
									HashMap<String, Object> agent = new HashMap<String, Object>();
									agent.put("id", map_page.get("id"));
									agent.put("name", map_page.get("name"));
									agent.put("email", map_page.get("email"));
									users.put((String) map_page.get("id"), agent);
								}
								if (map_pages.size() == 0)
									page = list_pages_size;
							}
						} catch (Exception e) {
							Log.GuardaLog("[" + new Date() + "][GET_USERS][ERROR] ---> " + e.getMessage());
							e.printStackTrace();
						}
					}
				});
				pool.addThread(th);
				if (page == 0) {
					pool.run();
				}
				pool.runWhenMaxThreads(10);
			} while (list_pages_size != page);
			pool.run();
		} catch (Exception e) {
			Log.GuardaLog("[" + new Date() + "][GET_USERS][ERROR] ---> " + e.getMessage());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void getTimeStatics(String id_agent) {
		Integer contador = Contador.Increase();
		String vsNombreAgente = "";
		TreeMap<String, HashMap<String, Object>> fix = new TreeMap<String, HashMap<String, Object>>();
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("es", "ES"));
			Date userStart = format.parse(day_ + " 00:00:00");
			for (int i = 0; i < 48; i++) {
				HashMap<String, Object> row = new HashMap<String, Object>();
				String split[] = format.format(userStart).split(" ");
				row.put("id", id_agent);
				HashMap<String, Object> agent = (HashMap<String, Object>) users.get(id_agent);
				row.put("name", agent.get("name"));
				row.put("email", agent.get("email"));
				row.put("INTERVALO", split[1]);
				row.put("OFFLINE", "30");
				row.put("INICIO_SESION_FECHA", day_);
				row.put("FIN_SESION_FECHA", day_);
				fix.put(split[1], row);
				userStart.setTime(userStart.getTime() + 30 * 60000);
			}
		} catch (Exception e) {
			Log.GuardaLog("[" + new Date() + "][GET_TIME_STATICS][ERROR] ---> " + e.getMessage() + ", AGENTE [" + id_agent + "]");
		}

		List<HashMap<String, Object>> rows = new ArrayList<HashMap<String, Object>>();
		boolean offline = true;
		String first_status = "";
		String last_status = "";
		StringBuilder sb = new StringBuilder();
		try {
			String response_page = HttpRequest.getUserActivity(id_agent);
			if (response_page.contentEquals("{}")) {
				Log.GuardaLog("[" + new Date() + "][GET_TIME_STATICS][WARNING] ---> NO ACTIVITY API RESPONSE, AGENTE [" + id_agent + "]");
				return;
			}
			try {
				List<HashMap<String, Object>> list_status = JsonPath.parse(response_page).read("$.results[*].data[*]");
				
				HashMap<String, Object> agentName = (HashMap<String, Object>) users.get(id_agent);
				sb.append("[" + new Date() + "][GET_TIME_STATICS][INFO] ---> AGENTE[" + contador + "], ID[" + id_agent + "], NAME[" + agentName.get("name") + "]\n");
				for (Map<String, Object> interval_m : list_status) {
					HashMap<String, Object> row = new HashMap<String, Object>();
					row.put("id", id_agent);
					HashMap<String, Object> agent = (HashMap<String, Object>) users.get(id_agent);
					vsNombreAgente = String.valueOf(agent.get("name"));
					row.put("name", agent.get("name"));
					row.put("email", agent.get("email"));
					String interval = (String) interval_m.get("interval");
					String[] inters = interval.split("/");
					Date startDate = null;
					String startDate_st = inters[0].replaceFirst("[.][^.]+$", "");
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", new Locale("es", "ES"));
					startDate = format.parse(startDate_st);
					startDate.setTime(startDate.getTime() - 3600 * 1000 * substract);
					startDate_st = format.format(startDate);
					String h1[] = startDate_st.split("T");
					row.put("INTERVALO", h1[1]);
					row.put("INICIO_SESION_FECHA", h1[0]);
					row.put("FIN_SESION_FECHA", h1[0]);
	
					
					for (Map<String, Object> presence : (List<HashMap<String, Object>>) interval_m.get("metrics")) {
						JsonObject j_presence = new Gson().fromJson(new Gson().toJson(presence), JsonObject.class);
						String id = j_presence.get("qualifier").getAsString();
						String presence_st = sub_status.get(id)[0];
						String substatus_st = sub_status.get(id)[1];
						if (!presence_st.contentEquals("OFFLINE"))
							offline = false;
						sb.append("[" + new Date() + "][GET_TIME_STATICS][INFO] --->\t\tINTERVALO[" + h1[1] + "], STATUS[" + presence_st + "]\n");
						Long milis = j_presence.get("stats").getAsJsonObject().get("sum").getAsLong();
						if (first_status.contentEquals("")) {
							first_status = presence_st;
						}
						last_status = presence_st;
						float f_milis = ((float) (milis / 1000) / 60);
						if (!substatus_st.contentEquals(presence_st)) {
							if (row.get(substatus_st) == null) {
								row.put(substatus_st, String.format("%.02f", f_milis));
							} else {
								float mins = Float.parseFloat((String) row.get(substatus_st));
								row.put(substatus_st, String.format("%.02f", f_milis + mins));
							}
						}

						if (row.get(presence_st) == null) {
							row.put(presence_st, String.format("%.02f", f_milis));
						} else {
							float mins = Float.parseFloat((String) row.get(presence_st));
							row.put(presence_st, String.format("%.02f", f_milis + mins));
						}
					}
					row.put("INICIO_SESION_HORA", "-");
					row.put("FIN_SESION_HORA", "-");
					fix.put(h1[1], row);
				}
			} catch (Exception e) {
				Log.GuardaLog("[" + new Date() + "][GET_TIME_STATICS][ERROR] ---> " + e.getMessage() + ", AGENTE [" + id_agent + "]");
				e.printStackTrace();
			}

		} catch (Exception e) {
			Log.GuardaLog("[" + new Date() + "][GET_TIME_STATICS][ERROR] ---> " + e.getMessage() + ", AGENTE [" + id_agent + "]");
			e.printStackTrace();
		}

		if (!offline) {
			Log.GuardaLog(sb.toString().substring(0,sb.length()-1));
			for (Entry<String, HashMap<String, Object>> entry : fix.entrySet()) {
				HashMap<String, Object> value = entry.getValue();
				rows.add(value);
			}
			if (rows.get(0).get("OFFLINE") != null)
				first_status = "OFFLINE";

			if (rows.get(rows.size() - 1).get("OFFLINE") != null)
				last_status = "OFFLINE";
			if (!first_status.contentEquals("OFFLINE"))
				rows.get(0).put("SIN_LOGOFF_DIA(S)PREVIO(S)", "1");

			if (!last_status.contentEquals("OFFLINE"))
				rows.get(rows.size() - 1).put("SIN_LOGOFF_DIAACTUAL", "1");

			String[] sum_cols = { "OFFLINE", "AVAILABLE", "ON QUEUE", "MEAL", "BUSY", "BREAK", "MEETING", "AWAY", "TRAINING", "IDLE" };
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("es", "ES"));
			Date startDate = null;
			try {
				startDate = format.parse(day_ + " 00:00:00");
			} catch (ParseException e1) {
				Log.GuardaLog("[" + new Date() + "][GET_TIME_STATICS][ERROR] ---> " + e1.getMessage() + ", AGENTE [" + id_agent + "]");
				e1.printStackTrace();
			}
			for (int i = 0; i < rows.size(); i++) {
				String startDate_st = format.format(startDate);
				String h1[] = startDate_st.split(" ");
				rows.get(i).put("INICIO_SESION_HORA", h1[1]);
				startDate.setTime(startDate.getTime() + (30 * 60000));
				startDate_st = format.format(startDate);
				h1 = startDate_st.split(" ");
				rows.get(i).put("FIN_SESION_HORA", h1[1]);
				Float sum = (float) 0.0;
				for (String status : sum_cols) {
					try {
						String num = (String) rows.get(i).get(status);
						if (num != null) {
							Float num_f = Float.parseFloat(num);
							sum += num_f;
						}
					} catch (Exception e) {
						Log.GuardaLog("[" + new Date() + "][GET_TIME_STATICS][ERROR] ---> " + e.getMessage() + ", AGENTE [" + id_agent + "]");
					}
				}
				if (sum < 29.5) {
					Float restante = 30 - sum;
					Float total_off = (float) 0.0;
					try {
						total_off = Float.parseFloat((String) rows.get(i).get("OFFLINE"));
					} catch (Exception e) {
						Log.GuardaLog("[" + new Date() + "][GET_TIME_STATICS][ERROR] ---> " + e.getMessage() + ", AGENTE [" + id_agent + "]");
					}
					rows.get(i).put("OFFLINE", String.format("%.02f", total_off + restante));
				}
			}
			content.addAll(rows);
		} else {
			Log.GuardaLog("[" + new Date() + "][GET_TIME_STATICS][INFO] ---> AGENTE[" + contador + "], ID[" + id_agent + "], NOMBRE[" + vsNombreAgente + "] OFFLINE ALL DAY");
		}
		sb = null;
	}

	@SuppressWarnings("unchecked")
	private void getPresence() {
		try {
			String presences = HttpRequest.getPresences();
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> req_map = mapper.readValue(presences, new TypeReference<Map<String, Object>>() {});
			List<HashMap<String, Object>> map_pages = (List<HashMap<String, Object>>) req_map.get("entities");
			if (map_pages != null) {
				for (HashMap<String, Object> map_page : map_pages) {
					String status = "";
					HashMap<String, Object> llabs = (HashMap<String, Object>) map_page.get("languageLabels");
					if (llabs.get("es") != null) {
						status = (String) llabs.get("es");
					} else if (llabs.get("en") != null) {
						status = (String) llabs.get("en");
					} else if (llabs.get("en_US") != null) {
						status = (String) llabs.get("en_US");
					}
					String s_p = (String) map_page.get("systemPresence");
					sub_status.put((String) map_page.get("id"),new String[] { s_p.toUpperCase(), status.toUpperCase() });
				}
			}
		} catch (Exception e) {
			Log.GuardaLog("[" + new Date() + "][GET_PRESENCE][ERROR] ---> " + e.getMessage());
			e.printStackTrace();
		}
	}
}
