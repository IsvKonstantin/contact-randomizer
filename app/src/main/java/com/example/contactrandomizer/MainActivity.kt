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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val WRITE_CONTACTS_PERMISSION = 11

class MainActivity : AppCompatActivity() {

    private lateinit var contacts: ArrayList<Contact>
    private var recyclerViewWasBuilt: Boolean = false

    /**
     * Checks if bundle was passed.
     * If bundle with contacts was passed, builds recycler view.
     * Sets on click listener for action button.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            contacts = savedInstanceState.getParcelableArrayList<Contact>("contacts")!!
            buildRecyclerView()
        }

        buttonRandomize.setOnClickListener {
            loadContactsTest()
        }
    }


    /**
     * Checks if user gave permission to access contacts and loads them if permission was given.
     */
    private fun loadContactsTest() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_CONTACTS),
                WRITE_CONTACTS_PERMISSION
            )
            return
        }

        loadContacts()
    }

    /**
     * Opens application settings.
     */
    private fun openApplicationSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(appSettingsIntent, WRITE_CONTACTS_PERMISSION)
    }

    /**
     * Shows a dialog view with an option to open application settings.
     */
    private fun showMessageOKCancel() {
        AlertDialog.Builder(this)
            .setMessage("Application needs access to contacts in order to randomize them.\n\nGo to application settings?")
            .setPositiveButton("OK") { _, _ -> openApplicationSettings() }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    /**
     * Depending on user's answer about providing permissions loads contacts on screen or
     * shows a dialog windows with an option to open application settings.
     */
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

    /**
     * Updates visibility of elements on screen deepening on application state.
     * Hides action button and recycler, shows progress bar with information (or vise versa).
     */
    private fun setVisibility(loading: Boolean) {
        buttonRandomize.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        recyclerView.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        progressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        loadingInfo.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Retrieves contacts and modifies them. Then loads contacts to recycler view.
     * Kotlin coroutines are used in order to prevent blocking UI thread by
     * fetching and modifying operations.
     */
    private fun loadContacts() {
        setVisibility(true)

        GlobalScope.launch {

            contacts = withContext(Dispatchers.IO) { fetchAllContacts() }

            for (contact in contacts) {
                modifyContact(contact.id, contact.phoneType, contact.phoneNumber)
            }
            withContext(Main) {
                buildRecyclerView()

                setVisibility(false)
            }
        }
    }

    /**
     * Builds recyclerView if needed, otherwise updates it's contacts data.
     */
    private fun buildRecyclerView() {
        if (!recyclerViewWasBuilt) {
            val viewManager = LinearLayoutManager(this)
            recyclerView.apply {
                layoutManager = viewManager
                adapter = ContactAdapter(contacts)
            }
        } else {
            recyclerView.adapter!!.notifyDataSetChanged()
        }
    }

    /**
     * Puts information about contacts into a bundle if they were fetched.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (this::contacts.isInitialized) {
            outState.putParcelableArrayList("contacts", contacts)
        }
    }
}
