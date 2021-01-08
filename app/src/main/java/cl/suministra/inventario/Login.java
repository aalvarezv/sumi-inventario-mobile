/**
 * Se agrega label con numero de serie, se debe validar error sin conexión al servicio.
 */

package cl.suministra.inventario;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

import cl.suministra.inventario.R;
import cz.msebera.android.httpclient.Header;


public class Login extends AppCompatActivity {

    private EditText EDTUsuarioCodigo;
    private EditText EDTUsuarioClave;
    private Button BTNLogin;
    private TextView TVNumeroSerie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_login);

        APPHelper.initSerialNum(this);
        init();

    }

    private void init() {

        TVNumeroSerie = (TextView) findViewById(R.id.TVNumeroSerie);
        TVNumeroSerie.setText(APPHelper.getNumero_serie());

        EDTUsuarioCodigo = (EditText) findViewById(R.id.EDTUsuarioCodigo);
        EDTUsuarioCodigo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.LBUsuarioCodigo);
                    label.setText(EDTUsuarioCodigo.getHint());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        EDTUsuarioCodigo.setBackground(getDrawable(R.drawable.text_border_selector));
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.MSJUsuarioCodigo);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.LBUsuarioCodigo);
                    label.setText("");
                }
            }

        });

        EDTUsuarioClave = (EditText) findViewById(R.id.EDTUsuarioClave);
        EDTUsuarioClave.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.LBUsuarioClave);
                    label.setText(EDTUsuarioClave.getHint());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        EDTUsuarioClave.setBackground(getDrawable(R.drawable.text_border_selector));
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.MSJUsuarioClave);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.LBUsuarioClave);
                    label.setText("");
                }
            }
        });



        BTNLogin = (Button) findViewById(R.id.BTNLogin);
        BTNLogin.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {

                String UsuarioCodigo = EDTUsuarioCodigo.getText().toString();
                if (UsuarioCodigo == null || UsuarioCodigo.isEmpty())
                {
                    TextView view = (TextView) findViewById(R.id.MSJUsuarioCodigo);
                    view.setText("Ingrese " + EDTUsuarioCodigo.getHint());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        EDTUsuarioCodigo.setBackground(getDrawable(R.drawable.text_border_error));
                    }
                    return;
                }

                String UsuarioClave = EDTUsuarioClave.getText().toString();
                if (UsuarioClave == null || UsuarioClave.isEmpty())
                {
                    TextView view = (TextView) findViewById(R.id.MSJUsuarioClave);
                    view.setText("Ingrese " + EDTUsuarioClave.getHint());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        EDTUsuarioClave.setBackground(getDrawable(R.drawable.text_border_error));
                    }
                    return;
                }


                RequestParams params = new RequestParams();
                params.put("codigo",UsuarioCodigo);
                params.put("clave",UsuarioClave);

                AsyncHttpClient cliente = new AsyncHttpClient();
                cliente.setConnectTimeout(5000);
                cliente.setResponseTimeout(5000);
                cliente.setMaxRetriesAndTimeout(0, 5000);

                cliente.post(Login.this,  APPHelper.getUrl()+"/api/auth/" , params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        try {
                            System.out.println(new String(bytes));
                            JSONObject jsonRootObject = new JSONObject(new String(bytes));
                            JSONObject usuario = (JSONObject) jsonRootObject.get("usuario");
                            String codigo_usuario = (String) usuario.get("codigo");
                            String nombre_usuario = (String) usuario.get("nombre");
                            APPHelper.setCodigo_usuario(codigo_usuario);
                            APPHelper.setNombre_usuario(nombre_usuario);

                            continuar();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this, "ERROR "+e.getMessage(), Toast.LENGTH_LONG).show();

                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable error) {

                        try {
                            JSONObject jsonRootObject = new JSONObject(new String(bytes));
                            String mensaje = (String) jsonRootObject.get("msg");
                            Toast.makeText(Login.this, "ERROR "+ statusCode +" "+ mensaje, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this, "ERROR "+e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }

                });

            }

        });

        iniciarControles();
    }


    private void continuar(){
        try {

            EDTUsuarioCodigo.setText("");
            EDTUsuarioCodigo.requestFocus();
            EDTUsuarioClave.setText("");

            Intent intent = new Intent(this, RegistroRack.class);
            startActivity(intent);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Pulse nuevamente la tecla volver para salir de la aplicación", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void iniciarControles(){
        EDTUsuarioCodigo.setText("");
        EDTUsuarioCodigo.requestFocus();
        EDTUsuarioClave.setText("");

    }

    public void salirApp(MenuItem item){
       finishAffinity();
       System.exit(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
