import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Desktop;

public class HoughCircumferenceTransform {
    private int width; // Ancho de la imagen
    private int height; // Altura de la imagen
    private int[][][] houghSpace; // Espacio de Hough para acumular votos
    private int minRadius; // Radio mínimo para buscar circunferencias
    private int maxRadius; // Radio máximo para buscar circunferencias

    // Constructor para inicializar el espacio de Hough y calcular los parámetros
    public HoughCircumferenceTransform(BufferedImage image, int minRadius, int maxRadius) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;

        // Inicializar el espacio de Hough con tres dimensiones (a, b, r)
        this.houghSpace = new int[width][height][maxRadius - minRadius + 1];

        // Detectar bordes y rellenar el espacio de Hough
        populateHoughSpace(image);
    }

    // Método para detectar bordes y rellenar el espacio de Hough
    private void populateHoughSpace(BufferedImage image) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Verifica si el píxel es un borde
                if (isEdgePixel(image, x, y)) {
                    // Acumula votos para circunferencias con diferentes radios
                    for (int r = minRadius; r <= maxRadius; r++) {
                        plotCenters(x, y, r);
                    }
                }
            }
        }
    }

    // Detección simple de borde basada en un umbral
    private boolean isEdgePixel(BufferedImage image, int x, int y) {
        int color = image.getRGB(x, y);
        int brightness = (color >> 16) & 0xff; // Usa el canal rojo como indicador de brillo
        return brightness < 128; // Considera un borde si el brillo es menor a 128
    }

    // Método para acumular votos en el espacio de Hough para posibles centros de circunferencias
    private void plotCenters(int x, int y, int radius) {
        for (int angle = 0; angle < 360; angle++) {
            double theta = Math.toRadians(angle);
            int a = (int) (x - radius * Math.cos(theta)); // Coordenada del centro en X
            int b = (int) (y - radius * Math.sin(theta)); // Coordenada del centro en Y

            // Verifica que los índices estén dentro de los límites
            if (a >= 0 && a < width && b >= 0 && b < height) {
                houghSpace[a][b][radius - minRadius]++;
            }
        }
    }

    // Encuentra circunferencias desde el espacio de Hough
    public ArrayList<Circumference> findCircumferences(int threshold) {
        ArrayList<Circumference> circumferences = new ArrayList<>();
        for (int a = 0; a < width; a++) {
            for (int b = 0; b < height; b++) {
                for (int r = minRadius; r <= maxRadius; r++) {
                    // Agrega circunferencias que superan el umbral de votos
                    if (houghSpace[a][b][r - minRadius] > threshold) {
                        circumferences.add(new Circumference(a, b, r));
                    }
                }
            }
        }
        return circumferences;
    }

    public static void main(String[] args) {
        try {
            // Cargar la imagen desde un archivo
            BufferedImage image = ImageIO.read(new File("input.png"));
            BufferedImage colorImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            colorImage.getGraphics().drawImage(image, 0, 0, null);

            // Aplicar la transformada de Hough para detectar circunferencias
            HoughCircumferenceTransform houghTransform = new HoughCircumferenceTransform(colorImage, 20, 50); // Rango de radios
            ArrayList<Circumference> circumferences = houghTransform.findCircumferences(150); // Umbral de votos

            // Dibujar las circunferencias detectadas en la imagen en rojo
            for (Circumference circumference : circumferences) {
                circumference.drawCircumference(colorImage, new Color(255, 0, 0, 255)); // Rojo con alfa completo
            }

            // Guardar el resultado en un archivo
            File outputFile = new File("output_circles.png");
            ImageIO.write(colorImage, "png", outputFile);

            // Abrir automáticamente la imagen resultante
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outputFile);
            } else {
                System.out.println("Abrir la imagen automáticamente no es compatible en este sistema.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Clase que representa una circunferencia detectada
class Circumference {
    private int a, b, radius; // Centro (a, b) y radio de la circunferencia

    public Circumference(int a, int b, int radius) {
        this.a = a;
        this.b = b;
        this.radius = radius;
    }

    // Dibuja la circunferencia en la imagen
    public void drawCircumference(BufferedImage image, Color color) {
        int width = image.getWidth();
        int height = image.getHeight();
        int redColor = color.getRGB(); // Color rojo

        for (int angle = 0; angle < 360; angle++) {
            double theta = Math.toRadians(angle);
            int x = (int) (a + radius * Math.cos(theta));
            int y = (int) (b + radius * Math.sin(theta));

            // Verifica que los puntos estén dentro de los límites
            if (x >= 0 && x < width && y >= 0 && y < height) {
                image.setRGB(x, y, redColor);
            }
        }
    }
}
