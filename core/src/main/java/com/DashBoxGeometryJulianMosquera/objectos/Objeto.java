package com.DashBoxGeometryJulianMosquera.objectos;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;


public abstract class Objeto {

    // Protegidos para que los hijos puedan acceder si es necesario
    protected Body body;
    protected Fixture fixture;
    protected float ancho, alto;

    // Buffer para evitar crear un Vector2 cada vez que calculamos la posición
    private static final Vector2 POS_BUFFER = new Vector2();

    public Objeto(float anchoForma, float altoForma, float posicionX, float posicionY, World world) {
        this.ancho = anchoForma;
        this.alto = altoForma;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        // Usamos el buffer para evitar instanciar un new Vector2()
        bodyDef.position.set(obtenerUbicacionReal(ancho, alto, posicionX, posicionY));
        bodyDef.bullet = false;
        bodyDef.fixedRotation = true;

        this.body = world.createBody(bodyDef);

        crearFixture();
    }

    private void crearFixture() {
        PolygonShape shape = new PolygonShape();

        float mitadAncho = ancho / 2f;
        float mitadAlto = alto / 2f;

        FixtureDef fixtureDef = new FixtureDef();

        // Llamada polimórfica: El hijo decidirá cómo dar forma al shape
        formarForma(shape, mitadAncho, mitadAlto);

        fixtureDef.shape = shape;
        this.fixture = body.createFixture(fixtureDef);
        fixture.setFriction(0);

        setUserDataNombreFixture();

        // ¡CRUCIAL y CORRECTO!
        shape.dispose();
    }

    // Método que DEBEN implementar los hijos
    protected void formarForma(PolygonShape shape, float mitadAncho, float mitadAlto){
        shape.setAsBox(mitadAncho, mitadAlto);
    };

    protected void setUserDataNombreFixture(){
        this.fixture.setUserData("OBSTACULO");
    }

    public Fixture getFixture() { return fixture; }
    public Body getBody() { return body; }

    // Corrección del bug lógico en la asignación de 'y'
    private Vector2 obtenerUbicacionReal(float ancho, float alto, float x, float y){
        if (x <= 0) {
            x = ancho / 2f;
        } else {
            x = x + (ancho / 2f);
        }

        // CORREGIDO: Tenías "if(x <= 0)" evaluando el eje Y
        if (y <= 0) {
            y = alto / 2f;
        } else {
            y = y + (alto / 2f);
        }

        return POS_BUFFER.set(x, y);
    }
}
