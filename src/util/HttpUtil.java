package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
	public  static void sendHttpRequest(final String address,final HttpCallbackListener listener){
	
new Thread(new Runnable(){
	@Override
	public void run() {
		//首先需要获取到HttpURLConnection的实例，一般只需要new出一个URL对象，并传入目标的网络地址，然后在调用一下openConnection()方法即可。
	       HttpURLConnection connection=null;
	       try{
	    	   URL url=new URL(address);
	    	   connection=(HttpURLConnection)url.openConnection();
	    	   /*得到了HttpURLConnection的实例之后，
	    	    * 设置一下HTTP的请求所使用的方法。常用的方法主要有两个，GET和POST。GET表示希望从服务器那里获取数据，而POST表示希望提交数据给服务器
	    	    */
	    	   connection.setRequestMethod("GET");
	    	   /*
	    	    * 自由定制，比如设置连接超时、读取超时的毫秒数
	    	    * */
	    	   connection.setConnectTimeout(8000);
	    	   connection.setReadTimeout(8000);
	    	   //之后调用getInputStream()方法就可以获取到服务器返回的输入流了，剩下的任务就是对输入流进行读取
	    	  InputStream in=connection.getInputStream();
	    	  BufferedReader reader=new BufferedReader(new InputStreamReader(in));
	    	  StringBuilder response=new StringBuilder();
	    	  String line;
	    	  while((line=reader.readLine())!=null){
	    		  response.append(line);
	    	  }
	    	  if(listener!=null)
	    	  {
	    		  //回调onFinish()方法
	    		  listener.onFinish(response.toString());
	    	  }
	    	   
	       }catch (Exception e){
	    	   if(listener!=null)
	    	   {
	    		   //回调onError()方法
	    		   listener.onError(e);
	    	   }
	       }finally{
	    	   if(connection!=null){
	    		   connection.disconnect();
	    	   }
	       }
	       
			}
		
	
	
}).start();

	}
}
