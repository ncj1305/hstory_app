package com.nal.hstory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlin.collections.ArrayList

class DashboardCardAdapter(val context: Context, val cardList: ArrayList<CardData>): BaseAdapter(){
    override fun getCount(): Int {
        return cardList.size
    }

    override fun getItem(position: Int): Any {
        return cardList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View?
        val vh: CardViewHolder
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.dashboard_card, parent, false)
            vh = CardViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as CardViewHolder
        }


        vh.tvUserName.text = cardList[position].userName
        vh.tvTitle.text = cardList[position].title

        return view
    }
}

class CardViewHolder(view: View?) {
    val ivIcon: ImageView
    val tvUserName: TextView
    val tvTitle: TextView

    init {
        this.ivIcon = view?.findViewById<ImageView>(R.id.ivIcon) as ImageView
        this.tvUserName = view?.findViewById<TextView>(R.id.tvUserName) as TextView
        this.tvTitle = view?.findViewById<TextView>(R.id.tvTitle) as TextView
    }
}