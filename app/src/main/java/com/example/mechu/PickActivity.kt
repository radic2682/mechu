package com.example.mechu

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mechu.databinding.ActivityPickBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PickActivity : AppCompatActivity() {
    private var getLatitude : Double? = null //위도
    private var getLongitude : Double? = null //경도

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val binding = ActivityPickBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getLatitude = intent.getDoubleExtra("Latitude", 0.0)
        getLongitude = intent.getDoubleExtra("Longitude", 0.0)

        // 네이버맵 띄우기
        Log.d("Test", "Body: $getLatitude")
        Log.d("Test", "Body: $getLongitude")
        // 버튼들

        //searchKeywordAndMaker()

    }

    private fun searchKeywordAndMaker() {
        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl(MainActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getSearchCategory(MainActivity.API_KEY, "FD6", getLongitude.toString(), getLatitude.toString(), 600)

        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                if(response.isSuccessful) {
                    // 통신 성공 (검색 결과는 response.body()에 담겨있음)
                    Log.d("Test", "Raw: ${response.raw()}")
                    Log.d("Test", "Body: ${response.body()}")

                    TODO("모든 코드 여기 작성")
                } else {
                    // 통신은 성공했지만 문제가 있는 경우
                }

            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("Test", "통신 실패: ${t.message}")
                TODO("메세지 띄우기 서버 통신 불량")
            }
        })
    }



}