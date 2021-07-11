package com.example.thorium

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.thorium.model.entity.CellInfo
import com.example.thorium.ui.InfoViewModel

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var infoViewModel: InfoViewModel
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        infoViewModel = ViewModelProvider(this).get(InfoViewModel::class.java)
        infoViewModel.info.observe(this, Observer { words ->
            words?.let { updateMarkers(it) }
        })


    }
    private fun getColor(cellInfo: CellInfo) : Float {
        var color: Float
        val type = cellInfo.type
        when(type) {
            "GSM" -> {
                if (cellInfo.strength.toInt() >= -60) color = BitmapDescriptorFactory.HUE_BLUE
                else if (cellInfo.strength.toInt() >= -80) color = BitmapDescriptorFactory.HUE_GREEN
                else if (cellInfo.strength.toInt() >= -100) color = BitmapDescriptorFactory.HUE_YELLOW
                else if (cellInfo.strength.toInt() >= -120) color = BitmapDescriptorFactory.HUE_ORANGE
                else color = BitmapDescriptorFactory.HUE_RED
            }

            "UMTS" -> {
                if (cellInfo.strength.toInt() >= -65) color = BitmapDescriptorFactory.HUE_BLUE
                else if (cellInfo.strength.toInt() >= -85) color = BitmapDescriptorFactory.HUE_GREEN
                else if (cellInfo.strength.toInt() >= -105) color = BitmapDescriptorFactory.HUE_YELLOW
                else if (cellInfo.strength.toInt() >= -120) color = BitmapDescriptorFactory.HUE_ORANGE
                else color = BitmapDescriptorFactory.HUE_RED
            }

            "LTE" -> {
                if (cellInfo.strength.toInt() >= -75) color = BitmapDescriptorFactory.HUE_BLUE
                else if (cellInfo.strength.toInt() >= -95) color = BitmapDescriptorFactory.HUE_GREEN
                else if (cellInfo.strength.toInt() >= -105) color = BitmapDescriptorFactory.HUE_YELLOW
                else if (cellInfo.strength.toInt() >= -125) color = BitmapDescriptorFactory.HUE_ORANGE
                else color = BitmapDescriptorFactory.HUE_RED

            }

            else -> color = BitmapDescriptorFactory.HUE_RED
        }
        return color
    }
    private fun updateMarkers(infos: List<CellInfo>) {
        for (cellInfo in infos) {
            val pos = LatLng(cellInfo.altitude, cellInfo.longitude)
            val color = getColor(cellInfo)

            val snip = "Strength: " + cellInfo.strength + "\n" +
                    "PLMN: " + cellInfo.mcc + cellInfo.mnc + "\n" +
                    "Longitude: " + cellInfo.longitude + " ms\n" +
                  "Altitude: " + cellInfo.altitude + " ms\n"
            val marker = mMap.addMarker(MarkerOptions().icon(
                BitmapDescriptorFactory.defaultMarker(color)).position(
                pos).title(cellInfo.type).snippet(snip))
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.setMyLocationEnabled(true)
        mMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val info = LinearLayout(applicationContext)
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(applicationContext)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title
                val snippet = TextView(applicationContext)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet
                info.addView(title)
                info.addView(snippet)
                return info
            }
        })
    }

}