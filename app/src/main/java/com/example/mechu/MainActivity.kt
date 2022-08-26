package com.example.mechu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mechu.databinding.ActivityMainBinding
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class MainActivity : AppCompatActivity() {
    private var getLatitude : Double? = null //위도
    private var getLongitude : Double? = null //경도

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
}