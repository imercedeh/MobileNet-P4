package com.example.thorium

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thorium.model.entity.CellInfo
import com.example.thorium.ui.InfoViewModel
import com.example.thorium.ui.Adapter
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var infoViewModel: InfoViewModel
    private var current_location: Location? = null
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    //For Project 4
    var jitter = 0
    var jitter_counter = 0
    var latency : Long = 0
    var content_latency :Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = Adapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val start_sample_btn = findViewById<Button>(R.id.start_sample)


        infoViewModel = ViewModelProvider(this).get(InfoViewModel::class.java)
        infoViewModel.info.observe(this, Observer { words ->

            words?.let { adapter.setWords(it) }
        })

        start_sample_btn.setOnClickListener {
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                        val mainHandler = Handler(Looper.getMainLooper())

                        mainHandler.post(object : Runnable {
                            @RequiresApi(Build.VERSION_CODES.M)
                            override fun run() {
                                minusOneSecond(tm)
                                mainHandler.postDelayed(this, 5000)
                            }
                        })

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken?
                    ) {
                    }
                }).check()
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    fun minusOneSecond(tm: TelephonyManager){

        var strength: String = ""
        var gsm_rssi: String = ""
        var lac: String = ""
        var tac: String = ""
        var plmn: String = ""
        var mcc: String = ""
        var cid : String = ""
        var mnc: String = ""
        var arfcn : String = ""
        var techno: String = ""
        var lte_rxlev: String = ""
        var umts_rscp: String = ""
        var lte_rsrp: String = ""
        var lte_rsrq: String = ""
        var lte_cqi: String = ""
        var upSpeed: Int = 0
        var downSpeed: Int = 0
        val infos = tm.allCellInfo
        if (infos.size == 0){
            Toast.makeText(this@MainActivity, "No Signal", Toast.LENGTH_SHORT).show()
        }

        try {
            val cellInfo = tm.allCellInfo[0]

            if (cellInfo is CellInfoWcdma) {
                val cellSignalStrengthWcdma: CellSignalStrengthWcdma = cellInfo.cellSignalStrength
                val cellIdentityWcdma: CellIdentityWcdma = cellInfo.cellIdentity
                strength = cellSignalStrengthWcdma.dbm.toString()
                lac = cellIdentityWcdma.lac.toString()
                umts_rscp = cellSignalStrengthWcdma.asuLevel.toString()
                cid = cellIdentityWcdma.cid.toString()
                mcc = cellIdentityWcdma.mccString.toString()
                mnc = cellIdentityWcdma.mncString.toString()
                plmn = mcc + mnc
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    arfcn = cellIdentityWcdma.uarfcn.toString()
                }
                //rac
                techno = "UMTS"
            }

            if (cellInfo is CellInfoGsm) {
                val cellSignalStrengthGsm: CellSignalStrengthGsm = cellInfo.cellSignalStrength
                val cellIdentityGsm: CellIdentityGsm = cellInfo.cellIdentity
                lac = cellIdentityGsm.lac.toString()
                mcc = cellIdentityGsm.mccString.toString()
                mnc = cellIdentityGsm.mncString.toString()
                cid = cellIdentityGsm.cid.toString()
                plmn = mcc + mnc
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    arfcn = cellIdentityGsm.arfcn.toString()
                }
                strength = cellSignalStrengthGsm.dbm.toString()
                gsm_rssi = cellSignalStrengthGsm.asuLevel.toString()
                techno = "GSM"

            }

            if (cellInfo is CellInfoLte) {
                val cellSignalStrengthLte: CellSignalStrengthLte = cellInfo.cellSignalStrength
                val cellIdentityLte: CellIdentityLte = cellInfo.cellIdentity
                cid = cellIdentityLte.ci.toString()
                mcc = cellIdentityLte.mccString.toString()
                mnc = cellIdentityLte.mncString.toString()
                plmn = mcc + mnc
                tac = cellIdentityLte.tac.toString()
                //rac
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    arfcn = cellIdentityLte.earfcn.toString()
                }
                strength = cellSignalStrengthLte.dbm.toString()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    lte_rsrq = cellSignalStrengthLte.rsrq.toString()
                    lte_rsrp = cellSignalStrengthLte.rsrp.toString()
                    lte_cqi = cellSignalStrengthLte.cqi.toString()
                    lte_rxlev = cellSignalStrengthLte.level.toString()
                }
                techno = "LTE"

            }

            if (cellInfo is CellInfoCdma) {
                val cellIdentityWcdma: CellIdentityCdma = cellInfo.cellIdentity
                val cellSignalStrengthCdma: CellSignalStrengthCdma = cellInfo.cellSignalStrength
                strength = cellSignalStrengthCdma.dbm.toString()
                techno = "UMTS"

            }

            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            var netInfo: NetworkInfo? = cm.activeNetworkInfo

            if (netInfo != null && netInfo.isConnected) {
                val n: NetworkCapabilities = cm.getNetworkCapabilities(cm.activeNetwork)
                if (n != null){
                    downSpeed = n.getLinkDownstreamBandwidthKbps()
                    upSpeed = n.getLinkUpstreamBandwidthKbps()
                }
            }

            content_latency = Ccontlatency("https://www.aparat.com/")
            latency = Clatency("8.8.8.8")

        }
        catch (e: IndexOutOfBoundsException) {
            Toast.makeText(this@MainActivity, "No Signal", Toast.LENGTH_SHORT).show()
        }
        finally {
            requestNewLocationData()
            if (current_location != null)
            {
                val info = CellInfo(cid=cid,mcc=mcc,mnc = mnc,plmn=plmn,arfcn = arfcn, latency = 0, content_latency = 0, tac = tac, lac = lac, type = techno, gsm_rssi = gsm_rssi, strength = strength, longitude = current_location!!.longitude,
                    altitude = current_location!!.latitude, time = System.currentTimeMillis(), jitter = jitter, upSpeed = upSpeed, downSpeed = downSpeed, lte_rxlev = lte_rxlev, umts_rscp = umts_rscp,
                lte_rsrp = lte_rsrp, lte_rsrq = lte_rsrq, lte_cqi = lte_cqi)
                infoViewModel.insert(info)
            }
        }

    }
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            current_location = mLastLocation
        }
    }

    fun showMap(view: View) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun Ccontlatency(addresss: String) :Long {
        val runtime = Runtime.getRuntime()
        var p : Long =999999999

        try
        {
            val IpProcess = runtime.exec("/system/bin/ping -c 1 "+ addresss)
            val exitVal  = IpProcess.waitFor(2,TimeUnit.SECONDS)
            var a : Long = (System.currentTimeMillis() %100000)
            if (exitVal )
            {
                var b : Long = (System.currentTimeMillis() %100000)
                if(b<=a)
                {
                    p = (100000 - a) + b
                }else{
                    p = b - a
                }
            }
            else
            {
                p = 999999999
            }
        }

        catch (ignore: InterruptedException)
        {
            Log.i("Exception", "stackTrace")
            ignore.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return p
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun Clatency(addresss: String) :Long {
        val runtime = Runtime.getRuntime()
        var p : Long =999999999

        try
        {
            val IpProcess = runtime.exec("/system/bin/ping -c 1 "+ addresss)
            val exitVal  = IpProcess.waitFor(2, TimeUnit.SECONDS)
            var a : Long = (System.currentTimeMillis() %100000)


            if (exitVal )
            {
                var b : Long = (System.currentTimeMillis() %100000)
                if(b<=a){ p = (100000 - a) + b }
                else{ p = b - a }
            }
            else
            {
                p = 999999999
            }
            if (jitter_counter != 0 && p != 999999999.toLong() && latency != 999999999.toLong())
            {
                var p2 = p.toInt()
                var p3 = kotlin.math.abs((p2 - latency).toInt())
                jitter = (((jitter_counter-1)*jitter)+(p3)) / jitter_counter
                jitter_counter++
            }else if (jitter_counter == 0)
            {
                jitter_counter++
            }

        }
        catch (ignore: InterruptedException)
        {
            Log.i("Exception", "stackTrace")
            ignore.printStackTrace()
        } catch (e: IOException)
        {
            e.printStackTrace()
        }

        return p
    }
}

