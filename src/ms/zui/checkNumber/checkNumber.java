package ms.zui.checkNumber;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.struts2.ServletActionContext;

import com.google.gson.Gson;



@Path("/checkNumber")
public class checkNumber {
	
	private static Map<String, String> map = new HashMap<String, String>();

	/**
	 * 
	 * @param url 应用地址，类似于http://ip:port/msg/
	 * @param account 账号
	 * @param pswd 密码
	 * @param mobile 手机号码，多个号码使用","分割
	 * @param msg 短信内容
	 * @param needstatus 是否需要状态报告，需要true，不需要false
	 * @return 返回值定义参见HTTP协议文档
	 * @throws Exception
	 */
	private String send(String url, String account, String pswd, String mobile, String msg,
			boolean needstatus, String product, String extno) throws Exception {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod();
		try {
			URI base = new URI(url, false);
			method.setURI(new URI(base, "HttpSendSM", false));
			method.setQueryString(new NameValuePair[] { 
					new NameValuePair("account", account),
					new NameValuePair("pswd", pswd), 
					new NameValuePair("mobile", mobile),
					new NameValuePair("needstatus", String.valueOf(needstatus)), 
					new NameValuePair("msg", msg),
					new NameValuePair("product", product), 
					new NameValuePair("extno", extno), 
				});
			int result = client.executeMethod(method);
			if (result == HttpStatus.SC_OK) {
				InputStream in = method.getResponseBodyAsStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = in.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				return URLDecoder.decode(baos.toString(), "UTF-8");
			} else {
				throw new Exception("HTTP ERROR Status: " + method.getStatusCode() + ":" + method.getStatusText());
			}
		} finally {
			method.releaseConnection();
		}

	}

	/**
	 * 
	 * @param url 应用地址，类似于http://ip:port/msg/
	 * @param account 账号
	 * @param pswd 密码
	 * @param mobile 手机号码，多个号码使用","分割
	 * @param msg 短信内容
	 * @param needstatus 是否需要状态报告，需要true，不需要false
	 * @return 返回值定义参见HTTP协议文档
	 * @throws Exception
	 */
	private String batchSend(String url, String account, String pswd, String mobile, String msg,
			boolean needstatus, String product, String extno) throws Exception {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod();
		try {
			URI base = new URI(url, false);
			method.setURI(new URI(base, "HttpBatchSendSM", false));
			method.setQueryString(new NameValuePair[] { 
					new NameValuePair("account", account),
					new NameValuePair("pswd", pswd), 
					new NameValuePair("mobile", mobile),
					new NameValuePair("needstatus", String.valueOf(needstatus)), 
					new NameValuePair("msg", msg),
					new NameValuePair("product", product),
					new NameValuePair("extno", extno), 
				});
			int result = client.executeMethod(method);
			if (result == HttpStatus.SC_OK) {
				InputStream in = method.getResponseBodyAsStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = in.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				return URLDecoder.decode(baos.toString(), "UTF-8");
			} else {
				throw new Exception("HTTP ERROR Status: " + method.getStatusCode() + ":" + method.getStatusText());
			}
		} finally {
			method.releaseConnection();
		}
	}
	
	
	/**
	 * 根据手机号生成验证码，然后发送短信
	 * @param phoneNumber
	 * @return
	 */
	@GET
    @Path("/send/{phoneNumber}")
    @Produces(MediaType.TEXT_PLAIN)
	public String send(@PathParam("phoneNumber") String phoneNumber){
		//生成随机数
		Random random = new Random();
		String result = "";
		for(int i = 0; i < 6 ; i++){
			result += random.nextInt(10);
		}
		map.put(phoneNumber, result);
		
		//发送短信
		String url = "http://222.73.117.158/msg/";// 应用地址
		String account = "Zms888";// 账号
		String pswd = "Aa123456";// 密码
		String mobile = phoneNumber;// 手机号码，多个号码使用","分割
		String msg = "亲爱的用户，您的验证码是" + map.get(phoneNumber) + "，5分钟内有效。";// 短信内容
		boolean needstatus = true;// 是否需要状态报告，需要true，不需要false
		String product = null;// 产品ID
		String extno = null;// 扩展码

		try {
			String returnString = batchSend(url, account, pswd, mobile, msg, needstatus, product, extno);
			System.out.println(returnString);
			return "success";
			// TODO 处理返回值,参见HTTP协议文档
		} catch (Exception e) {
			// TODO 处理异常
			e.printStackTrace();
			return "fail";
		}
	}
	
	
	/**
	 * 校验验证码
	 * @param phoneNumber
	 * @param checkNumber
	 * @return
	 */
	@GET
    @Path("/check/{phoneNumber}/{checkNumber}")
    @Produces(MediaType.APPLICATION_JSON)
	public String check(@PathParam("phoneNumber")String phoneNumber, @PathParam("checkNumber")String checkNumber, @Context HttpServletResponse response){
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, String> result = new HashMap<String, String>();
		Gson gson = new Gson();
		java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<String, String>>() {  
        }.getType();
		if(map.containsKey(phoneNumber)){
			if(map.get(phoneNumber).equals(checkNumber)){
				map.remove(phoneNumber);
				result.put("result", "success");
				return gson.toJson(result, type);
			}
		}
		result.put("result", "fail");
		return gson.toJson(result, type);
	}
}
