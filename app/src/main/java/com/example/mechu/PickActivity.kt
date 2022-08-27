package com.example.mechu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mechu.databinding.ActivityPickBinding

class PickActivity : AppCompatActivity() {
    private var listItems = ArrayList<Place>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val binding = ActivityPickBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 카카오맵 띄우기
        // val mapView = MapView(this)
        // binding.mapView.addView(mapView)

        //마커 설정


    }
}