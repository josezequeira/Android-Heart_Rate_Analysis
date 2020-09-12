package pfe.savfc;

/* Librerias importadas */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/* Inicio Actividad Principal */
public class Act_RegGuardadosMemTEL extends ListActivity {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_vermemtel";
	
	// Miembros
	private static List<String> misRegistrosTEL = new ArrayList<String>();
    private static ArrayAdapter<String> miAdaptadorArrayRegistrosTEL;
    private static TextView noReg;
    private static File appDir;
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
		
		// Se listan los archivos de la memoria del telefono
		listarArchivosTEL();
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
		misRegistrosTEL.clear();
		
	}
	/*- Fin Metodos para el ciclo de vida de la actividad -*/
	
	
	/* Metodo para listar los archivos de la memoria del telefono */
	public void listarArchivosTEL() {
		Log.i(TAG,"+++ listarArchivosTEL +++");
		
		// Directorio donde se encuentran los archivos del telefono
		appDir = Act_RegGuardadosMemTEL.this.getFilesDir();
		
		// Se verifica si hay archivos en el directorio
		if (appDir.listFiles().length > 0) {
			Log.i(TAG,"+++ lista de Archivos > 0 +++");
			// Se llena la lista con los nombres de los archivos
			for (File Archivo : appDir.listFiles()) {
				misRegistrosTEL.add(Archivo.getName());
			}
			// Se pone la lista de los nombres de los archivos en el AdaptadorArray para ser visualizados
			miAdaptadorArrayRegistrosTEL = new ArrayAdapter<String>(this, R.layout.ly_regguardados_nombre, misRegistrosTEL);
			setListAdapter(miAdaptadorArrayRegistrosTEL);
		}
		else {
			noReg.setVisibility(View.VISIBLE);
		}
	}
	
	
	/* Acción al hacer click sobre un registro */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.d(TAG, "+++ onListItemClick +++");
		
		// Se obtiene el nombre del registro clickeado
		registroSeleccionado = misRegistrosTEL.get(position);
		
		// Se muestran las opcines
		showDialog(DIALOGO_OPCIONES);
	}
	
	
	/* Dialogo de opciones al clickear un archivo */
	private static final int DIALOGO_OPCIONES = 1;
	private static final int DIALOLO_ELIMINAR = 2;
	
	protected synchronized Dialog onCreateDialog(int id){
		Log.d(TAG, "+++ onCreateDialog +++");
		switch(id){
		case DIALOGO_OPCIONES:
			// Se crea un dialogo de alerta
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.Dialogo_RegGuardados_Titulo);
			builder.setItems(R.array.Dialogo_RegGuardados_Opciones, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int ItemSeleccionado) { // Inicio onClick
					
					// Se muestra que archivo fue seleccionado
					Toast.makeText(Act_RegGuardadosMemTEL.this, "Ha seleccionado el registro: "+registroSeleccionado , Toast.LENGTH_SHORT).show();
					
					// Accion al seleccionar una opion o item
					if (ItemSeleccionado == 0) {
						Log.i(TAG,"+++ Ha elegido la opción Visualizar +++");
						
						// Se visualiza el archivo seleccionad en la actividad visualizarreg
						Intent intento = new Intent(Act_RegGuardadosMemTEL.this, Act_VisualizarRegistro.class);
						String[] registroDir = new String[] {"memTEL",registroSeleccionado};
						intento.putExtra("regDir", registroDir);
						startActivity(intento);
						
					} else if (ItemSeleccionado == 1) {
						Log.i(TAG,"+++ Ha elegido la opción Eliminar +++");
						
						// Se muestra un dialogo de validacion
						showDialog(DIALOLO_ELIMINAR);
						
					} else if (ItemSeleccionado == 2) {
						Log.i(TAG,"+++ Ha elegido la opción Procesar +++");
						
						// Se procesa el registro seleccionado
						Intent intento = new Intent(Act_RegGuardadosMemTEL.this, Act_ProcesarReg.class);
						String[] registroDir = new String[] {"memTEL",registroSeleccionado};
						intento.putExtra("regDir", registroDir);
						startActivity(intento);
						
					} else if (ItemSeleccionado == 3){
						Log.i(TAG,"+++ Ha elegido la opción Enviar por Email +++");
						
						// Se envia por email el registro seleccionado
						Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
						emailIntent.setType("plain/text");
						emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "");
						emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Registro de VFC");
						
						// Se lee el archivo a enviar
						// Se declara en FileInputStram para obtener los bytes del sistema de archivos
						FileInputStream fis = null;
						
						try {
							// Archivo de la memoria del telefono que se quiere leer
							fis = openFileInput(registroSeleccionado);
							
							// Byte array de tamaño estimado del archivo donde se van a guardar los bytes que se lean
							byte[] reader = new byte[fis.available()];
							
							// Se leen los bytes del archivo y se guardan en el byte array reader
							while (fis.read(reader) != -1) {
							}
							
							// Se pone el contenfido del registro en el texto del mensaje
							String registro = new String(reader);
							emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
									"\n" +
									registro +
									"\n" +
									"\n" +
									"Registro de la variabilidad de la frecuencia cardiaca" + "\n" +
									"Enviado desde la aplicación Análisis de VFC en Android");
							
						} catch (IOException e) {
							Log.e("+++ leerRegTEL +++", e.getMessage(), e);
						} finally {
							if (fis != null) {
								try {
									fis.close();
								} catch (IOException e) {
									Log.e("+++ leerRegTEL: fis.close +++", e.getMessage(), e);
								}
							}
						}
						
						// Directorio donde se encuentran los archivos del telefono
						//appDir = Act_RegGuardadosMemTEL.this.getFileStreamPath(registroSeleccionado);
						//emailIntent.putExtra(android.content.Intent.EXTRA_STREAM,Uri.parse("file://" + appDir));
						
						try {
							startActivity(Intent.createChooser(emailIntent, "Enviar correo electrónico..."));
						} catch (android.content.ActivityNotFoundException ex) {
							Toast.makeText(Act_RegGuardadosMemTEL.this, R.string.Toast_No_Hay_ClienteCorreo, Toast.LENGTH_SHORT).show();
						}
						
					} else {
						// Se indica que hay un error porque no se ha seleccionado ninguna opción
						Toast.makeText(Act_RegGuardadosMemTEL.this, R.string.Toast_No_Opcion, Toast.LENGTH_SHORT).show();
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
					boolean eliminado  = Act_RegGuardadosMemTEL.this.deleteFile(registroSeleccionado);
					
					if (eliminado == true) {
						// Se remueve el registro de la lista
						miAdaptadorArrayRegistrosTEL.remove(registroSeleccionado);
						// Se actualiza la lista
						miAdaptadorArrayRegistrosTEL.notifyDataSetChanged();
						// Se indica al usuario que se ha eliminado satisfactoriamente
						Toast.makeText(Act_RegGuardadosMemTEL.this, R.string.Toast_Eliminado_Registro, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(Act_RegGuardadosMemTEL.this, R.string.Toast_No_Eliminado, Toast.LENGTH_SHORT).show();
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