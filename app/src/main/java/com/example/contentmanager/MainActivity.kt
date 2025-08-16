package com.example.contentmanager
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.content.MediaType.Companion.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ContactsPermissionWrapper {
                    ShowContactsAndImages()
                }
            }
        }
    }
}

@Composable
fun ContactsPermissionWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    if (hasPermission) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.padding(16.dp),
                onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }
            ) {
                Text("Allow Contacts Permission")
            }
        }
    }
}

@Composable
fun ShowContactsAndImages() {
    val context = LocalContext.current
    val contacts = remember { getAllContacts(context) }
    val images = remember { getAllImages(context) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Contacts:", style = MaterialTheme.typography.titleLarge) }
        items(contacts) { contact -> Text(text = contact) }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item { Text("Images:", style = MaterialTheme.typography.titleLarge) }
        items(images) { img -> Text(text = img) }
    }
}

fun getAllContacts(context: android.content.Context): List<String> {
    val contactsList = mutableListOf<String>()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null, null, null, null
    )
    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (it.moveToNext()) {
            val name = it.getString(nameIndex)
            val number = it.getString(numberIndex)
            contactsList.add("$name - $number")
        }
    }
    return contactsList
}

fun getAllImages(context: android.content.Context): List<String> {
    val imageList = mutableListOf<String>()
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val cursor = context.contentResolver.query(
        uri,
        arrayOf(MediaStore.Images.Media.DISPLAY_NAME),
        null, null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )
    cursor?.use {
        val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
        while (it.moveToNext()) {
            val name = it.getString(nameIndex)
            imageList.add(name)
        }
    }
    return imageList
}
