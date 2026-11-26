package com.DashBoxGeometryJulianMosquera.objectos;

import com.badlogic.gdx.physics.box2d.World;

public class Cuadrado extends Objeto {

    public Cuadrado (float anchoForma, float altoForma, float posicionX, float posicionY, World world) {
        super(anchoForma, altoForma, posicionX, posicionY, world);

    }

    @Override
    public void setUserDataNombreFixture() {
        this.fixture.setUserData("CUADRADO");
    }
}
