package pfe.savfc;

/* Librerias importadas */
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/* Inicio Actividad Principal */
public class Act_RegGuardadosMemTSD extends ListActivity {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_vermemsd";
	
	// Miembros
	private static final String directorio = new String(Environment.getExternalStorageDirectory().getPath()+"/Análisis_VFC/");
    private static List<String> misRegistrosTSD = new ArrayList<String>();
    private static ArrayAdapter<String> miAdaptadorArrayRegistrosTSD;
    private static TextView noReg;
    private static String registroSeleccionado;
    
	/* Fin Declaración de variables de la actividad */
	
	
	/*- Inicio Metodos para el ciclo de vida de la actividad -*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "+++ ON CREATE +++");
		
		// Se activan las banderas para mantener la pantalla encendida
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Se inicializa el layout
		setContentView(R.layout.ly_regguardados_lista);
		
		// Se inicializan los elementos de layout
		noReg = (TextView) findViewById(R.id.ly_RegGuardados_lista_Texto);
		
		// Se listan los archivos de la tarjeta SD
		listarArchivosTSD();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "+++ ON START +++");
	}
	
	@Override
	protected synchronized void onResume() {
		super.onResume();
		Log.d(TAG, "+++ ON RESUME +++");
	}
	
	@Override
	protected synchronized void onPause() {
		super.onPause();
		Log.d(TAG, "+++ ON PAUSE +++");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "+++ ON STOP +++");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "+++ ON DESTROY +++");
		
		// Se vacia la lista
		misRegistrosTSD.clear();
	}
	/*- Fin Metodos para el ciclo de vida de la actividad -*/
	
	
	/* Metodo para listar loas archivos de la memoria SD */
	public void listarArchivosTSD() {
		Log.i(TAG,"+++ listarArchivosTSD +++");
		
		// Inicio: Se verifica que el almacenamiento externo se encuentre disponible
		String estado = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(estado)) {
			// Se puede leer y escribir de la tarjeta SD
			
			// Inicio: Se verifica el directorio este disponible, si no lo esta lo crea
			File appDir = new File(directorio);
			
			if (appDir.exists() && appDir.canRead()) {
				// Se verifica si hay archivos en el directorio
				if (appDir.listFiles().length > 0) {
					Log.i(TAG,"+++ lista de Archivos > 0 +++");
					// Se llena la lista con los nombres de los archivos
					for (File registro : appDir.listFiles()) {
						misRegistrosTSD.add(registro.getName());
					}
					// Se pone la lista de los nombres de los archivos en el AdaptadorArray para ser visualizados
					miAdaptadorArrayRegistrosTSD = new ArrayAdapter<String>(this, R.layout.ly_regguardados_nombre, misRegistrosTSD);
					setListAdapter(miAdaptadorArrayRegistrosTSD);
				}
				else {
					noReg.setVisibility(View.VISIBLE);
				}
			}
			else {
				// Se crea el directorio
				appDir = new File(directorio);
				appDir.mkdir();
				
				// Se indica que no hay ningun registro
				noReg.setVisibility(View.VISIBLE);
			}
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(estado)) {
			// Unicamente se puede leer de la tarjeta SD
			Toast.makeText(this, R.string.Toast_TSD_No_se_puede_escribir, Toast.LENGTH_LONG).show();
		}
		else {
			// No se puede leer ni escribir en la tarjeta SD
			Toast.makeText(this, R.string.Toast_TSD_No_se_puede_leer_ni_escribir, Toast.LENGTH_LONG).show();
		}
		// Fin: Se verifica que el almacenamiento externo se encuentre disponible
	}
	
	
	/* Acción al hacer click sobre un registro */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.d(TAG, "+++ onListItemClick +++");
		
		// Se guarda el nombre del registro clickeado
		registroSeleccionado = misRegistrosTSD.get(position);
		
		// Se muestran las opcines
		showDialog(DIALOGO_OPCIONES);
	}
	
	
	/* Dialogo de opciones al clickear un archivo */
	private static final int DIALOGO_OPCIONES = 1;
	private static final int DIALOLO_ELIMINAR = 2;
	
	protected synchronized Dialog onCreateDialog(int id){
		Log.d(TAG, "+++ onCreateDialog +++");
		switch(id){ // Inicio switch
		case DIALOGO_OPCIONES:
			// Se crea un dialogo de alerta
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.Dialogo_RegGuardados_Titulo);
			builder.setItems(R.array.Dialogo_RegGuardados_Opciones, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int ItemSeleccionado) { // Inicio onClick
					Log.i(TAG,"+++ Ha seleccionado la opción" + ItemSeleccionado + "+++");
					
					// Se muestra que archivo fue seleccionado
					Toast.makeText(Act_RegGuardadosMemTSD.this, "Ha seleccionado el registro: "+registroSeleccionado , Toast.LENGTH_LONG).show();
					
					// Accion al seleccionar una opion o item
					if (ItemSeleccionado == 0) {
						Log.i(TAG,"+++ Ha elegido la opción Visualizar +++");
						
						// Se visualiza el archivo seleccionad en la actividad visualizarreg
						Intent intento = new Intent(Act_RegGuardadosMemTSD.this, Act_VisualizarRegistro.class);
						String[] registroDir = new String[] {"memTSD",directorio + registroSeleccionado};
						intento.putExtra("regDir",registroDir);
						startActivity(intento);
						
					} else if (ItemSeleccionado == 1) {
						Log.i(TAG,"+++ Ha elegido la opción Eliminar +++");
						
						// Se muestra un dialogo de validacion
						showDialog(DIALOLO_ELIMINAR);
						
					} else if (ItemSeleccionado == 2) {
						Log.i(TAG,"+++ Ha elegido la opción Procesar +++");
						
						// Se procesa el registro seleccionado
						Intent intento = new Intent(Act_RegGuardadosMemTSD.this, Act_ProcesarReg.class);
						String[] registroDir = new String[] {"memTSD",directorio + registroSeleccionado};
						intento.putExtra("regDir",registroDir);
						startActivity(intento);
						
					} else if (ItemSeleccionado == 3) {
						Log.i(TAG,"+++ Ha elegido la opción Enviar por Email +++");
						
						// Se envia por email el registro seleccionado
						Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
						emailIntent.setType("plain/text");
						emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "");
						emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Registro de VFC");
						emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, 
								"Registro de la variabilidad de la frecuencia cardiaca" + "\n" +
								"Enviado desde la aplicación Análisis de VFC en Android");
						emailIntent.putExtra(android.content.Intent.EXTRA_STREAM,Uri.parse("file://" + directorio + registroSeleccionado));
						
						try {
							startActivity(Intent.createChooser(emailIntent, "Enviar correo electrónico..."));
						} catch (android.content.ActivityNotFoundException ex) {
							Toast.makeText(Act_RegGuardadosMemTSD.this, R.string.Toast_No_Hay_ClienteCorreo, Toast.LENGTH_SHORT).show();
						}
						
					} else {
						// Se indica que hay un error porque no se ha seleccionado ninguna opción
						Toast.makeText(Act_RegGuardadosMemTSD.this, R.string.Toast_No_Opcion, Toast.LENGTH_SHORT).show();
					}
				} // Fin onClick
			});
			AlertDialog opciones = builder.create();
			opciones.show();
			break;
		case DIALOLO_ELIMINAR:
			// Se crea un dialogo de alerta
    		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
    		builder2.setMessage(R.string.Dialogo_Eliminar_Texto);
    		builder2.setCancelable(false);
    		builder2.setPositiveButton(R.string.Dialogo_Aceptar, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id) {
					// Se elimina el registro seleccionado
					File registro = new File(directorio + registroSeleccionado);
					boolean eliminado = registro.delete();
					
					if (eliminado == true) {
						// Se remueve el registro de la lista
						miAdaptadorArrayRegistrosTSD.remove(registroSeleccionado);
						// Se actualiza la lista
						miAdaptadorArrayRegistrosTSD.notifyDataSetChanged();
						// Se indica al usuario que se ha eliminado satisfactoriamente
						Toast.makeText(Act_RegGuardadosMemTSD.this, R.string.Toast_Eliminado_Registro, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(Act_RegGuardadosMemTSD.this, R.string.Toast_No_Eliminado, Toast.LENGTH_SHORT).show();
					}
				}
			});
    		builder2.setNegativeButton(R.string.Dialogo_Cancelar, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					// Quitar el dialogo
					dialog.cancel();
				}
			});
    		AlertDialog eliminar = builder2.create();
    		eliminar.show();
    		break;
		} // Fin switch
		return super.onCreateDialog(id);
	}
	
	
}/* Fin Actividad Principal */