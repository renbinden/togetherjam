package uk.co.rossbinden.togetherjam.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite
import uk.co.rossbinden.togetherjam.component.*
import uk.co.rossbinden.togetherjam.system.*

class MainScreen : ScreenAdapter() {

    companion object {
        val PIXELS_PER_METER = 32f
    }

    val textureAtlas = TextureAtlas(Gdx.files.internal("textures-packed/pack.atlas"))
    val engine = Engine()
    val camera = OrthographicCamera()
    val world = World(Vector2(0f, 0f), false)
    val tiledMap = TmxMapLoader().load("maps/cave.tmx")
    val caveAmbience = Gdx.audio.newMusic(Gdx.files.internal("sound/Cave Ambience.wav"))
    val inputProcessor = object: InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            engine.getEntitiesFor(CONTROLLABLES).forEach { entity ->
                CONTROLLABLE[entity].keyPressMapping[keycode]?.invoke(entity)
            }
            return true
        }

        override fun keyUp(keycode: Int): Boolean {
            engine.getEntitiesFor(CONTROLLABLES).forEach { entity ->
                CONTROLLABLE[entity].keyReleaseMapping[keycode]?.invoke(entity)
            }
            return true
        }
    }

    init {
        camera.setToOrtho(false)
        caveAmbience.isLooping = true
        caveAmbience.play()
        tiledMap.layers.get("Object Layer 1").objects.forEach { obj ->
            when (obj.properties["type"]) {
                "exit" -> {}
                "wall" -> {
                    val wallBodyDef = BodyDef()
                    wallBodyDef.type = BodyDef.BodyType.StaticBody
                    wallBodyDef.position.set((obj.properties["x"] as Float / PIXELS_PER_METER) + ((obj.properties["width"] as Float / PIXELS_PER_METER) / 2f), (obj.properties["y"] as Float / PIXELS_PER_METER) + ((obj.properties["height"] as Float / PIXELS_PER_METER) / 2f))
                    val wallShape = PolygonShape()
                    wallShape.setAsBox((obj.properties["width"] as Float / PIXELS_PER_METER) / 2f, (obj.properties["height"] as Float / PIXELS_PER_METER) / 2f)
                    val wallBody = world.createBody(wallBodyDef)
                    val wallFixtureDef = FixtureDef()
                    wallFixtureDef.shape = wallShape
                    wallFixtureDef.density = 1f
                    wallFixtureDef.friction = 0f
                    wallFixtureDef.restitution = 0f
                    wallBody.createFixture(wallFixtureDef)
                    wallBody.isFixedRotation = true
                    wallShape.dispose()
                }
                "player" -> {
                    val player = Entity()
                    val playerWalkingSprite = AnimatedSprite(
                            Animation(
                                    0.5f,
                                    com.badlogic.gdx.utils.Array<TextureRegion>(
                                            arrayOf(
                                                    textureAtlas.findRegion("person1_frame1"),
                                                    textureAtlas.findRegion("person1_frame2"),
                                                    textureAtlas.findRegion("person1_frame3"),
                                                    textureAtlas.findRegion("person1_frame4")
                                            )
                                    ),
                                    Animation.PlayMode.LOOP
                            )
                    )
                    val playerIdleSprite = Sprite(
                            textureAtlas.findRegion("person1_frame1")
                    )
                    player.add(
                            SpriteComponent(
                                    playerIdleSprite
                            )
                    )
                    player.add(
                            PositionComponent(
                                    obj.properties["x"] as Float,
                                    obj.properties["y"] as Float
                            )
                    )
                    player.add(
                            VelocityComponent(
                                    0f,
                                    0f
                            )
                    )
                    player.add(
                            ControllableComponent(
                                    keyPressMapping = mapOf(
                                            Pair(Input.Keys.A, { entity ->
                                                if (VELOCITY.has(entity)) {
                                                    val velocity = VELOCITY[entity]
                                                    if (velocity.dx > 0.0001f) {
                                                        velocity.dx = 0f
                                                        if (Math.abs(velocity.dy) < 0.0001f) {
                                                            if (SPRITE.has(entity)) {
                                                                val sprite = SPRITE[entity]
                                                                sprite.sprite = playerIdleSprite
                                                            }
                                                        }
                                                    } else {
                                                        velocity.dx = -100f
                                                        if (SPRITE.has(entity)) {
                                                            val sprite = SPRITE[entity]
                                                            sprite.sprite = playerWalkingSprite
                                                        }
                                                    }
                                                }
                                            }),
                                            Pair(Input.Keys.D, { entity ->
                                                if (VELOCITY.has(entity)) {
                                                    val velocity = VELOCITY[entity]
                                                    if (velocity.dx < -0.0001f) {
                                                        velocity.dx = 0f
                                                        if (Math.abs(velocity.dy) < 0.0001f) {
                                                            if (SPRITE.has(entity)) {
                                                                val sprite = SPRITE[entity]
                                                                sprite.sprite = playerIdleSprite
                                                            }
                                                        }
                                                    } else{
                                                        velocity.dx = 100f
                                                        if (SPRITE.has(entity)) {
                                                            val sprite = SPRITE[entity]
                                                            sprite.sprite = playerWalkingSprite
                                                        }
                                                    }
                                                }
                                            }),
                                            Pair(Input.Keys.W, { entity ->
                                                if (VELOCITY.has(entity)) {
                                                    val velocity = VELOCITY[entity]
                                                    if (velocity.dy < -0.0001f) {
                                                        velocity.dy = 0f
                                                        if (Math.abs(velocity.dx) < 0.0001f) {
                                                            if (SPRITE.has(entity)) {
                                                                val sprite = SPRITE[entity]
                                                                sprite.sprite = playerIdleSprite
                                                            }
                                                        }
                                                    } else {
                                                        velocity.dy = 100f
                                                        if (SPRITE.has(entity)) {
                                                            val sprite = SPRITE[entity]
                                                            sprite.sprite = playerWalkingSprite
                                                        }
                                                    }
                                                }
                                            }),
                                            Pair(Input.Keys.S, { entity ->
                                                if (VELOCITY.has(entity)) {
                                                    val velocity = VELOCITY[entity]
                                                    if (velocity.dy > 0.0001f) {
                                                        velocity.dy = 0f
                                                        if (Math.abs(velocity.dx) < 0.0001f) {
                                                            if (SPRITE.has(entity)) {
                                                                val sprite = SPRITE[entity]
                                                                sprite.sprite = playerIdleSprite
                                                            }
                                                        }
                                                    } else {
                                                        velocity.dy = -100f
                                                        if (SPRITE.has(entity)) {
                                                            val sprite = SPRITE[entity]
                                                            sprite.sprite = playerWalkingSprite
                                                        }
                                                    }
                                                }
                                            })
                                    ),
                                    keyReleaseMapping = mapOf(
                                            Pair(Input.Keys.A, { entity ->
                                                if (VELOCITY.has(entity)) {
                                                    val velocity = VELOCITY[entity]
                                                    if (velocity.dx < -0.0001f) {
                                                        velocity.dx = 0f
                                                        if (Math.abs(velocity.dy) < 0.0001f) {
                                                            if (SPRITE.has(entity)){
                                                                val sprite = SPRITE[entity]
                                                                sprite.sprite = playerIdleSprite
                                                            }
                                                        }
                                                    } else {
                                                        velocity.dx = 100f
                                                        if (SPRITE.has(entity)) {
                                                            val sprite = SPRITE[entity]
                                                            sprite.sprite = playerWalkingSprite
                                                        }
                                                    }
                                                }
                                            }),
                                            Pair(Input.Keys.D, { entity ->
                                                if (VELOCITY.has(entity)) {
                                                    val velocity = VELOCITY[entity]
                                                    if (velocity.dx > 0.0001f) {
                                                        velocity.dx = 0f
                                                        if (Math.abs(velocity.dy) < 0.0001f) {
                                                            if (SPRITE.has(entity)) {
                                                                val sprite = SPRITE[entity]
                                                                sprite.sprite = playerIdleSprite
                                                            }
                                                        }
                                                    } else{
                                                        velocity.dx = -100f
                                                        if (SPRITE.has(entity)) {
                                                            val sprite = SPRITE[entity]
                                                            sprite.sprite = playerWalkingSprite
                                                        }
                                                    }
                                                }
                                            }),
                                            Pair(Input.Keys.W, { entity ->
                                                if (VELOCITY.has(entity)) {
                                                    val velocity = VELOCITY[entity]
                                                    if (velocity.dy > 0.0001f) {
                                                        velocity.dy = 0f
                                                        if (Math.abs(velocity.dx) < 0.0001f) {
                                                            if (SPRITE.has(entity)) {
                                                                val sprite = SPRITE[entity]
                                                                sprite.sprite = playerIdleSprite
                                                            }
                                                        }
                                                    } else {
                                                        velocity.dy = -100f
                                                        if (SPRITE.has(entity)) {
                                                            val sprite = SPRITE[entity]
                                                            sprite.sprite = playerWalkingSprite
                                                        }
                                                    }
                                                }
                                            }),
                                            Pair(Input.Keys.S, { entity ->
                                                if (VELOCITY.has(entity)) {
                                                    val velocity = VELOCITY[entity]
                                                    if (velocity.dy < -0.0001f) {
                                                        velocity.dy = 0f
                                                        if (Math.abs(velocity.dx) < 0.0001f) {
                                                            if (SPRITE.has(entity)) {
                                                                val sprite = SPRITE[entity]
                                                                sprite.sprite = playerIdleSprite
                                                            }
                                                        }
                                                    } else {
                                                        velocity.dy = 100f
                                                        if (SPRITE.has(entity)) {
                                                            val sprite = SPRITE[entity]
                                                            sprite.sprite = playerWalkingSprite
                                                        }
                                                    }
                                                }
                                            })
                                    )
                            )
                    )
                    player.add(
                            FootstepSoundsComponent(
                                    com.badlogic.gdx.utils.Array<Sound>(
                                            arrayOf(
                                                    Gdx.audio.newSound(Gdx.files.internal("sound/Triangle Step.wav"))
                                            )
                                    )
                            )
                    )
                    val playerBodyDef = BodyDef()
                    playerBodyDef.type = BodyDef.BodyType.DynamicBody
                    playerBodyDef.position.set(POSITION[player].x / PIXELS_PER_METER, POSITION[player].y / PIXELS_PER_METER)
                    val playerShape = PolygonShape()
                    playerShape.setAsBox((SPRITE[player].sprite.width / PIXELS_PER_METER) / 2f, (SPRITE[player].sprite.height / PIXELS_PER_METER) / 2f)
                    val playerBody = world.createBody(playerBodyDef)
                    val playerFixtureDef = FixtureDef()
                    playerFixtureDef.shape = playerShape
                    playerFixtureDef.density = 1f
                    playerFixtureDef.friction = 0f
                    playerFixtureDef.restitution = 0f
                    playerBody.createFixture(playerFixtureDef)
                    playerBody.isFixedRotation = true
                    playerShape.dispose()
                    player.add(
                            BodyComponent(
                                    playerBody
                            )
                    )
                    engine.addEntity(player)
                    engine.addSystem(CameraSystem(camera, player))
                }
            }
        }
        engine.addSystem(MovementSystem())
        engine.addSystem(GraphicsSystem(camera, tiledMap))
        engine.addSystem(FootstepSoundsSystem())
        engine.addSystem(CollisionSystem(world))
        engine.addSystem(Box2DDebugRenderSystem(world, camera))
    }

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun render(delta: Float) {
        engine.update(delta)
    }

    override fun dispose() {
        engine.getSystem(GraphicsSystem::class.java).dispose()
        textureAtlas.dispose()
    }

}