package com.DashBoxGeometryJulianMosquera.objetos;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Triangulo extends Objeto {

    // Buffer de vértices reutilizable (Excelente práctica para LibGDX)
    private static final Vector2[] VERTICES_BUFFER = new Vector2[] {
        new Vector2(), new Vector2(), new Vector2()
    };

    public Triangulo(float anchoForma, float altoForma, float posicionX, float posicionY, World world) {
        super(anchoForma, altoForma, posicionX, posicionY, world);
    }

    @Override
    protected void formarForma(PolygonShape shape, float mitadAncho, float mitadAlto) {
        // Vértice 0: Esquina inferior izquierda
        VERTICES_BUFFER[0].set(-mitadAncho, -mitadAlto);
        // Vértice 1: Esquina inferior derecha
        VERTICES_BUFFER[1].set(mitadAncho, -mitadAlto);
        // Vértice 2: Punta superior central
        VERTICES_BUFFER[2].set(0, mitadAlto);

        // Box2D exige que los vértices se declaren en sentido Anti-Horario (Counter-Clockwise).
        // Tu orden (Bottom-Left -> Bottom-Right -> Top-Center) es correcto.
        shape.set(VERTICES_BUFFER);
    }

    @Override
    public void setUserDataNombreFixture() {
        this.fixture.setUserData("TRIANGULO");
    }
}
