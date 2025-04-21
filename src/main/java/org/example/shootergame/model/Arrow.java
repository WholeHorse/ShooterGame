package org.example.shootergame.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class Arrow extends Path {
    private static final double defaultArrowHeadSize = 9.0;
    private static final double defaultShaftThickness = 2.5;

    public Arrow(double startX, double startY, double endX, double endY, double arrowHeadSize) {
        super();
        strokeProperty().bind(fillProperty());
        setFill(Color.DARKBLUE);
        setStrokeWidth(defaultShaftThickness);

        // Shaft
        getElements().add(new MoveTo(startX, startY));
        getElements().add(new LineTo(endX, endY));

        // Head
        double angle = Math.atan2((endY - startY), (endX - startX)) - Math.PI / 2.0;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        // 1 point
        double x1 = (-1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + endX;
        double y1 = (-1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + endY;

        // 2 point
        double x2 = (1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + endX;
        double y2 = (1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + endY;

        getElements().add(new LineTo(x1, y1));
        getElements().add(new LineTo(x2, y2));
        getElements().add(new LineTo(endX, endY));
    }

    public Arrow(double startX, double startY, double endX, double endY) {
        this(startX, startY, endX, endY, defaultArrowHeadSize);
    }
}