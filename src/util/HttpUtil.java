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
		//������Ҫ��ȡ��HttpURLConnection��ʵ����һ��ֻ��Ҫnew��һ��URL���󣬲�����Ŀ��������ַ��Ȼ���ڵ���һ��openConnection()�������ɡ�
	       HttpURLConnection connection=null;
	       try{
	    	   URL url=new URL(address);
	    	   connection=(HttpURLConnection)url.openConnection();
	    	   /*�õ���HttpURLConnection��ʵ��֮��
	    	    * ����һ��HTTP��������ʹ�õķ��������õķ�����Ҫ��������GET��POST��GET��ʾϣ���ӷ����������ȡ���ݣ���POST��ʾϣ���ύ���ݸ�������
	    	    */
	    	   connection.setRequestMethod("GET");
	    	   /*
	    	    * ���ɶ��ƣ������������ӳ�ʱ����ȡ��ʱ�ĺ�����
	    	    * */
	    	   connection.setConnectTimeout(8000);
	    	   connection.setReadTimeout(8000);
	    	   //֮�����getInputStream()�����Ϳ��Ի�ȡ�����������ص��������ˣ�ʣ�µ�������Ƕ����������ж�ȡ
	    	  InputStream in=connection.getInputStream();
	    	  BufferedReader reader=new BufferedReader(new InputStreamReader(in));
	    	  StringBuilder response=new StringBuilder();
	    	  String line;
	    	  while((line=reader.readLine())!=null){
	    		  response.append(line);
	    	  }
	    	  if(listener!=null)
	    	  {
	    		  //�ص�onFinish()����
	    		  listener.onFinish(response.toString());
	    	  }
	    	   
	       }catch (Exception e){
	    	   if(listener!=null)
	    	   {
	    		   //�ص�onError()����
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
