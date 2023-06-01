package com.almasu.myapplication.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.almasu.myapplication.R
import com.almasu.myapplication.databinding.DeleteLayoutBinding
import com.almasu.myapplication.databinding.SendMsgBinding
import com.almasu.myapplication.models.ModelMessage
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter (
    var context: Context,
    messagesList: ArrayList<ModelMessage>?,
    senderRoom: String,
    receiverRoom: String
):RecyclerView.Adapter<RecyclerView.ViewHolder?>()
{

    lateinit var messagesList: ArrayList<ModelMessage>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    private val senderRoom: String
    var receiverRoom: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == ITEM_SENT){
            val view = LayoutInflater.from(context).inflate(
                R.layout.send_msg,
                parent, false)
            SentMsgHolder(view)
        } else{
            val view = LayoutInflater.from(context).inflate(
                R.layout.receive_msg,
                parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = messagesList[position]
        return if (FirebaseAuth.getInstance().uid == messages.senderId){
            ITEM_SENT
        } else{
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]
        if (holder.javaClass == SentMsgHolder::class.java){
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.messageTv.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.messageTv.text = message.message
            viewHolder.itemView.setOnLongClickListener {

                val view = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Удаленное сообщение")
                    .setView(binding.root)
                    .create()
                binding.everyoneDelete.setOnClickListener {
                    message.message = "Это сообщение удалено"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()
                }

                binding.forMeDelete.setOnClickListener {
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }

                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        } else{
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.messageTv.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.messageTv.text = message.message
            viewHolder.itemView.setOnLongClickListener {

                val view = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Удаленное сообщение")
                    .setView(binding.root)
                    .create()
                binding.everyoneDelete.setOnClickListener {
                    message.message = "Это сообщение удалено"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()
                }

                binding.forMeDelete.setOnClickListener {
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }

                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }
    }

    override fun getItemCount(): Int = messagesList.size

    inner class SentMsgHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var binding:SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var binding:SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    init {
        if (messagesList != null){
            this.messagesList = messagesList
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }
}