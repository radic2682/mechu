package com.mechu.mechu.api

data class ResultSearchKeyword(
    var meta: PlaceMeta,
    var documents: List<Place>          // 검색 결과
)

data class Place(
    var place_name: String,             // 장소명, 업체명
    var address_name: String,           // 전체 지번 주소
    var road_address_name: String,      // 전체 도로명 주소
    var phone: String,                  // 전화번호
    var place_url: String,              // 장소 상세페이지 URL
    var x: String,                      // X 좌표값 혹은 longitude
    var y: String,                      // Y 좌표값 혹은 latitude
)

data class PlaceMeta(
    var total_count: Int,               // 검색어에 검색된 문서 수
    var pageable_count: Int,            // total_count 중 노출 가능 문서 수, 최대 45 (API에서 최대 45개 정보만 제공)
    var is_end: Boolean,                // 현재 페이지가 마지막 페이지인지 여부
)