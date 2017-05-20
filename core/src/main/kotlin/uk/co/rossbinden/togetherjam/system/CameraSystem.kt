package uk.co.rossbinden.togetherjam.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import uk.co.rossbinden.togetherjam.component.POSITION

class CameraSystem(val camera: OrthographicCamera, var following: Entity): EntitySystem() {

    override fun update(deltaTime: Float) {
        if (POSITION.has(following)) {
            val position = POSITION[following]
            camera.position.set(Vector3(position.x, position.y, 0f))
        }
    }

}