package pfe.savfc;

/* Librerias importadas */
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TabHost;

/* Inicio Actividad Principal */
public class Act_RegGuardados extends TabActivity {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_verreg";
	
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
		setContentView(R.layout.ly_regguardados);
		
		Resources res = getResources(); // Objeto recursos para acceder a los iconos
		TabHost tabHost = getTabHost();	// Anfitrion de la actividad
		TabHost.TabSpec spec;			// Especificaciones de pestaña
		Intent intento;  				// Intento reusable para cada pestaña
		
		// Pestaña 1
		intento = new Intent(this, Act_RegGuardadosMemTEL.class);
		spec = tabHost.newTabSpec("Tab1").setIndicator("Memoria del Telefono", res.getDrawable(R.drawable.ic_tab_memtel)).setContent(intento);
		tabHost.addTab(spec);
		
		// Pestaña 2
		intento = new Intent(this, Act_RegGuardadosMemTSD.class);
		spec = tabHost.newTabSpec("Tab2").setIndicator("Tarjeta SD", res.getDrawable(R.drawable.ic_tab_memsd)).setContent(intento);
		tabHost.addTab(spec);
		
	}
	/*- Fin Metodos para el ciclo de vida de la actividad -*/
	
}/* Fin Actividad Principal */