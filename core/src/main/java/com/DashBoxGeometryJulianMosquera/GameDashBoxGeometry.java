package com.DashBoxGeometryJulianMosquera;

import com.DashBoxGeometryJulianMosquera.Pantallas.PantallaMenuPrincipal;
import com.badlogic.gdx.Game;

public class GameDashBoxGeometry extends Game {

    @Override
    public void create() {

        setScreen(new PantallaMenuPrincipal(this));

    }

    public void iniciarPantallaJuego(){

    }



}
