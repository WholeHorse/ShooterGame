package org.example.shootergame.model;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class Arrow extends Pane {
    private static final double DEFAULT_ARROW_HEAD_SIZE = 9.0;
    private static final double DEFAULT_SHAFT_THICKNESS = 2.5;
    private static final double DEFAULT_FLETCH_LENGTH = 15.0;

    public Arrow(double startX, double startY, double endX, double endY) {
        this(startX, startY, endX, endY, DEFAULT_ARROW_HEAD_SIZE, DEFAULT_SHAFT_THICKNESS, DEFAULT_FLETCH_LENGTH);
    }

    public Arrow(double startX, double startY, double endX, double endY, double arrowHeadSize, double shaftThickness, double fletchLength) {
        // Calculate angle of the shaft
        double angle = Math.atan2((endY - startY), (endX - startX));

        // Shaft
        Line shaft = new Line(startX, startY, endX, endY);
        shaft.setStroke(Color.BROWN);
        shaft.setStrokeWidth(shaftThickness);

        // Arrowhead
        double sin = Math.sin(angle - Math.PI / 2.0);
        double cos = Math.cos(angle - Math.PI / 2.0);
        double x1 = (-0.5 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + endX;
        double y1 = (-0.5 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + endY;
        double x2 = (0.5 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + endX;
        double y2 = (0.5 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + endY;
        Polygon arrowhead = new Polygon();
        arrowhead.getPoints().addAll(endX, endY, x1, y1, x2, y2);
        arrowhead.setFill(Color.GRAY);

        // Fletching
        double fletchAngle1 = angle + Math.PI + Math.toRadians(30); // 30 degrees from backward direction
        double fletchX1 = startX + fletchLength * Math.cos(fletchAngle1);
        double fletchY1 = startY + fletchLength * Math.sin(fletchAngle1);
        double fletchAngle2 = angle + Math.PI - Math.toRadians(30); // 30 degrees from backward direction
        double fletchX2 = startX + fletchLength * Math.cos(fletchAngle2);
        double fletchY2 = startY + fletchLength * Math.sin(fletchAngle2);

        Line fletch1 = new Line(startX, startY, fletchX1, fletchY1);
        Line fletch2 = new Line(startX, startY, fletchX2, fletchY2);
        fletch1.setStroke(Color.WHITE);
        fletch2.setStroke(Color.WHITE);
        fletch1.setStrokeWidth(1.0);
        fletch2.setStrokeWidth(1.0);

        // Add components to the pane
        getChildren().addAll(fletch1, fletch2, shaft, arrowhead);
    }
}