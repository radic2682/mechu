package com.mechu.mechu

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoAPI {
    @GET("v2/local/search/category.json") // Keyword.json의 정보를 받아옴
    fun getSearchCategory(
        @Header("Authorization") key: String, // 카카오 API 인증키 [필수]
        @Query("category_group_code") category: String, // 카테고리
        @Query("x") x: String,
        @Query("y") y: String,
        @Query("radius") radius: Int

    ): Call<ResultSearchKeyword> // 받아온 정보가 ResultSearchKeyword 클래스의 구조로 담김
}