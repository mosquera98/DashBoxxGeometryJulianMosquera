package com.DashBoxGeometryJulianMosquera;

import com.DashBoxGeometryJulianMosquera.Pantallas.Nivel_1;
import com.badlogic.gdx.InputAdapter;

public class GameInputProcessor extends InputAdapter {

    private boolean screenTouched;
    private boolean touchConsumed;

    private final Nivel_1 unNivel_1;

    public GameInputProcessor(Nivel_1 unNivel) {
        this.unNivel_1 = unNivel;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        // El nivel decide cuándo aplicar el salto; el toque se conserva si el
        // jugador sigue cayendo durante el inicio de la partida.
        unNivel_1.solicitarSalto();
        return true; // Indicamos que el evento fue procesado

    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        //unNivel_1.solicitarSalto();
        return true;
    }

    


}
