package uk.co.rossbinden.togetherjam.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Array
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
    val world: World
    lateinit var tiledMap: TiledMap
    val caveAmbience = Gdx.audio.newMusic(Gdx.files.internal("sound/Cave Ambience.wav"))
    val waterfall = Gdx.audio.newMusic(Gdx.files.internal("sound/Relaxing Waterfall.wav"))
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
    val contactListener = object: ContactListener {
        override fun endContact(contact: Contact) {
        }

        override fun beginContact(contact: Contact) {
            val entity1 = contact.fixtureA.body.userData as? Entity ?: return
            val entity2 = contact.fixtureB.body.userData as? Entity ?: return
            if (TARGET_AREA.has(entity1)) {
                loadingArea = true
                areaName = TARGET_AREA[entity1].mapName
            } else if (TARGET_AREA.has(entity2)) {
                loadingArea = true
                areaName = TARGET_AREA[entity2].mapName
            }
        }

        override fun preSolve(contact: Contact, oldManifold: Manifold) {
        }

        override fun postSolve(contact: Contact, impulse: ContactImpulse) {
        }

    }
    var loadingArea = false
    var areaName = "cave"

    init {
        Box2D.init()
        world = World(Vector2(0f, 0f), false)
        world.setContactListener(contactListener)
        camera.setToOrtho(false)
        loadArea("cave")
        engine.addSystem(MovementSystem())
        engine.addSystem(GraphicsSystem(camera, tiledMap))
        engine.addSystem(FootstepSoundsSystem())
        engine.addSystem(CollisionSystem(world))
        engine.addSystem(ParticleSystem())
    }

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun render(delta: Float) {
        if (loadingArea) {
            loadArea(areaName)
            loadingArea = false
        }
        engine.update(delta)
    }

    override fun dispose() {
        engine.getSystem(GraphicsSystem::class.java).dispose()
        textureAtlas.dispose()
    }

    fun loadArea(area: String) {
        val bodies = com.badlogic.gdx.utils.Array<Body>()
        world.getBodies(bodies)
        for (body in bodies) {
            world.destroyBody(body)
        }
        engine.removeAllEntities()
        engine.removeSystem(engine.getSystem(CameraSystem::class.java))
        when (area) {
            "cave" -> {
                waterfall.stop()
                caveAmbience.isLooping = true
                caveAmbience.play()
                tiledMap = TmxMapLoader().load("maps/cave.tmx")
            }
            "waterfall" -> {
                caveAmbience.stop()
                waterfall.isLooping = true
                waterfall.volume = 0.3f
                waterfall.play()
                tiledMap = TmxMapLoader().load("maps/waterfall.tmx")
                val graphicsSystem = engine.getSystem(GraphicsSystem::class.java)
                graphicsSystem.tiledMap = tiledMap
                graphicsSystem.tiledMapRenderer.map = tiledMap
            }
            "riverside" -> {
                waterfall.stop()
                tiledMap = TmxMapLoader().load("maps/riverside.tmx")
                val graphicsSystem = engine.getSystem(GraphicsSystem::class.java)
                graphicsSystem.tiledMap = tiledMap
                graphicsSystem.tiledMapRenderer.map = tiledMap
            }
        }
        tiledMap.layers.get("Object Layer 1").objects.forEach { obj ->
            when (obj.properties["type"]) {
                "exit" -> {
                    val exit = Entity()
                    val exitBodyDef = BodyDef()
                    exitBodyDef.type = BodyDef.BodyType.StaticBody
                    exitBodyDef.position.set((obj.properties["x"] as Float / PIXELS_PER_METER) + ((obj.properties["width"] as Float / PIXELS_PER_METER) / 2f), (obj.properties["y"] as Float / PIXELS_PER_METER) + ((obj.properties["height"] as Float / PIXELS_PER_METER) / 2f))
                    val exitShape = PolygonShape()
                    exitShape.setAsBox((obj.properties["width"] as Float / PIXELS_PER_METER) / 2f, (obj.properties["height"] as Float / PIXELS_PER_METER) / 2f)
                    val exitBody = world.createBody(exitBodyDef)
                    val exitFixtureDef = FixtureDef()
                    exitFixtureDef.shape = exitShape
                    exitFixtureDef.density = 1f
                    exitFixtureDef.friction = 0f
                    exitFixtureDef.restitution = 0f
                    exitFixtureDef.isSensor = true
                    exitBody.createFixture(exitFixtureDef)
                    exitShape.dispose()
                    exit.add(BodyComponent(exitBody))
                    exitBody.userData = exit
                    exit.add(TargetAreaComponent(obj.properties["target"] as String))
                    engine.addEntity(exit)
                }
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
                                    Array<TextureRegion>(
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
                                    Array<Sound>(
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
                    playerBody.userData = player
                    player.add(
                            BodyComponent(
                                    playerBody
                            )
                    )
                    engine.addEntity(player)
                    engine.addSystem(CameraSystem(camera, player))
                }
                "waterfall" -> {
                    val waterfall = Entity()
                    val particleEffect = ParticleEffect()
                    particleEffect.load(Gdx.files.internal("particles/waterfall.p"), Gdx.files.internal("particles"))
                    particleEffect.setPosition(obj.properties["x"] as Float, obj.properties["y"] as Float)
                    particleEffect.start()
                    waterfall.add(ParticleEffectComponent(particleEffect))
                    engine.addEntity(waterfall)
                }
//                "character1" -> {
//                    val character1 = Entity()
//                    character1.add(
//                            SpriteComponent(
//                                    Sprite(
//                                            textureAtlas.findRegion("person2_frame1")
//                                    )
//                            )
//                    )
//                    val character1BodyDef = BodyDef()
//
//                }
            }
        }
    }

}