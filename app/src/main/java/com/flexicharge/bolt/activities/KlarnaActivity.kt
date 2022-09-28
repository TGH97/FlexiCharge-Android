package com.flexicharge.bolt.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flexicharge.bolt.*
import com.flexicharge.bolt.api.flexicharge.RetrofitInstance
import com.flexicharge.bolt.api.flexicharge.TransactionList
import com.flexicharge.bolt.api.flexicharge.TransactionOrder
import com.flexicharge.bolt.api.klarna.OrderClient
import com.klarna.mobile.sdk.api.payments.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class KlarnaActivity : AppCompatActivity(), KlarnaPaymentViewCallback {
    private val klarnaPaymentView by lazy { findViewById<KlarnaPaymentView>(R.id.klarnaActivity_KlarnaPaymentVie) }
    private val authorizeButton by lazy { findViewById<Button>(R.id.klarnaActivity_button_authorize) }
    private var chargerId : Int = 0
    private var clientToken : String = ""
    private var transactionId : Int = 0
    private var authTokenId : String = ""

    private val paymentCategory = KlarnaPaymentCategory.PAY_NOW // please update this value if needed

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_klarna)
        chargerId = intent.getIntExtra("ChargerId", 0)
        clientToken = intent.getStringExtra("ClientToken").toString()
        transactionId = intent.getIntExtra("TransactionId", 0)
        Log.d("CLIENTTOKEN", clientToken)
        initialize()

        setupButtons()
        klarnaPaymentView.category = paymentCategory
    }

    private fun initialize() {
        if (OrderClient.hasSetCredentials()) {
            job = GlobalScope.launch {
            try {
                runOnUiThread {
                    klarnaPaymentView.initialize(
                        clientToken,
                        "${getString(R.string.return_url_scheme)}://${getString(R.string.return_url_host)}"
                    )
                }
            }
            catch (exception: Exception) {
                showError(exception.message)
            }
            }
        } else {
            showError(getString(R.string.error_credentials))
        }
    }

    private fun setupButtons() {
        authorizeButton.setOnClickListener {
            klarnaPaymentView.authorize(true, null)
        }
    }

    private fun showError(message: String?) {
        runOnUiThread {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setMessage(message ?: getString(R.string.error_general))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
        }
    }

    private fun runOnUiThread(action: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            action.invoke()
        }
    }

    override fun onInitialized(view: KlarnaPaymentView) {

        // load the payment view after its been initialized
        view.load(null)
    }

    override fun onLoaded(view: KlarnaPaymentView) {

        // enable the authorization after the payment view is loaded
        authorizeButton.isEnabled = true
    }

    override fun onLoadPaymentReview(view: KlarnaPaymentView, showForm: Boolean) {}

    override fun onAuthorized(
        view: KlarnaPaymentView,
        approved: Boolean,
        authToken: String?,
        finalizedRequired: Boolean?
    ) {
        if (authToken != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val requestBody = TransactionOrder(transactionId, authToken!!)
                    val response = RetrofitInstance.flexiChargeApi.transactionStart(transactionId, requestBody)
                    if (response.isSuccessful) {
                        //TODO Backend Klarna/Order/Session Request if successful
                        val transaction = response.body() as TransactionList
                        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().apply { putInt("TransactionId", transactionId) }.apply()
                        lifecycleScope.launch(Dispatchers.Main) {
                            finish()
                        }
                    } else {
                        //TODO Don't fake a successful transaction
                        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().apply() { putInt("TransactionId", transactionId) }.apply()

                        /* Actual expected behaviour:
                        lifecycleScope.launch(Dispatchers.Main) {
                            authorizeButton.text = "Transaction Failed, try again!"
                        }
                        */

                        finish()
                    }
                } catch (e: HttpException) {

                } catch (e: IOException) {

                }
            }
        }
        if (authToken != null) {
            authTokenId = authToken
        }
    }

    override fun onReauthorized(view: KlarnaPaymentView, approved: Boolean, authToken: String?) {}

    override fun onErrorOccurred(view: KlarnaPaymentView, error: KlarnaPaymentsSDKError) {
        println("An error occurred: ${error.name} - ${error.message}")
        if (error.isFatal) {
            klarnaPaymentView.visibility = View.INVISIBLE
        }
    }

    override fun onFinalized(view: KlarnaPaymentView, approved: Boolean, authToken: String?) {}

    override fun onResume() {
        super.onResume()
        klarnaPaymentView.registerPaymentViewCallback(this)
    }

    override fun onPause() {
        super.onPause()
        klarnaPaymentView.unregisterPaymentViewCallback(this)
        job?.cancel()
    }

}
