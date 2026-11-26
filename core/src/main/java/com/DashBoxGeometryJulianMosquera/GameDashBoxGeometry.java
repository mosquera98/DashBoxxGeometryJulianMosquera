package com.DashBoxGeometryJulianMosquera;

import com.DashBoxGeometryJulianMosquera.Pantallas.Nivel_1;
import com.DashBoxGeometryJulianMosquera.Pantallas.PantallaMenuPrincipal;
import com.badlogic.gdx.Game;

public class GameDashBoxGeometry extends Game {

    @Override
    public void create() {

        setScreen(new PantallaMenuPrincipal(this));

    }

    public void iniciarPantallaJuego(){
        setScreen(new Nivel_1(this));
    }

    public void disposearPantallaActual(){

        getScreen().dispose();
    }



}
