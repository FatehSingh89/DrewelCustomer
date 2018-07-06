package com.os.drewel.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.os.drewel.R
import com.os.drewel.activity.CartActivity
import com.os.drewel.activity.MyOrderDetailActivity
import com.os.drewel.activity.NotificationActivity
import com.os.drewel.activity.ProductDetailActivity
import com.os.drewel.apicall.responsemodel.notificationresponsemodel.Notification
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.delegate.OnClick
import com.os.drewel.firebase.DrewelFirebaseMessagingService
import com.os.drewel.utill.Utils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.notification_row_selector.view.*


class NotificationAdapter(val mContext: Activity?, private var notificationList: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationHolder>() {
    lateinit var defaultAddressClickSubject: PublishSubject<Int>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_row_selector, parent, false)

        return NotificationHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        if ((notificationList[position].isRead!!.isBlank()))
            if (notificationList[position].equals("0"))
                holder.itemView.ll_main.setBackgroundColor(mContext!!.getResources().getColor(R.color.colorPrimary_verylight))
            else
                holder.itemView.ll_main.setBackgroundColor(mContext!!.getResources().getColor(R.color.white))
        else
            holder.itemView.ll_main.setBackgroundColor(mContext!!.getResources().getColor(R.color.white))
        holder.itemView.notification_tv.text = notificationList[position].message
        holder.itemView.notification_time_tv.text = Utils.getInstance().changeTimeToRelativeTime(Utils.getInstance().getTimeStampFromDate(notificationList[position].created!!))
        holder.itemView.setOnClickListener {
            defaultAddressClickSubject.onNext(holder.layoutPosition)
        }
    }


    override fun getItemCount(): Int {
        return notificationList.size
    }

    class NotificationHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}