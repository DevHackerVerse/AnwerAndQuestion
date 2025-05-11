package com.example.answerandquestion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anwerandquestion.R
import com.example.anwerandquestion.databinding.DeleteLayoutBinding
import com.example.anwerandquestion.databinding.ReceiveMsgBinding
import com.example.anwerandquestion.databinding.SendMsgBinding
import com.example.anwerandquestion.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter(
    var context: Context,
    messages: ArrayList<Message>?,
    var senderRoom: String,
    var receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var messages = messages ?: ArrayList()
    private val ITEM_SENT = 1
    private val ITEM_RECEIVE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId) ITEM_SENT else ITEM_RECEIVE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) {
            bindSentMessage(holder, message)
        } else if (holder is ReceiverViewHolder) {
            bindReceivedMessage(holder, message)
        }
    }

    private fun bindSentMessage(holder: SentViewHolder, message: Message) {
        if (message.message == "photo") {
            holder.binding.image.visibility = View.VISIBLE
            holder.binding.message.visibility = View.GONE
            Glide.with(context)
                .load(message.imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.binding.image)
        } else {
            holder.binding.image.visibility = View.GONE
            holder.binding.message.visibility = View.VISIBLE
            holder.binding.message.text = message.message
        }

        holder.itemView.setOnLongClickListener {
            showDeleteDialog(message)
            true
        }
    }

    private fun bindReceivedMessage(holder: ReceiverViewHolder, message: Message) {
        if (message.message == "photo") {
            holder.binding.image.visibility = View.VISIBLE
            holder.binding.message.visibility = View.GONE
            Glide.with(context)
                .load(message.imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.binding.image)
        } else {
            holder.binding.image.visibility = View.GONE
            holder.binding.message.visibility = View.VISIBLE
            holder.binding.message.text = message.message
        }

        holder.itemView.setOnLongClickListener {
            showDeleteDialog(message)
            true
        }
    }

    private fun showDeleteDialog(message: Message) {
        val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
        val binding = DeleteLayoutBinding.bind(view)
        val dialog = AlertDialog.Builder(context)
            .setTitle("Delete Message")
            .setView(binding.root)
            .create()

        binding.everyone.setOnClickListener {
            message.message = "This message is removed."
            updateMessageInDatabase(message)
            dialog.dismiss()
        }
        binding.delete.setOnClickListener {
            deleteMessageInDatabase(message)
            dialog.dismiss()
        }
        binding.cancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun updateMessageInDatabase(message: Message) {
        message.messageId?.let { id ->
            FirebaseDatabase.getInstance().reference.child("chats")
                .child(senderRoom).child("messages").child(id).setValue(message)
            FirebaseDatabase.getInstance().reference.child("chats")
                .child(receiverRoom).child("messages").child(id).setValue(message)
        }
    }

    private fun deleteMessageInDatabase(message: Message) {
        message.messageId?.let { id ->
            FirebaseDatabase.getInstance().reference.child("chats")
                .child(senderRoom).child("messages").child(id).setValue(null)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }
}
