package com.nal.hstory

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    var lvListView:ListView? = null
    var cardList = ArrayList<CardData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lvListView = view?.findViewById<ListView?>(R.id.lvCardList)

//        processSslConnection()
        processVolley()
    }

    private fun processSslConnection(){
        val cf = CertificateFactory.getInstance("X.509")
// From https://www.washington.edu/itconnect/security/ca/load-der.crt
//        val caInput = BufferedInputStream(FileInputStream("cert.crt"))
        val caInput = this.context.applicationContext.resources.openRawResource(R.raw.cert)
        try {
            var ca = cf.generateCertificate(caInput)
            caInput.close()
            System.out.println("ca=" + (ca as X509Certificate).subjectDN)

            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)

// Create a TrustManager that trusts the CAs in our KeyStore
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)

// Create an SSLContext that uses our TrustManager
            val context = SSLContext.getInstance("TLS")
            context.init(null, tmf.trustManagers, null)

// Tell the URLConnection to use a SocketFactory from our SSLContext
            val url = URL("https://certs.cac.washington.edu/CAtest/")
            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.sslSocketFactory = context.socketFactory
            val urlInputStream = urlConnection.inputStream
            // copyInputStreamToOutputStream(urlInputStream, System.out)
        } finally {
            caInput.close()
            // Create a KeyStore containing our trusted CAs

        }
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
//        val queue = Volley.newRequestQueue(this.context, HurlStack(null, newSslSocketFactory()))
        val queue = Volley.newRequestQueue(this.context, hurlStack)

        val url = "https://ec2-54-180-90-166.ap-northeast-2.compute.amazonaws.com:8081/reviews/all"

        Log.d("test", "start request")
        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    Log.d("test", "get response")
                    Log.d("test", "Response is: ${response.substring(0, 500)}")
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
//        val queue = Volley.newRequestQueue(this.context)
//        val url = "http://ec2-54-180-90-166.ap-northeast-2.compute.amazonaws.com:8080/reviews/all"
//
//        Log.d("test", "start request")
//        val stringRequest = StringRequest(Request.Method.GET, url,
//                Response.Listener<String> { response ->
//                    // Display the first 500 characters of the response string.
//                    Log.d("test", "get response")
//                    Log.d("test", "Response is: ${response.substring(0, 500)}")
//                    val resData = JSONObject(response)
//                    val reviewsArray = resData.getJSONArray("reviews")
//                    for (index in 0 until reviewsArray.length()) {
//                        val reviewData = reviewsArray.getJSONObject(index)
//                        cardList.add(CardData(
//                                reviewData.getString("name"),
//                                reviewData.getString("title"),
//                                reviewData.getString("content")
//                                ))
//                    }
//                    lvListView?.adapter = DashboardCardAdapter(this.context, cardList)
//                },
//                Response.ErrorListener { err ->
//                    Log.d("error", err.toString())
//                })

        queue.add(stringRequest)
    }

    val hostnameVerifier: HostnameVerifier = HostnameVerifier { hostname, session ->
        val hv = HttpsURLConnection.getDefaultHostnameVerifier()
        hv.verify("na", session)
    }

    private fun newSslSocketFactory(): SSLSocketFactory {
        try {
            // Get an instance of the Bouncy Castle KeyStore format
            val trusted = KeyStore.getInstance("BKS")
            val cf = CertificateFactory.getInstance("X.509")
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            // val inputStream = this.context.applicationContext.resources.openRawResource(R.raw.bks_by_crt)
            val caInput = context.applicationContext.resources.openRawResource(R.raw.cert)
            val ca = cf.generateCertificate(caInput)
            caInput.close()

//            try {
//                // Initialize the keystore with the provided trusted certificates
//                // Provide the password of the keystore
//                trusted.load(inputStream, "e9e9e9".toCharArray())
//            } finally {
//                inputStream.close()
//            }

//            val keyStoreType = KeyStore.getDefaultType()
//            val keyStore = KeyStore.getInstance(keyStoreType)
            val keyStore = KeyStore.getInstance("BKS")
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
//            val tmf = TrustManagerFactory.getInstance("X509")
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