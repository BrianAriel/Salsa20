package salsa20;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

// Clase que sirve como UI
public class Interface extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private File file;
	private Salsa20 salsita;
	private ImageManager imgManager;

	// Constantes en orden: fuentes, modos de operacion, path para cifrar y descifrar, mensajes de error y de exito, y al final constantes de titulos.
	private static final Font FONT_TITULOS = new Font("Arial", Font.BOLD, 20);
	private static final Font FONT_SUBTITULOS = new Font("Arial", Font.BOLD, 18);
	private static final Font FONT_CAMPOS = new Font("Arial", Font.PLAIN, 16);

	private static final int OPERACION_CIFRAR = 1;
	private static final int OPERACION_DESCIFRAR = 2;

	private static final String PATH_CIFRADO_OUT = "./cifrado.png";
	private static final String PATH_DESCIFRADO_OUT = "./descifrado.png";
	
	private static final String MSJ_ERROR_KEY_VACIA = "Ingrese un numero como key";
	private static final String MSJ_ERROR_KEY_NO_NUMERICA = "La key debe ser un valor numerico";
	private static final String MSJ_ERROR_KEY_LONGITUD = "La key debe tener una longitud de 32 caracteres";
	private static final String MSJ_ERROR_FILE_NO_SELECCIONADO = "Seleccione un archivo a encriptar";
	private static final String MSJ_ERROR_NO_SE_PUDO_ABRIR_FILE = "No se logro a acceder a ";
	private static final String MSJ_ERROR_FILE_NO_PNG = "Solo se pueden usar archivos PNG";
	private static final String MSJ_EXITO_OPERACION_REALIZADA = "Operacion realizada!";
	private static final String MSJ_CONFIRNAR_CERRAR_VENTANA = "Deseas cerrar la ventana?";
	
	private static final String TITULO_ERROR = "Error!";
	private static final String TITULO_EXITO = "Exito!";
	private static final String TITULO_CERRAR_VENTANA = "Cerrar Ventana";
	private static final String TITULO_APLICACION = "Salsa 20";

	// Variables para la interfaz
	private JPanel contentPane;
	private JTextField txtKey;
	private JTextField txtNombre;

	// Iniciar la aplicación
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Interface frame = new Interface();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Interface() {
		try {
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException e1) {
			
		} catch (InstantiationException e1) {
		
		} catch (IllegalAccessException e1) {

		} catch (UnsupportedLookAndFeelException e1) {

		}

		// Definimos no realizar acciones al cierre, el tamaño que sea inmodificable y un titulo
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(508, 440);
		setResizable(false);
		setTitle(TITULO_APLICACION);

		// Instanciar el contentPane
		contentPane = new JPanel();
		contentPane.setBackground(Color.LIGHT_GRAY);
		contentPane.setLayout(null);
		setContentPane(contentPane);

		// Iniciar el cifrador el flujo y el manager de imagenes
		salsita = new Salsa20();
		imgManager = new ImageManager();

		// De aca en adelante son elementos que se agregan al layout
		JLabel lblTitulo = new JLabel();
		lblTitulo.setBounds(200, 11, 159, 45);
		lblTitulo.setText("SALSA 20");
		lblTitulo.setFont(FONT_TITULOS);
		contentPane.add(lblTitulo);

		JLabel lblTitulo_1 = new JLabel();
		lblTitulo_1.setBackground(Color.LIGHT_GRAY);
		lblTitulo_1.setBounds(30, 70, 463, 22);
		lblTitulo_1.setText("Seleccione una imagen y realice una operacion");
		lblTitulo_1.setFont(FONT_SUBTITULOS);
		contentPane.add(lblTitulo_1);

		JButton btnArchivo = new JButton("Seleccionar imagen");
		btnArchivo.setFont(FONT_CAMPOS);
		btnArchivo.setBounds(134, 120, 216, 45);
		contentPane.add(btnArchivo);

		JLabel lblArchivoSel = new JLabel("Imagen seleccionada:");
		lblArchivoSel.setBounds(34, 201, 159, 35);
		lblArchivoSel.setFont(FONT_CAMPOS);
		contentPane.add(lblArchivoSel);

		JLabel lblKey = new JLabel("Clave (32 caracteres):");
		lblKey.setFont(FONT_CAMPOS);
		lblKey.setBounds(34, 247, 159, 35);
		contentPane.add(lblKey);

		txtKey = new JTextField();
		txtKey.setBounds(202, 247, 255, 35);
		contentPane.add(txtKey);
		txtKey.setColumns(10);

		txtNombre = new JTextField();
		txtNombre.setEditable(false);
		txtNombre.setColumns(10);
		txtNombre.setBounds(202, 201, 255, 35);
		contentPane.add(txtNombre);

		JButton btnCifrar = new JButton("Cifrar");
		btnCifrar.setFont(FONT_CAMPOS);
		btnCifrar.setBounds(10, 329, 216, 45);
		contentPane.add(btnCifrar);

		JButton btnDescifrar = new JButton("Descifrar");
		btnDescifrar.setFont(FONT_CAMPOS);
		btnDescifrar.setBounds(241, 329, 216, 45);
		contentPane.add(btnDescifrar);


		// Añadir listener para cerrar la ventana
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {

				// Preguntamos al usuario si desea realizar la acci�n
				int resultado = mensajeCerrarVentana();

				// Si apreto SI salimos, en caso contrario nos quedamos
				if (resultado == JOptionPane.YES_OPTION) {
					cerrarVentana();
				}
			}

		});

		// Definimos listeners para acciones especificas. Los nombres de los metodos son autoexplicativos
		btnArchivo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				buscarArchivo();
			}
		});

		btnCifrar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				realizarOperacion(OPERACION_CIFRAR);
			}
		});

		btnDescifrar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				realizarOperacion(OPERACION_DESCIFRAR);
			}
		});
	}

	// Metodos para mostrar mensajes
	private int mensajeCerrarVentana() {
		return JOptionPane.showConfirmDialog(null, MSJ_CONFIRNAR_CERRAR_VENTANA, TITULO_CERRAR_VENTANA,
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
	}
	private int mensajeError(String msj) {
		return JOptionPane.showConfirmDialog(null, msj, TITULO_ERROR, JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
	}
	private int mensajeExito(String msj) {
		return JOptionPane.showConfirmDialog(null, msj, TITULO_EXITO, JOptionPane.CLOSED_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void cerrarVentana() {
		this.dispose();
	}

	// Metodos para buscar un archivo en el equipo
	private void buscarArchivo() {
		JFileChooser chooser = new JFileChooser();

		// Especificamos que solo se pueden seleccionar archivos
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Abrir la ventana para buscar archivos y que se verifique que sea un .png
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();

			if(!file.getName().contains(".png")) {

				// En caso de querer elegir un archivo no .png, suelta un mensaje de error y seteamos el file elegido como null
				mensajeError(MSJ_ERROR_FILE_NO_PNG);
				file = null;
				txtNombre.setText("");
				return;
			}
			
			txtNombre.setText(file.getName());
		}
	}

	// Metodo que, dependiendo el modo de operacion, cifra o descifra una imagen
	private void realizarOperacion(int op) {
		String keyString = txtKey.getText();
		BufferedImage img;
		byte[] key;
		byte[] mensajeClaro;
		byte[] criptograma;

		// Debemos validar que: haya una key, que sea numerica de al menos 32 caracteres y que el archivo a cifrar no es nulo
		if (keyString == null || keyString.trim().isEmpty()) {
			mensajeError(MSJ_ERROR_KEY_VACIA);
			return;
		}
		if (!keyString.matches("^[0-9]+$")) {
			mensajeError(MSJ_ERROR_KEY_NO_NUMERICA);
			return;
		}
		if(keyString.length() != 32) {
			mensajeError(MSJ_ERROR_KEY_LONGITUD);
			return;
		}
		if (file == null) {
			mensajeError(MSJ_ERROR_FILE_NO_SELECCIONADO);
			return;
		}
		
		// Pasamos la key a bytes y despues, dependiendo el modo de operación, ciframos o desciframos
		key = keyString.getBytes();
		if (op == OPERACION_CIFRAR) {
			try {
				// Para cifrar leemos la imagen y la transformamos a textoClaro (array de bytes)
				img = imgManager.leerImagen(file.getPath());
				mensajeClaro = imgManager.convertirBytes(img);

				// Ciframos ese array de bytes y despues generamos la imagen que representa el mensaje cifrado
				criptograma = salsita.encriptar(mensajeClaro, key);
				imgManager.escribirImagen(criptograma, img.getWidth(), img.getHeight(), PATH_CIFRADO_OUT);
			} catch (IOException e) {
				// Mostramos por pantalla el error y finalizamos la ejecución
				mensajeError(MSJ_ERROR_NO_SE_PUDO_ABRIR_FILE + file.getPath());
				return;
			}
		} else if (op == OPERACION_DESCIFRAR) {
			try {
				// Para descifrar leemos la imagen y lo transformamos al criptograma (array de bytes)
				img = imgManager.leerImagen(file.getPath());
				criptograma = imgManager.convertirBytes(img);

				// Desciframos el mensaje y despues generamos la imagen que representa el mensaje claro
				mensajeClaro = salsita.desencriptar(criptograma, key);
				imgManager.escribirImagen(mensajeClaro, img.getWidth(), img.getHeight(), PATH_DESCIFRADO_OUT);
			} catch (IOException e) {
				// Mostramos por pantalla el error y finalizamos la ejecución
				mensajeError(MSJ_ERROR_NO_SE_PUDO_ABRIR_FILE + file.getPath());
				return;
			}
		}
		mensajeExito(MSJ_EXITO_OPERACION_REALIZADA);

		// Reseteamos el file
		file = null;
		txtNombre.setText("");
	}
}
