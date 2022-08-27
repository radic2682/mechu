package com.example.mechu

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.mechu.databinding.ActivityPickBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
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

    private lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick)

        val binding = ActivityPickBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getLatitude = intent.getDoubleExtra("Latitude", 0.0)
        getLongitude = intent.getDoubleExtra("Longitude", 0.0)
        placeName = intent.getStringExtra("place_name")!!
        phone = intent.getStringExtra("phone")!!
        x = intent.getStringExtra("x")!!
        y = intent.getStringExtra("y")!!
        roadAddressName = intent.getStringExtra("road_address_name")!!

        binding.nameOfRest.text = placeName
        binding.phone.text = phone
        binding.address.text = roadAddressName

        Log.d("Test", "Body: $getLatitude")
        Log.d("Test", "Body: $getLongitude")
        Log.d("Test", "Body: $x")
        Log.d("Test", "Body: $y")


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