# API research history (per-session sections)

Moved out of `docs/API_NOTES.md` in session 43. The QUICK INDEX there is the working table; this file holds the long per-session research notes the table's "Where verified" column points to ("Assumed in Phase 4b", "Session 4, part 7", ...). Grep it by those names.

## Where each fact comes from (re-verify by cloning; sandbox can reach github.com)
* Working MDK template: https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle  (26.1 and 26.3 exist too)
* NeoForge source + tests: https://github.com/neoforged/NeoForge  branch `26.2.x` (sparse-clone `tests`, `patches`, `src`)
  * Entity + renderer + model example: `tests/.../oldtest/client/rendering/EntityRendererEventsTest.java`
  * DeferredRegister entity type example: `tests/.../oldtest/entity/PartEntityTest.java`
  * Spawn egg example: `tests/.../debug/item/ItemTests.java` (`new SpawnEggItem(props.spawnEgg(entityType))`)
* Porting primers: https://raw.githubusercontent.com/neoforged/.github/main/primers/26.1/index.md (also 26.2, 26.3)

## Verified from the NeoForge 26.2.x test sources
* Java 25, Gradle 9.2.1, plugin `net.neoforged.moddev` 2.0.147, no Parchment/obfuscation (Mojang names, official params).
* Resource ids are `net.minecraft.resources.Identifier` (NOT ResourceLocation): `Identifier.fromNamespaceAndPath(ns, path)`.
* Entity type: `EntityType.Builder.of(Factory::new, MobCategory.X).sized(w,h).clientTrackingRange(n).build(ResourceKey.create(Registries.ENTITY_TYPE, id))`
  — `build` now REQUIRES a ResourceKey.
* Register via `DeferredRegister.create(Registries.ENTITY_TYPE, MODID)`; attributes via `EntityAttributeCreationEvent` (mod bus): `event.put(type, builder.build())`.
* Client events (mod bus, `EntityRenderersEvent`): `RegisterLayerDefinitions#registerLayerDefinition(ModelLayerLocation, Supplier<LayerDefinition>)`,
  `RegisterRenderers#registerEntityRenderer(type, Context -> renderer)`, `AddLayers`.
* Model: `class M extends EntityModel<LivingEntityRenderState>`, ctor `super(ModelPart)`. `LayerDefinition.create(MeshDefinition, texW, texH)`.
* Renderer: `LivingEntityRenderer<T, S, M>` / `MobRenderer<T, S, M>` (3 generics). Must override `createRenderState()` and `getTextureLocation(S)`;
  ctor `super(context, model, shadowRadius)`; `context.bakeLayer(ModelLayerLocation)`.
* Render pipeline is state-based: entity data is copied into a `*RenderState`; models read the state in `setupAnim(S state)`
  (vanilla calls `model.setupAnim(state)`). `EntityRenderState.ageInTicks` exists.
* `net.minecraft.world.entity.ai.goal.FloatGoal` and `PanicGoal` exist. `Monster.createMonsterAttributes()`, `Pig.createAttributes()` exist.
* `@EventBusSubscriber(modid = ..., value = Dist.CLIENT)` with static `@SubscribeEvent` methods works for mod-bus events (bus auto-detected).
* Item/block DeferredRegisters: `DeferredRegister.createItems`, `registerSimpleItem`, `registerSimpleBlock` (from the MDK example).

## CONFIRMED BY COMPILING + RUNNING (these were written from memory of 1.21.x and turned out correct on 26.2)
* `LivingEntityRenderState` fields `walkAnimationPos`, `walkAnimationSpeed`, `xRot`, `yRot` (used in BunnidogModel).
* `MobRenderer` constructor signature `(Context, M, float)`.
* `Mob.createMobAttributes()` still exists under that name (Pig uses a newer `createAttributes()` pattern).
* AI goal classes `LookAtPlayerGoal`, `RandomLookAroundGoal`, `WaterAvoidingRandomStrollGoal` and the `PanicGoal(PathfinderMob, double)` ctor.
* `PathfinderMob#registerGoals` visibility/name; `ModelPart#getChild`.
* Item definitions for new items/spawn eggs in 26.x need `assets/<ns>/items/<id>.json` — format not yet checked.

## Verified from NeoForge 26.2.x sources in session 2 (used by Phase 1; CONFIRMED by the user's Phase 1 compile + run)
* Synced data: `SynchedEntityData.defineId(Class, serializer)` -> `EntityDataAccessor<T>`; override
  `protected void defineSynchedData(SynchedEntityData.Builder b)` and call `b.define(accessor, default)`
  (`EntityDataSerializers.INT` is a vanilla serializer; only INT is used, split a 48-bit genome into two 24-bit ints).
* Saving: `readAdditionalSaveData(ValueInput)` / `addAdditionalSaveData(ValueOutput)` (package
  `net.minecraft.world.level.storage`). Methods seen in use: `putString/putInt/putLong/putBoolean` and
  `getStringOr/getIntOr/getLongOr/getBooleanOr(key, default)`. (We declare our overrides `public`, which compiles whether
  the vanilla method is public or protected.)
* `level.isClientSide()` is a METHOD now.
* Renderer: `EntityRenderer#extractRenderState(T entity, S state, float partialTicks)` fills the state (called from
  `createRenderState(entity, partialTicks)`); NeoForge also adds `state.partialTick`. Custom state = subclass of
  `LivingEntityRenderState` returned from `createRenderState()`. Rendering goes through `submit(...)` with a
  `SubmitNodeCollector` now (relevant if we ever add a `RenderLayer`: `submit(poseStack, nodeCollector, lightness,
  renderState, netHeadYaw, headPitch)` and `nodeCollector.submitModel(model, state, poseStack, renderType, light,
  overlay, color, null)`).
* `ModelPart` has public `xScale/yScale/zScale` (plus x,y,z,xRot,yRot,zRot,visible). Used for gene proportions.
* Commands: `RegisterCommandsEvent#getDispatcher().register(Commands.literal(..)...)`, `ctx.getSource().getLevel()`
  (ServerLevel), `.getPosition()` (Vec3), `.sendSuccess(() -> Component.literal(..), false)`,
  `Level#getEntitiesOfClass(Class, AABB)`, `new AABB(BlockPos)`, `BlockPos.containing(Vec3)`.
  Permissions are `.requires(Commands.hasPermission(Commands.LEVEL_OWNERS))` (LEVEL_* names other than OWNERS unchecked);
  our debug command has no `.requires` (read-only).
* `net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal` (package confirmed). Attributes seen: `MAX_HEALTH,
  MOVEMENT_SPEED, ATTACK_DAMAGE, KNOCKBACK_RESISTANCE, ...`.
* `EntitySpawnReason` exists (`net.minecraft.world.entity`); entity creation/spawn now takes it (needed in Phase 2 when
  spawning babies; exact `EntityType#create/spawn` signature not checked yet).
* Entity type constants for vanilla live in `EntityTypes` (e.g. `EntityTypes.ZOMBIE`) in the test sources.

## Assumed in Phase 1 -> CONFIRMED (Phase 1 compiled and ran perfectly in game; every name below works as written)
* `Attributes.JUMP_STRENGTH` and `Attributes.FOLLOW_RANGE` constant names (pre-1.21.2 prefix-free naming assumed).
* Constructors: `AvoidEntityGoal(PathfinderMob, Class<T>, float, double, double)`, `MeleeAttackGoal(PathfinderMob, double,
  boolean)`, `HurtByTargetGoal(PathfinderMob, Class<?>...)`; package of the first two is `...ai.goal`.
* `public void extractRenderState(...)` override in `MobRenderer` (visibility of the vanilla method not seen).
* `Entity#getRandom().nextLong()`, `LivingEntity#getHealth/getMaxHealth/setHealth/getAttribute(Holder)/getAttributeValue(Holder)`,
  `Entity#distanceToSqr(Vec3)`.
* `EntityModel#setupAnim` really resets every part (including x/y) before our code, so `part.y -= lift` does not
  accumulate. Phase 0's ear twitch (`zRot +=`) already relies on the same reset and looked right in-game.

## Session 3 findings (research only, nothing compiled)
* The NeoForge repo (sparse clone of `tests`, `patches`, `src`) contains NO vanilla sources, only patches. So vanilla names such as
  `Attributes.JUMP_STRENGTH`, `Attributes.FOLLOW_RANGE`, `AvoidEntityGoal`, `MeleeAttackGoal` CANNOT be confirmed from it (grep finds
  nothing). Only `HurtByTargetGoal` shows up (`tests/.../LivingEntityEventTests.java`: `new HurtByTargetGoal(zombie)`).
  The 26.1 and 26.2 porting primers (`primers/26.1`, `26.2` in neoforged/.github) mention no rename of any of these, which is weak
  evidence they kept their names. The real check is still the user's first compile.
* Primer 26.2, entity spawning (matters for Phase 2 baby spawning):
  * New class `EntitySpawnRequest` = spawn reason + whether to ignore spawn requirements. `EntityType#canSpawn` is new.
  * `EntityType#create` "now has an overload" taking `EntitySpawnRequest` instead of `EntitySpawnReason` (so the reason-based
    overload should still exist, but check its exact parameters before relying on it).
  * `EntityType#spawn` / `create` now take a `PostSpawnProcessor` instead of a `Consumer` for the post-spawn callback.
  * `EntityType#create(ValueInput, Level, EntitySpawnReason)` was REPLACED by `create(ValueInput, Level, EntitySpawnRequest)`.
  * Plan for Phase 2: read the actual signatures (primer text + any 26.2.x test that spawns an entity) before writing the
    spawn-a-baby code; do not write it from memory of 1.21.
* `Level#getRandom()` is confirmed (primer 26.1: the `Level#random` field became protected, use the `getRandom` method).
  `Entity#getRandom()`, which `Bunnidog`'s constructor uses, is still only assumed.

## Session 3, part 2: verified from NeoForge 26.2.x patches (Phase 2 breeding; CONFIRMED by the user's Phase 2 compile + run)
* `net.minecraft.world.entity.animal.Animal` exists (patch `patches/.../animal/Animal.java.patch`) and `AgeableMob` is in
  `net.minecraft.world.entity`. Seen in the patch: `public void spawnChildFromBreeding(ServerLevel level, Animal partner)`
  calls `AgeableMob offspring = this.getBreedOffspring(level, partner)` (returns `AgeableMob`, may be null), then
  `offspring.setBaby(true)` and `offspring.snapTo(x, y, z, yRot, xRot)`; `setAge(int)`, `resetLove()` and `getLoveCause()` are
  PUBLIC (inferred: Fox's inner breed goal, in another package, calls them on an `Animal`-typed field; `setAge(6000)` is
  vanilla's post-breeding cooldown).
* NeoForge posts `BabyEntitySpawnEvent(parentA, parentB, @Nullable AgeableMob child)` from `Animal#spawnChildFromBreeding`;
  cancelling resets love and sets both parents' age to 6000.
* `EntityType#create(level, EntitySpawnReason)` two-argument form is used by vanilla in 26.2.x (e.g. `EntityTypes.CAT.create(level,
  EntitySpawnReason.NATURAL)`); constants seen: NATURAL, EVENT, TRIGGERED, SPAWN_ITEM_USE.
* `level.addFreshEntityWithPassengers(entity)` (vanilla uses it for child mobs) and `level.addFreshEntity(entity)` both exist.
* `LivingEntityRenderer#extractRenderState` does `state.isBaby = entity.isBaby()` (patch), so `LivingEntityRenderState#isBaby`
  exists and is filled for every living entity. The model uses it.
* 26.2 primer: `AgeableMob#isBaby` and `setBaby` are now `final` (override `canBeABaby` instead); we only call them.
  26.1 primer: vanilla animals now use separate adult/baby model classes, so vanilla does not scale babies generically (our model
  scales itself, see `PUP_SCALE` in `BunnidogModel`).
* `PartEntityTest` (NeoForge tests) registers a pig-based entity with `Pig.createAttributes().build()`.

## Assumed in Phase 2 -> CONFIRMED (Phase 2 compiled and ran; the names below work as used in `Bunnidog.java`)
* `Animal.createAnimalAttributes()`: recalled from 1.21.5+, where it adds the `tempt_range` attribute that `TemptGoal` reads. If it
  does not exist, fall back to `Mob.createMobAttributes()` AND add `Attributes.TEMPT_RANGE` (10.0) by hand, or TemptGoal crashes
  at runtime with a missing-attribute exception (drop the TemptGoal line if in doubt).
* Constructors: `BreedGoal(Animal, double)`, `TemptGoal(PathfinderMob, double, Predicate<ItemStack>, boolean)`,
  `FollowParentGoal(Animal, double)`, all in `net.minecraft.world.entity.ai.goal`.
* `getBreedOffspring(ServerLevel, AgeableMob)`: the second parameter type (AgeableMob vs Animal) was not seen; return type is.
* `public boolean isFood(ItemStack)` on `Animal`; `EntitySpawnReason.BREEDING`; `Items.CARROT`; `ItemStack#is(Item)`.
* `Animal#isFood`-driven carrot feeding of pups (vanilla `mobInteract`) works for our subclass without any override.
* Pup hitbox: whether vanilla shrinks a baby's collision box is still unchecked (the user saw nothing wrong); the model shrink is ours.

## Session 4: verified from the NeoForge 26.1 docs (docs.neoforged.net; bash had no network, so web_fetch of the doc pages was used)
* Biome modifier file: `data/<modid>/neoforge/biome_modifier/<name>.json`. Add spawns:
  `{"type":"neoforge:add_spawns","biomes":"#ns:tag" (or id / list of ids),"spawners":{"type":"ns:entity","weight":6,"minCount":2,"maxCount":3}}`
  (spawners may be one object or a list; weight non-negative, counts positive).
* A biome modifier that names a biome id which does not exist CRASHES the world load. Fix used in this mod: point the modifier at a biome TAG and
  make the tag's entries optional: `{"replace": false, "values": [{"id": "minecraft:x", "required": false}, "#ns:other_tag"]}`.
* Biome tags live in `data/<namespace>/tags/worldgen/biome/<name>.json` (Minecraft wiki "Biome tag", August 2026 revision; also misode's 26.2 listing).
* Docs say a new entity must have a spawn restriction registered on `RegisterSpawnPlacementsEvent` or it "could spawn in mid-air, fall and die".
  The 26.x docs (Architectury page for 26.2) show the vanilla side: `SpawnPlacementTypes.ON_GROUND`, `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES`,
  predicate `(type, level, spawnReason, pos, random) -> boolean` (5 parameters).
* Natural spawning (NeoForge docs "Living Entities" / Natural Spawning): the friendly categories (CREATURE etc.) spawn EVERY tick when the spawn cap
  allows, and additionally at chunk generation when the category is persistent; the position is chosen at random inside the chunk (any height), so
  underground positions are possible; biome spawn data is looked up at the chosen position. 3 position attempts per try.
* `Identifier.fromNamespaceAndPath` is used in the 26.1 docs for biome modifier keys (matches the rest of this mod).

## Assumed in Phase 3 -> CONFIRMED (Phase 3 compiled for the user, session 4; every name below works as used)
* `net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent` (mod bus) with
  `register(EntityType<T>, SpawnPlacementType, Heightmap.Types, SpawnPlacements.SpawnPredicate<T>, RegisterSpawnPlacementsEvent.Operation)` and
  `Operation.REPLACE`; `net.minecraft.world.entity.SpawnPlacementTypes` and `net.minecraft.world.level.levelgen.Heightmap`. The event NAME is
  confirmed by the 26.1 docs, the parameter list is recalled from 1.21.x. If `register` complains, look at the parameter list in the IDE.
  Fix if the lambda has the wrong arity: it is the 5-parameter `SpawnPredicate`; drop the lambda for `Mob::checkMobSpawnRules` if in doubt.
* `TagKey.create(Registries.BIOME, Identifier)`, `Holder#is(TagKey)`, `LevelReader#getBiome(BlockPos)` (returns `Holder<Biome>`),
  `LevelReader#canSeeSky(BlockPos)` (all in `ModBiomeTags`). `Level`/`ServerLevelAccessor` are passed where `LevelReader` is expected.
* `public void tick()` can be overridden on `Bunnidog` (`super.tick()` first); `blockPosition()`.
* The lambda in `registerSpawnPlacements` gets a `ServerLevelAccessor` as `level`; if it is a different type, `ModBiomeTags.canSpawnNaturally`
  takes `LevelReader`, which every level type is.

## Session 4, part 5: items (Phase 4b), verified from the NeoForge 26.1 docs (docs.neoforged.net/docs/items/ and .../resources/client/models/items)
* `DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID)`; `DeferredItem<Item> X = ITEMS.registerSimpleItem("name", props -> props ...)`
  (26.1 form: the second argument is a UNARY OPERATOR on `Item.Properties`; the id is set for you; older docs passed an `Item.Properties` object).
  `Item.Properties#food(FoodProperties)` exists. Every item needs its id set (the helper does it).
* Client item JSON is REQUIRED for an item to render: `assets/<ns>/items/<id>.json` = `{"model": {"type": "minecraft:model", "model": "<ns>:item/<id>"}}`,
  and that model is `assets/<ns>/models/item/<id>.json` (`parent: minecraft:item/generated`, `textures.layer0: <ns>:item/<id>` = `textures/item/<id>.png`).
  Translation key `item.<ns>.<id>`.
* Creative tab: `BuildCreativeModeTabContentsEvent` on the MOD bus, `event.getTabKey() == CreativeModeTabs.X`, `event.accept(item)`.

## Session 4, part 8: Phase 4d, verified in the REAL NeoForge 26.2.x sources (same sparse clone as part 7)
* **`BlockEvent.BreakEvent` NO LONGER EXISTS.** It is `net.neoforged.neoforge.event.level.block.BreakBlockEvent extends BlockEvent implements ICancellableEvent` with `getPlayer()`, `getPos()`, `getState()`,
  `getLevel()` (a `LevelAccessor`), `setCanceled(boolean)`, `shouldNotifyClient()` and `setNotifyClient(boolean)`. It fires on BOTH sides; when cancelling on the server only, call `setNotifyClient(true)` so the
  client undoes its prediction. Javadoc elsewhere in NeoForge still says `BlockEvent.BreakEvent`: grep for class declarations, not for names in comments.
* Blocks: `DeferredRegister.createBlocks(MODID)` -> `DeferredRegister.Blocks`; `registerBlock(String, Function<BlockBehaviour.Properties, ? extends B>, UnaryOperator<BlockBehaviour.Properties>)` (also `Supplier` and
  no-properties forms); `properties.noCollision()`, `.instabreak()`, `.noOcclusion()`, `.noLootTable()` are all used in NeoForge's tests. `class X extends Block implements EntityBlock` needs NO `codec()`.
  `createBlockStateDefinition(StateDefinition.Builder<Block, BlockState>)` (protected), `registerDefaultState(stateDefinition.any().setValue(...))`, `BlockStateProperties.AGE_7`,
  `getShape(BlockState, BlockGetter, BlockPos, CollisionContext)`, `canSurvive(BlockState, LevelReader, BlockPos)` and `randomTick(BlockState, ServerLevel, BlockPos, RandomSource)` (protected in vanilla; we declare
  our overrides public, which compiles either way), `newBlockEntity(BlockPos, BlockState)`.
* Block entities: `new BlockEntityType<>(SomeBlockEntity::new, block)` (constructor `(BlockPos, BlockState)`, then `super(type, pos, state)` inside), registered with
  `DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID)`; saving is `protected void saveAdditional(ValueOutput)` / `protected void loadAdditional(ValueInput)` (call super); our `getStringOr` / `putString` pattern from the entity works.
* Fortune: `ItemStack#getEnchantmentLevel(Holder<Enchantment>)`; the holder is `level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)`.
  Players: `hasInfiniteMaterials()` (creative), `sendSystemMessage(Component)`, `getMainHandItem()`. Items: `public InteractionResult useOn(UseOnContext)`; `UseOnContext#getClickedPos()`, `#getClickedFace()`; `Direction.UP`.
* JSON: blockstates are `{"variants": {"age=0": {"model": "ns:block/name"}, ...}}`; a model may carry a top-level `"render_type": "cutout"` (bare names: `cutout`, `cutout_mipped`, `translucent`).
* All 20 Minecraft/NeoForge import paths used by the new files were matched against real `import` lines in NeoForge's tests/src (ItemEntity, LevelReader, BlockGetter, BlockTags, Shapes, VoxelShape, CollisionContext,
  Enchantments, SoundType, RandomSource, StateDefinition, BlockStateProperties, IntegerProperty, InteractionResult, Direction, UseOnContext, EntityBlock, BlockEntityType, DeferredBlock, DeferredHolder).

## Assumed in Phase 4d: the NAMES are CONFIRMED by the compile (session 4 part 9); the RUNTIME behaviour is still open (only an in-game run shows it)
* CONFIRMED by the compile: `BlockBehaviour.Properties#randomTicks()` and `.sound(SoundType.CROP)` (vanilla names, never seen in NeoForge's sources).
* CONFIRMED by the compile: `Shapes.box(double x6)`, the static factory for the crop's hitbox.
* CONFIRMED by the compile: `BlockState#is(Block)`, `BlockState#is(TagKey<Block>)`, `BlockTags.DIRT`, `BlockState#isAir()`, `Level#setBlock(BlockPos, BlockState, int)`, `BlockEntity#setChanged()`.
* STILL OPEN (a resource, so the compile cannot show it): the vanilla parent model `minecraft:block/crop` with the texture key `crop` (a vanilla asset, absent from NeoForge's repo). If the crop renders as a cube or is purple-black, check that name first.
* STILL OPEN (in-game only): transparent pixels. If the plant shows a black or white background, `render_type: cutout` is being ignored in this version.
* STILL OPEN (in-game only): `@EventBusSubscriber(modid = ...)` picking the game bus for `BreakBlockEvent` on its own (it does for `RegisterCommandsEvent` in our commands class). If harvesting does nothing at all, look at the bus first.
* CONFIRMED by the compile: `RandomSource#nextInt(int)` (growth roll and the XP amount).

## Assumed in Phase 4b -> CONFIRMED (Phase 4b compiled for the user, session 4; every name below works as used)
* `new FoodProperties.Builder().nutrition(int).saturationModifier(float).build()` (documented for 1.21.3; the 26.1 page was not re-read), package `net.minecraft.world.food`.
* `CreativeModeTabs.FOOD_AND_DRINKS` (vanilla constant; the docs example uses INGREDIENTS) and `net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent`.
* `ItemStack#is(TagKey<Item>)`, `TagKey.create(Registries.ITEM, Identifier)` (same pattern as the biome tag key, which compiled), and the item tag folder `data/<ns>/tags/item/`.
* `net.neoforged.neoforge.registries.DeferredItem` (import).

## Session 4, part 7: Phase 4c, verified in the REAL NeoForge 26.2.x sources (sparse clone of tests + patches + src; bash reached github.com this session)
* Data components: `DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID)` (two arguments) returns `DeferredRegister.DataComponents`;
  `registerComponentType(String, UnaryOperator<DataComponentType.Builder<D>>)` returns `DeferredHolder<DataComponentType<?>, DataComponentType<D>>`; builder methods
  `.persistent(Codec)` and `.networkSynchronized(StreamCodec)` (`tests/.../oldtest/DeferredRegistryTest.java`, `.../unittest/transfer/EnergyTests.java`). Imports:
  `net.minecraft.core.component.DataComponentType`, `net.minecraft.network.codec.ByteBufCodecs` (has `STRING_UTF8`, `VAR_INT`), `net.minecraft.util.ExtraCodecs`, `com.mojang.serialization.Codec`.
  The result must be declared with an explicit type (`DeferredHolder<DataComponentType<?>, DataComponentType<String>>`) so `D` can be inferred.
* Items: `DeferredRegister.Items#registerItem(String, Function<Item.Properties, ? extends I>, UnaryOperator<Item.Properties>)` (also a `Supplier<Item.Properties>` variant and a two-argument
  form); it applies the properties to a new `Item.Properties` for you. Returns `DeferredItem<I>`.
* Tooltip: `Item#appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)` (`tests/.../oldtest/client/CustomTooltipTest.java`); imports
  `net.minecraft.world.item.component.TooltipDisplay`, `net.minecraft.world.item.TooltipFlag`, `net.minecraft.ChatFormatting`; `TooltipFlag#isAdvanced()` exists. Same test shows
  `Item#use(Level, Player, InteractionHand)` returning `InteractionResult` (not used yet; it is what a planting item will override in 4d).
* `ItemStack#set(DataComponentType, value)`, `#get(...)` (nullable), `#shrink(int)` all in use in NeoForge's tests.
* Dropping an item: `Entity#spawnAtLocation(ServerLevel, ItemStack, float yOffset)` returns the `ItemEntity` (`common/IShearable.java`); `new ItemEntity(Level, x, y, z, ItemStack)` also exists.
* XP: `new ExperienceOrb(Level, Vec3 pos, Vec3 movement, int value)` (`net.minecraft.world.entity.ExperienceOrb`); `Vec3` fields `x, y, z` are public; `Entity#position()` returns a `Vec3`.
* Particles: `ServerLevel#sendParticles(options, x, y, z, count, xDist, yDist, zDist, speed)` (9 arguments, seen in the `LivingEntity` patch); `net.minecraft.core.particles.ParticleTypes` is the package.
* Commands: `CommandSourceStack#getPlayerOrException()` (throws `CommandSyntaxException`), `#getPlayer()` (nullable), `#sendFailure(Component)`.
* Creative tabs: `BuildCreativeModeTabContentsEvent implements CreativeModeTab.Output`; `accept(ItemStack, CreativeModeTab.TabVisibility)` with `TabVisibility.PARENT_AND_SEARCH_TABS`;
  `CreativeModeTabs.INGREDIENTS` is used in NeoForge's tests.
* Breeding flow (`patches/.../animal/Animal.java.patch`): `spawnChildFromBreeding` calls `getBreedOffspring`, posts `BabyEntitySpawnEvent`, and only continues when the offspring is non-null;
  a null child means vanilla resets nothing, which is why `Bunnidog.spawnChildFromBreeding` calls `resetLove()` and `setAge(...)` itself. The name `finalizeSpawnChildFromBreeding` could not be seen
  (vanilla body absent), so it is not used. `net.minecraft.stats.Stats.ANIMALS_BRED` exists (careful: it clashes with our own `genetics.Stats`; the statistic is not awarded).

## Assumed in Phase 4c -> CONFIRMED (Phase 4c compiled for the user, session 4 part 9; every name below works as used). Compiling proves the names exist and the types fit, not the behaviour: whether the hearts show and the tooltip renders is an in-game check
* `ParticleTypes.HEART`: a long-standing vanilla constant, but NeoForge's sources never reference it. If it is missing, use another `SimpleParticleType` or drop the `sendParticles` line.
* `RandomSource#nextInt(int)` on `this.getRandom()` (used for the XP amount): certain in 1.21.x, not re-seen.
* Generic inference of `ITEMS.registerItem("seed", SeedItem::new, props -> props)` into `DeferredItem<SeedItem>`: the signature was read, the exact call was not compiled.
* `com.mojang.brigadier.arguments.DoubleArgumentType` with `Commands.argument("bonus", DoubleArgumentType.doubleArg(0.0D))` and `DoubleArgumentType.getDouble(ctx, "bonus")`: standard Brigadier, not seen in NeoForge's sources.
* The `stack.is(ModItems.SEED.get())` call takes the `SeedItem` where an `Item` is expected: fine by subtyping, only listed for completeness.

## Assumed in Phase 4a -> CONFIRMED (Phase 4a compiled for the user, session 4; `AgeableMob#getAge()` and the rest work as used)
* `AgeableMob#getAge()` is public and returns the server-side age (negative = pup) - used in `Bunnidog.growthTick()`. `setAge(int)` is already used
  since Phase 2. If `getAge()` does not exist, look for the age accessor in the IDE (it is what `setAge` writes); nothing else in 4a is new API.
* Everything else reuses names that already compiled: `putString` / `getStringOr` for the `Stats` tag, `getAttribute(...).setBaseValue`, `tick()`.
  (An int-array tag was avoided on purpose: its `ValueOutput` method name was never verified.)

## Session 5: Phase 4e (guava tiers), verified in the REAL NeoForge 26.2.x sources (sparse clone of tests + patches + src; bash reached github.com). The user compiled it: "It compiled well"
* **CONFIRMED by the compile (names and types):** `public InteractionResult mobInteract(Player, InteractionHand)` override on `Bunnidog` (an `Animal`), `InteractionResult#consumesAction()`, `ITEMS.registerItem(name, props -> new GuavaItem(props, tier), props -> props.food(..).rarity(..))`,
  `Item.Properties#rarity(Rarity)` and `Rarity.{COMMON,UNCOMMON,RARE,EPIC}`, `ValueOutput#putInt` / `ValueInput#getIntOr` inside the entity, the `appendHoverText` override of `GuavaItem`, `DeferredItem<GuavaItem>` lists in `ModItems`.
* **SOURCE, open until the game loads it:** the six recipe JSON files in `data/bunnidogs/recipe/` (format copied from the generated test data, see the Quick Index row), the six client items and models, the tag with seven entries, the six new textures.
* **ASSUMED (vanilla behaviour, not visible in the compile):** that `mobInteract` runs on the server before the item is used (the code reads the tier first and relies on `consumesAction()` afterwards), and that vanilla refuses a second guava for an animal that is already in love.
* **Deliberately AVOIDED:** `isInLove()` / `canFallInLove()`. The love mode is mirrored by `genetics/GuavaMemory` (600 ticks, cleared when the age leaves 0), which also made it testable in plain Java.
* Method used to keep the wiring honest without Minecraft: `tools/GenomeCheck.java` greps the glue for the exact lines that matter (see PROJECT_CONTEXT "How the self-check is organized").

## Model space cheat sheet
Java entity model: y-down, ground at y=24, front = -Z, `addBox(x, y, z, w, h, d)` where (x,y,z) is the min corner
relative to the part's pivot. `PartPose.offset(px,py,pz)` places the pivot; rotations in radians.

## Session 6: bone meal for the seed crop + embryo models (SOURCE; the user's compile and in-game run are open)
* Bone meal was deferred in 4d because the interface signatures could not be seen. They are now: see the QUICK INDEX row "Bone meal on a custom block". Everything in `SeedCropBlock` for it is plain
  Java on names already COMPILED (`RandomSource#nextInt`, `ServerLevel`, `BlockState#setValue`, `Level#setBlock`) plus the three interface methods and two imports (`BonemealableBlock`, `Level`).
* `BonemealableBlock` has more (default) methods in recent versions (`getType`, `getParticlePos`, `hasSpreadableNeighbourPos`...). They are default or static, so a class that implements the three above compiles.
  If the compiler says the class is not abstract and does not override something, that method name is the thing to read in the IDE; `getType()` (a `BonemealableBlock.Type`, used for the particle colour) is the likeliest.
* Vanilla spends the bone meal and shows the green particles itself; the block only decides whether it applies and what changes. The block entity (the planted seed) survives a `setBlock` that only changes the state (same block).
* Crop model: `minecraft:block/cross` is a vanilla model file, so only the docs confirm its name. If the embryo shows as a purple-black cube, check that name first (the old `block/crop` parent had the same status).

## Session 6, Phase 4f (juice and infusion): what to read first if it does not compile
Everything pure (`Juice`, `Infusion`) is tested here. The glue is `item/JuiceItem.java`, `SeedItem#use`, and three lines in `ModItems`/`BunnidogsCommands`. The likeliest compile errors, most likely first:
1. `new MobEffectInstance(effect, ticks, amplifier)`: if the 3-argument constructor is not there, use the 2-argument one and call `.withAmplifier`-style API, or look at `MobEffectInstance`'s constructors in the IDE (the amplifier is the third `int`).
2. `getOrThrow(ResourceKey)` on `lookupOrThrow(Registries.MOB_EFFECT)`: identical to the enchantment call already in `SeedCropHarvest`; if it fails, `BuiltInRegistries.MOB_EFFECT.get(...)` variants exist.
3. `props.stacksTo(int)` in `ModItems` (a long-standing `Item.Properties` method; if it is gone, the quantity is set through the `DataComponents.MAX_STACK_SIZE` component).
4. `ItemUseAnimation.DRINK` (seen in a NeoForge test), `Items.GLASS_BOTTLE`, `AABB#inflate(double)`, `Entity#blockPosition()`: all standard names that are not in the sources I can read as calls.
Runtime, not compile: a juice that drinks but gives no effect means an effect id was refused: look for `Juice: no such effect` in `run/logs/latest.log`.

## Session 6, Phase 4g (warrens + guava trees): what to read first if it does not compile
Seen in the 26.2.x sources: a custom feature is registered with `DeferredRegister<Feature<?>>` (`Registries.FEATURE`) as `new XFeature(NoneFeatureConfiguration.CODEC)` (`tests/.../oldtest/world/WorldgenRegistryDesyncTest.java`); `useWithoutItem` is `protected InteractionResult useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)` (`CustomDebugSubscriberTest`);
`registerSimpleBlockItem(DeferredBlock)` (several tests); `setPersistenceRequired()`, `addFreshEntityWithPassengers`, `snapTo(...)`, `create(level, EntitySpawnReason...)`; `neoforge:add_features` biome modifier JSON (`tests/src/generated/resources/data/biome_modifiers_test`).
Assumed (standard names, not seen as calls), most likely first:
1. `Feature#place(FeaturePlaceContext<NoneFeatureConfiguration>)` returning boolean, and `context.level()/origin()/random()`.
2. `EntitySpawnReason.STRUCTURE` (the compiled code uses `BREEDING`; `CHUNK_GENERATION` is the other candidate).
3. `WorldGenLevel#getLevel()` returning the `ServerLevel`; `BlockState#canBeReplaced()`, `getDestroySpeed(BlockGetter, BlockPos)`, `hasBlockEntity()`, `getFluidState()`; `LeavesBlock.PERSISTENT`; `BlockStateProperties.AGE_2`; `CreativeModeTabs.NATURAL_BLOCKS`; `SoundType.GRASS`.
Data formats written from memory of the 1.21 formats: `placed_feature` (`rarity_filter` chance, `in_square`, `heightmap` WORLD_SURFACE_WG, `height_range` uniform, `biome`), `loot_table/blocks/*.json` (singular `loot_table` folder), `block_state_property` loot condition. A bad one shows up as an error in `run/logs/latest.log`.

## Session 9 (fix9, warrens on uneven ground): no new Minecraft names
Everything used was already compiled: `BlockState#isAir/canBeReplaced/getFluidState/isFaceSturdy`, `Blocks.DIRT/COARSE_DIRT/GRASS_BLOCK/AIR`, `BlockPos` (`new BlockPos(x, y, z)`, `offset`, `above`, `below`), `WorldGenLevel#getBlockState/setBlock`, `BunnidogsMod.LOGGER`, Brigadier `Commands.literal` under an argument (the `surface` literal after `level`). Status: SOURCE-level only until the user compiles. Not used on purpose: `Heightmap`/`getHeight` (never verified; the topmost-block scan needs nothing new).

### Session 11 (Phase 6e): new names, ASSUMED until the user compiles
`Blocks.HAY_BLOCK`, `BARREL`, `COBBLESTONE`, `MOSSY_COBBLESTONE`, `WATER`, `OAK_FENCE`, `OAK_SLAB`, `LANTERN`, `PUMPKIN`, `MELON`, `WHEAT`, `CARROTS`, `POTATOES` (vanilla block fields; `FARMLAND` already compiled). Used only in `WarrenFeature.propBlock` with `defaultBlockState()`.

### Session 11 (Phase 6f): new names, ASSUMED until the user compiles
Vanilla `Blocks.*` fields used only in `WarrenPalette`: SPRUCE_PLANKS, SNOW_BLOCK, STONE_BRICKS, SPRUCE_FENCE, SPRUCE_SLAB, SANDSTONE, CUT_SANDSTONE, SMOOTH_SANDSTONE, ACACIA_FENCE, ACACIA_SLAB, MUD_BRICKS, MOSS_BLOCK, PACKED_MUD, MANGROVE_FENCE, MANGROVE_SLAB, COBBLESTONE_WALL, STONE_BRICK_SLAB, NETHER_BRICKS, BLACKSTONE, NETHER_BRICK_FENCE, NETHER_BRICK_SLAB, NETHER_WART_BLOCK, SOUL_SOIL, NETHER_WART, END_STONE_BRICKS, PURPUR_BLOCK, PURPUR_PILLAR, END_STONE_BRICK_WALL, PURPUR_SLAB, MYCELIUM, RED_MUSHROOM, BROWN_MUSHROOM. `ModBiomeTags.lineageAt(LevelReader, BlockPos)` is now also called with a `WorldGenLevel`.

### Session 11 (Phase 6g): new name, ASSUMED until the user compiles
`Blocks.GLASS` (vanilla block field), used only in `WarrenFeature.buildHouse`.

(Session 20, S1: no new Minecraft API; `genetics/Plain` and `genetics/Units` are pure Java. The jump simulation uses the vanilla constants gravity 0.08 and drag 0.98 per tick, SOURCE: the known vanilla jump height of 1.2522 blocks for 0.42 is reproduced by the self-check. The blocks-per-second factor 43.17 is ASSUMED.)
