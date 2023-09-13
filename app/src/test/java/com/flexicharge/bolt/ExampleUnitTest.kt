package com.flexicharge.bolt

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
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

    @RunWith(AndroidJUnit4::class)
    class TestMainActivity {
        @Test fun testUnixToDateTimeIsCorrect() {
            val activityScenario = launch(MainActivity::class.java)
            activityScenario.onActivity { activity ->
                val unixTime = 0
                val dateTime = activity.unixToDateTime(unixTime.toString())
                println("Unix time: %s, dateTime: %s".format(unixTime.toString(), dateTime))
                assert(dateTime == "01/01/00:00")
            }
        }

        @Test fun testValidateChargerIdIsCorrect() {
            val activityScenario = launch(MainActivity::class.java)

            fun getIdString(id: Int): String {
                val ones = id % 10
                val tens = (id / 10) % 10
                val hundreds = (id / 100) % 10
                val thousands = (id / 1000) % 10
                val tenThousands = (id / 10000) % 10
                val hundredThousands = (id / 100000) % 10

                return "%d%d%d%d%d%d".format(
                    hundredThousands,
                    tenThousands,
                    thousands,
                    hundreds,
                    tens,
                    ones
                )
            }
            activityScenario.onActivity { activity ->
                for(i in 0 .. 999999) {
                    val idString = getIdString(i)
                    assert(activity.validateChargerId(idString))
                }

                assert(!activity.validateChargerId("-00000"))
                assert(!activity.validateChargerId("00000000"))
                assert(!activity.validateChargerId("0000000"))
                assert(!activity.validateChargerId("00000"))
                assert(!activity.validateChargerId("0000"))
                assert(!activity.validateChargerId("000"))
                assert(!activity.validateChargerId("00"))
                assert(!activity.validateChargerId("0"))

            }

        }

    @Test
    fun testGetChargePoint() = runBlocking {
        val response = RetrofitInstance.flexiChargeApi.getChargePoint(24)
        assert(response.isSuccessful)
    }

    @Test
    fun addition_isCorrect1() {
        assertEquals(4, 1 + 3)
    }
}