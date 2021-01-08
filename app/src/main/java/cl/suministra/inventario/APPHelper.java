
package cl.suministra.inventario;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;


public class APPHelper {

    public static String codigo_usuario = "";
    public static String nombre_usuario = "";
    public static String token = "";
    //public static String url = "https://192.168.1.85:3002";
    public static String url = "https://factoria.suministra.cl/apirest-inventario";
    public static String numero_serie = "";

    static Date date = new Date();
    static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    public static String fechaActual = formatter.format(date);

    public static String getCodigo_usuario() {
        return codigo_usuario;
    }

    public static void setCodigo_usuario(String rut_usuario) {
        APPHelper.codigo_usuario = rut_usuario;
    }

    public static String getNombre_usuario() {
        return nombre_usuario;
    }

    public static void setNombre_usuario(String nombre_usuario) {
        APPHelper.nombre_usuario = nombre_usuario;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        APPHelper.token = token;
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        APPHelper.url = url;
    }

    public static String getNumero_serie() {
        return numero_serie;
    }

    public static void setNumero_serie(String numero_serie) {
        APPHelper.numero_serie = numero_serie;
    }

    public static String getFechaActual() {
        return fechaActual;
    }

    public static void setLogout(){
        APPHelper.codigo_usuario = "";
        APPHelper.nombre_usuario = "";
        APPHelper.token = "";
    }

    public static void initSerialNum(Context context){
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            numero_serie = (String) get.invoke(c, "ro.serialno");
            //numero_serie = serial_no.substring(0,8);
        }catch (Exception e) {
            Toast.makeText(context,"Ocurrió un error al obtener número de serie "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

}
