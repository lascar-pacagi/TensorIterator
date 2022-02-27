package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import util.Util;

public class Rendering {
	private static final Color BACKGROUND_COLOR = Color.rgb(44, 68, 43);
	private static final Color FOREGROUND_COLOR = Color.DIMGRAY;
	private static final Color CELL_COLOR = Color.BLACK;
	private static final Color TEXT_COLOR = Color.BEIGE;
	private static final Color ITERATOR_COLOR = Color.RED;

	public static void clear(GraphicsContext gc) {
		gc.setFill(BACKGROUND_COLOR);
		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
	}

	private static void setFont(GraphicsContext gc) {
		gc.setFill(TEXT_COLOR);
		double min_dim = Math.min(gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		int size = (int) Math.round(0.04 * min_dim);
		gc.setFont(new Font(Font.getDefault().getName(), size));
	}

	public static void drawArray(GraphicsContext gc, int arrayIndex, int nbArrays,
			                     multidimentional.Array<Integer> array, int index) {
		double width = gc.getCanvas().getWidth();
		double height = gc.getCanvas().getHeight();
		double delta_x = width / 20;
		double delta_y = height / (nbArrays + 1) * (arrayIndex + 1) + (arrayIndex - 1) * height / 20;
		double d = Math.min(width, height) / 180;
		double array_width = width - 2 * delta_x;
		double array_height = height / 10;
		double cell_width = array_width / array.underlyingArrayLength();
		gc.setFill(CELL_COLOR);
		for (int i : array) {
			gc.fillRect(delta_x + i * cell_width + d, delta_y + d, cell_width - d, array_height - d);
		}
		gc.setFill(FOREGROUND_COLOR);
		for (int i = 0; i <= array.underlyingArrayLength(); i++) {
			gc.fillRect(delta_x + i * cell_width, delta_y + d, d, array_height - d);
		}
		gc.fillRect(delta_x, delta_y, array_width + d, d);
		gc.fillRect(delta_x, delta_y + array_height, array_width + d, d);
		double text_start_x = delta_x + array_width / 2 - array_width / 8;
		double text_start_y = delta_y - height / 70;
		setFont(gc);
		String text = "Shape: " + Util.intArrayToString(array.shape());
		gc.fillText(text, text_start_x, text_start_y);
		if (index != -1) {
			text = "Index: " + Util.intArrayToString(array.flatIndexToIndices(index));
			gc.fillText(text, text_start_x, delta_y + array_height + array_height / 2);
			gc.setFill(ITERATOR_COLOR);
			gc.fillRect(delta_x + index * cell_width + d, delta_y + d, cell_width - d, array_height - d);
		}
	}
}
