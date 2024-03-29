package com.drewel.drewel.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.drewel.drewel.R
import com.drewel.drewel.fragment.DiscountFragment
import kotlinx.android.synthetic.main.app_toolbar.*

class CouponCodeActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.coupon_code_activity_layout)
        initView()
    }

    /* set toolbar and back button*/
    private fun initView() {
        toolbarTitleTv.text = getString(R.string.apply_coupon)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val discountFragment = DiscountFragment()
        discountFragment.isFromCheckout = true
        setFragment(discountFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager

        fragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
    }
}