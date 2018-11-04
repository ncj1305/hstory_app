package com.nal.hstory

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.*
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.security.KeyStore
import java.security.cert.CertificateFactory

import java.security.cert.X509Certificate
import javax.net.ssl.*
import javax.security.cert.Certificate
import javax.security.cert.CertificateException


class DashboardFragment: Fragment() {
    var fragmentTransaction: FragmentTransaction? = null
    var lvListView:ListView? = null
    var cardList = ArrayList<CardData>()
    var writeFragment = WriteFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        fragmentTransaction = fragmentManager.beginTransaction()
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.actions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.write -> {
                fragmentTransaction
                        ?.replace(R.id.contents, writeFragment)
                        ?.addToBackStack("Write")
                        ?.commit()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lvListView = view?.findViewById<ListView?>(R.id.lvCardList)

        processVolley()
    }

    override fun onResume() {
        super.onResume()
        cardList.clear()
    }

    private fun processVolley() {
        val hurlStack = object : HurlStack() {
            @Throws(IOException::class)
            override fun createConnection(url: URL): HttpURLConnection {
                val httpsURLConnection = super.createConnection(url) as HttpsURLConnection
                try {
                    httpsURLConnection.sslSocketFactory = newSslSocketFactory()
                    httpsURLConnection.hostnameVerifier = hostnameVerifier
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return httpsURLConnection
            }
        }
        val queue = Volley.newRequestQueue(this.context, hurlStack)

        val url = "https://ec2-13-124-204-77.ap-northeast-2.compute.amazonaws.com:8081/reviews/all"

        Log.d("test", "start request")
        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    Log.d("test", "get response")
                    Log.d("test", "Response is: $response")
                    val resData = JSONObject(response)
                    val reviewsArray = resData.getJSONArray("reviews")
                    for (index in 0 until reviewsArray.length()) {
                        val reviewData = reviewsArray.getJSONObject(index)
                        cardList.add(CardData(
                                reviewData.getString("name"),
                                reviewData.getString("title"),
                                reviewData.getString("content")
                        ))
                    }
                    lvListView?.adapter = DashboardCardAdapter(this.context, cardList)
                },
                Response.ErrorListener { err ->
                    Log.d("error", err.toString())
                })
        queue.add(stringRequest)
    }

    val hostnameVerifier: HostnameVerifier = HostnameVerifier { hostname, session ->
        val hv = HttpsURLConnection.getDefaultHostnameVerifier()
        hv.verify("na", session)
    }

    private fun newSslSocketFactory(): SSLSocketFactory {
        try {
            val cf = CertificateFactory.getInstance("X.509")
            val caInput = context.resources.openRawResource(R.raw.cert)
            val ca = cf.generateCertificate(caInput)
            caInput.close()

            val keyStore = KeyStore.getInstance("BKS")
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)

            val wrappedTrustManagers = getWrappedTrustManagers(tmf.trustManagers)

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, wrappedTrustManagers, null)


            return sslContext.socketFactory
        } catch (e: Exception) {
            throw AssertionError(e)
        }

    }

    private fun getWrappedTrustManagers(trustManagers: Array<TrustManager>): Array<TrustManager> {
        val originalTrustManager = trustManagers[0] as X509TrustManager
        return arrayOf(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return originalTrustManager.acceptedIssuers
            }

            override fun checkClientTrusted(certs: Array<X509Certificate>?, authType: String) {
                try {
                    if (certs != null && certs.isNotEmpty()) {
                        certs[0].checkValidity()
                    } else {
                        originalTrustManager.checkClientTrusted(certs, authType)
                    }
                } catch (e: CertificateException) {
                    Log.w("checkClientTrusted", e.toString())
                }

            }

            override fun checkServerTrusted(certs: Array<X509Certificate>?, authType: String) {
                try {
                    if (certs != null && certs.isNotEmpty()) {
                        certs[0].checkValidity()
                    } else {
                        originalTrustManager.checkServerTrusted(certs, authType)
                    }
                } catch (e: CertificateException) {
                    Log.w("checkServerTrusted", e.toString())
                }

            }
        })
    }
}