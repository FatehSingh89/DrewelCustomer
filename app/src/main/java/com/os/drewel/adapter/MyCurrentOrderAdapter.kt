package com.os.drewel.adapter

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.activity.*
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.myorderresponsemodel.Order
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.Constants.CURRENT_ORDER
import com.os.drewel.delegate.OnClickItem
import com.os.drewel.prefrences.Prefs
import com.os.drewel.rxbus.CartRxJavaBus
import com.os.drewel.utill.CommonUtil
import com.os.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_my_order_item.view.*
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by monikab on 3/13/2018.
 */

class MyCurrentOrderAdapter(val mContext: Context?, private val myCurrentOrderList: List<Order>, val TYPE: Int, var onClickItem: OnClickItem) : RecyclerView.Adapter<MyCurrentOrderAdapter.MyCurrentOrderHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCurrentOrderHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_my_order_item, parent, false)
        return MyCurrentOrderHolder(view)
    }

    override fun onBindViewHolder(holder: MyCurrentOrderHolder, position: Int) {
        if (TYPE == CURRENT_ORDER) {
            holder.itemView.btn_reorder.visibility = View.GONE
            holder.itemView.btn_edit.visibility = View.GONE
            if (holder.timer != null) {
                holder.timer!!.cancel()
            }
            holder.itemView.btn_delete.visibility = View.GONE

            if (myCurrentOrderList[position].is_edited!!.isNotEmpty()) {
                if (myCurrentOrderList[position].is_edited == "1") {
                    holder.itemView.btn_edit.visibility = View.GONE
                } /*else
                    startTimer(myCurrentOrderList[position], holder)*/
            }
            if (myCurrentOrderList[position].stopTimer!!) {
                if (holder.timer != null) {
                    holder.timer!!.cancel()
                }
            } else
                startTimer(myCurrentOrderList[position], holder)
        } else {
            holder.itemView.btn_reorder.visibility = View.VISIBLE
            holder.itemView.btn_edit.visibility = View.GONE
            holder.itemView.btn_delete.visibility = View.GONE
        }

        val order = myCurrentOrderList[position]
        holder.itemView.order_item_txt_order_id.text = order.orderId
        holder.itemView.order_item_txt_order_delivery_date.text = Utils.getInstance().convertTimeFormat(order.deliveryDate!!, "yyyy-MM-dd", "MMM dd, yyyy")
        if (mContext != null) {
            val deliveryTime = mContext.getString(R.string.from) + " " + Utils.getInstance().convertTimeFormat(order.deliveryStartTime!!, "HH:mm:ss", "hh:mm a") + " " + mContext.getString(R.string.to) + " " + Utils.getInstance().convertTimeFormat(order.deliveryEndTime!!, "HH:mm:ss", "hh:mm a")
            holder.itemView.order_item_txt_order_delivery_time.text = deliveryTime
        }
        holder.itemView.order_item_txt_no_of_items.text = order.totalQuantity
        val amount = NumberFormat.getInstance().format(order.totalAmount?.toDouble()) + " " + mContext?.getString(R.string.omr)
        holder.itemView.order_item_txt_order_amount.text = amount
        holder.itemView.order_item_txt_payment_method.text = order.paymentMode
        holder.itemView.order_item_txt_order_status.text = order.orderDeliveryStatus
    }

    @SuppressLint("SimpleDateFormat")
    fun startTimer(order: Order, holder: MyCurrentOrderHolder) {
        if (!(order.order_date).isNullOrEmpty() && !(order.server_time).isNullOrEmpty()) {
            val time = dateDifference(order.order_date!!, order.server_time!!)
//            if (time > 0) {

            var min2 = TimeUnit.MINUTES.toMillis(10)
            var actualTime = min2 - TimeUnit.MILLISECONDS.toMillis(time)
            Log.e("time=", TimeUnit.MILLISECONDS.toMinutes(time).toString() + ", min2=" + min2 + " actualTime==" + TimeUnit.MILLISECONDS.toMinutes(actualTime) + " position=" + holder.adapterPosition)
            if (TimeUnit.MILLISECONDS.toMillis(time) < TimeUnit.MINUTES.toMillis(10)) {
                if (myCurrentOrderList[holder.adapterPosition].is_edited!!.isNotEmpty()) {
                    if (myCurrentOrderList[holder.adapterPosition].is_edited == "1") {
                        holder.itemView.btn_edit.visibility = View.GONE
                    } /*else
                    startTimer(myCurrentOrderList[position], holder)*/
                }
                holder.itemView.btn_delete.visibility = View.VISIBLE
                var millisUntil: Long? = 0
                holder.timer = object : CountDownTimer(actualTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val h = Handler(Looper.getMainLooper())
                        h.post(Runnable {
                            run {
                                val secondsInMilli = 1000
                                val minutesInMilli = secondsInMilli * 60
                                val hoursInMilli = minutesInMilli * 60
                                val daysInMilli = hoursInMilli * 24
                                var different = millisUntilFinished
                                val elapsedDays = different / daysInMilli
                                different %= daysInMilli
                                val elapsedHours = different / hoursInMilli
                                different %= hoursInMilli

                                val elapsedMinutes = different / minutesInMilli
                                different %= minutesInMilli
                                val elapsedSeconds = different / secondsInMilli
                                Log.e("Time diff is ",
                                        "%d days, %d hours, %d minutes, %d seconds%n" +
                                                elapsedDays + ", " + elapsedHours + ", " + elapsedMinutes + ", " + elapsedSeconds)
                            }
                        })
                    }

                    override fun onFinish() {
                        holder.itemView.btn_delete.visibility = View.GONE
                        holder.itemView.btn_edit.visibility = View.GONE
                    }
                }.start()
            } else {
                holder.itemView.btn_edit.visibility = View.GONE
                holder.itemView.btn_delete.visibility = View.GONE
            }

//            } else {
//                holder.itemView.btn_delete.visibility = View.GONE
//                holder.itemView.btn_edit.visibility = View.GONE
////                }
//            }
        }
    }

    fun dateDifference(startDate: String, endDate: String): Long {
        Log.e("startDate==>", startDate + ", endDate==>" + endDate)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var difference: Long? = null
        try {
            val start: Date = simpleDateFormat.parse(startDate)
            val end: Date = simpleDateFormat.parse(endDate)
            difference = end.time - start.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return difference!!
    }

    var progressDialog: ProgressDialog? = null
    private fun callReorderApi(position: Int, itemView: View) {
        itemView.btn_reorder.isEnabled = false
        val pref = Prefs.getInstance(mContext)
        progressDialog = CommonUtil.showLoadingDialog(mContext!!)

        val reorderRequest = HashMap<String, String>()
        reorderRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        reorderRequest["language"] = DrewelApplication.getInstance().getLanguage()
        reorderRequest["order_id"] = myCurrentOrderList[position].orderId!!

        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).reorder(reorderRequest)

        val disposable = cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)
                    itemView.btn_reorder.isEnabled = true
                    progressDialog?.dismiss()
                    Toast.makeText(mContext, result.response!!.message, Toast.LENGTH_SHORT).show()
                    if (result.response!!.status!!) {
                        pref.setPreferenceStringData(pref.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)
                    }
                }, { error ->
                    itemView.btn_reorder.isEnabled = true
                    progressDialog?.dismiss()
                    Toast.makeText(mContext, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    override fun getItemCount(): Int {
        return myCurrentOrderList.size
    }

    fun stopTimer() {
//        if (timer != null)
//            timer!!.cancel()
    }

    inner class MyCurrentOrderHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var timer: CountDownTimer? = null

        init {
            itemView.setOnClickListener(this)
            itemView.btn_reorder.setOnClickListener(this)
            itemView.btn_edit.setOnClickListener(this)
            itemView.btn_delete.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.myOrderItemCardView -> {
                    val intent = Intent(mContext, MyOrderDetailActivity::class.java)
                    intent.putExtra(AppIntentExtraKeys.ORDER_ID, myCurrentOrderList[adapterPosition].orderId)
                    mContext?.startActivity(intent)
                }
                R.id.btn_reorder -> {
                    val logoutAlertDialog = AlertDialog.Builder(mContext!!, R.style.DeliveryTypeTheme).create()
                    logoutAlertDialog.setTitle(mContext!!.getString(R.string.app_name))
                    logoutAlertDialog.setMessage(mContext!!.getString(R.string.want_to_reorder))
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext!!.getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                        callReorderApi(adapterPosition, itemView)
                    })
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext!!.getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                    })
                    logoutAlertDialog.show()
                }
                R.id.btn_delete -> {
                    val logoutAlertDialog = AlertDialog.Builder(mContext!!, R.style.DeliveryTypeTheme).create()
                    logoutAlertDialog.setTitle(mContext!!.getString(R.string.app_name))
                    logoutAlertDialog.setMessage(mContext!!.getString(R.string.want_to_delete_order))
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext!!.getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
//                        callReorderApi(adapterPosition, itemView)
                        onClickItem.onClick("Delete", adapterPosition)
                    })
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext!!.getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                    })
                    logoutAlertDialog.show()
                }
                R.id.btn_edit -> {
                    val logoutAlertDialog = AlertDialog.Builder(mContext!!, R.style.DeliveryTypeTheme).create()
                    logoutAlertDialog.setTitle(mContext!!.getString(R.string.app_name))
                    logoutAlertDialog.setMessage(mContext!!.getString(R.string.want_to_edit))
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext!!.getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                        var pref = Prefs.getInstance(mContext)
                        callEditCartApi(adapterPosition, itemView)
                    })
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext!!.getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                    })
                    logoutAlertDialog.show()
                }
            }
        }
    }

    private fun callEditCartApi(position: Int, itemView: View) {
        itemView.btn_edit.isEnabled = false
        val pref = Prefs.getInstance(mContext)
        progressDialog = CommonUtil.showLoadingDialog(mContext!!)
        val updateCartRequest = HashMap<String, String>()
        updateCartRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        updateCartRequest["language"] = DrewelApplication.getInstance().getLanguage()
        updateCartRequest["order_id"] = myCurrentOrderList[position].orderId!!

        val updateCartObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).editCart(updateCartRequest)
        updateCartObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    itemView.btn_edit.isEnabled = true
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)
                    progressDialog?.dismiss()
                    if (result.response!!.status!!) {
//                        notifyItemRemoved(position)
                        pref.setPreferenceStringData(pref.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)
                        mContext.startActivity(Intent(mContext, CartActivity::class.java))

                    } else {
                        notifyItemRemoved(position)
                        Toast.makeText(mContext, result.response!!.message, Toast.LENGTH_LONG).show()
                    }
                }, { error ->
                    progressDialog?.dismiss()
                    itemView.btn_edit.isEnabled = true
                    notifyItemChanged(position)
                    Toast.makeText(mContext, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }
}