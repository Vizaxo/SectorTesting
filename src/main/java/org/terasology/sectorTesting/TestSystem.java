/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.sectorTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.sectors.LoadedSectorUpdateEvent;
import org.terasology.entitySystem.sectors.SectorSimulationComponent;
import org.terasology.entitySystem.sectors.SectorSimulationEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TestSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(TestSystem.class);

    @In
    private EntityManager entityManager;

    @Override
    public void postBegin() {
        //Creating sector-scope entity with TestComponent for simulation
        //LocationComponent added to see loading/unloading behaviour
        EntityRef entity = entityManager.createSectorEntity(1);
        entity.addComponent(new TestComponent());
        entity.addComponent(new LocationComponent(new Vector3f(0, 0, 0)));
    }

    @ReceiveEvent
    public void simulateCounting(SectorSimulationEvent event, EntityRef entity) {
        //Simulation only. This is the only place the value changes
        TestComponent testComponent = entity.getComponent(TestComponent.class);
        testComponent.floatValue += event.getDelta() * 2;
        logger.info("Entity {} simulating. Value is {}", entity.getId(), testComponent.floatValue);
    }

    @ReceiveEvent
    public void echoCounting(LoadedSectorUpdateEvent event, EntityRef entity) {
        //Where changes to the world would be made
        //This is only called while the chunk is loaded
        logger.info("Entity {} loaded. Value is {}", entity.getId(), entity.getComponent(TestComponent.class).floatValue);
    }

    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void chunkLoaded(OnChunkLoaded event, EntityRef entity) {
        //Called once when the chunk loads
        logger.info("Chunk load: entity {}", entity.getId());
    }

    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void chunkUnloaded(BeforeChunkUnload event, EntityRef entity) {
        //Called once when the chunk unloads
        logger.info("Chunk unload: entity {}", entity.getId());
    }

}
