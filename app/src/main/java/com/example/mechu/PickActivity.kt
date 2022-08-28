package com.example.mechu

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.mechu.databinding.ActivityPickBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage


class PickActivity : AppCompatActivity(), OnMapReadyCallback {
    private var getLatitude : Double? = null //위도
    private var getLongitude : Double? = null //경도
    private var placeName : String? = null
    private var phone : String? = null
    private var x : String? = null
    private var y : String? = null
    private var roadAddressName : String? = null
    private var placeurl : String? = null

    private lateinit var naverMap: NaverMap

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick)

        val binding = ActivityPickBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 광고 설정
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)



        // 데이터 인텐트에서 받아오기
        getLatitude = intent.getDoubleExtra("Latitude", 0.0)
        getLongitude = intent.getDoubleExtra("Longitude", 0.0)
        placeName = intent.getStringExtra("place_name")!!
        phone = intent.getStringExtra("phone")!!
        x = intent.getStringExtra("x")!!
        y = intent.getStringExtra("y")!!
        roadAddressName = intent.getStringExtra("road_address_name")!!
        placeurl = intent.getStringExtra("place_url")!!

        // 데이터 화면에 출력
        binding.nameOfRest.text = placeName
        if (phone == ""){
            binding.phone.text = " 전화번호가 없어요...ㅠㅠ"
        } else{
            binding.phone.text = " $phone"
        }
        binding.address.text = " $roadAddressName"
        binding.linkButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(placeurl)
            startActivity(intent)
        }

        val mapFragment = displayMapOnScreen(supportFragmentManager)
        mapFragment.getMapAsync(this)

    }
    private fun displayMapOnScreen(fm : FragmentManager) : MapFragment {
        return fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        val xDouble: Double = x!!.toDouble()
        val yDouble: Double = y!!.toDouble()

        Log.d("Test", "xDouble: $xDouble")
        Log.d("Test", "yDouble: $yDouble")

        val cameraPosition = CameraPosition(LatLng(yDouble, xDouble), 16.0)
        naverMap.cameraPosition = cameraPosition

        // 지도상에 마커 표시
        val marker = Marker()
        marker.position = LatLng(yDouble, xDouble)
        marker.icon = OverlayImage.fromResource(R.drawable.ic_baseline_restaurant_menu_24)
        marker.iconTintColor = Color.BLACK
        marker.width = 100
        marker.height = 100
        marker.captionText = placeName!!
        marker.map = naverMap
    }
}