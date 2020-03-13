package com.example.contactrandomizer

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.contact_card.view.*

class ContactViewHolder (private val root: View) : RecyclerView.ViewHolder(root) {
    val contactNameText: TextView = root.contact_name
    val contactPhoneNumberText: TextView = root.contact_phone_number
}