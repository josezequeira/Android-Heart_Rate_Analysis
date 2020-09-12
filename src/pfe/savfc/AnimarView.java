package pfe.savfc;

/* Librerias importadas */
import android.graphics.Matrix;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/* Inicio Clase Principal */
public class AnimarView extends Animation {
	
	/* Inicio declaración de variables */
	
	// Depuración
	private static final String TAG = "Clase AnimarView";
	
	// Miembros
	//private static float anchoPantalla;
	private static int altoPantalla;
	private static int altoView;
	
	/* Fin declaración de variables */
	
	// Metodo inicial de configuracion de la animación
	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		Log.d(TAG,"+++ initialize +++");
		
		// Se introduce el tiempo en milisegundos
		//setDuration(2500);
		
		// Se indica que la animacion se mantenga cuando termine
		setFillAfter(true);
		
		// Se indica acceleracion de la animación 
		setInterpolator(new LinearInterpolator());
		
		// Se indica que se repita
		setRepeatCount(INFINITE);
		
		// Se indica que regrese cuando llegue al final
		setRepeatMode(REVERSE);
		
		altoPantalla = parentHeight;
		altoView = height;
	}
	
	// Animación que se aplica sobre el View
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		
		final Matrix matrix = t.getMatrix();
		matrix.setTranslate(0, (altoPantalla - altoView)*interpolatedTime);
	}
	
}/* Fin Clase Principal */