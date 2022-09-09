package com.mechu.mechu

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.fragment.app.FragmentManager
import com.mechu.mechu.api.KakaoAPI
import com.mechu.mechu.api.Place
import com.mechu.mechu.api.ResultSearchKeyword
import com.mechu.mechu.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var getLatitude : Double? = null //위도
    private var getLongitude : Double? = null //경도

    private var listItems = ListLiveData<Place>()

    // REST API 키
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 5bc9b77baaa71c5e14d7af8fbadf57a0"
    }

    // 네이버 지도
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap:NaverMap

    private var pageNumberofAPI:Int = 1


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pickIntent = Intent(this, PickActivity::class.java)
        val gameIntent = Intent(this, GameActivity::class.java)
        val infoIntent = Intent(this, InfoActivity::class.java)

        // 네이버맵 추가
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val fm: FragmentManager = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment!!.getMapAsync(this)

        // 버튼들
        binding.PickFab.setOnClickListener {
            pickIntent.putExtra("Latitude", getLatitude)
            pickIntent.putExtra("Longitude", getLongitude)

            // 인텐트로 pickActivity에 전달
            val random = Random()
            val randomNum = random.nextInt(listItems.size)
            Log.d("Test", "listItems.size: ${listItems.size}")
            Log.d("Test", "randomNum: $randomNum")

            val tempPlace: Place = listItems[randomNum]
            val tempPlaceName: String = tempPlace.place_name
            pickIntent.putExtra("place_name", tempPlaceName)
            pickIntent.putExtra("x", tempPlace.x)
            pickIntent.putExtra("y", tempPlace.y)
            pickIntent.putExtra("phone", tempPlace.phone)
            pickIntent.putExtra("road_address_name", tempPlace.road_address_name)
            pickIntent.putExtra("place_url", tempPlace.place_url)

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
                    setPosition() //위치 정보를 받아
                    searchKeyword() //지역 정보 받아오고 마커 찍기
                    binding.map.visibility = View.VISIBLE

                } else {
                    missingPermisson(binding)
                }
        }.launch("android.permission.ACCESS_FINE_LOCATION")

        listItems.observe(this) {
            Log.d("Test", "Body: ${listItems.size}")

            binding.PickFab.visibility = View.VISIBLE

            if(listItems.size != 0){
                binding.findCount.text = "행운이에요!\n근처에 ${listItems.size}개의 음식점이 있어요!"
            } else {
                binding.findCount.text = "ㅠㅠ...\n근처에 음식점이 없어요..."
            }
        }

    }

    override fun onMapReady(naverMap: NaverMap) {
        naverMap.also { this@MainActivity.naverMap = it }
        naverMap.locationSource = locationSource

        val cameraPosition = CameraPosition(
            LatLng(37.5666805, 126.9784147),  // 위치 지정
            16.0 // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

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
        val call = api.getSearchCategory(API_KEY, "FD6", getLongitude.toString(), getLatitude.toString(), 600, pageNumberofAPI)

        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                if(response.isSuccessful) {
                    // 통신 성공 (검색 결과는 response.body()에 담겨있음)
                    Log.d("Test", "Raw: ${response.raw()}")
                    Log.d("Test", "Body: ${response.body()}")

                    listItems.addAll(response.body()!!.documents)
                    addMaker(response.body())

                    if(!response.body()!!.meta.is_end){ // 마지막 페이지가 아닌 경우
                        pageNumberofAPI += 1
                        searchKeyword()
                    }

                } else {
                    Log.w("MainActivity", "통신은 성공했지만 알 수 없는 문제 발생")
                }
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("MainActivity", "통신 실패: ${t.message}")
            }
        })
    }

    private fun addMaker(searchResult: ResultSearchKeyword?) {
        if (!searchResult?.documents.isNullOrEmpty()) {
            // 검색 결과 있음
            for (document in searchResult!!.documents) {
                // 지도에 마커 추가
                val marker = Marker()
                marker.position = LatLng(document.y.toDouble(), document.x.toDouble())
                marker.map = naverMap
                marker.width = 100
                marker.height = 100
                marker.captionText = document.place_name
                marker.captionRequestedWidth = 100
                marker.icon = OverlayImage.fromResource(R.drawable.restaurantmaker)
            }
        } else { // 검색 결과 없음
        }
    }


}