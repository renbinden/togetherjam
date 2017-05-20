package uk.co.rossbinden.togetherjam.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import uk.co.rossbinden.togetherjam.component.SPRITE
import uk.co.rossbinden.togetherjam.component.SpriteComponent

class GraphicsSystem(val camera: OrthographicCamera, val tiledMap: TiledMap): EntitySystem() {

    val spriteBatch = SpriteBatch()
    val renderables = Family.all(SpriteComponent::class.java).get()
    val tiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, spriteBatch)

    override fun update(deltaTime: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        spriteBatch.projectionMatrix = camera.combined
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()
        spriteBatch.begin()
        engine.getEntitiesFor(renderables).forEach { entity ->
            SPRITE[entity].sprite.draw(spriteBatch)
        }
        spriteBatch.end()
    }

    fun dispose() {
        spriteBatch.dispose()
        tiledMapRenderer.dispose()
    }

}