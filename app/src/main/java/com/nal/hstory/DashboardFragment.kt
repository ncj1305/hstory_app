package com.nal.hstory

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DashboardFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var lvListView = view?.findViewById<ListView?>(R.id.lvCardList)
        var cardList = ArrayList<CardData>()
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://ec2-54-180-90-166.ap-northeast-2.compute.amazonaws.com:8080/reviews/all"

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

        queue.add(stringRequest)
    }

}