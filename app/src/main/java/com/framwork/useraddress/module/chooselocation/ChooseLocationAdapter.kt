package com.framwork.useraddress.module.chooselocation

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.base.AddressList
import com.framwork.useraddress.lib.interfaces.OnRecyclerClickListener
import com.framwork.useraddress.lib.utils.hide
import com.framwork.useraddress.lib.utils.inflate
import com.framwork.useraddress.lib.utils.show
import kotlinx.android.synthetic.main.row_choose_location.view.*

/**
 * ChooseLocationAdapter
 *
 * @author Aditi Shirsat
 */
class ChooseLocationAdapter(
    private var mList: ArrayList<AddressList>,
    var onRecyclerClickListener: OnRecyclerClickListener
) :
    RecyclerView.Adapter<ChooseLocationAdapter.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        return ViewHolder(parent.inflate(R.layout.row_choose_location))
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Set Data
        holder.itemView.tv_title?.text =
            String.format("%s %s", mContext.getString(R.string.location), mList[position].id)
        holder.itemView.tv_address?.text = String.format(
            "%s, %s ,%s, %s",
            mList[position].address_line1,
            mList[position].address_line2,
            mList[position].city,
            mList[position].pin_code
        )

        if (position == mList.size - 1) {
            holder.itemView.view_divider.hide()
        } else {
            holder.itemView.view_divider.show()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}