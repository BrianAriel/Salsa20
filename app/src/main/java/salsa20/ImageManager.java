package salsa20;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

// Clase que contiene metodos para gestionar la imagen
public class ImageManager {

	// Lee una imagen y la retorna en la variable bf
	public BufferedImage leerImagen(String path) throws IOException {
		BufferedImage bf;

		bf = ImageIO.read(new File(path));
		return bf;
	}

	// Toma una imagen y la convierte en un array de bytes para procesarlo
	public byte[] convertirBytes(BufferedImage bf) {
		int alto, ancho;
		byte[] pixelsByte;
		int [] pixelsInt;

		// Armar el array de enteros con el tamaño necesario para almacenar la iamgen
		alto = bf.getHeight();
		ancho = bf.getWidth();
		pixelsInt = new int[alto * ancho];

		// Recorremos toda la imagen guardando los pixeles
		for (int i = 0; i < alto; i++) {
			for (int j = 0; j < ancho; j++) {
				pixelsInt[i * ancho + j] = bf.getRGB(j, i);
			}
		}
		
		// Pasar el array de enteros a un array de bytes
		ByteBuffer byteBf = ByteBuffer.allocate(pixelsInt.length * 4);
		IntBuffer intBf = byteBf.asIntBuffer();
		intBf.put(pixelsInt);
		pixelsByte = byteBf.array();
	
		return pixelsByte;
	}
	
	// Obtiene 4 bytes de un array a partir de una posicion de inicio
	private byte[] obtener4Bytes(byte[] pixels, int inicio) {
		byte[] resultado = new byte[4];
		
		// Recorremos la entrada y vamos copiando
		for(int i = 0; i < 4; i++) {
			resultado[i] = pixels[inicio + i];
		}

		return resultado;
	}

	// Genera una imagen a partir de una altura y anchura, su correspondiente array de bytes y un string indicando el path para guardar el archivo
	public void escribirImagen(byte[] pixelsByte, int width, int height, String path) throws IOException {
		// Inicializamos las variables a usar
		BufferedImage bf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] pixelsInt = new int[pixelsByte.length / 4];
		int len = pixelsInt.length;
		
		// Necesitamos pasar el array de bytes a uno de enteros
		for(int i = 0; i < len; i++) {
			pixelsInt[i] = ByteBuffer.wrap(obtener4Bytes(pixelsByte, i * 4)).getInt();
		}
		
		// Vamos generando los pixeles para la nueva imagen y luego generamos la imagen resultado
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				bf.setRGB(j, i, pixelsInt[i * width + j]);
			}
		}
		ImageIO.write(bf, "png", new File(path));
	}
}