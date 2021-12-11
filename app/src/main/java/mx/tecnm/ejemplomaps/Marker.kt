package mx.tecnm.ejemplomaps

import com.google.firebase.firestore.GeoPoint
import kotlin.math.abs

class Marker {
    var nombre: String = ""
    var posicion1: GeoPoint = GeoPoint(0.0,0.0)
    var posicion2: GeoPoint = GeoPoint(0.0,0.0)
    var descripcion: String = ""
    var img1: String=""
    var img2: String=""
    var img3: String=""

    override fun toString(): String {
        return nombre+"\n"+posicion1.latitude+","+posicion1.longitude+"\n"+
               posicion2.latitude+","+posicion2.longitude
    }
    fun estoyEn(PA:GeoPoint):Boolean{
        if(PA.latitude>=posicion1.latitude && PA.latitude <= posicion2.latitude){
            if(abs(PA.longitude) >=abs(posicion1.longitude)&& abs(PA.longitude) <=abs(posicion2.longitude)){
                return true
            }
            else if(PA.latitude>=posicion1.latitude && PA.latitude <= posicion2.latitude) {
                if (abs(PA.longitude) >= abs(posicion1.longitude) && abs(PA.longitude) <= abs(
                        posicion2.longitude
                    )
                ) {
                    return true
                }
            }
        }
        return false
    }
}