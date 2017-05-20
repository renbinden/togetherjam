package uk.co.rossbinden.togetherjam.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World

class Box2DDebugRenderSystem(val world: World, val camera: OrthographicCamera): EntitySystem() {
    val debugRenderer = Box2DDebugRenderer()
    override fun update(deltaTime: Float) {
        debugRenderer.render(world, camera.combined)
    }
}