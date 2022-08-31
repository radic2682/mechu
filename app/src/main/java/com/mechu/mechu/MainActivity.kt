package com.mechu.mechu

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mechu.mechu.databinding.ActivityMainBinding
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private var getLatitude : Double? = null //위도
    private var getLongitude : Double? = null //경도

    private val listItems = ArrayList<Place>()

    // REST API 키
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 5bc9b77baaa71c5e14d7af8fbadf57a0"
    }

    @SuppressLint("PackageManagerGetSignatures")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //supportActionBar?.hide()

        val pickIntent = Intent(this, PickActivity::class.java)
        val gameIntent = Intent(this, GameActivity::class.java)
        val infoIntent = Intent(this, InfoActivity::class.java)

        // 카카오맵 띄우기
        val mapView = MapView(this)
        binding.mapView.addView(mapView)

        // 버튼들
        binding.PickFab.setOnClickListener {
            searchKeywordAndMaker(binding, mapView, pickIntent)
            pickIntent.putExtra("Latitude", getLatitude)
            pickIntent.putExtra("Longitude", getLongitude)
            startActivity(pickIntent)
        }
        binding.gameButton.setOnClickListener {
            startActivity(gameIntent)
        }
        binding.infoButton.setOnClickListener {
            startActivity(infoIntent)
        }

        //최초 권한 확인
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
                if(isGranted){
                    setPosition() //위치 정보를 받아옴

                    //지도 설정
                    mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(getLatitude!!, getLongitude!!), 2,true)
                    mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading

                    searchKeywordAndMaker(binding, mapView, pickIntent) //지역 정보 받아오고 마커 찍기
                } else {
                    missingPermisson(binding)
                }
        }.launch("android.permission.ACCESS_FINE_LOCATION")
    }

    private fun missingPermisson(binding: ActivityMainBinding) {
        Toast.makeText(this, "앱 설정에서 위치 정보 제공을 동의해야\n" +
                "정상적인 이용이 가능합니다." , Toast.LENGTH_LONG).show()
        binding.PickFab.visibility = View.INVISIBLE
        binding.gameButton.visibility = View.INVISIBLE
        binding.warnText.visibility = View.VISIBLE
    }

    // 위치 권한 요청과 현재 위치 반환
    private fun setPosition() {
        val manager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            when { //프로바이더 제공자 활성화 여부 체크
                isNetworkEnabled -> {
                    val location =
                        manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) //인터넷기반으로 위치를 찾음
                    getLatitude = location?.latitude
                    getLongitude = location?.longitude
                }
                isGPSEnabled -> {
                    val location =
                        manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) //GPS 기반으로 위치를 찾음
                    getLatitude = location?.latitude
                    getLongitude = location?.longitude
                }
            }
        }
    }

    private fun searchKeywordAndMaker(binding: ActivityMainBinding, mapView: MapView, pickIntent:Intent) {
        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getSearchCategory(API_KEY, "FD6", getLongitude.toString(), getLatitude.toString(), 600)

        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                if(response.isSuccessful) {
                    // 통신 성공 (검색 결과는 response.body()에 담겨있음)
                    binding.PickFab.visibility = View.VISIBLE

                    Log.d("Test", "Raw: ${response.raw()}")
                    Log.d("Test", "Body: ${response.body()}")

                    saveResultofAPI(mapView, response.body())

                    // 인텐트로 pickActivity에 전달
                    val randomNum = (0..listItems.size).random()
                    pickIntent.putExtra("place_name", listItems[randomNum].place_name)
                    pickIntent.putExtra("x", listItems[randomNum].x)
                    pickIntent.putExtra("y", listItems[randomNum].y)
                    pickIntent.putExtra("phone", listItems[randomNum].phone)
                    pickIntent.putExtra("road_address_name", listItems[randomNum].road_address_name)
                    pickIntent.putExtra("place_url", listItems[randomNum].place_url)

                    if(listItems.size != 0){
                        binding.findCount.text = "행운이에요!\n근처에 ${listItems.size}개의 음식점이 있어요!"
                    } else {
                        binding.findCount.text = "ㅠㅠ...\n근처에 음식점이 없어요..."
                    }
                } else {
                    // 통신은 성공했지만 문제가 있는 경우
                }

            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("MainActivity", "통신 실패: ${t.message}")
            }
        })
    }

    private fun saveResultofAPI(mapView: MapView, searchResult: ResultSearchKeyword?) {
        if (!searchResult?.documents.isNullOrEmpty()) {
            // 검색 결과 있음
            listItems.clear()           // 리스트 초기화
            mapView.removeAllPOIItems() // 지도의 마커 모두 제거
            for (document in searchResult!!.documents) {
                val tempPlace = Place(document.place_name,
                    document.road_address_name,
                    document.address_name,
                    document.phone,
                    document.place_url,
                    document.x,
                    document.y
                )
                listItems.add(tempPlace)

                // 지도에 마커 추가
                mapView.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater))
                val point = MapPOIItem()

                point.apply {
                    itemName = document.place_name
                    mapPoint = MapPoint.mapPointWithGeoCoord(document.y.toDouble(), document.x.toDouble())
                    markerType = MapPOIItem.MarkerType.CustomImage
                    customImageResourceId = R.drawable.restaurantmaker
                    isCustomImageAutoscale = true
                }
                mapView.addPOIItem(point)
            }
        } else {
            // 검색 결과 없음
        }
    }

    class CustomBalloonAdapter(inflater: LayoutInflater): CalloutBalloonAdapter {
        @SuppressLint("InflateParams")
        private val mCalloutBalloon: View = inflater.inflate(R.layout.balloon_layout, null)
        private val name: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_name)

        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            // 마커 클릭 시 나오는 말풍선
            name.text = poiItem?.itemName   // 해당 마커의 정보 이용 가능
            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
            // 말풍선 클릭 시
            return mCalloutBalloon
        }
    }

}