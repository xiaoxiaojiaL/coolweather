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
import android.content.Intent;
import android.content.IntentSender.OnFinished;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	 * 省列表
	 */

	private List<Province> provinceList;

	/*
	 * 
	 * 市列表
	 */

	private List<City> cityList;

	/*
	 * 
	 * 省列表
	 */

	private List<County> countyList;
	/*
	 * 选中的省份
	 */
	private Province selectedProvince;
	/*
	 * 选中的城市
	 */

	private City selectedCity;

	/*
	 * 当前选中的级别
	 */

	private int currentLeve1;
	
	/*
	 * 是否从WeatherActivity中跳转过来。
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		//已经选择了城市且不是从WeatherActivity跳转过来，才会直接跳转到WeatherActivity
		if(prefs.getBoolean("city_selected", false)&& !isFromWeatherActivity){
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
			}
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
			 }else if(currentLeve1==LEVEL_COUNTY){
				 String countyCode=countyList.get(arg2).getCountyCode();
				
				 Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
				 
				 intent.putExtra("county_code", countyCode);
				 startActivity(intent);
				 finish();
			 }
			
				
			}
			
		
		});
		queryProvinces();//加载省级数据
	}
		
		/*查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
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
				titleText.setText("中国");
				currentLeve1=LEVEL_PROVINCE;
			}else{
				queryFromServer(null,"province");
			}
		}
		
		/*查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
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
		 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
		 * */
	private void queryCounties(){
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			//设置选中0
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLeve1=LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	/*
	 * 根据传入的代号和类型从服务器上查询省市县数据
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
					//通过runOnUiThread()方法回到主线处理逻辑
					
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
				//通过runOnUiThread()方法主线程处理逻辑
				runOnUiThread(new Runnable(){
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
		
		}
	
	
	/*
	 * 显示进度对话框
	 * */
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载。。。");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*
	 * 关闭进度对话框
	 * */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/*
	 * 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。
	 * */
	
	@Override
	public void onBackPressed() {
	if(currentLeve1==LEVEL_COUNTY){
		queryCities();
	}else if(currentLeve1==LEVEL_CITY){
		queryProvinces();
	}else{
		if(isFromWeatherActivity){
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
		}
		finish();
	}
	}

}
