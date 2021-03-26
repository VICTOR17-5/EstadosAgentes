package com.kranon.reportesUNITEC.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.jayway.jsonpath.JsonPath;

public class HttpRequest {
	private static String token = "gMW4Ca12HSiopofJuviRqRUdnVRheyNRhuAYr_pdnfiRnUer0oKxbyIv0cDvRSwbIGRGpAYlJFaqI9S4xo0enQ";
	public static String date_range = "2019-08-12T05:00:00.000Z/2019-08-13T04:59:00.000Z";

	
	public static void loadToken(String client_id, String client_secret) throws Exception {
		String TipoAuth = "?grant_type=client_credentials";
		String encodeData = new String(Base64.encodeBase64((client_id + ":" + client_secret).getBytes("ISO-8859-1")));
		String URL = "https://login.mypurecloud.com/oauth/token" + TipoAuth;
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				String json_response = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				if (status >= 200 && status < 300) {
					return json_response;
				} else {
					if (status == 429) {
						return "429";
					}
					throw new ClientProtocolException("Unexpected response status: " + status + " response:" + json_response);
				}
			}
		};
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(URL);
		StringEntity entity = new StringEntity("", "UTF-8");
		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("authorization", " Basic " + encodeData);
		String response = client.execute(httpPost, responseHandler);
		token = JsonPath.read(response, "$.access_token");
		client.close();
	}

	public static String getPageAgents(Integer Page) throws Exception {
		String URL = "https://api.mypurecloud.com/api/v2/users?pageSize=100&pageNumber=" + Page;
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				String json_response = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				if (status >= 200 && status < 300) {
					return json_response;
				} else {
					if (status == 429 || status == 504) {
						return "429";
					}
					throw new ClientProtocolException("Unexpected response status: " + status + " response:" + json_response);
				}
			}
		};
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpPost = new HttpGet(URL);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("authorization", "Bearer " + token);
		String response = client.execute(httpPost, responseHandler);
		client.close();
		if (response.equals("429")) {
			Thread.sleep(1000);
			return getPageAgents(Page);
		}
		return response;
	}

	public static String getPresences() throws Exception {
		String URL = "https://api.mypurecloud.com/api/v2/presencedefinitions";
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				String json_response = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				if (status >= 200 && status < 300) {
					return json_response;
				} else {
					return "429";
				}
			}
		};

		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(URL);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("Content-type", "application/json");
		httpGet.setHeader("authorization", "Bearer " + token);
		String response = client.execute(httpGet, responseHandler);
		client.close();
		if (response.equals("429")) {
			Thread.sleep(500);
			return getPresences();
		}
		return response;
	}

	public static String getUserActivity(String user) throws Exception {
		Thread.sleep(500);
		String URL = "https://api.mypurecloud.com/api/v2/analytics/users/aggregates/query";
		String body = "{\r\n" + "	\"interval\": \"" + date_range + "\",\r\n" + "	\"filter\": {\r\n"
				+ "		\"type\": \"and\",\r\n" + "		\"clauses\": [],\r\n" + "		\"predicates\": [\r\n"
				+ "			{\r\n" + "				\"dimension\": \"userId\",\r\n" + "				\"value\": \""
				+ user + "\"\r\n" + "			}\r\n" + "		]\r\n" + "	},\r\n" + "	\"metrics\": [\r\n" +
				"\"tOrganizationPresence\"" + "	],\r\n" + "	\"granularity\": \"PT30M\"\r\n" + "}";

		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();

				String json_response = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				if (status >= 200 && status < 300) {
					return json_response;
				} else {
					return "429";
				}
			}
		};

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(URL);
		StringEntity entity = new StringEntity(body, "UTF-8");
		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("authorization", "Bearer " + token);
		String response = client.execute(httpPost, responseHandler);
		client.close();
		if (response.equals("429")) {
			Thread.sleep(30 * 1000);
			return getUserActivity(user);
		}
		return response;
	}

}
