package com.techpuzzle.keopi.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.databinding.ActivityMainBinding
import com.techpuzzle.keopi.ui.caffebars.CafeBarsFragmentDirections
import com.techpuzzle.keopi.utils.connection.ConnectivityManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @ExperimentalCoroutinesApi
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    @DelicateCoroutinesApi
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Keopi)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectivityManager.checkIfNetworkHasInternet()

        navigateToCafeBarsFragmentIfNeeded(intent)

        connectivityManager.isNetworkAvailableLiveData.observe(this) {
            if (it) {
                binding.tvNoInternetConnection.visibility = View.INVISIBLE
            } else {
                binding.tvNoInternetConnection.visibility = View.VISIBLE
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            navigateToCafeBarsFragmentIfNeeded(intent)
        }
    }

    private val TAG = "TAG"
    private fun navigateToCafeBarsFragmentIfNeeded(intent: Intent) {
        Log.d(TAG, "intent etras ${intent.extras}")
        intent.extras?.let {
            val id = it.getString("_id", "empty")
            if (id != "empty") {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController
                val cafeBar = getCafe(it)
                Log.d("TAG", "cafe bar = $cafeBar")
                val action =
                    CafeBarsFragmentDirections.actionCafesFragmentToCafeBarFragment(cafeBar = cafeBar)
                navController.navigate(action)
                removeExtras(intent)
            }
        }
    }

    private fun removeExtras(intent: Intent) {
        intent.removeExtra("_id")
        intent.removeExtra("address")
        intent.removeExtra("bio")
        intent.removeExtra("name")
        intent.removeExtra("capacity")
        intent.removeExtra("betting")
        intent.removeExtra("location")
        intent.removeExtra("cityId")
        intent.removeExtra("areaId")
        intent.removeExtra("latitude")
        intent.removeExtra("longitude")
        intent.removeExtra("music")
        intent.removeExtra("age")
        intent.removeExtra("smoking")
        intent.removeExtra("startingWorkTime")
        intent.removeExtra("endingWorkTime")
        intent.removeExtra("picture")
        intent.removeExtra("instagram")
        intent.removeExtra("facebook")
        intent.removeExtra("terrace")
        intent.removeExtra("dart")
        intent.removeExtra("billiards")
        intent.removeExtra("hookah")
        intent.removeExtra("playground")
    }

    private fun getCafe(bundle: Bundle): CafeBar {
        return CafeBar(
            id = bundle.getString("_id", ""),
            address = bundle.getString("address", ""),
            bio = bundle.getString("bio", ""),
            name = bundle.getString("name", ""),
            capacity = bundle.getString("capacity", ""),
            betting = bundle.getString("betting", "").toBoolean(),
            location = bundle.getString("location", ""),
            cityId = bundle.getString("cityId", ""),
            areaId = bundle.getString("areaId", ""),
            latitude = bundle.getString("latitude", ""),
            longitude = bundle.getString("longitude", ""),
            music = bundle.getString("music", ""),
            age = bundle.getString("age", ""),
            smoking = bundle.getString("smoking", "").toBoolean(),
            startingWorkTime = bundle.getString("startingWorkTime", "").toInt(),
            endingWorkTime = bundle.getString("endingWorkTime", "").toInt(),
            picture = bundle.getString("picture", ""),
            instagram = bundle.getString("instagram", ""),
            facebook = bundle.getString("facebook", ""),
            terrace = bundle.getString("terrace", "").toBoolean(),
            dart = bundle.getString("dart", "").toBoolean(),
            billiards = bundle.getString("billiards", "").toBoolean(),
            hookah = bundle.getString("hookah", "").toBoolean(),
            playground = bundle.getString("playground", "").toBoolean(),
        )
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        connectivityManager.shouldCheckInternet = false
        super.onDestroy()
    }
}