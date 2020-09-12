package pfe.savfc;

/* Librerias importadas */
import java.util.Vector;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/* Inicio Clase Principal */
public class VisualizarReg_View extends View {
	
	/** Inicio declaración de variables */
	// Miembros
	private Vector<Double> RR = new Vector<Double>();	// Vector donde ponen los RR leidos
	private Paint miPaint = new Paint();
	private double anchoPantalla;
	private double altoPantalla;
	private double fescalaY;
	private double fescalaX;
	
	/** Fin declaración de variables */
	
	/** Inicio constructores */
	public VisualizarReg_View(Context context) {
		super(context);
	}
	
	public VisualizarReg_View(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public VisualizarReg_View(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	/** Fin constructores */
	
	/** Inicio Gets and Setters */
	public Vector<Double> getRR() {
		return RR;
	}

	public void setRR(Vector<Double> rR) {
		RR = rR;
	}
	/** Fin Gets and Setters */
	
	// Metodo onSizeChanged para controlar los cambios de tamaño de la pantalla
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        
		// Ancho y alto
		altoPantalla = h;
        anchoPantalla = w;
        // Se calcula el factor escala en el eje Y, el intervalo de tiempor que
 		// se quiere graficar esta entre 0.0ms y 1200.0ms
      	fescalaY = altoPantalla/1200.0;
      	// Se calcula el factor de escala en el eje X, que son el numero de latidos
      	// que en este caso es el tamaño del vector donde estan los datos
      	fescalaX = anchoPantalla/ Double.valueOf(RR.size());
      	// Color de la linea
      	miPaint.setColor(Color.WHITE);
      	// Grosor de la linea
      	miPaint.setStrokeWidth(2);
      	
        super.onSizeChanged(w, h, oldw, oldh);
    }
	
	// Metodo para dibujar
	@Override
	protected void onDraw(Canvas canvas){
		
		// Color de fondo
 		canvas.drawRGB(0, 0, 0);
 		
		// Graficar RR
		for (int i=0; i<RR.size()-1; i++) {
			//Pasar de tipo Double a float
			double yiniD = RR.elementAt(i);
			double yfinD = RR.elementAt(i+1);
			float yini = (float) Math.round(yiniD);
			float yfin = (float) Math.round(yfinD);
			// Se escala el valor leido respecto al alto del canvas
			yini = Math.round(yini * fescalaY);
			yfin = Math.round(yfin * fescalaY);
			// Se escala el valor leido respecto al ancho del canvas
			long xini = Math.round(i * fescalaX);
			// Diferencia entre el alto del canvas y el valor en Y
			long yiniDif = Math.round(altoPantalla - yini);
			long yfinDif = Math.round(altoPantalla - yfin);
			
			// Dibuja las lineas
			canvas.drawLine(xini, yiniDif, xini+1, yfinDif, miPaint);
		}
	}
	
}/* Fin Clase Principal */