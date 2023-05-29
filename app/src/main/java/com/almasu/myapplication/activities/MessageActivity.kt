package com.almasu.myapplication.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.almasu.myapplication.databinding.ActivityMessageBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date

class MessageActivity : AppCompatActivity() {

    var binding: ActivityMessageBinding? = null
//    private var adapter: MessageAdapter? = null
//    var messages: ArrayList<ModelMessage>? = null
//    var senderRoom: String? = null
//    var receiverRoom: String? = null
//    var database: FirebaseDatabase? = null
//    var storage: FirebaseStorage? = null
//    var dialog: ProgressDialog? = null
//    var senderUid: String? = null
//    var reseiverUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

//        setSupportActionBar(binding!!.toolbarRl)
//        database = FirebaseDatabase.getInstance()
//        storage = FirebaseStorage.getInstance()
//        dialog = ProgressDialog(this@MessageActivity)
//        dialog!!.setMessage("Загрузка изображения...")
//        dialog!!.setCancelable(false)
////        messages = ArrayList()
//
//        val name = intent.getStringExtra("name")
//        val profile = intent.getStringExtra("image")
//        binding!!.userNameTv.text = name
//        Glide.with(this@MessageActivity).load(profile)
////            .placeholder(R.drawable.placeholder)
//            .into(binding!!.profileIv)
//        binding!!.toolbarBackBtn.setOnClickListener { finish() }
//        reseiverUid = intent.getStringExtra("uid")
//        senderUid = FirebaseAuth.getInstance().uid
//        database!!.reference.child("Presence").child(reseiverUid!!)
//            .addValueEventListener(object :ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()){
//                        val stutus = snapshot.getValue(String::class.java)
//                        if (stutus == "offline"){
//                            binding!!.status.visibility = View.GONE
//                        } else{
//                            binding!!.status.setText(stutus)
//                            binding!!.status.visibility = View.VISIBLE
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })
//
//        senderRoom = senderUid + reseiverUid
//        receiverRoom = reseiverUid + senderUid
////        adapter = MessageAdapter(this@MessageActivity, messages, senderRoom!!, receiverRoom!!)
////
////        binding!!.messageRv.layoutManager = LinearLayoutManager(this@MessageActivity)
////        binding!!.messageRv.adapter = adapter
////        database!!.reference.child("chats")
////            .child(senderRoom!!)
////            .child("message")
////            .addValueEventListener(object : ValueEventListener{
////                override fun onDataChange(snapshot: DataSnapshot) {
////                    messages!!.clear()
////                    for (snapshot1 in snapshot.children ){
////                        val message :Message? = snapshot1.getValue(Message::class.java)
////                        message!!.messageId = snapshot1.key
////                        messages!!.add(message)
////
////                    }
////                    adapter!!.notifyDataSetChanged()
////                }
////
////                override fun onCancelled(error: DatabaseError) {}
////
////            })
////
////        binding!!.sendMessageIv.setOnClickListener {
////            val messageTxt: String = binding!!.messageBoxEt.text.toString()
////            val date = Date()
////            val message = Message(messageTxt, senderUid, date.time)
////
////            binding!!.messageBoxEt.setText("")
////            val randomKey = database!!.reference.push().key
////            val lastMsgObj = HashMap<String, Any>()
////            lastMsgObj["lastMsg"] = message.message!!
////            lastMsgObj["lastMsgTime"] = date.time
////
////            database!!.reference.child("chats").child(senderRoom!!)
////                .updateChildren(lastMsgObj)
////            database!!.reference.child("chats").child(receiverRoom!!)
////                .updateChildren(lastMsgObj)
////            database!!.reference.child("chats").child(senderRoom!!)
////                .child("messages")
////                .child(randomKey!!)
////                .setValue(message).addOnSuccessListener {
////                    database!!.reference.child("chats")
////                        .child(receiverRoom!!)
////                        .child("message")
////                        .child(randomKey)
////                        .setValue(message)
////                        .addOnSuccessListener { }
////                }
////        }
////        binding!!.attachment.setOnClickListener {
////            val intent = Intent()
////            intent.action = Intent.ACTION_GET_CONTENT
////            intent.type = "image/*"
////            startActiityForResult(intent, 25)
////        }
//
//        val handler = Handler()
//        binding!!.messageBoxEt.addTextChangedListener(object :TextWatcher{
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                database!!.reference.child("Presence")
//                    .child(senderUid!!)
//                    .setValue("typing...")
//                handler.removeCallbacksAndMessages(null)
//                handler.postDelayed(userStoppedTyping, 1000)
//            }
//            var userStoppedTyping = Runnable {
//                database!!.reference.child("Presence")
//                    .child(senderUid!!)
//                    .setValue("Online")
//            }
//
//
//        })
//
//        supportActionBar?.setDisplayShowTitleEnabled(false)
//
//    }
//
////    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
////        super.onActivityResult(requestCode, resultCode, data)
////        if (requestCode == 25){
////            if (data != null){
////                if (data.data != null){
////                    val selectedImage = data.data
////                    val calendar = Calendar.getInstance()
////                    var refence = storage!!.reference.child("chats")
////                        .child(calendar.timeInMillis.toString()+"")
////                    dialog!!.show()
////                    refence.putFile(selectedImage!!)
////                        .addOnCompleteListener{ task ->
////                            dialog!!.dismiss()
////                            if (task.isSuccessful){
////                                refence.downloadUrl.addOnSuccessListener { uri ->
////                                    val filePath = uri.toString()
////                                    val messageTxt :String = binding!!.messageBoxEt.text.toString()
////                                    val date = Date()
////                                    val message = Message(messageTxt,senderUid, date.time)
////                                    message.message = "photo"
////                                    massage.imageUrl = filePath
////                                    binding!!.messageBoxEt.setText("")
////                                    val randomkey = database!!.reference.push().key
////                                    val lastMsgObj = HashMap<String, Any>()
////                                    lastMsgObj["lastMsg"] = message.message!!
////                                    lastMsgObj["LastMsg"] = date.time
////                                    database!!.reference.child("chats")
////                                        .updateChildren(lastMsgObj)
////                                    database!!.reference.child("chats")
////                                        .child(receiverRoom!!)
////                                        .updateChildren(lastMsgObj)
////                                    database!!.reference.child("chats")
////                                        .child(senderRoom!!)
////                                        .child("message")
////                                        .child(randomkey!!)
////                                        .setValue(message).addOnSuccessListener {
////                                            database!!.reference.child("chats")
////                                                .child(receiverRoom!!)
////                                                .child("messages")
////                                                .child(randomkey)
////                                                .setValue(message)
////                                                .addOnSuccessListener {  }
////                                        }
////                                }
////                            }
////                        }
////                }
////            }
////        }
////    }
//
//    override fun onResume() {
//        super.onResume()
//        val currentId = FirebaseAuth.getInstance().uid
//        database!!.reference.child("Presence")
//            .child(currentId!!)
//            .setValue("Online")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        val currentId = FirebaseAuth.getInstance().uid
//        database!!.reference.child("Presence")
//            .child(currentId!!)
//            .setValue("offline")
//    }
//
//
//    private fun setSupportActionBar(toolbarRl: RelativeLayout) {
//
//    }

}
}