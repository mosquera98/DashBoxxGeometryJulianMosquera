package com.DashBoxGeometryJulianMosquera.Pantallas;

import com.DashBoxGeometryJulianMosquera.GameDashBoxGeometry;
import com.DashBoxGeometryJulianMosquera.GameInputProcessor;
import com.DashBoxGeometryJulianMosquera.objetos.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Nivel_1 implements Screen {


    private final GameDashBoxGeometry miGame;
    private OrthographicCamera camara;
    private Viewport fitViewport;
    private float  cantidadDeSaltosEnElMomento = 0, tiempoBufferSalto = 0f, ANCHO_PANTALLA, ALTO_PANTALLA;
    private static final float JUMP_VELOCITY = 27.5f,  VELOCIDADhJUGADOR = 9.05f, VENTANA_BUFFER_SALTO = 0.15f,
        TIME_STEP = 1 / 60f, cantidadMaxcimaDeSaltosJugador = 2, ALTO_MUNDO = 9f, ANCHO_MUNDO =17f,
        GRAVEDAD_MUNDO = - 150;
    private World world; // El simulador físico principal.
    // Herramientas de renderizado
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Sprite spriteJugador; // La representación visual en pantalla.
    private Texture texturaJugador, texturaPiso, texturaTriangulo, texturaPlataformaX3, texturaPlataformaX5, texturaCuadrado,
        texturaCaja, textureFondoBotonReiniciarUp, textureFondoBotonReiniciarDonw;
    private Body bodyJugador;


    // Estado lógico. En un juego complejo, esto debería vivir en un PlayerComponent o StateMachine.
    private boolean puedeSaltar = false, saltoPendiente = false, esperandoPrimerAterrizaje = true,
        estaVivo = true;



    float[][] posicionesDePisos, posicionesDeTriangulos, posicionesDePlataformasX3, posicionesDePlataformasX5, posicionesDeCuadrados, posicionesDeCajas;
    Triangulo[] triangulos;
    Piso[] pisos;
    PlataformaX3[] plataformasX3;
    PlataformaX5[] plataformasX5;
    Cuadrado[] cuadrados;
    Caja[] cajas;
    Sprite[] spritesPisos, spritesTriangulos, spritesPlataformasX3, spritesCuadrados, spritesCajas, spritesPlataformasX5;


    private float accumulator = 0f;
    // Variable para almacenar el residuo del acumulador (Alpha para interpolación)
    private float alpha = 0f;

    // Vectores para almacenar las posiciones anterior y actual del cuerpo físico
    private Vector2 previousPosition = new Vector2();
    private Vector2 renderPosition = new Vector2();

    // Iteraciones del solver de Box2D. 6 de velocidad y 2 de posición es el estándar dorado recomendado por Erin Catto
    // para equilibrar precisión de colisiones y rendimiento en CPU móviles.
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private GameInputProcessor inputProcessor;



    Button botonReiniciar;
    Stage stage;

    Music cancionDeFondo;
    Sound efectoSonidoChoque;

    /// ////////////////////////////////////////////////////////////////
    public Nivel_1(GameDashBoxGeometry unGame){
        this.miGame =  unGame;
    }



    /// ////////////////////////////////////////////////////////////////


    /// /////////////////////////////////////////////////////////////////
    void crearJugador(float anchoYAlto){

        spriteJugador = new Sprite(texturaJugador);
        spriteJugador.setSize(1, 1);
        spriteJugador.setOriginCenter();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(0, ALTO_MUNDO / 2f);//El objeto aparece desde la mitad del mundo

        bodyDef.bullet = false; // BULLET = TRUE: Esto habilita la Detección de Colisiones Continua (CCD). CRÍTICO en juegos rápidos. Evita el "Tunneling" (que el cubo atraviese el suelo o una pared si va muy rápido entre frames).
        bodyDef.fixedRotation = false; // FIXED ROTATION = TRUE: Evita que el motor físico gire el cubo si choca con la esquina de un bloque.

        bodyJugador = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();// Se crea el shapa con una forma poligonal
        shape.setAsBox(anchoYAlto / 2f, anchoYAlto / 2f);// 1 metro de alto y ancho

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f; // La densidad afecta la masa. Con una forma de 1x1, masa = densidad * area = 1kg.
        fixtureDef.restitution = 0.0f; // Restitución 0: Un bloque de Geometry Dash aterriza y se queda pegado, no rebota como una pelota de goma.
        fixtureDef.friction = 1f; // Misma razón que el suelo.

        Fixture playerFixture = bodyJugador.createFixture(fixtureDef);
        playerFixture.setUserData("JUGADOR");

        shape.dispose();


    }
    /**
     * Registra el toque sin descartarlo si ocurre durante la caída inicial.
     * Así el primer salto se ejecuta al tocar el piso, aunque el jugador haya
     * pulsado la pantalla antes de que Box2D notificara el aterrizaje.
     */
    public void solicitarSalto() {
        saltoPendiente = true;
        tiempoBufferSalto = esperandoPrimerAterrizaje
            ? Float.POSITIVE_INFINITY
            : VENTANA_BUFFER_SALTO;
    }
    private void procesarSaltoPendiente() {
        if (!saltoPendiente) {
            return;
        }

        if (puedeSaltar) {
            saltarJugador();
            saltoPendiente = false;
            tiempoBufferSalto = 0f;
        } else if (!esperandoPrimerAterrizaje) {
            // Fuera de la caída inicial solo conservamos el toque durante una
            // ventana corta para no provocar un salto tardío e inesperado.
            tiempoBufferSalto -= TIME_STEP;
            if (tiempoBufferSalto <= 0f) {
                saltoPendiente = false;
            }
        }
    }
    private void saltarJugador() {
        if (cantidadDeSaltosEnElMomento < cantidadMaxcimaDeSaltosJugador && puedeSaltar && estaVivo) {

            bodyJugador.setLinearVelocity(bodyJugador.getLinearVelocity().x, JUMP_VELOCITY);
            bodyJugador.setAngularVelocity(-4.5f);
            cantidadDeSaltosEnElMomento += 1;
            actualizarEstadoDeSalto();
        }
    }
    private void correrJugador(){
        bodyJugador.setLinearVelocity( VELOCIDADhJUGADOR , 0);
    }
    private void agregarListenerDeContactosJugador() {
        // El ContactListener es la única forma confiable de saber cuándo dos Fixtures se tocan en Box2D.
        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(Contact contact) {
                // Recuperamos el UserData.
                // [OJO]: Usar Strings aquí está bien para un prototipo, pero es ineficiente en producción. (Ver sección de recomendaciones).
                Object userDataA = contact.getFixtureA().getUserData();
                Object userDataB = contact.getFixtureB().getUserData();

                // Verificamos colisión sin importar el orden (A vs B o B vs A).
                if ((("JUGADOR".equals(userDataA) && "PISO".equals(userDataB)) ||  ("PISO".equals(userDataA) && "JUGADOR".equals(userDataB))) ||
                    (("JUGADOR".equals(userDataA) && "PLATAFORMA".equals(userDataB)) || ("PLATAFORMA".equals(userDataA) && "JUGADOR".equals(userDataB)))) {

                    contactosDePiso++;
                    cantidadDeSaltosEnElMomento = 0;
                    esperandoPrimerAterrizaje = false;
                    actualizarEstadoDeSalto();
                    System.out.println(bodyJugador.getPosition().x);


                }

                else if ((("TRIANGULO".equals(userDataA) && "JUGADOR".equals(userDataB)) || ("JUGADOR".equals(userDataA) && "TRIANGULO".equals(userDataB))) ||
                    (("CUADRADO".equals(userDataA) && "JUGADOR".equals(userDataB)) || ("JUGADOR".equals(userDataA) && "CUADRADO".equals(userDataB)))) {

                    cancionDeFondo.stop();
                    tinturarJugador();
                    estaVivo = false;
                    efectoSonidoChoque.play(0.1f);
                    mostrarBotonReiniciar();


                }

            }

            @Override
            public void endContact(Contact contact) {

                Object userDataA = contact.getFixtureA().getUserData();
                Object userDataB = contact.getFixtureB().getUserData();


                if ((("JUGADOR".equals(userDataA) && "PISO".equals(userDataB)) || ("PISO".equals(userDataA) && "JUGADOR".equals(userDataB))) ||
                    (("JUGADOR".equals(userDataA) && "PLATAFORMA".equals(userDataB)) || ("PLATAFORMA".equals(userDataA) && "JUGADOR".equals(userDataB)))){
                    //Si el contacto termina (salto exitoso o caída por borde).

                    // Un jugador puede tocar dos segmentos contiguos a la vez.
                    // Contar contactos evita desactivar el salto al salir de solo
                    // uno de ellos mientras todavía sigue apoyado en el otro.
                    contactosDePiso = Math.max(0, contactosDePiso - 1);
                    actualizarEstadoDeSalto();

                }

                else if (("JUGADOR".equals(userDataA) && "TRIANGULO".equals(userDataB)) ||
                    ("TRIANGULO".equals(userDataA) && "JUGADOR".equals(userDataB))) {
                    // reiniciar nivel, etc.
                }

            }

            // preSolve y postSolve se usan para modificar la física justo antes/después de que el solver la resuelva
            // (ej: plataformas one-way). Aquí no los necesitamos.
            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }
    /*private void agregarListenerDeContactosJugador() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object userDataA = contact.getFixtureA().getUserData();
                Object userDataB = contact.getFixtureB().getUserData();

                if (("PIES_JUGADOR".equals(userDataA) && "PISO".equals(userDataB)) ||
                    ("PISO".equals(userDataA) && "PIES_JUGADOR".equals(userDataB))) {

                    cantidadDeSaltosEnElMomento = 0;
                    puedeSaltar = true;

                    calcularPisoTocadoPresente(contact);
                }
            }

            @Override
            public void endContact(Contact contact) {
                Object userDataA = contact.getFixtureA().getUserData();
                Object userDataB = contact.getFixtureB().getUserData();

                if (("PIES_JUGADOR".equals(userDataA) && "PISO".equals(userDataB)) ||
                    ("PISO".equals(userDataA) && "PIES_JUGADOR".equals(userDataB))) {

                    calcularUltimoPisoTocado(contact);

                    puedeSaltar = preguntarSiCambioDePiso() || (cantidadDeSaltosEnElMomento != 0);

                }
            }
            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }*/
    // Número de fixtures de piso que sostienen al jugador actualmente.
    private int contactosDePiso = 0;
    private void actualizarEstadoDeSalto() {
        // Antes del primer aterrizaje no hay salto aéreo. Después se mantiene el
        // segundo salto previsto por el diseño, aun cuando ya dejó el piso.
        puedeSaltar = !esperandoPrimerAterrizaje
            && (contactosDePiso > 0 || cantidadDeSaltosEnElMomento > 0);
    }
    public void tinturarJugador(){
        spriteJugador.setColor(new Color(Color.RED));
    }
    /// //////////////////////////////////////////////////////////



    /// //////////////////////////////////////////////////////////

    /**
     * Método helper optimizado para evitar re-cálculos repetitivos y zero-allocation.
     */
    private void updateSpriteFromBody(Sprite sprite, Body body, Vector2 overridePos) {
        float posX = (overridePos != null) ? overridePos.x : body.getPosition().x;
        float posY = (overridePos != null) ? overridePos.y : body.getPosition().y;

        float originX = sprite.getWidth() / 2f;
        float originY = sprite.getHeight() / 2f;

        sprite.setOrigin(originX, originY);
        sprite.setPosition(posX - originX, posY - originY);
        sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }
    private Sprite ObtenerSprite(Texture unaTextura, float anchoSprite, float altoSprite){


        Sprite unaSprite = new Sprite(unaTextura);
        unaSprite.setSize(anchoSprite, altoSprite);
        unaSprite.setOriginCenter();

        return unaSprite;



    }
    /// /////////////////////////////////////////////////////////////



    /// /////////////////////////////////////////////////////////////////////
    private void crearPosicionesDePisos(){

        posicionesDePisos = new float[][]{
            {0,0},
            {9,0},
            {24,0},
            {33,0},
            {42,0},
            {51,0},
            {102.5f,0},
            {111.5f,0},
            {120.5f,0},
            {150.5f,0},
            {159.5f,0},
            {168.5f,0},
            {210.5f,0},
            {219.5f,0},
            {228.5f,0}        };

    }
    private void crearArregloBodyesPisosYAgregarPisosAMundo(){

        int cantidadDePisos = posicionesDePisos.length;

        pisos = new Piso[cantidadDePisos];

        for (int i = 0; i < cantidadDePisos; i++){
            pisos[i] = new Piso(9,1, posicionesDePisos[i][0], posicionesDePisos[i][1], world);
        }
    }
    private void crearArraySpritePisos(){

        int cantidadDePisos = posicionesDePisos.length;

        spritesPisos = new Sprite[cantidadDePisos];

        for (int i = 0; i < cantidadDePisos; i++){

            spritesPisos[i] = ObtenerSprite(texturaPiso, 9f, 1f);

        }

    }
    private void centrarSpritesConBodysPisos(){
        int cantidadDePisos = posicionesDePisos.length;


        for (int i = 0; i < cantidadDePisos; i++){

            updateSpriteFromBody(spritesPisos[i], pisos[i].getBody(), null);

        }

    }
    private void dibujarSpritesPisos(){

        int cantidadDePisos = posicionesDePisos.length;

        for (int i = 0; i < cantidadDePisos; i++){

            spritesPisos[i].draw(batch);
        }
    }
    /// //////////////////////////////////////////////////////



    /// /////////////////////////////////////////////////////////
    private void crearPosicionesTriangulos(){

        posicionesDeTriangulos = new float[][]{
            {5,1},
            {15.5f,2},
            {32.5f,1},
            {36.5f,1},
            {40.5f,1},
            {50.5f,1},
            {54.5f,1},
            {91.5f,4},
            {96.5f,3},
            {101.5f,2},
            {106.5f,1},
            {116.5f,4},
            {117.5f,1},
            {118.5f,1},
            {119.5f,1},
            {120.5f,1},
            {153,1},
            {154.5f,2},
            {160.5f,1},
            {163.5f,1},
            {162,2},
            {171,1},
            {169.5f,2},
            {184.5f,3},
            {186.5f,3},
            {185.5f,3},
            {192.5f,3},
            {193.5f,3},
            {194.5f,3},
            {200.5f,3},
            {201.5f,3},
            {202.5f,3},
            {27.5f,3},
            {44.5f,2}
        };

    }
    public void crearArregloBodyesTriangulosYAgregarTriangulosAMundo(){

        int cantidadDeTriangulos = posicionesDeTriangulos.length;

        triangulos = new Triangulo[cantidadDeTriangulos];

        for (int i = 0; i < cantidadDeTriangulos; i++){

            triangulos[i] = new Triangulo(1,1, posicionesDeTriangulos[i][0], posicionesDeTriangulos[i][1], world);
        }

    }
    public void crearArraySpriteTriangulo(){

        int cantidadDeTriangulos = posicionesDeTriangulos.length;

        spritesTriangulos = new Sprite[cantidadDeTriangulos];

        for (int i = 0; i < cantidadDeTriangulos; i++){

            spritesTriangulos[i] = ObtenerSprite(texturaTriangulo, 1f, 1f);

        }


    }
    private void centrarSpritesConBodysTriangulo(){
        int cantidadDeTriangulos = posicionesDeTriangulos.length;


        for (int i = 0; i < cantidadDeTriangulos; i++){

            updateSpriteFromBody(spritesTriangulos[i], triangulos[i].getBody(), null);

        }

    }
    private void dibujarSpritesTriangulos(){

        int cantidadDeTriangulos = posicionesDeTriangulos.length;

        for (int i = 0; i < cantidadDeTriangulos; i++){

            spritesTriangulos[i].draw(batch);
        }
    }
    /// ///////////////////////////////////////////////////////



    /// //////////////////////////////////////////////////////
    private void crearPosicionesPlataformasX3(){

        posicionesDePlataformasX3 = new float[][]{
            {60,1},
            {63,2},
            {66,3},
            {69,4},
            {72,5},
            {76.5f,5},
            {81,5},
            {85.5f,5},
            {89.5f,3},
            {132.5f,1},
            {137,1},
            {141.5f,1},
            {178.5f,1},
            {181.5f,2}        };
    }
    public void crearArregloBodyesPlataformasX3YAgregarPlataformasX3AMundo(){

        int cantidadDePlataformas = posicionesDePlataformasX3.length;

        plataformasX3 = new PlataformaX3[cantidadDePlataformas];

        for (int i = 0; i < cantidadDePlataformas; i++){

            plataformasX3[i] = new PlataformaX3(3,1f, posicionesDePlataformasX3[i][0], posicionesDePlataformasX3[i][1], world);
        }

    }
    public void crearArraySpritePlataformasX3(){

        int cantidadDePlataformas = posicionesDePlataformasX3.length;

        spritesPlataformasX3 = new Sprite[cantidadDePlataformas];

        for (int i = 0; i < cantidadDePlataformas; i++){

            spritesPlataformasX3[i] = ObtenerSprite(texturaPlataformaX3, 3f, 1f);

        }
    }
    private void centrarSpritesConBodysPlataformasX3(){
        int cantidadDePlataformas = posicionesDePlataformasX3.length;

        for (int i = 0; i < cantidadDePlataformas; i++){

            updateSpriteFromBody(spritesPlataformasX3[i], plataformasX3[i].getBody(), null);

        }
    }
    private void dibujarSpritesPlataformasX3(){

        int cantidadDePlataformas = posicionesDePlataformasX3.length;

        for (int i = 0; i < cantidadDePlataformas; i++){

            spritesPlataformasX3[i].draw(batch);
        }
    }
    /// //////////////////////////////////////////////////////



    /// //////////////////////////////////////////////////////
    private void crearPosicionesPlataformasX5(){

        posicionesDePlataformasX5 = new float[][]{
            {12.5f,1},
            {17.5f,1},
            {92.5f,2},
            {97.5f,1},
            {112.5f,3},
            {187.5f,2},
            {195.5f,2},
            {203.5f,2}
        };
    }
    public void crearArregloBodyesPlataformasX5YAgregarPlataformasX5AMundo(){

        int cantidadDePlataformas = posicionesDePlataformasX5.length;

        plataformasX5 = new PlataformaX5[cantidadDePlataformas];

        for (int i = 0; i < cantidadDePlataformas; i++){

            plataformasX5[i] = new PlataformaX5(5,1f, posicionesDePlataformasX5[i][0], posicionesDePlataformasX5[i][1], world);
        }

    }
    public void crearArraySpritePlataformasX5(){

        int cantidadDePlataformas = posicionesDePlataformasX5.length;

        spritesPlataformasX5 = new Sprite[cantidadDePlataformas];

        for (int i = 0; i < cantidadDePlataformas; i++){

            spritesPlataformasX5[i] = ObtenerSprite(texturaPlataformaX5, 5f, 1f);

        }
    }
    private void centrarSpritesConBodysPlataformasX5(){
        int cantidadDePlataformas = posicionesDePlataformasX5.length;

        for (int i = 0; i < cantidadDePlataformas; i++){

            updateSpriteFromBody(spritesPlataformasX5[i], plataformasX5[i].getBody(), null);

        }
    }
    private void dibujarSpritesPlataformasX5(){

        int cantidadDePlataformas = posicionesDePlataformasX5.length;

        for (int i = 0; i < cantidadDePlataformas; i++){

            spritesPlataformasX5[i].draw(batch);
        }
    }
    /// //////////////////////////////////////////////////////



    /// //////////////////////////////////////////////////////
    private void crearPosicionesCuadrados(){

        posicionesDeCuadrados = new float[][]{
            {8.5f,2.5f},
            {8.5f,3.5f},
            {8.5f,4.5f},
            {9.5f,4.5f},
            {9.5f,3.5f},
            {27.5f,1},
            {27.5f,2},
            {44.5f,1},
            {111.5f,1},
            {111.5f,2},
            {111.5f,3},
            {154f,1},
            {155f,1},
            {161.5f,1},
            {162.5f,1},
            {169f,1},
            {170f,1},
            {174.5f,3.5f},
            {174.5f,2.5f},
            {175.5f,3.5f} };
    }
    public void crearArregloBodyesCuadradosYAgregarCuadradosAMundo(){

        int cantidadDecuadrados = posicionesDeCuadrados.length;

        cuadrados = new Cuadrado[cantidadDecuadrados];

        for (int i = 0; i < cantidadDecuadrados; i++){

            cuadrados[i] = new Cuadrado(1,1f, posicionesDeCuadrados[i][0], posicionesDeCuadrados[i][1], world);
        }

    }
    public void crearArraySpriteCuadrados(){

        int cantidadDeCuadrados = posicionesDeCuadrados.length;

        spritesCuadrados = new Sprite[cantidadDeCuadrados];

        for (int i = 0; i < cantidadDeCuadrados; i++){

            spritesCuadrados[i] = ObtenerSprite(texturaCuadrado, 1f, 1f);

        }
    }
    private void centrarSpritesConBodysCuadrados(){
        int cantidadDeCuadrados = posicionesDeCuadrados.length;

        for (int i = 0; i < cantidadDeCuadrados; i++){

            updateSpriteFromBody(spritesCuadrados[i], cuadrados[i].getBody(), null);

        }
    }
    private void dibujarSpritesCuadrados(){

        int cantidadDeCuadrados = posicionesDeCuadrados.length;

        for (int i = 0; i < cantidadDeCuadrados; i++){

            spritesCuadrados[i].draw(batch);
        }
    }
    /// //////////////////////////////////////////////////////



    /// /////////////////////////////////////////////////////////
    private void crearPosicionesCajas(){

        posicionesDeCajas= new float[][]{

            {129,1},
            {130,1},
            {146.5f,1},
            {147.5f,1},
            {177.5f,0}
        };

    }
    public void crearArregloBodyesCajasYAgregarCajasAMundo(){

        int cantidadDeCajas = posicionesDeCajas.length;

        cajas = new Caja[cantidadDeCajas];

        for (int i = 0; i < cantidadDeCajas; i++){

            cajas[i] = new Caja(1,1, posicionesDeCajas[i][0], posicionesDeCajas[i][1], world);
        }

    }
    public void crearArraySpritesCajas(){

        int cantidadDeCajas = posicionesDeCajas.length;

        spritesCajas = new Sprite[cantidadDeCajas];

        for (int i = 0; i < cantidadDeCajas; i++){

            spritesCajas[i] = ObtenerSprite(texturaCaja, 1f, 1f);

        }


    }
    private void centrarSpritesConBodysCajas(){

        int cantidadDeCajas = posicionesDeCajas.length;


        for (int i = 0; i < cantidadDeCajas; i++){

            updateSpriteFromBody(spritesCajas[i], cajas[i].getBody(), null);

        }

    }
    private void dibujarSpritesCajas(){

        int cantidadDeCajas = posicionesDeCajas.length;

        for (int i = 0; i < cantidadDeCajas; i++){

            spritesCajas[i].draw(batch);
        }
    }
    /// ///////////////////////////////////////////////////////



    /// //////////////////////////////////////////////////////
    public void crearBotonReiniciarConEstilo(){
        textureFondoBotonReiniciarUp = new Texture(Gdx.files.internal("BOTONES/botonReiniciarUp.png"), true);
        textureFondoBotonReiniciarUp.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        textureFondoBotonReiniciarDonw= new Texture(Gdx.files.internal("BOTONES/botonReiniciarDown.png"), true);
        textureFondoBotonReiniciarDonw.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        NinePatch patchUp = new NinePatch(textureFondoBotonReiniciarUp);
        NinePatch patchDown = new NinePatch(textureFondoBotonReiniciarDonw);

        NinePatchDrawable drawableUp = new NinePatchDrawable(patchUp);
        NinePatchDrawable drawableDown = new NinePatchDrawable(patchDown);

        Button.ButtonStyle estiloBotonMusica = new Button.ButtonStyle();

        estiloBotonMusica.up = drawableUp;
        estiloBotonMusica.down = drawableDown;

        botonReiniciar = new Button(estiloBotonMusica);
        botonReiniciar.setHeight(ALTO_PANTALLA / 15);
        botonReiniciar.setWidth(botonReiniciar.getHeight());

        botonReiniciar.setPosition(((ANCHO_PANTALLA - botonReiniciar.getWidth())) / 2,  (( ALTO_PANTALLA) -  botonReiniciar.getHeight()) / 2);
        botonReiniciar.setTransform(true);
        botonReiniciar.setOrigin(Align.center);
        botonReiniciar.getColor().a = 0;
    }

    public void agregarEventosBotonReiniciar(){

        botonReiniciar.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                miGame.disposearPantallaActual();
                miGame.iniciarPantallaJuego();

            }

        });

    }

    public void mostrarBotonReiniciar(){
        botonReiniciar.addAction(Actions.fadeIn(2f));
    }
    /// ///////////////////////////////////////////////////////





    /// ////////////////////////////////////////////////////////
    @Override
    public void show() {

        ANCHO_PANTALLA = Gdx.graphics.getWidth();
        ALTO_PANTALLA = Gdx.graphics.getHeight();

        texturaJugador = new Texture(Gdx.files.internal("PERSONAJES/Gemini_Generated_Image_g2i43ag2i43ag2i4.png"));
        texturaPiso = new Texture(Gdx.files.internal("PISOS/PISO.png"));
        texturaTriangulo = new Texture(Gdx.files.internal("OBSTACULOS/TRIANGULO2-Photoroom.png"));
        texturaPlataformaX3 = new Texture(Gdx.files.internal("OBSTACULOS/PLATAFORMAx3.png"));
        texturaPlataformaX5 = new Texture(Gdx.files.internal("OBSTACULOS/PLATAFORMAx5.png"));
        texturaCuadrado = new Texture(Gdx.files.internal("OBSTACULOS/CUADRADO-Photoroom.png"));
        texturaCaja = new Texture(Gdx.files.internal("OBSTACULOS/CAJA-Photoroom.png"));

        cancionDeFondo = Gdx.audio.newMusic(Gdx.files.internal("AUDIO/CANCIONES/The_Last_Lap.mp3"));
        efectoSonidoChoque = Gdx.audio.newSound(Gdx.files.internal("AUDIO/EFECTOS_DE_SONIDO/u_fe12rqkbth-sword-clash-241729.mp3"));


        cancionDeFondo.setVolume(0.1f);
        cancionDeFondo.play();



        inputProcessor = new GameInputProcessor(this);


        camara = new OrthographicCamera();
        fitViewport = new ExtendViewport(9, 9, 16, 21, camara);

        world = new World(new Vector2(0, GRAVEDAD_MUNDO), true); //Se le pone true para indicarle al sistema que cuando no halla nada por simular, entonces no haga nada
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        stage = new Stage();
        //stage = new Stage(new FitViewport(ANCHO_MUNDO, ALTO_MUNDO));

        // 3. Configuración del InputMultiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        // IMPORTANTE: El Stage va primero. Si tocas un botón, el Stage consume el evento.
        multiplexer.addProcessor(stage);
        // El procesador del juego va después. Si el Stage no usó el toque, pasa al juego.
        multiplexer.addProcessor(inputProcessor);
        Gdx.input.setInputProcessor(multiplexer);


        crearBotonReiniciarConEstilo();
        stage.addActor(botonReiniciar);
        agregarEventosBotonReiniciar();


        crearPosicionesDePisos();
        crearArregloBodyesPisosYAgregarPisosAMundo();
        crearArraySpritePisos();
        centrarSpritesConBodysPisos();

        crearPosicionesTriangulos();
        crearArregloBodyesTriangulosYAgregarTriangulosAMundo();
        crearArraySpriteTriangulo();
        centrarSpritesConBodysTriangulo();

        crearPosicionesPlataformasX3();
        crearArregloBodyesPlataformasX3YAgregarPlataformasX3AMundo();
        crearArraySpritePlataformasX3();
        centrarSpritesConBodysPlataformasX3();

        crearPosicionesPlataformasX5();
        crearArregloBodyesPlataformasX5YAgregarPlataformasX5AMundo();
        crearArraySpritePlataformasX5();
        centrarSpritesConBodysPlataformasX5();

        crearPosicionesCuadrados();
        crearArregloBodyesCuadradosYAgregarCuadradosAMundo();
        crearArraySpriteCuadrados();
        centrarSpritesConBodysCuadrados();

        crearPosicionesCajas();
        crearArregloBodyesCajasYAgregarCajasAMundo();
        crearArraySpritesCajas();
        centrarSpritesConBodysCajas();

        crearJugador(1);
        correrJugador();

        agregarListenerDeContactosJugador();
    }


    @Override
    public void render(float delta) {

        // 2. LIMPIEZA DE BUFFER
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (estaVivo && bodyJugador.getLinearVelocity().x != 9.05f){
            bodyJugador.setLinearVelocity(9.05f, bodyJugador.getLinearVelocity().y);
        }

        // 3. APLICAR VIEWPORT
        //fitViewport.apply();


        // 4. FIXED TIMESTEP LOOP (Físicas continuas)
        accumulator += Math.min(delta, 0.25f);
        while (accumulator >= TIME_STEP) {
            previousPosition.set(bodyJugador.getPosition());
            procesarSaltoPendiente();
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            accumulator -= TIME_STEP;
        }

        // 5. INTERPOLACIÓN FÍSICA
        alpha = accumulator / TIME_STEP;
        renderPosition.set(
            previousPosition.x + (bodyJugador.getPosition().x - previousPosition.x) * alpha,
            previousPosition.y + (bodyJugador.getPosition().y - previousPosition.y) * alpha
        );

        // 6. ACTUALIZACIÓN DE CÁMARA
        camara.position.set(renderPosition.x + 4f, ALTO_MUNDO / 2f, 0);
        camara.update();

        // 7. RENDERIZADO DEL FONDO (Sin interferir con SpriteBatch)
        // Deshabilitamos la mezcla de colores innecesaria para el fondo opaco
        Gdx.gl.glDisable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camara.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0.0f, 0f, 1f); // Azul oscuro
        shapeRenderer.rect(camara.position.x - ANCHO_MUNDO / 2f, 0, ANCHO_MUNDO, ALTO_MUNDO);
        shapeRenderer.end();
        //Gdx.gl.glEnable(GL20.GL_BLEND); // Reactivamos blend para Sprites transparentes

        // 8. SINCRONIZACIÓN DE SPRITES (Visual / Physics Sync)
        updateSpriteFromBody(spriteJugador, bodyJugador, renderPosition);


        // 9. DIBUJADO DE TEXTURAS (Batch optimizado)
        batch.setProjectionMatrix(camara.combined);
        batch.begin();
        dibujarSpritesPisos();
        dibujarSpritesTriangulos();
        dibujarSpritesPlataformasX3();
        dibujarSpritesPlataformasX5();
        dibujarSpritesCuadrados();
        dibujarSpritesCajas();
        spriteJugador.draw(batch); // Dibujar jugador al final para estar sobre el piso
        batch.end();

        stage.act(delta);
        stage.draw();


    }




    @Override
    public void resize(int width, int height) {
        fitViewport.update(width, height);

    }



    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

        texturaJugador.dispose();
        texturaPiso.dispose();
        texturaTriangulo.dispose();
        texturaCaja.dispose();
        texturaCuadrado.dispose();
        texturaPlataformaX3.dispose();
        texturaPlataformaX5.dispose();
        textureFondoBotonReiniciarDonw.dispose();
        textureFondoBotonReiniciarUp.dispose();

        efectoSonidoChoque.dispose();
        cancionDeFondo.dispose();

        world.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        stage.dispose();


    }

}
