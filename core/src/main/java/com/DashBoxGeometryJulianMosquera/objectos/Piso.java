package com.DashBoxGeometryJulianMosquera.objectos;

import com.badlogic.gdx.physics.box2d.World;

public class Piso extends Objeto {

    public Piso(float anchoForma, float altoForma, float posicionX, float posicionY, World world) {
        super(anchoForma, altoForma, posicionX, posicionY, world);
        // ¡No necesitas hacer nada más aquí! El padre se encarga de llamar a los métodos.
    }

    @Override
    public void setUserDataNombreFixture() {
        this.fixture.setUserData("PISO");
    }


}
