package com.example.contactrandomizer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val WRITE_CONTACTS_PERMISSION = 11

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonRandomize: FloatingActionButton = findViewById(R.id.buttonRandomize)
        buttonRandomize.setOnClickListener {
            loadContactsTest()
        }

    }

    private fun loadContactsTest() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            /*           if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                           showMessageOKCancel()
                           return
                       }*/

            requestPermissions(
                arrayOf(Manifest.permission.WRITE_CONTACTS),
                WRITE_CONTACTS_PERMISSION
            )
            return
        }

        loadContacts()
    }

    private fun openApplicationSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(appSettingsIntent, WRITE_CONTACTS_PERMISSION)
    }

    private fun showMessageOKCancel() {
        AlertDialog.Builder(this)
            .setMessage("Application needs access to contacts in order to randomize them.\n\nGo to application settings?")
            .setPositiveButton("OK") { _, _ -> openApplicationSettings() }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            WRITE_CONTACTS_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    showMessageOKCancel()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setVisibility(loading: Boolean) {
        buttonRandomize.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        recyclerView.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        progressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        loadingInfo.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    private fun loadContacts() {
        setVisibility(true)
        val viewManager = LinearLayoutManager(this)

        GlobalScope.launch {

            val contactList = withContext(Dispatchers.IO) { fetchAllContacts() }

            for (contact in contactList) {
                modifyContact(contact.id, contact.phoneType, contact.phoneNumber)
            }
            withContext(Main) {
                recyclerView.apply {
                    layoutManager = viewManager
                    adapter = ContactAdapter(contactList)
                }

                setVisibility(false)
            }
        }
    }
}
