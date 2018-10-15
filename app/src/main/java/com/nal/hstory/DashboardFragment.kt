package com.nal.hstory

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class DashboardFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val textView = view?.findViewById<TextView>(R.id.textView)
// Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://ec2-54-180-90-166.ap-northeast-2.compute.amazonaws.com:8080/reviews/all"

// Request a string response from the provided URL.
        Log.d("test", "start request")
        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    Log.d("test", "get response")
                    Log.d("test", "Response is: ${response.substring(0, 500)}")
                    textView?.text = "Response is: ${response.substring(0, 500)}"
                },
                Response.ErrorListener { textView?.text = "That didn't work!" })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

}