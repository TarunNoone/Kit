package com.taruninc.kit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.taruninc.kit.database.User
import kotlinx.android.synthetic.main.user_view.view.*

class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var userList = emptyList<User>()

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.user_view,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = userList[userList.size - 1 - position]
        holder.itemView.tv_firstname.text = currentItem.userFirstName
        holder.itemView.tv_lastname.text = currentItem.userLastName

        holder.itemView.user_view_layout.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToEditUserFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun setData(user: List<User>) {
        userList = user
        notifyDataSetChanged()
    }

}