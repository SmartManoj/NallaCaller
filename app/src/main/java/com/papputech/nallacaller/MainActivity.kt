package com.papputech.nallacaller
//நல்ல காலர்

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.io.PrintWriter
import java.io.StringWriter
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import coil.compose.rememberImagePainter
import io.ktor.client.HttpClient
import io.ktor.client.features.get
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.input.ImeAction


data class Contact(
    val name: String,
    val number: String,
    val profileImage: Uri?
//    val averageDuration: Int,
//    val totalCalls: Int
)
data class Call(
    val name: String, 
    val number: String,
    val profileImage: Uri?,
    val status: String?, 
)

class CallStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val telephony = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        when (telephony.callState) {
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call has ended, make your request here
                CoroutineScope(Dispatchers.IO).launch {
                    makeRequestWithNullNumber(context)
                }
            }
        }
    }
}
fun API_URL(): String {
    return "https://nallacaller.pythonanywhere.com/calls"
//    return "https://42f5-103-154-35-167.ngrok-free.app/calls"
}
fun makeRequestWithNullNumber(context: Context) {
    // Here's a simple example using OkHttp
    val client = OkHttpClient()
    var url = API_URL()
    val requestBody = RequestBody.create(
        "application/json; charset=utf-8".toMediaTypeOrNull(),
        "{\"number\":null,\"myPhoneNumber\":\"${
                                     getPhoneNumber(
                                         context
                                     )
                                 }\"}"
    )
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()
    client.newCall(request).execute().use { response ->
        {
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val res = (response.body!!.string())
        }
                             }

}

@Composable
fun ContactList(contacts: List<Contact>) {
    LazyColumn {
        items(contacts) { contact ->
//            val minutes = contact.averageDuration / 60
//            val seconds = contact.averageDuration % 60
//            val durationText = buildString {
//                if (minutes > 0) {
//                    append("${minutes}m ")
//                }
//                append("${seconds}s")
//            }
            androidx.compose.material3.Text(
                text = "Name: ${contact.name}",
                        fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.material3.Text(
                text = "Number: ${contact.number}"
            )
//            androidx.compose.material3.Text(
//                text = "Avg Duration: $durationText"
//            )
        }
    }
}

fun getContactPhotoUri(context: Context, number: String): Uri? {
    // Try to fetch the contact's ID based on their phone number
    val contactUri: Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
    val projection = arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.PHOTO_URI)

    context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val photoUriIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)
            val photoUriStr = if (photoUriIndex != -1) cursor.getString(photoUriIndex) else null
            if (!photoUriStr.isNullOrEmpty()) {
                return Uri.parse(photoUriStr)
            }
        }
    }

    // Return a default photo if the contact photo doesn't exist
    val packageName = context.packageName
    return Uri.parse("android.resource://$packageName/drawable/unk")
}


fun fetchCallLogs(context: Context): List<Call> {
    val sortOrder = "${CallLog.Calls.DATE} DESC"
    val contentResolver = context.contentResolver
    val callLogsList = mutableListOf<Call>()
    val cursor = contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        null,
        null,
        null,
        sortOrder
    )
    // total cursor length

//    callLogsList.add(Call(cursor?.count.toString() , "=", R.drawable.call))
    var numbers = mutableListOf<String>()
    var i = 0
    cursor?.use {
        while (it.moveToNext()) {
            val nameIndex =
                it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val photoIndex = it.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)
            
            i += 1
            if (nameIndex != -1 && numberIndex != -1) {
                val number = it.getString(numberIndex)
                val name = it.getString(nameIndex)
                // callLogsList as set
        val packageName = context.packageName
                var photoUri = if (photoIndex == -1) {
                    getContactPhotoUri(context,number)
                } else{
                    val photoUriString = it.getString(photoIndex)
                    if (photoUriString != null)
                        Uri.parse(photoUriString)
                    else
                        getContactPhotoUri(context,number)
                }


                if (number !in numbers){
                    if (name==null){
                        callLogsList.add(Call("Unknown",number, null,null))
                    }else{
                    callLogsList.add(Call(name, number, photoUri,null))
                    }
                    numbers.add(number)
                }
//                Toast.makeText(context,name,Toast.LENGTH_LONG).show()
//                val avgDuration = fetchAverageCallDuration(context, number)
//                if (avgDuration[0] > 0)

            } else {
                Log.e("fetchContacts", "Column not found.")
            }
        }
    }
    return callLogsList
}

fun fetchContacts(context: Context): List<Contact> {
   
    val contactsList = mutableListOf<Contact>()

    try {

        val contentResolver = context.contentResolver
        val selection = "${ContactsContract.Contacts.STARRED}=?"
        val selectionArgs = arrayOf("1")

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )
        val cursor2 = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        var names = mutableListOf<String>()

        fun iterateContact(it: android.database.Cursor){
            val nameIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                 val photoIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                    val photoUri =  if (photoIndex != -1) {
                                val contactPhotoUriStr = it.getString(photoIndex)
                                if (!contactPhotoUriStr.isNullOrEmpty()) {
                                     Uri.parse(contactPhotoUriStr)
                                } else null
                            } else null
                if (nameIndex != -1 && numberIndex != -1) {
                    val name = it.getString(nameIndex)
                    val number = it.getString(numberIndex)

//                    val avgDuration = fetchAverageCallDuration(context, number)
//                    if (avgDuration[0] > 0)
                    if (name !in names) {
                        contactsList.add(Contact(name, number, photoUri))
                        names.add(name)
                    }
                } else {
                    Log.e("fetchContacts", "Column not found.")
                }
        }
        cursor?.use {
            while (it.moveToNext()) {
                iterateContact(it)
            }
        }
        cursor2?.use {
            while (it.moveToNext()) {
                iterateContact(it)
            }
        }

    } catch (e: Exception) {
        Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val exceptionAsString = sw.toString()
        Log.e("ex",exceptionAsString)
    }
    return contactsList

}
private fun fetchAverageCallDuration(context: Context, number: String): List<Int> {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        arrayOf(CallLog.Calls.DURATION),
        "${CallLog.Calls.NUMBER} = ?",
        arrayOf(number),
        null
    )

    var totalDuration = 0
    var callCount = 0

    cursor?.use {
        while (it.moveToNext()) {
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

            if (durationIndex != -1) {
                val duration = it.getInt(durationIndex)
                if (duration>0){
                    totalDuration += duration
                    callCount++
                }
            } else {
                Log.e("fetchAverageCallDuration", "Duration column not found.")
            }
        }
    }

    return listOf(if (callCount > 0) totalDuration / callCount else 0,callCount)
}
fun savePhoneNumber(context: Context, phoneNumber: String) {
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("myPhoneNumber", phoneNumber)
    editor.apply()
}
fun getPhoneNumber(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString("myPhoneNumber", null)  // Returns null if not found
}
 val DarkColorPalette = darkColorScheme(
    primary = Color.Gray,  // Based on the icon color
    onPrimary = Color.White,
    background = Color(0xFF2C2C2C),  // Roughly the background color of the image
    onBackground = Color.Gray,  // Text color on background
    surface = Color(0xFF3C3C3C),  // Roughly the color of each item background
    onSurface = Color.White  // Text color on surface
)
    val LightColorPalette = lightColorScheme(
    primary = Color.Blue,  // For variety
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.LightGray,
    onSurface = Color.Black
)
//@Composable
//fun ThemedApp() {
//    var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme()) }
//    ToggleButton(value = isDarkTheme, onValueChange = { isDarkTheme = !isDarkTheme })
//
//    MyApp(isDarkTheme = isDarkTheme) {
//        // Your main app content
//    }
//}

@Composable
fun MyApp(isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (isDarkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(colorScheme = colorScheme, content = content)

}
class MainActivity : ComponentActivity() {
    lateinit var myPhoneNumber : String

    private lateinit var requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var smsRoleResultLauncher: ActivityResultLauncher<Intent>
//    private val CHANGE_DEFAULT_DIALER_CODE = 25

//    private fun offerReplacingDefaultDialer() {
//        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
//            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
//            startActivity(intent)
//        } else {
//            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
//            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
//            startActivityForResult(intent, CHANGE_DEFAULT_DIALER_CODE)
//        }
//    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//offerReplacingDefaultDialer()
// if (false) {
        try {

            if (true) {
                val launcher =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            requestMultiplePermissionsLauncher = registerForActivityResult(
                                ActivityResultContracts.RequestMultiplePermissions()
                            ) { permissions ->
                                val granted = permissions.entries.all { it.value }
                                if (granted) {
                                    // Fetch contacts
                                    // Your logic to fetch contacts goes here
                                } else {
                                    // Handle permissions denial
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                                }
                            }
                            requestMultiplePermissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CONTACTS,
                                    Manifest.permission.READ_CALL_LOG,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.CALL_PHONE,
                                    Manifest.permission.ANSWER_PHONE_CALLS,
                                    Manifest.permission.READ_PHONE_NUMBERS,
                                    Manifest.permission.READ_SMS,
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.SEND_SMS,

                                    )
                            )
                        } else {
//        Toast.makeText(this, "Failed to set as default dialer.", Toast.LENGTH_SHORT).show()
                        }

                    }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                    if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
                            RoleManager.ROLE_DIALER
                        )
                    ) {
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                        launcher.launch(intent)
                    }
                }



                Log.i("dt:", "oncreate")
                smsRoleResultLauncher =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            requestMultiplePermissionsLauncher = registerForActivityResult(
                                ActivityResultContracts.RequestMultiplePermissions()
                            ) { permissions ->
                                val granted = permissions.entries.all { it.value == true }
                                if (granted) {
                                    // Fetch contacts
                                    // Your logic to fetch contacts goes here
                                } else {
                                    // Handle permissions denial
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                                }
                            }
                            requestMultiplePermissionsLauncher.launch(
                                arrayOf(
//            Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG,
//            Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,
//            Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_PHONE_NUMBERS,
                                    Manifest.permission.READ_SMS,
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.SEND_SMS,

                                    )
                            )
                        } else {
                            // App was not granted the role
                            //    toast("App was not granted the role")
                        }
                    }
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                )
                    smsRoleResultLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS))

                val telephonyManager =
                    getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    myPhoneNumber = telephonyManager.line1Number
                    savePhoneNumber(this, myPhoneNumber)

                } else {
                    // Request permission
//    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), YOUR_REQUEST_CODE)
                }


            }
            // Declare this outside any methods but inside your class


// When you want to set your app as the default dialer
//val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager


            // Initialize permission request launcher


            setContent {
                MainScreen()
            }
        } catch (e: Exception) {

        }
        // Request permissions

    }

    private fun toast(s: String) {
//        TODO("Not yet implemented")
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }


}

@Composable
fun MainScreen2() {

    val contacts = fetchContacts(LocalContext.current)
    ContactList(contacts)

}
@Composable
fun DialerScreen(context: Context) {
    var dialedNumber by remember { mutableStateOf("") }
    // text field to display the dialed number
    Text(text = dialedNumber)
    
    DialPad(
        onNumberClick = { number ->
            if (dialedNumber == "X") {
                // Handle backspace
                dialedNumber = dialedNumber.dropLast(1)
            }
            dialedNumber += number
            // Update your UI with the dialed number
            
        },
        onCallClick = {
            // Handle call initiation
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$dialedNumber"))
             startActivity(context, intent, null)
        }
    )
}

@Composable
fun HomeScreen(context: Context) {


    LazyColumn {
        val callLogs = fetchCallLogs(context)
        items(callLogs) { call ->
            CallItem(call)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchExecute: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = {
            onQueryChange(it)
        },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
        },
        placeholder = {
            Text(text = "Search contacts...")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchExecute()
            }
        )
    )
}

@Composable
fun ContactsScreen(context: Context) {
    var query by remember { mutableStateOf("") }

    val contactsList  = fetchContacts(context)
    val filteredContacts = if (query.isEmpty()) {
        contactsList
    } else {
        contactsList.filter { contact ->
            contact.name.contains(query, ignoreCase = true)
                    }
    }
    Column {
        ContactsSearchBar(
            query = query,
            onQueryChange = {
                query = it
            },
            onSearchExecute = {
//                // Here, you filter the contacts based on the search query
//                filteredContacts = contactsList.filter { contact ->
//                    contact.name.contains(query, ignoreCase = true)
//                }
            }
        )

        LazyColumn {
            items(filteredContacts) { contact ->
                ContactItem(contact = contact)
            }
        }
    }
}
@Composable
fun showAlert(s: String){
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text("Alert")
        },
        text = {
            Text(s)
        },
        confirmButton = {
            TextButton(onClick = { }) {
                Text("ACCEPT")
            }
        },
        dismissButton = {
            TextButton(onClick = { }) {
                Text("DECLINE")
            }
        }
    )
}

@Composable
fun UploadCallLogDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Upload Call Logs Information")
        },
        text = {
            Text("This app is about to upload users' Call Log information to the server. So, that the other user will call you at the right time. Be a Good caller and accept it.")
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("ACCEPT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("DECLINE")
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    MyApp {
         var showDialog by remember { mutableStateOf(true) }
    
        if (showDialog) {
            UploadCallLogDialog(
                onAccept = {
                    // Handle action for accepted permission
                    showDialog = false
                },
                onDismiss = {
                    // Handle action for declined permission
                    showDialog = false
                }
            )
        }
        var context = LocalContext.current
//        var selectedScreen by remember { mutableStateOf("Dialer") }
            var selectedScreen by remember { mutableStateOf("Home") }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("நல்ல காலர் \uD83E\uDD70") })
            },
            bottomBar = {
                NavigationBar {

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        selected = selectedScreen == "Home",
                        onClick = {
                            selectedScreen = "Home"
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = null) },
                        label = { Text("Contacts") },
                        selected = selectedScreen == "Contacts",
                        onClick = {
                            selectedScreen = "Contacts"
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = null) },
                        label = { Text("SMS") },
                        selected = selectedScreen == "SMS",
                        onClick = {
                            selectedScreen = "SMS"
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Dialer") },
                        selected = selectedScreen == "Dialer",
                        onClick = {
                            selectedScreen = "Dialer"
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                when (selectedScreen) {
                    "Home" -> HomeScreen(context)
                    "Contacts" -> ContactsScreen(context)
                    "SMS" -> test(context)
                    "Dialer" -> DialerScreen(context)
                }
            }
        }
    }
}
fun test(context: Context){
    Toast.makeText(context, "comming soon", Toast.LENGTH_SHORT).show()

}
suspend fun isUserSpeaking(number: String): Boolean {
//    return true
    val client = OkHttpClient()
    val url = "${API_URL()}?user=$number"

    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        return response.body?.string() != "0"
    }
}
@Composable
fun CallItem(call: Call) {
    // Remember the user's speaking status (either true or false)
//    val isSpeaking by produceState(initialValue = false, key1 = call.number) {
//        value = withContext(Dispatchers.IO) {
//            isUserSpeaking(call.number)
//        }
//    }
    var name = call.name
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* Handle call item click */ },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            contentAlignment = Alignment.TopEnd // To position the dot on the top right corner of the image
        ) {
            // Profile Image
            Image(
                painter = rememberImagePainter(data = call.profileImage),
                contentDescription = "null",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Display dot based on user's speaking status
//            if (isSpeaking) {
//                Canvas(modifier = Modifier.size(8.dp)) {
//                    drawCircle(color = Color.Red)
//                }
//            } else {
//                Canvas(modifier = Modifier.size(8.dp)) {
//                    drawCircle(color = Color(0xFF006400))
//                }
//            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = if (!name.isNullOrEmpty()) name else call.number  , fontWeight = FontWeight.Bold)
//            Text(text = call.number)
            Text(text = "Incoming Call")
        }

        callButton(call.number)
    }
}
@Composable
fun DialPad(onNumberClick: (String) -> Unit, onCallClick: () -> Unit) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("*", "0", "#"),
        listOf(" ", " ", "X")
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { number ->
                    Button(onClick = { onNumberClick(number) }) {
                        Text(text = number)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCallClick) {
            Icon(imageVector = Icons.Default.Call, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Call")
        }
    }
}


@Composable
fun ContactItem(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* Handle call item click */ },
        horizontalArrangement = Arrangement.Start
    ) {
        // Profile Image
        Image(
            painter = rememberImagePainter(data = contact.profileImage),
            contentDescription = null,  // Use `null` for decorative images
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .align(Alignment.CenterVertically),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Contact Info (Name and Number)
        Column(
            modifier = Modifier
                .weight(1f)  // This will make the column occupy the maximum available width
                .align(Alignment.CenterVertically)
        ) {
            Text(text = contact.name, fontWeight = FontWeight.Bold)
            Text(text = contact.number)
//            Text(text = "+91 9876 543 210")
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Call Button
        callButton(contact.number)
    }
}



@Composable
fun callButton(number: String){
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

     Box(
             modifier = Modifier
                 .size(30.dp)
                 .clickable {
                     // Handle call icon click to initiate the call
                     scope.launch {
                         CoroutineScope(Dispatchers.IO).launch {
                             var url = API_URL()
                             var data = "{\"number\":\"${number}\"}"
                             //  maka a post request
                             val client = OkHttpClient()

                             val requestBody = RequestBody.create(
                                 "application/json; charset=utf-8".toMediaTypeOrNull(),
                                 "{\"number\":\"${number}\",\"myPhoneNumber\":\"${
                                     getPhoneNumber(
                                         context
                                     )
                                 }\"}"
                             )

                             val request = Request
                                 .Builder()
                                 .url(url)
                                 .post(requestBody)
                                 .build()

                             client
                                 .newCall(request)
                                 .execute()
                                 .use { response ->
                                     if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                     val res = (response.body!!.string())
                                     withContext(Dispatchers.Main) {
                                         // Update your UI here
                                         Toast
                                             .makeText(
                                                 context,
                                                 "OG2: $number $res",
                                                 Toast.LENGTH_LONG
                                             )
                                             .show()
                                         Log.d("nc2", "OG2: $number $res")
                                     }
                                 }
                         }
                     }
                     // make a call as a default dialer
                     val intent = Intent(Intent.ACTION_CALL)
                     intent.data = Uri.parse("tel:$number")
                     startActivity(context, intent, null)

                 }
        ) {
            Image(
                painter = painterResource(id = R.drawable.call),
                contentDescription = "Call icon",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
}