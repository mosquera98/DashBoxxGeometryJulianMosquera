package com.DashBoxGeometryJulianMosquera.objetos;

import com.badlogic.gdx.physics.box2d.World;

public class PlataformaX5 extends Objeto {
    public PlataformaX5(float anchoForma, float altoForma, float posicionX, float posicionY, World world) {
        super(anchoForma, altoForma, posicionX, posicionY, world);
    }

    @Override
    public void setUserDataNombreFixture() {
        this.fixture.setUserData("PLATAFORMA");
    }
}

