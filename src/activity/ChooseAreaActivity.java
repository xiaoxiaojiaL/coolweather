package activity;

import java.util.ArrayList;
import java.util.List;


import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import com.coolweather.app.R;

import db.CoolWeatherDB;

import model.City;
import model.County;
import model.Province;

import android.app.Activity;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.content.IntentSender.OnFinished;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private ListView listView;
	private TextView titleText;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;

	private List<String> dataList = new ArrayList<String>();
	/*
	 * ʡ�б�
	 */

	private List<Province> provinceList;

	/*
	 * 
	 * ���б�
	 */

	private List<City> cityList;

	/*
	 * 
	 * ʡ�б�
	 */

	private List<County> countyList;
	/*
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/*
	 * ѡ�еĳ���
	 */

	private City selectedCity;

	/*
	 * ��ǰѡ�еļ���
	 */

	private int currentLeve1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
			 if(currentLeve1==LEVEL_PROVINCE){
				 selectedProvince=provinceList.get(arg2);
				 queryCities();
			 }else if(currentLeve1==LEVEL_CITY){
				 selectedCity=cityList.get(arg2);
				 queryCounties();
			 }
			
				
			}
			
		
		});
		queryProvinces();//����ʡ������
	}
		
		/*��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
		 * */
		
		
		private void queryProvinces(){
			provinceList=coolWeatherDB.loadProvinces();
			if(provinceList.size()>0){
				dataList.clear();
				for(Province province:provinceList){
					dataList.add(province.getProvinceName());
				}
				
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText("�й�");
				currentLeve1=LEVEL_PROVINCE;
			}else{
				queryFromServer(null,"province");
			}
		}
		
		/*��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
		 * */
		
		
		private void  queryCities(){
			cityList=coolWeatherDB.loadCities(selectedProvince.getId());
			if(cityList.size()>0){
				dataList.clear();
				for(City city:cityList){
					dataList.add(city.getCityName());
					
				}
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText(selectedProvince.getProvinceName());
				currentLeve1=LEVEL_CITY;
			}else{
				queryFromServer(selectedProvince.getProvinceCode(),"city");
			}
		}
		/*
		 * ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
		 * */
	private void queryCounties(){
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			//����ѡ��0
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLeve1=LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	/*
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ��������
	 * */
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
			
		}else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			@Override
			public void onFinish(String response){
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){
					//ͨ��runOnUiThread()�����ص����ߴ����߼�
					
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						closeProgressDialog();
						if("province".equals(type)){
							 queryProvinces();
						}else if("city".equals(type)){
							queryCities();
						}else if("county".equals(type)){
							queryCounties();
						}
					}
				});
				}
			}
			@Override
			public void onError(Exception e){
				//ͨ��runOnUiThread()�������̴߳����߼�
				runOnUiThread(new Runnable(){
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
		
		}
	
	
	/*
	 * ��ʾ���ȶԻ���
	 * */
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("���ڼ��ء�����");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*
	 * �رս��ȶԻ���
	 * */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/*
	 * ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б���ʡ�б�������ֱ���˳���
	 * */
	
	@Override
	public void onBackPressed() {
	if(currentLeve1==LEVEL_COUNTY){
		queryCities();
	}else if(currentLeve1==LEVEL_CITY){
		queryProvinces();
	}else{
		finish();
	}
	}

}