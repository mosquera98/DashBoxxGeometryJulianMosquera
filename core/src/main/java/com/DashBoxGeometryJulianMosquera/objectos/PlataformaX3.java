package com.DashBoxGeometryJulianMosquera.objectos;

import com.badlogic.gdx.physics.box2d.World;

public class PlataformaX3 extends Objeto {

    public PlataformaX3(float anchoForma, float altoForma, float posicionX, float posicionY, World world) {
        super(anchoForma, altoForma, posicionX, posicionY, world);
        // ¡No necesitas hacer nada más aquí! El padre se encarga de llamar a los métodos.
    }

    @Override
    public void setUserDataNombreFixture() {
        this.fixture.setUserData("PLATAFORMA");
    }




}
