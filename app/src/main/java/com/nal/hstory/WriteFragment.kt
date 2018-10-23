package com.nal.hstory

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateFactory

import java.security.cert.X509Certificate
import javax.net.ssl.*
import javax.security.cert.CertificateException


class WriteFragment: Fragment() {
    var fragmentTransaction: FragmentTransaction? = null
    var etName: EditText? = null
    var etTitle: EditText? = null
    var etContent: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        fragmentTransaction = fragmentManager.beginTransaction()
        return inflater.inflate(R.layout.fragment_write, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        etName = view?.findViewById<EditText?>(R.id.et_name)
        etTitle = view?.findViewById<EditText?>(R.id.et_title)
        etContent = view?.findViewById<EditText?>(R.id.et_content)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.write_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.cancel -> {
                clearForm()
            }
            R.id.upload -> {
                if (isFormVailid()) {
                    processUploadVolley()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun clearForm() {
        etName?.text?.clear()
        etTitle?.text?.clear()
        etContent?.text?.clear()
    }

    private fun isFormVailid(): Boolean {
        if (!etName?.text.toString().equals("") &&
            !etTitle?.text.toString().equals("") &&
            !etContent?.text.toString().equals("")) {
            return true
        }
        Toast.makeText(context,"Please fill out all section!",Toast.LENGTH_SHORT).show()
        return false
    }

    private fun processUploadVolley() {
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
        val queue = Volley.newRequestQueue(context, hurlStack)

        val url = "https://ec2-54-180-90-166.ap-northeast-2.compute.amazonaws.com:8081/post"

        val jsonBody = JSONObject()
        jsonBody.put("name", etName?.text.toString());
        jsonBody.put("title", etTitle?.text.toString());
        jsonBody.put("content", etContent?.text.toString());
        val requestBody = jsonBody.toString()

        Log.d("test", "start request")
        val stringRequest = object: StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
                Log.d("test", "get response")
                Log.d("test", "Response is: $response")

                var builder = AlertDialog.Builder(context)
                builder.setTitle("Success")
                builder.setMessage("Your message is uploaded");
                builder.setPositiveButton("Confirm"){dialog, which ->
                    clearForm()
                    // Do something when user press the positive button
                    Toast.makeText(context,"Good!",Toast.LENGTH_SHORT).show()
                }
                builder.show()
            },
            Response.ErrorListener { err ->
                Log.d("error", err.toString())
            })
        {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(charset("utf-8"))
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                var responseString = "";
                if (response != null) {
                    responseString = response.statusCode.toString();
                    // can get more details such as response.h
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }

        }
        queue.add(stringRequest)
    }

    val hostnameVerifier: HostnameVerifier = HostnameVerifier { hostname, session ->
        val hv = HttpsURLConnection.getDefaultHostnameVerifier()
        hv.verify("na", session)
    }

    private fun newSslSocketFactory(): SSLSocketFactory {
        try {
            val cf = CertificateFactory.getInstance("X.509")
            val caInput = context.applicationContext.resources.openRawResource(R.raw.cert)
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