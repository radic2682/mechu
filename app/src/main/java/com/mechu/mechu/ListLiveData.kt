package com.mechu.mechu

import androidx.lifecycle.MutableLiveData

class ListLiveData<Place> : MutableLiveData<MutableList<Place>>() {
    private val temp: MutableList<Place> by lazy { mutableListOf() }

    init {
        value = temp
    }

    override fun getValue() = super.getValue()!!
    val size: Int get() = value.size

    operator fun get(idx: Int) =
        if (size > idx) {
            value[idx]
        } else {
            throw ArrayIndexOutOfBoundsException("Index $idx Size $size")
        }

    fun data(): List<Place> {
        return temp
    }

    fun add(item: Place) {
        temp.add(item)
        value = temp
    }

    fun addAll(list: List<Place>) {
        temp.addAll(list)
        value = temp
    }

    fun clear() {
        temp.clear()
        value = temp
    }
}

