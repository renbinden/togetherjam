package uk.co.rossbinden.togetherjam.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import uk.co.rossbinden.togetherjam.component.*
import java.util.*

class FootstepSoundsSystem: IteratingSystem(
        Family.all(
                PositionComponent::class.java,
                VelocityComponent::class.java,
                FootstepSoundsComponent::class.java
        ).get()
) {
    val random = Random()
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val velocity = VELOCITY[entity]
        val footstepSounds = FOOTSTEP_SOUNDS[entity]
        if (Math.abs(velocity.dx) > 0.0001f || Math.abs(velocity.dy) > 0.0001f) {
            footstepSounds.dt += deltaTime
            if (footstepSounds.dt > 0.5f) {
                footstepSounds.dt = 0f
                val sound = footstepSounds.sounds[random.nextInt(footstepSounds.sounds.size)]
                sound.setVolume(sound.play(), 0.1f)
            }
        }
    }
}