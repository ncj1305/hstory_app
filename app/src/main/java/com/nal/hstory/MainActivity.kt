package com.nal.hstory

import android.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {

    var dashboardFragment = DashboardFragment()
    var fragmentTransaction: FragmentTransaction? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction
                ?.replace(R.id.contents, dashboardFragment)
                ?.commit()
    }

//    override fun onBackPressed() {
//        val count = fragmentManager.backStackEntryCount
//        if (count == 0) {
//            super.onBackPressed()
//            //additional code
//        } else {
//            fragmentManager.popBackStack()
//        }
//    }
}
