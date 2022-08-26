package com.example.mechu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mechu.databinding.ActivityMainBinding
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

    // REST API 키
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 18385c4f27c0bc5b310a297d80a3fbed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 카카오맵 띄우기
        val mapView = MapView(this)
        binding.mapView.addView(mapView)

        // 버튼들
        binding.PickFab.setOnClickListener {
            val nextIntent = Intent(this, PickActivity::class.java)
            startActivity(nextIntent)
        }
        binding.gameButton.setOnClickListener {
            val nextIntent = Intent(this, GameActivity::class.java)
            startActivity(nextIntent)
        }

        //최초 권한 확인
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
                if(isGranted){
                    //위치 정보를 받아옴
                    setPosition()

                    //지도 설정
                    mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(getLatitude!!, getLongitude!!), 3, true)
                    mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading

                    //지역 정보 받아오기
                    searchKeyword()


                    //마커 설정


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

    private fun searchKeyword() {
        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getSearchCategory(API_KEY, "FD6", getLongitude.toString(), getLatitude.toString(), 500)   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                // 통신 성공 (검색 결과는 response.body()에 담겨있음)
                Log.d("Test", "Raw: ${response.raw()}")
                Log.d("Test", "Body: ${response.body()}")
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("MainActivity", "통신 실패: ${t.message}")
            }
        })
    }

}