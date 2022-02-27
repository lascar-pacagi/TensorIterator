package controller;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import multidimentional.Array;
import multidimentional.Operator;
import util.Pair;
import util.Util;
import view.Rendering;

public class Controller {
	@FXML
	private StackPane pane;
	@FXML
	private Canvas canvas;
	@FXML
	private MenuItem reshape;
	@FXML
	private MenuItem slice;
	@FXML
	private MenuItem iterate;
	private GraphicsContext gc;
	private State state = new State();

	@FXML
	void initialize() {
		gc = canvas.getGraphicsContext2D();
		state.draw();
		canvas.widthProperty().bind(pane.widthProperty());
		canvas.heightProperty().bind(pane.heightProperty());
		canvas.widthProperty().addListener(o -> state.draw());
		canvas.heightProperty().addListener(o -> state.draw());
		pane.setOnKeyPressed(e -> keyPressed(e));
	}

	private void keyPressed(KeyEvent e) {
		state.keyPressed(e);
	}

	@FXML
	void createOneArray() {
		try {
			state = new OneArrayState();
		} catch (Exception e) {
		}
	}

	@FXML
	void createTwoArrays() {
		try {
			state = new TwoArraysState();
		} catch (Exception e) {
		}
	}

	@FXML
	void reshape() {
		state.reshape();
	}

	@FXML
	void slice() {
		state.slice();
	}

	@FXML
	void iterate() {
		state.iterate();
	}

	@FXML
	void help() {
		alert("Help", "", "README at ");
	}

	@FXML
	void about() {
		alert("About", "", "Author: MrC00der");
	}

	private static void error(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("An error occured");
		alert.setContentText(msg);
		alert.showAndWait();
	}

	private static Optional<String> dialog(String title, String header, String content) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		dialog.setContentText(content);
		return dialog.showAndWait();
	}

	private static void alert(String title, String header, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
	@SuppressWarnings("serial")
	public static class InvalideStateException extends Exception {
	}

	private class State {
		public void draw() {
			pane.requestFocus();
			reshape.setDisable(true);
			slice.setDisable(true);
			iterate.setDisable(true);
			Rendering.clear(gc);
		}

		public void keyPressed(KeyEvent e) {
		}

		public void iterate() {
		}

		public void reshape() {
		}

		public void slice() {
		}

		protected Array<Integer> initArray(int... dims) {
			Array<Integer> res = new Array<>(dims);
			Operator.init(res, new Supplier<Integer>() {
				int k = 0;

				@Override
				public Integer get() {
					return k++;
				}
			});
			return res;
		}
	}

	private class OneArrayState extends State {
		private Array<Integer> array;
		private Iterator<Integer> it = new Util.EmptyIterator<>();
		private int index = -1;

		public OneArrayState() throws InvalideStateException {
			Optional<String> shape = dialog("Create Array", "", "Enter the shape:");
			if (shape.isPresent()) {
				try {
					array = initArray(Util.stringToIntArray(shape.get()));
					draw();
				} catch (Exception e) {
					error("Bad shape");
					throw new InvalideStateException();
				}
			} else {
				throw new InvalideStateException();
			}
		}

		@Override
		public void draw() {
			super.draw();
			reshape.setDisable(false);
			slice.setDisable(false);
			iterate.setDisable(false);
			Rendering.drawArray(gc, 0, 1, array, index);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getCode() == KeyCode.ESCAPE) {
				it = new Util.EmptyIterator<>();
			}
			if (it.hasNext()) {
				index = it.next();
			} else {
				index = -1;
			}
			draw();
		}

		@Override
		public void iterate() {
			it = array.iterator();
			index = it.next();
			draw();
		}

		@Override
		public void reshape() {
			Optional<String> shape = 
					dialog("Reshape", 
					       "Actual shape is: " + Util.intArrayToString(array.shape()),
					       "Enter the new shape:");
			if (shape.isPresent()) {
				try {
					Array<Integer> res = array.reshape(Util.stringToIntArray(shape.get()));
					if (!res.sameUnderlyingArray(array)) {
						array = initArray(res.shape());
					} else {
						array = res;
					}
					draw();
				} catch (Exception e) {
					error("Bad shape");
				}
			}
		}

		@Override
		public void slice() {
			int[] shape = array.shape();
			Optional<String> slice = 
					dialog("Slice", 
						   "Actual shape is: " + Util.intArrayToString(shape),
						   "Enter the bounds:");
			if (slice.isPresent()) {
				try {
					int[][] bounds = Util.stringToBounds(slice.get());
					for (int i = 0; i < bounds.length; i++) {
						if (bounds[i][1] == -1) bounds[i][1] = shape[i];
					}
					array = array.slice(bounds);
					draw();
				} catch (Exception e) {
					error("Bad bounds");
				}
			}
		}

	}

	private class TwoArraysState extends State {
		private Array<Integer> array1;
		private Array<Integer> array2;
		private Array<Integer> array3;
		private Iterator<Pair<Integer, Integer>> it1 = new Util.EmptyIterator<>();
		private Iterator<Integer> it2 = new Util.EmptyIterator<>();
		private int index1 = -1;
		private int index2 = -1;
		private int index3 = -1;

		public TwoArraysState() throws InvalideStateException {
			try {
				Optional<String> shape = 
						dialog("Create Array", 
							   "First array", 
							   "Enter the shape:");
				if (shape.isPresent()) {
					array1 = initArray(Util.stringToIntArray(shape.get()));
				} else {
					throw new InvalideStateException();
				}
				shape = dialog("Create Array", "Second array", "Enter the shape:");
				if (shape.isPresent()) {
					array2 = initArray(Util.stringToIntArray(shape.get()));
				} else {
					throw new InvalideStateException();
				}
				draw();
			} catch (Exception e) {
				error("Bad shape");
				throw new InvalideStateException();
			}
		}

		@Override
		public void draw() {
			super.draw();
			iterate.setDisable(false);
			Rendering.drawArray(gc, 0, 3, array1, index1);
			Rendering.drawArray(gc, 1, 3, array2, index2);
			if (index1 != -1) {
				Rendering.drawArray(gc, 2, 3, array3, index3);
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getCode() == KeyCode.ESCAPE) {
				it1 = new Util.EmptyIterator<>();
				it2 = new Util.EmptyIterator<>();
			}
			if (it1.hasNext()) {
				Pair<Integer, Integer> p = it1.next();
				index1 = p.first;
				index2 = p.second;
				index3 = it2.next();
			} else {
				index1 = -1;
				index2 = -1;
			}
			draw();
		}

		@Override
		public void iterate() {
			try {
				it1 = new Array.ArrayMultiIterator<>(array1, array2);
				Pair<Integer, Integer> p = it1.next();
				index1 = p.first;
				index2 = p.second;
				array3 = initArray(Array.broadcastedDims(array1, array2));
				it2 = array3.iterator();
				index3 = it2.next();
				draw();
			} catch (IllegalArgumentException e) {
				error("Broadcast impossible");
			}
		}

	}

}