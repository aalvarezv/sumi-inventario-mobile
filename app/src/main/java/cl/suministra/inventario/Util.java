package cl.suministra.inventario;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;



public class Util {

    public static void alertDialog(Context context, String titulo, String mensaje) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(titulo);
        alertDialog.setMessage(mensaje);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();

    }







}

