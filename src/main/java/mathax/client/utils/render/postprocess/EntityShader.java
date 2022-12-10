package mathax.client.utils.render.postprocess;

import mathax.client.mixin.WorldRendererAccessor;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.WorldRenderer;

import static mathax.client.MatHax.mc;

public abstract class EntityShader extends PostProcessShader {
    private Framebuffer prevBuffer;

    @Override
    protected void preDraw() {
        WorldRenderer worldRenderer = mc.worldRenderer;
        WorldRendererAccessor worldRendererAccessor = (WorldRendererAccessor) worldRenderer;
        prevBuffer = worldRenderer.getEntityOutlinesFramebuffer();
        worldRendererAccessor.setEntityOutlinesFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        if (prevBuffer == null) {
            return;
        }

        WorldRenderer worldRenderer = mc.worldRenderer;
        WorldRendererAccessor worldRendererAccessor = (WorldRendererAccessor) worldRenderer;
        worldRendererAccessor.setEntityOutlinesFramebuffer(prevBuffer);
        prevBuffer = null;
    }

    public void endRender() {
        endRender(() -> vertexConsumerProvider.draw());
    }
}