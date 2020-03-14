package com.example.contactrandomizer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 *  Adapter class for RecyclerView containing information about contacts.
 */
class ContactAdapter(private val contacts: List<Contact>) :
    RecyclerView.Adapter<ContactViewHolder>() {

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.contactNameText.text = contacts[position].name
        holder.contactPhoneNumberText.text = contacts[position].phoneNumber
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactViewHolder {

        return ContactViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.contact_card,
                parent,
                false
            )
        )
    }
}