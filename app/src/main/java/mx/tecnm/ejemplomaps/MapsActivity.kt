package mx.tecnm.ejemplomaps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.*
import android.os.Bundle
import android.view.Menu
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import mx.tecnm.ejemplomaps.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var b: ActivityMapsBinding
    private var BD = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Marker>()
    var datoActual = arrayOf("", "", "", "", "")
    var datoActualg = arrayOf("", "", "", "", "")
    private var nombres = ArrayList<String>()
    private lateinit var mark: com.google.android.gms.maps.model.Marker

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    private lateinit var locacion: LocationManager
    private lateinit var mFused: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        BD.collection("Puntos").addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, "Error en firebase", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            posicion.clear()
            nombres.clear()
            for (document in value!!) {
                val data = Marker()
                data.nombre = document.getString("nombre").toString()
                data.posicion1 = document!!.getGeoPoint("Posicion 1")!!
                data.posicion2 = document.getGeoPoint("Posicion 2")!!
                data.descripcion = document.getString("descripcion").toString()
                data.img1 = document.getString("img1").toString()
                data.img2 = document.getString("img2").toString()
                data.img3 = document.getString("img3").toString()
                posicion.add(data)
                nombres.add(data.nombre)
            }
        }
        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val oyente = Oyente(this, b)
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 01f, oyente)
        b.button.setOnClickListener {
            if (datoActualg[0] == "") return@setOnClickListener
            val intent = Intent(this, Lugares::class.java)
            intent.putExtra("Lugar", datoActualg)
            startActivity(intent)
        }
        b.btnInfo.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Nosotros")
            builder.setMessage("Trabajo Realizado por:\n Alexis Torres Acosta\n Keiry Yoseli Rodriguez Gonzalez\n Fernando Miramontes Alvarez \n Luis Daniel Mendez Castellanos ")
            builder.setPositiveButton("OK"){ d,w->
                d.dismiss()
            }
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.items, menu)

        val menuItem = menu?.findItem(R.id.search)
        val searchView: SearchView = menuItem?.actionView as SearchView

        searchView.queryHint = "Busca Puntos aqui"


        val searchAutoComplete: SearchView.SearchAutoComplete =
            searchView.findViewById(R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.setBackgroundColor(Color.BLACK)
        searchAutoComplete.setTextColor(Color.WHITE)
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.holo_blue_light)
        val newsAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombres)
        searchAutoComplete.setAdapter(newsAdapter)
        searchAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, itemIndex, _ ->
                val queryString = adapterView.getItemAtPosition(itemIndex) as String
                val numero = retPosi(queryString)
                mark.remove()
                val punto = LatLng(
                    posicion[numero].posicion1.latitude,
                    posicion[numero].posicion1.longitude
                )
                mark =
                    mMap.addMarker(MarkerOptions().position(punto).title(posicion[numero].nombre))
                mMap.animateCamera(CameraUpdateFactory.newLatLng(punto), 400, null)
                searchAutoComplete.setText(queryString)
                /*Toast.makeText(
                    this,
                    "you clicked ${posicion[numero].nombre}",
                    Toast.LENGTH_LONG
                ).show()*/
            }


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(applicationContext, query, Toast.LENGTH_LONG).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun retPosi(dato: String): Int {
        nombres.forEachIndexed { i, e ->
            if (e == dato) {
                return i
            }
        }
        return 0
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableLocation()
        // Add a marker in Sydney and move the camera
        val tepic = LatLng(21.502227, -104.898208)
        mark = mMap.addMarker(MarkerOptions().position(tepic).title("Tepic: La loma"))
        mark.remove()
        mMap.moveCamera(CameraUpdateFactory.newLatLng(tepic))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener { p0 ->
            val pos = retPosi(p0.title)
            datoActual[0] = posicion[pos].nombre
            datoActual[1] = posicion[pos].img1
            datoActual[2] = posicion[pos].img2
            datoActual[3] = posicion[pos].img3
            datoActual[4] = posicion[pos].descripcion
                if(datoActual[0]!=""){
                    val intent = Intent(this, Lugares::class.java)
                    intent.putExtra("Lugar", datoActual)
                    startActivity(intent)
                }
            false
        }
        mFused = LocationServices.getFusedLocationProviderClient(this)

        val location = mFused.lastLocation
        location.addOnCompleteListener {
            if(it.isSuccessful){
                val currentL = it.result
                val geoPosicion = GeoPoint(currentL!!.latitude,currentL.longitude)
                for(item in posicion){
                    if(item.estoyEn(geoPosicion)){
                        b.txt.text = "Estas en ${item.nombre}"
                        datoActualg[0] = item.nombre
                        datoActualg[1] = item.img1
                        datoActualg[2] = item.img2
                        datoActualg[3] = item.img3
                        datoActualg[4] = item.descripcion
                    }
                }
            }
        }



    }
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED

    private fun enableLocation(){
        if(!::mMap.isInitialized) return
        if(isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
        }
        else{
            requestLocationPermission()
        }
    }
    private fun requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this,"Haz rechazado los permisos, ve a ajustes y aceptalos",Toast.LENGTH_LONG).show()
        }
        else{
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
          REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
              if (ActivityCompat.checkSelfPermission(
                      this,
                      android.Manifest.permission.ACCESS_FINE_LOCATION
                  ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                      this,
                      android.Manifest.permission.ACCESS_COARSE_LOCATION
                  ) != PackageManager.PERMISSION_GRANTED
              ) {
                  return
              }
              mMap.isMyLocationEnabled = true
          }else{
              Toast.makeText(this,"Haz rechazado los permisos, ve a ajustes y aceptalos",Toast.LENGTH_LONG).show()
          }
          else ->{}
      }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if(!::mMap.isInitialized) return
        if(!isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled= false
            Toast.makeText(this,"Haz rechazado los permisos, ve a ajustes y aceptalos",Toast.LENGTH_LONG).show()
        }
    }

}

class Oyente(private var p: MapsActivity, private var b: ActivityMapsBinding):LocationListener {
    override fun onLocationChanged(location: Location) {
        val geoPosicion = GeoPoint(location.latitude,location.longitude)
        for(item in p.posicion){
            if(item.estoyEn(geoPosicion)){
                p.datoActualg[0] = item.nombre
                p.datoActualg[1] = item.img1
                p.datoActualg[2] = item.img2
                p.datoActualg[3] = item.img3
                p.datoActualg[4] = item.descripcion
                b.txt.text = "Estas en ${item.nombre}"
            }
        }
    }

}