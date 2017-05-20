package uk.co.rossbinden.togetherjam.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.World

class CollisionSystem(val world: World): EntitySystem() {

    override fun update(deltaTime: Float) {
        world.step(deltaTime, 8, 2)
    }

}