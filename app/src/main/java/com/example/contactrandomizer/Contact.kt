package com.example.contactrandomizer

import android.content.*
import android.os.Parcelable
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import kotlinx.android.parcel.Parcelize

/**
 * Data class for storing contact information.
 */
@Parcelize
data class Contact(val name: String, val phoneNumber: String, val id: String, val phoneType: Int) :
    Parcelable

/**
 * Retrieves all contacts which have any phone number. Returns array of contacts sorted in
 * alphabetical order. Note that contact with multiple phone numbers will be considered as
 * multiple contacts with different phone numbers.
 */
fun Context.fetchAllContacts(): ArrayList<Contact> {
    val selection: String =
        ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1" + " AND " +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1"

    val sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"

    contentResolver.query(
        CommonDataKinds.Phone.CONTENT_URI,
        null,
        selection,
        null,
        sortOrder
    )
        .use { cursor ->
            if (cursor == null) return arrayListOf()
            val contacts = ArrayList<Contact>()
            while (cursor.moveToNext()) {
                val name =
                    cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME))
                val id =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val phoneNumber =
                    cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER))
                val phoneType =
                    cursor.getInt(cursor.getColumnIndex(CommonDataKinds.Phone.TYPE))

                contacts.add(Contact(name, phoneNumber, id, phoneType))
            }
            return contacts
        }
}

/**
 * Finds contact by id, then replaces it's phone number with a given phone number.
 */
fun Context.modifyContact(contactId: String, phoneType: Int, phoneNumber: String) {
    val contentValues = ContentValues()
    val rn = randomizePhoneNumber(phoneNumber)
    contentValues.put(CommonDataKinds.Phone.NUMBER, rn)

    val where: String = ContactsContract.Data._ID + '=' + contactId + " AND " +
            ContactsContract.Data.MIMETYPE + " = '" +
            CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" + " AND " +
            CommonDataKinds.Phone.TYPE + " = " + phoneType

    contentResolver.update(ContactsContract.Data.CONTENT_URI, contentValues, where, null)
}
/**
 * Changes phone number: function replaces one digit at a random position in the given phone number
 * by a random digit. New digit is guaranteed to differ from previous digit.
 */
fun randomizePhoneNumber(phoneNumber: String): String {
    val indices = ArrayList<Int>()
    for (i in phoneNumber.indices) {
        if (phoneNumber[i].isDigit()) {
            indices.add(i)
        }
    }

    val randomIndex = indices.random()
    val newDigit = (0..9).filter { it != phoneNumber[randomIndex].toInt() }.random()

    return phoneNumber.substring(
        0,
        randomIndex
    ) + newDigit + phoneNumber.substring(randomIndex + 1)
}