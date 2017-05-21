package uk.co.rossbinden.togetherjam.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import uk.co.rossbinden.togetherjam.component.ParticleEffectComponent

class ParticleSystem: IteratingSystem(Family.all(ParticleEffectComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        //PARTICLE_EFFECT[entity].particleEffect.update(deltaTime)
    }
}