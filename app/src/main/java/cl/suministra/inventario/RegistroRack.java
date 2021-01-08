package cl.suministra.inventario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
    private TextView TVProductoDesc;
    private Button BTNAceptar;
    private EditText EDTCantidad;

    CustomAdapter customAdapter = null;

    List<RackProductos> rackProductosList = new ArrayList<RackProductos>();
    private ListView LVRackProductos;
    private TextView TVRack;
    private TextView TVProducto;
    private TextView TVCantidad;

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
        TVRackDesc = (TextView) findViewById(R.id.TVRackDesc);
        BTNProducto = (Button) findViewById(R.id.BTNProducto);
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
                tipoBarcode = "RACK";
                EDTCantidad.clearFocus();
                openBarcodeReader();
            }
        });

        BTNProducto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){
                tipoBarcode = "PRODUCTO";
                EDTCantidad.clearFocus();
                openBarcodeReader();
            }
        });

        BTNAceptar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){
                enviarProducto();

            }
        });

    }

    public void openBarcodeReader(){

        Intent intent = new Intent ("ACTION_BAR_TRIGSCAN");
        intent.putExtra("timeout", 1);
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

    public void procesarBarcode(String tipo, String valor ){

        switch (tipo){
            case "RACK":
                seleccionaRack(valor);
                break;
            case "PRODUCTO":
                seleccionaProducto(valor);
                break;
        }

        EDTCantidad.setText("1");
        EDTCantidad.setSelection(EDTCantidad.getText().length());

    }

    public void seleccionaRack(final String codigo_rack){

        rackProductosList.clear();

        AsyncHttpClient cliente = new AsyncHttpClient();
        cliente.setConnectTimeout(5000);
        cliente.setResponseTimeout(5000);
        cliente.setMaxRetriesAndTimeout(0, 5000);

        cliente.get(RegistroRack.this,  APPHelper.getUrl()+"/api/rack/"+codigo_rack+"/"+APPHelper.getCodigo_usuario() , new AsyncHttpResponseHandler() {
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

                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR",e.getMessage());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable error) {

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

        AsyncHttpClient cliente = new AsyncHttpClient();
        cliente.setConnectTimeout(5000);
        cliente.setResponseTimeout(5000);
        cliente.setMaxRetriesAndTimeout(0, 5000);

        cliente.get(RegistroRack.this,  APPHelper.getUrl()+"/api/producto/"+codigo_producto , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {

                    JSONObject jsonRootObject = new JSONObject(new String(bytes));
                    JSONObject productoSelect = (JSONObject) jsonRootObject.get("producto");
                    TVProductoDesc.setText(productoSelect.get("descripcion").toString());

                    codigoProductoSelect = codigo_producto;

                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR", e.getMessage());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable error) {

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

                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.alertDialog(RegistroRack.this,"ERROR", e.getMessage());

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable error) {

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

        EDTCantidad.setText("1");
        EDTCantidad.setSelection(EDTCantidad.getText().length());
        TVProductoDesc.setText("");
        codigoProductoSelect = "";

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