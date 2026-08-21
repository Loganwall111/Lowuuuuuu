package net.dabicco.witherstormmod.entity.state;

import java.util.ArrayList;
import java.util.List;
import net.dabicco.witherstormmod.client.ClusterMesh;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class WitherStormClusterRenderState extends EntityRenderState {
   public boolean legacy;
   public ClusterMesh mesh;
   public float clusterScale = 1.0F;
   public int packedLight;
   public float yRot;
   public float xRot;
   public float roll;
   public final List<DarkenedMovingBlockRenderState> legacyBlocks = new ArrayList();
   public final List<BlockPos> legacyOffsets = new ArrayList();
}
