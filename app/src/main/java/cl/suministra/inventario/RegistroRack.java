package cl.suministra.inventario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class RegistroRack extends AppCompatActivity{


    private ScanBroadcastReceiver ScanReceiver = null;
    private IntentFilter ScanIntentFilter      = null;

    private String tipoBarcode = "";
    private String codigoRackSelect = "";
    private String codigoProductoSelect = "";

    private TextView TVNombreUsuario;
    private TextView TVFechaActual;
    private Button BTNRack;
    private TextView TVRackDesc;
    private Button BTNProducto;
    private Button BTNProductoManual;
    private TextView TVProductoDesc;
    private Button BTNAceptar;
    private EditText EDTCantidad;

    CustomAdapter customAdapter = null;

    List<RackProductos> rackProductosList = new ArrayList<RackProductos>();
    private ListView LVRackProductos;
    private TextView TVRack;
    private TextView TVProducto;
    private TextView TVCantidad;

    int colorDefault = Color.parseColor("#696969");
    int colorRojo = Color.parseColor("#FF0000");
    int colorVerde = Color.parseColor("#008000");

    private boolean BARCODE_ON = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_rack);

        inicio();
    }

    private void inicio(){

        TVNombreUsuario = (TextView) findViewById(R.id.TVUsuarioNombre);
        TVNombreUsuario.setText(APPHelper.getNombre_usuario());
        TVFechaActual = (TextView) findViewById(R.id.TVFechaActual);
        TVFechaActual.setText(APPHelper.getFechaActual());


        BTNRack = (Button) findViewById(R.id.BTNRack);
        BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
        TVRackDesc = (TextView) findViewById(R.id.TVRackDesc);
        BTNProducto = (Button) findViewById(R.id.BTNProducto);
        BTNProductoManual = (Button) findViewById(R.id.BTNProductoManual);
        TVProductoDesc = (TextView) findViewById(R.id.TVProductoDesc);
        EDTCantidad = (EditText) findViewById(R.id.EDTCantidad);
        EDTCantidad.setText("1");
        //ubica el cursor al final
        EDTCantidad.setSelection(EDTCantidad.getText().length());
        BTNAceptar = (Button) findViewById(R.id.BTNAceptar);
        LVRackProductos = (ListView) findViewById(R.id.LVRackProductos);

        EDTCantidad.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String cantidad = EDTCantidad.getText().toString();
                    if(cantidad.matches("0") || cantidad.matches("")){
                        EDTCantidad.setText("1");
                    }
                    EDTCantidad.setSelection(EDTCantidad.getText().length());
                }
            }

        });


        BTNRack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){

                if(BARCODE_ON){
                    return;
                }

                BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorVerde, PorterDuff.Mode.SRC));
                BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));

                tipoBarcode = "RACK";
                codigoRackSelect = "";
                TVRackDesc.setText("");
                EDTCantidad.clearFocus();
                codigoProductoSelect = "";
                TVProductoDesc.setText("");
                EDTCantidad.clearFocus();
                openBarcodeReader();
                verificaEstadoBarCode(tipoBarcode);
            }
        });

        BTNProducto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){

                if(codigoRackSelect.equals("")){
                    return;
                }

                BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorVerde, PorterDuff.Mode.SRC));
                BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorVerde, PorterDuff.Mode.SRC));
                BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));

                tipoBarcode = "PRODUCTO";
                codigoProductoSelect = "";
                TVProductoDesc.setText("");
                EDTCantidad.clearFocus();
                openBarcodeReader();
                verificaEstadoBarCode(tipoBarcode);
            }
        });

        BTNProductoManual.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){

                if(codigoRackSelect.equals("")){
                    return;
                }

                if(BARCODE_ON){
                    return;
                }

                codigoProductoSelect = "";
                TVProductoDesc.setText("");
                EDTCantidad.clearFocus();

                BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorVerde, PorterDuff.Mode.SRC));
                BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorVerde, PorterDuff.Mode.SRC));
                BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));


                AlertDialog.Builder alert = new AlertDialog.Builder(RegistroRack.this);
                final EditText EDTProductoManual = new EditText(RegistroRack.this);
                alert.setTitle("INGRESAR CÓDIGO DE PRODUCTO");
                alert.setView(EDTProductoManual);

                alert.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(!EDTProductoManual.getText().toString().trim().equals("")){
                            seleccionaProducto(EDTProductoManual.getText().toString());
                        }else{
                            BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                            BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                            BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                            BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                            EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                        }
                    }
                });

                alert.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                        EDTProductoManual.setText("");

                        BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                        BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                        BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                        BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                        EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));

                    }
                });


                alert.show();
            }
        });

        BTNAceptar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){

                if(codigoRackSelect.equals("") || codigoProductoSelect.equals("")){
                    return;
                }

                BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorVerde, PorterDuff.Mode.SRC));
                EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorVerde, PorterDuff.Mode.SRC));

                enviarProducto();

            }
        });

    }



    public void openBarcodeReader(){

        if(BARCODE_ON){
            return;
        }

        BARCODE_ON = true;

        Intent intent = new Intent ("ACTION_BAR_TRIGSCAN");
        //levanta el scanner
        getApplicationContext().sendBroadcast(intent);

        if (ScanReceiver != null) {
            getApplicationContext().unregisterReceiver(ScanReceiver);
            ScanReceiver = null;
        }

        //crea los objetos
        ScanReceiver     = new ScanBroadcastReceiver();
        ScanIntentFilter = new IntentFilter("ACTION_BAR_SCAN");
        //envia los objetos para obtener el resultado del scan
        getApplicationContext().registerReceiver(ScanReceiver, ScanIntentFilter);

    }

    public void verificaEstadoBarCode(final String tipo){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                switch (tipo){

                    case "RACK":
                        if(codigoRackSelect.equals("")){
                            BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                            BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                            BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                            BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                            EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                        }
                        break;
                    case "PRODUCTO":
                        if(codigoProductoSelect.equals("")){
                            BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                            BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                            BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                            BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                            EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                        }
                        break;
                }

                BARCODE_ON = false;

            }

        }, 10500);

    }

    public void procesarBarcode(String tipo, String valor ){

        BARCODE_ON = false;

        switch (tipo){
            case "RACK":
                seleccionaRack(valor);
                break;
            case "PRODUCTO":
                codigoProductoSelect = "";
                TVProductoDesc.setText("");
                seleccionaProducto(valor);
                break;
        }

        EDTCantidad.setText("1");
        EDTCantidad.setSelection(EDTCantidad.getText().length());

    }

    public void seleccionaRack(final String codigo_rack){

        rackProductosList.clear();

        RequestParams params = new RequestParams();
        params.put("codigo",codigo_rack);
        params.put("codigo_usuario",APPHelper.getCodigo_usuario());

        AsyncHttpClient cliente = new AsyncHttpClient();
        cliente.setConnectTimeout(5000);
        cliente.setResponseTimeout(5000);
        cliente.setMaxRetriesAndTimeout(0, 5000);

        cliente.get(RegistroRack.this,  APPHelper.getUrl()+"/api/rack/", params , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {

                    JSONObject jsonRootObject = new JSONObject(new String(bytes));
                    JSONObject rackSelect = (JSONObject) jsonRootObject.get("rack");
                    TVRackDesc.setText(rackSelect.get("descripcion").toString());
                    codigoRackSelect = codigo_rack;

                    JSONArray rackProductos = (JSONArray) jsonRootObject.get("rack_productos");

                    for(int j = 0; j < rackProductos.length(); j++){

                        JSONObject producto = (JSONObject) rackProductos.getJSONObject(j).get("producto");
                        String productoCod  = (String) producto.get("codigo");
                        String productoDesc = (String) producto.get("descripcion");
                        JSONObject rack = (JSONObject) rackProductos.getJSONObject(j).get("rack");
                        String rackCod  = (String) rack.get("codigo");
                        String rackDesc = (String) rack.get("descripcion");
                        String total = (String) rackProductos.getJSONObject(j).get("total");

                        RackProductos rackProducto = new RackProductos(rackDesc, productoDesc, total);
                        rackProductosList.add(rackProducto);

                    }

                    customAdapter = new CustomAdapter();
                    customAdapter.notifyDataSetChanged();
                    LVRackProductos.setAdapter(customAdapter);

                    BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                    BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                    BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));


                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR",e.getMessage());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable error) {

                BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));

                if(statusCode == 0){
                    Util.alertDialog(RegistroRack.this, "ERROR "+statusCode, error.getMessage());
                    return;
                }

                try {

                    JSONObject jsonRootObject = new JSONObject(new String(bytes));
                    String mensaje = (String) jsonRootObject.get("msg");
                    Util.alertDialog(RegistroRack.this,"ERROR: "+statusCode, mensaje);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR",e.getMessage());
                }

            }

        });

    }

    public void seleccionaProducto(final String codigo_producto){

        RequestParams params = new RequestParams();
        params.put("codigo",codigo_producto);

        AsyncHttpClient cliente = new AsyncHttpClient();
        cliente.setConnectTimeout(5000);
        cliente.setResponseTimeout(5000);
        cliente.setMaxRetriesAndTimeout(0, 5000);

        cliente.get(RegistroRack.this,  APPHelper.getUrl()+"/api/producto/", params , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {

                    JSONObject jsonRootObject = new JSONObject(new String(bytes));
                    JSONObject productoSelect = (JSONObject) jsonRootObject.get("producto");
                    TVProductoDesc.setText(productoSelect.get("descripcion").toString()+"\n"+codigo_producto);
                    codigoProductoSelect = codigo_producto;

                    BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                    BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                    BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                    EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR", e.getMessage());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable error) {

                BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));

                if(statusCode == 0){
                    Util.alertDialog(RegistroRack.this, "ERROR "+statusCode, error.getMessage());
                    return;
                }

                try {

                    JSONObject jsonRootObject = new JSONObject(new String(bytes));
                    String mensaje = (String) jsonRootObject.get("msg");
                    Util.alertDialog(RegistroRack.this,"ERROR: "+statusCode, mensaje);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR", e.getMessage());
                }

            }

        });
    }

    private void enviarProducto(){

        if(codigoRackSelect.matches("")){
            Util.alertDialog(RegistroRack.this,"Alerta","No ha ingresado la ubicación");
            return;
        }

        if(codigoProductoSelect.matches("")){
            Util.alertDialog(RegistroRack.this,"Alerta","No ha ingresado el producto a registrar");
            return;
        }

        if(EDTCantidad.getText().toString().matches("") || EDTCantidad.getText().toString().matches("0")){
            Util.alertDialog(RegistroRack.this,"Alerta","Ingrese una cantidad válida");
            return;
        }

        rackProductosList.clear();
        int productoCant = 1;
        String cantidad = EDTCantidad.getText().toString();
        productoCant = Integer.parseInt(cantidad);


        RequestParams params = new RequestParams();
        params.put("codigo",codigoProductoSelect);
        params.put("codigo_rack",codigoRackSelect);
        params.put("codigo_usuario", APPHelper.getCodigo_usuario());
        params.put("codigo_maquina", APPHelper.getNumero_serie());
        params.put("cantidad", productoCant);

        AsyncHttpClient cliente = new AsyncHttpClient();
        cliente.setConnectTimeout(5000);
        cliente.setResponseTimeout(5000);
        cliente.setMaxRetriesAndTimeout(0, 5000);

        cliente.post(RegistroRack.this,  APPHelper.getUrl()+"/api/producto/registrar" , params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {

                    JSONObject jsonRootObject = new JSONObject(new String(bytes));
                    JSONArray rackProductos = (JSONArray) jsonRootObject.get("rack_productos");

                    for(int j = 0; j < rackProductos.length(); j++){

                        JSONObject producto = (JSONObject) rackProductos.getJSONObject(j).get("producto");
                        String productoCod  = (String) producto.get("codigo");
                        String productoDesc = (String) producto.get("descripcion");
                        JSONObject rack = (JSONObject) rackProductos.getJSONObject(j).get("rack");
                        String rackCod  = (String) rack.get("codigo");
                        String rackDesc = (String) rack.get("descripcion");
                        String total = (String) rackProductos.getJSONObject(j).get("total");

                        RackProductos rackProducto = new RackProductos(rackDesc, productoDesc, total);
                        rackProductosList.add(rackProducto);

                    }

                    customAdapter = new CustomAdapter();
                    customAdapter.notifyDataSetChanged();
                    LVRackProductos.setAdapter(customAdapter);

                    BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                    BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                    BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                    BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                    EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));

                    EDTCantidad.setText("1");
                    EDTCantidad.setSelection(EDTCantidad.getText().length());
                    TVProductoDesc.setText("");
                    codigoProductoSelect = "";

                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR", e.getMessage());

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable error) {

                BTNRack.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProducto.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNProductoManual.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC));
                BTNAceptar.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));
                EDTCantidad.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(colorRojo, PorterDuff.Mode.SRC));

                if(statusCode == 0){
                    Util.alertDialog(RegistroRack.this, "ERROR "+statusCode, error.getMessage());
                    return;
                }

                try {

                    JSONObject jsonRootObject = new JSONObject(new String(bytes));
                    String mensaje = (String) jsonRootObject.get("msg");
                    Util.alertDialog(RegistroRack.this,"ERROR: "+statusCode, mensaje);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR", e.getMessage());
                }

            }

        });



    }

    private class ScanBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            /*****EXTRAS POSIBLES*****
             Bundle bundle = intent.getExtras();
             if (bundle != null) {
             Set<String> keys = bundle.keySet();
             Iterator<String> it = keys.iterator();
             while (it.hasNext()) {
             String key = it.next();
             Log.d(AppHelper.LOG_TAG, "[" + key + "=" + bundle.get(key) + "]");
             }
             }
             EXTRA_SCAN_STATE
             EXTRA_SCAN_ENCODE_MODE
             EXTRA_SCAN_LENGTH
             EXTRA_SCAN_DATA
             **************************/
            final String scanResult = intent.getStringExtra("EXTRA_SCAN_DATA");

            if (scanResult.length() > 0) {
                procesarBarcode(tipoBarcode, scanResult);

            }

        }
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return rackProductosList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.activity_lista_rack_productos_custom, null);
            TVRack     = (TextView)  convertView.findViewById(R.id.TVRack);
            TVProducto = (TextView)  convertView.findViewById(R.id.TVProducto);
            TVCantidad = (TextView)  convertView.findViewById(R.id.TVCantidad);

            TVRack.setText(String.valueOf(rackProductosList.get(position).rackDesc));
            TVProducto.setText(String.valueOf(rackProductosList.get(position).productoDesc));
            TVCantidad.setText(String.valueOf(rackProductosList.get(position).cantidad));

            return convertView;

        }
    }

    public class RackProductos{

        String rackDesc;
        String productoDesc;
        String cantidad;

        public RackProductos(String rackDesc, String productoDesc, String cantidad){
            this.rackDesc  = rackDesc;
            this.productoDesc = productoDesc;
            this.cantidad = cantidad;
        }

    }

}