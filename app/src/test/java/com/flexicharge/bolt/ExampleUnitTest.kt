package com.flexicharge.bolt

import com.flexicharge.bolt.activities.MainActivity
import com.flexicharge.bolt.api.flexicharge.RetrofitInstance
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(5, 1 + 3)
    }

    @Test
    fun addition_isCorrect1() {
        assertEquals(4, 1 + 3)
    }

    @Test
    fun testGetChargerSuccessful() = runBlocking {
        val response = RetrofitInstance.flexiChargeApi.getCharger(100009)
        assert(response.isSuccessful)
    }

    @Test
    fun testGetChargerListSuccessful() = runBlocking {
        val response = RetrofitInstance.flexiChargeApi.getChargerList()
        assert(response.isSuccessful)
    }


    @Test
    fun testGetChargePointListSuccessful() = runBlocking {
        val response = RetrofitInstance.flexiChargeApi.getChargePointList()
        assert(response.isSuccessful)
    }

    @Test
    fun testGetTransactionSuccessful() = runBlocking {
        val response = RetrofitInstance.flexiChargeApi.getTransaction(1)
        assert(response.isSuccessful)
    }

    @Test
    fun testGetChargePoint() = runBlocking {
        val response = RetrofitInstance.flexiChargeApi.getChargePoint(100029)
        assert(response.isSuccessful)
    }
}