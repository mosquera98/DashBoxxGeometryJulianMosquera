package com.DashBoxGeometryJulianMosquera.Pantallas;

import com.DashBoxGeometryJulianMosquera.GameDashBoxGeometry;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

public class PantallaMenuPrincipal extends ScreenAdapter {

    GameDashBoxGeometry miGame;
    float ANCHO_PANTALLA, ALTO_PANTALLA;

    Texture textureBotonPlayUp, textureBotonPlayDown;

    Stage stage;
    Button botonPlay;

    public PantallaMenuPrincipal(GameDashBoxGeometry unGame){
        this.miGame = unGame;
    }

    public void crearBotonReiniciarConEstilo(){

        textureBotonPlayUp = new Texture(Gdx.files.internal("BOTONES/botonPlayUp.png"), true);
        textureBotonPlayUp.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        textureBotonPlayDown = new Texture(Gdx.files.internal("BOTONES/botonPlayDown.png"), true);
        textureBotonPlayDown.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        NinePatch patchUp = new NinePatch(textureBotonPlayUp);
        NinePatch patchDown = new NinePatch(textureBotonPlayDown);

        NinePatchDrawable drawableUp = new NinePatchDrawable(patchUp);
        NinePatchDrawable drawableDown = new NinePatchDrawable(patchDown);

        Button.ButtonStyle estiloBotonMusica = new Button.ButtonStyle();

        estiloBotonMusica.up = drawableUp;
        estiloBotonMusica.down = drawableDown;

        botonPlay = new Button(estiloBotonMusica);
        botonPlay.setHeight(ALTO_PANTALLA / 15);
        botonPlay.setWidth(botonPlay.getHeight());

        botonPlay.setPosition(((ANCHO_PANTALLA - botonPlay.getWidth())) / 2,  (( ALTO_PANTALLA) -  botonPlay.getHeight()) / 2);
        botonPlay.setTransform(true);
        botonPlay.setOrigin(Align.center);

        botonPlay.setRotation(-45);
    }

    public void crearAgregarListenerABotonPlay(){

        botonPlay.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                hide();
                miGame.iniciarPantallaJuego();
            }

        });
    }


    @Override
    public void show() {

        ANCHO_PANTALLA = Gdx.graphics.getWidth();
        ALTO_PANTALLA = Gdx.graphics.getHeight();

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        crearBotonReiniciarConEstilo();
        crearAgregarListenerABotonPlay();
        stage.addActor(botonPlay);

    }



    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();


    }

    @Override
    public void resize(int width, int height) {


    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void hide() {
        // Desconecta el InputProcessor para evitar fugas de eventos a otras pantallas
        Gdx.input.setInputProcessor(null);

    }

    @Override
    public void dispose() {

        textureBotonPlayUp.dispose();
        textureBotonPlayDown.dispose();


        stage.dispose();


    }
}
