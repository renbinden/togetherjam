package uk.co.rossbinden.togetherjam.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import uk.co.rossbinden.togetherjam.component.*
import uk.co.rossbinden.togetherjam.screen.MainScreen.Companion.PIXELS_PER_METER

class MovementSystem: IteratingSystem(
        Family.all(PositionComponent::class.java, VelocityComponent::class.java, BodyComponent::class.java).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = POSITION[entity]
        val velocity = VELOCITY[entity]
        val body = BODY[entity]
        position.x = body.body.position.x * PIXELS_PER_METER
        position.y = body.body.position.y * PIXELS_PER_METER
        position.x += velocity.dx * deltaTime
        position.y += velocity.dy * deltaTime
        body.body.linearVelocity = Vector2(velocity.dx / PIXELS_PER_METER, velocity.dy / PIXELS_PER_METER)
        if (SPRITE.has(entity)) {
            val sprite = SPRITE[entity]
            sprite.sprite.x = position.x - (sprite.sprite.width / 2f)
            sprite.sprite.y = position.y - (sprite.sprite.height / 2f)
        }
    }
}