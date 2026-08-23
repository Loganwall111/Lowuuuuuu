package net.dabicco.devouringstorms.entity.state;

import java.util.ArrayList;
import java.util.List;
import net.dabicco.devouringstorms.client.ClusterMesh;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;

public class WitherStormClusterRenderState extends EntityRenderState {
   public ClusterMesh mesh;
   public int packedLight;
   public boolean legacy;
   public final List<DarkenedMovingBlockRenderState> legacyBlocks = new ArrayList<>();
   public final List<BlockPos> legacyOffsets = new ArrayList<>();
   public float yRot = 0.0F;
   public float xRot = 0.0F;
   public float roll;
   public float clusterScale = 1.0F;
}
