package corgitaco.corgilib.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class RenderUtils {

    public static void drawFlatColoredSphere(Matrix4f pose, MultiBufferSource bufferSource, float radius, float originX, float originY, float originZ, float r, float g, float b, float a) {
        drawSphere(pose, bufferSource.getBuffer(RenderTypes.debugQuads()), radius, originX, originY, originZ, 15, 15, (pose1, consumer1, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3) -> {
            consumer1.addVertex(pose1, x0, y0, z0).setColor(r, g, b, a);
            consumer1.addVertex(pose1, x1, y1, z1).setColor(r, g, b, a);
            consumer1.addVertex(pose1, x2, y2, z2).setColor(r, g, b, a);
            consumer1.addVertex(pose1, x3, y3, z3).setColor(r, g, b, a);
            // Renders inverse
            consumer1.addVertex(pose1, x3, y3, z3).setColor(r, g, b, a);
            consumer1.addVertex(pose1, x2, y2, z2).setColor(r, g, b, a);
            consumer1.addVertex(pose1, x1, y1, z1).setColor(r, g, b, a);
            consumer1.addVertex(pose1, x0, y0, z0).setColor(r, g, b, a);
        });
    }


    public static void drawSphere(Matrix4f pose, VertexConsumer consumer, float radius, float originX, float originY, float originZ, float rings, float segments, SphereDrawHandler sphereDrawHandler) {
        float ringStep = (float) Math.PI / rings;
        float segmentStep = 2 * (float) Math.PI / segments;

        for (int ring = 0; ring < rings; ring++) {
            float theta = ring * ringStep;
            float nextTheta = (ring + 1) * ringStep;

            for (int segment = 0; segment < segments; segment++) {
                float phi = segment * segmentStep;
                float nextPhi = (segment + 1) * segmentStep;

                float x0 = originX + radius * Mth.sin(theta) * Mth.cos(phi);
                float y0 = originY + radius * Mth.cos(theta);
                float z0 = originZ + radius * Mth.sin(theta) * Mth.sin(phi);

                float x1 = originX + radius * Mth.sin(theta) * Mth.cos(nextPhi);
                float y1 = originY + radius * Mth.cos(theta);
                float z1 = originZ + radius * Mth.sin(theta) * Mth.sin(nextPhi);

                float x2 = originX + radius * Mth.sin(nextTheta) * Mth.cos(nextPhi);
                float y2 = originY + radius * Mth.cos(nextTheta);
                float z2 = originZ + radius * Mth.sin(nextTheta) * Mth.sin(nextPhi);

                float x3 = originX + radius * Mth.sin(nextTheta) * Mth.cos(phi);
                float y3 = originY + radius * Mth.cos(nextTheta);
                float z3 = originZ + radius * Mth.sin(nextTheta) * Mth.sin(phi);


                sphereDrawHandler.draw(pose, consumer, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3);
            }
        }
    }

    public static void drawRing(Matrix4f pose, MultiBufferSource bufferSource, float distance, float originX, float originY, float originZ, float r, float g, float b, float a) {
        drawRing(pose, bufferSource.getBuffer(RenderTypes.debugQuads()), distance, originX, originY, originZ, 15, 2, (pose1, vertexConsumer1, x1, y1, z1, x2, z2, y2) -> {
            vertexConsumer1.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
            vertexConsumer1.addVertex(pose, x2, y1, z2).setColor(r, g, b, a);
            vertexConsumer1.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
            vertexConsumer1.addVertex(pose, x1, y2, z1).setColor(r, g, b, a);
            // Render Inverse
            vertexConsumer1.addVertex(pose, x1, y2, z1).setColor(r, g, b, a);
            vertexConsumer1.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
            vertexConsumer1.addVertex(pose, x2, y1, z2).setColor(r, g, b, a);
            vertexConsumer1.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
        });
    }

    public static void drawRing(Matrix4f pose, VertexConsumer vertexConsumer, float distance, float originX, float originY, float originZ, int segments, float ringHeight, RingDrawHandler ringDrawHandler) {
        float multiplier = Mth.PI * 2;

        for (int segmentIdx = 0; segmentIdx < segments; segmentIdx++) {
            float hPct = ((float) segmentIdx) / segments;
            float nextHPct = ((float) (segmentIdx + 1)) / segments;

            float startX = Mth.sin(hPct * multiplier) * distance;
            float startZ = Mth.cos(hPct * multiplier) * distance;

            float endX = Mth.sin(nextHPct * multiplier) * distance;
            float endZ = Mth.cos(nextHPct * multiplier) * distance;

            float x1 = originX + startX;
            float y1 = originY + ringHeight;
            float z1 = originZ + startZ;

            float x2 = originX + endX;
            float y2 = originY - ringHeight;
            float z2 = originZ + endZ;

            ringDrawHandler.draw(pose, vertexConsumer, x1, y1, z1, x2, z2, y2);
        }
    }

    @FunctionalInterface
    public interface SphereDrawHandler {
        void draw(Matrix4f pose, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3);
    }

    @FunctionalInterface
    public interface RingDrawHandler {
        void draw(Matrix4f pose, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float z2, float y2);
    }
}