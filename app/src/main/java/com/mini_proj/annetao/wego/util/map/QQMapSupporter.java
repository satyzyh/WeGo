package com.mini_proj.annetao.wego.util.map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.UiSettings;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 16/7/8.
 */
public class QQMapSupporter implements TencentLocationListener,TencentMap.OnMapLoadedCallback,TencentMap.OnMapClickListener {


    public static final String QQ_MAP_TYPE_LOCATION = "qq.map.type.location";
    public static final String QQ_MAP_TYPE_EXERCISES = "qq.map.type.execises";
    public static final String QQ_MAP_TYPE_ONE_EXERCISE = "qq.map.type.one.execise";
    public static final int COMPONENT_ID_MY_LOCATION_BUTTON = 1;
    private List<Marker> markerList;
    private Marker nowMarker;
    private LatLng nowMarkerLatLng;
    private String mapType;
    private MapView mapView;
    private TencentMap tencentMap;
    private Activity activity;
    private DemoLocationSource locationSource;
    Handler componentEnabledHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QQMapSupporter.COMPONENT_ID_MY_LOCATION_BUTTON:
                    mapView.getMap().getUiSettings().setMyLocationButtonEnabled(true);
                    break;

            }

            super.handleMessage(msg);
            if(QQ_MAP_TYPE_LOCATION == mapType) {
                initialLocationMap();
            }
            else if(QQ_MAP_TYPE_EXERCISES == mapType){
                initialExercisesMap();
            }
            else if(QQ_MAP_TYPE_ONE_EXERCISE == mapType){
                initialOneExerciseMap();
            }
        }
    };

    public QQMapSupporter(Activity activity, MapView mapView, String type) {
        this.activity = activity;
        this.mapView = mapView;
        this.mapType = type;
        this.tencentMap = mapView.getMap();
        markerList = new ArrayList<>();
    }

    public void initialMapView(){
        tencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);
        UiSettings mapUiSettings = tencentMap.getUiSettings();
        mapUiSettings.setZoomControlsEnabled(false);
        mapUiSettings.setCompassEnabled(false);

        tencentMap.setOnMapLoadedCallback(this);
        tencentMap.setOnMapClickListener(this);

        locationSource = new DemoLocationSource(activity);
        mapView.getMap().setMyLocationEnabled(true);
        tencentMap.setLocationSource(locationSource);
        nowMarker = null;



    }
    public void initialLocationMap(){
        moveCameraToNow();
    }
    public void initialExercisesMap(){
        setMarker();
        moveCameraByMarkers();
    }
    public void initialOneExerciseMap(){

    }

    @Override
    public void onMapLoaded() {
        Message msg = new Message();
        msg.what = QQMapSupporter.COMPONENT_ID_MY_LOCATION_BUTTON;
        componentEnabledHandler.sendMessage(msg);


    }

    public void setMarker(){
        final Marker beiJingMarker = tencentMap.addMarker(new MarkerOptions().
                position(new LatLng(39.906901,116.397972)).//先纬度后经度
                icon(BitmapDescriptorFactory.defaultMarker()).
                title("北京").
                snippet("DefaultMarker"));
        final Marker shangHaiMarker = tencentMap.addMarker(new MarkerOptions().
                position(new LatLng(31.247241,121.492696)).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).
                title("上海").
                snippet("Click infowindow to change icon"));
        final Marker jiNanMarker = tencentMap.addMarker(new MarkerOptions().
                position(new LatLng(36.666574,117.028908)).
                icon(BitmapDescriptorFactory.defaultMarker()).
                title("济南").
                snippet("icon from assets").
                anchor(0.5f, 1f));
        markerList.add(beiJingMarker);
        markerList.add(shangHaiMarker);
        markerList.add(jiNanMarker);

    }
    public void moveCameraByMarkers(){

        double minLongitude = markerList.get(0).getPosition().longitude;
        double maxLongitude = markerList.get(0).getPosition().longitude;
        double minLatitude = markerList.get(0).getPosition().latitude;
        double maxLatitude = markerList.get(0).getPosition().latitude;

        for (Marker marker:markerList) {
            if(marker.getPosition().longitude<minLongitude) minLongitude = marker.getPosition().longitude;
            if(marker.getPosition().longitude>maxLongitude) maxLongitude = marker.getPosition().longitude;
            if(marker.getPosition().latitude<minLatitude) minLatitude = marker.getPosition().latitude;
            if(marker.getPosition().latitude>maxLatitude) maxLatitude = marker.getPosition().latitude;
        }


        LatLngBounds bounds = new LatLngBounds.Builder().include(new LatLng(maxLatitude,minLongitude))
                .include(new LatLng(minLatitude,maxLongitude)).build();

        tencentMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,200));
    }






    public void moveCameraTo(LatLng latLng) {
        CameraUpdate cameraSigma =
                CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        latLng, //新的中心点坐标
                        tencentMap.getCameraPosition().zoom,  //新的缩放级别
                        0f, //俯仰角 0~45° (垂直地图时为0)
                        0f)); //偏航角 0~360° (正北方为0)
//移动地图
        tencentMap.moveCamera(cameraSigma);
    }

    public void refreshNowMarker(WeGoLocation weGoLocation) {
        if(null != nowMarker){
            nowMarker.remove();
        }
        LatLng latLng = weGoLocation.getLatLng();

        nowMarker = tencentMap.addMarker(new MarkerOptions().
                position(latLng).//先纬度后经度
                icon(BitmapDescriptorFactory.defaultMarker()).
                title(weGoLocation.title).
                snippet(weGoLocation.address));
        nowMarker.showInfoWindow();

    }

    public void addMarkerFromSearch(WeGoLocation weGoLocation) {
        refreshNowMarker(weGoLocation);
        moveCameraTo(weGoLocation.getLatLng());

    }

    public void moveCameraToNow() {
        TencentLocationRequest request = TencentLocationRequest.create();
        TencentLocationManager locationManager = TencentLocationManager.getInstance(activity);
        int error = locationManager.requestLocationUpdates(request, this);

    }





    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (TencentLocation.ERROR_OK == i) {
            Toast.makeText(activity,"success", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(tencentLocation.getLatitude(),tencentLocation.getLongitude());
            moveCameraTo(latLng);
            TencentLocationManager locationManager =
                    TencentLocationManager.getInstance(activity);
            locationManager.removeUpdates(this);// 定位成功
        } else {
            // 定位失败
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        nowMarkerLatLng = latLng;
        TencentSearch tencentSearch = new TencentSearch(activity);
        //还可以传入其他坐标系的坐标，不过需要用coord_type()指明所用类型
        //这里设置返回周边poi列表，可以在一定程度上满足用户获取指定坐标周边poi的需求
        com.tencent.lbssearch.object.Location location =
                new com.tencent.lbssearch.object.Location((float) latLng.latitude,(float)latLng.longitude);
        Geo2AddressParam geo2AddressParam = new Geo2AddressParam().
                location(location).get_poi(true);
        tencentSearch.geo2address(geo2AddressParam, new HttpResponseListener() {

            @Override
            public void onSuccess(int arg0, Header[] arg1, BaseObject arg2) {
                // TODO Auto-generated method stub
                if (arg2 == null) {
                    return;
                }
                Geo2AddressResultObject obj = (Geo2AddressResultObject)arg2;
                WeGoLocation weGoLocation = new WeGoLocation(nowMarkerLatLng);
                weGoLocation.address = obj.result.address;
                if(obj.result.pois!=null&&obj.result.pois.size()>0) {
                    weGoLocation.title = obj.result.pois.get(0).title;
                }
                refreshNowMarker(weGoLocation);


//                StringBuilder sb = new StringBuilder();
//                sb.append(getResources().getString(R.string.regeocoder));
//                sb.append("\n地址：" + obj.result.address);
//                sb.append("\npois:");
//                for (Geo2AddressResultObject.ReverseAddressResult.Poi poi : obj.result.pois) {
//                    sb.append("\n\t" + poi.title);
//                }
//                printResult(sb.toString());
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, String arg2, Throwable arg3) {
                // TODO Auto-generated method stub
                Toast.makeText(activity,arg2, Toast.LENGTH_SHORT).show();
            }
        });
        refreshNowMarker(new WeGoLocation(latLng));

    }


    class DemoLocationSource implements LocationSource, TencentLocationListener {

        private Context mContext;
        private OnLocationChangedListener mChangedListener;
        private TencentLocationManager locationManager;
        private TencentLocationRequest locationRequest;

        public DemoLocationSource(Context context) {
            // TODO Auto-generated constructor stub
            mContext = context;
            locationManager = TencentLocationManager.getInstance(mContext);
            locationRequest = TencentLocationRequest.create();
            locationRequest.setInterval(2000);
        }

        @Override
        public void onLocationChanged(TencentLocation arg0, int arg1,
                                      String arg2) {
            // TODO Auto-generated method stub
            if (arg1 == TencentLocation.ERROR_OK && mChangedListener != null) {
                Log.e("maplocation", "location: " + arg0.getCity() + " " + arg0.getProvider());
                Location location = new Location(arg0.getProvider());
                location.setLatitude(arg0.getLatitude());
                location.setLongitude(arg0.getLongitude());
                location.setAccuracy(arg0.getAccuracy());
                mChangedListener.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusUpdate(String arg0, int arg1, String arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void activate(OnLocationChangedListener arg0) {
            // TODO Auto-generated method stub
            mChangedListener = arg0;
            int err = locationManager.requestLocationUpdates(locationRequest, this);
            switch (err) {
                case 1:
                    Toast.makeText(activity,"设备缺少使用腾讯定位服务需要的基本条件", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(activity,"manifest 中配置的 key 不正确", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(activity,"自动加载libtencentloc.so失败", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void deactivate() {
            // TODO Auto-generated method stub
            locationManager.removeUpdates(this);
            mContext = null;
            locationManager = null;
            locationRequest = null;
            mChangedListener = null;
        }

        public void onPause() {
            locationManager.removeUpdates(this);
        }

        public void onResume() {
            locationManager.requestLocationUpdates(locationRequest, this);
        }

    }
}