package mathax.client.gui.renderer;

import mathax.client.gui.renderer.operations.GuiRenderOperation;
import mathax.client.init.PostInit;
import mathax.client.systems.themes.Theme;
import mathax.client.gui.renderer.operations.TextOperation;
import mathax.client.gui.renderer.packer.GuiTexture;
import mathax.client.gui.renderer.packer.TexturePacker;
import mathax.client.gui.widgets.WWidget;
import mathax.client.renderer.GL;
import mathax.client.renderer.Renderer2D;
import mathax.client.renderer.Texture;
import mathax.client.utils.Utils;
import mathax.client.utils.misc.MatHaxIdentifier;
import mathax.client.utils.misc.Pool;
import mathax.client.utils.render.ByteTexture;
import mathax.client.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static mathax.client.utils.Utils.getWindowHeight;
import static mathax.client.utils.Utils.getWindowWidth;

public class GuiRenderer {
    private static final Color WHITE = new Color(255, 255, 255);

    private static final TexturePacker TEXTURE_PACKER = new TexturePacker();
    private static ByteTexture TEXTURE;

    public static GuiTexture CIRCLE;
    public static GuiTexture TRIANGLE;
    public static GuiTexture EDIT;
    public static GuiTexture RESET;
    public static GuiTexture FAVORITE_NO, FAVORITE_YES;

    public Theme theme;

    private final Renderer2D colorRenderer = new Renderer2D(false);
    private final Renderer2D textureRenderer = new Renderer2D(true);

    private final Pool<Scissor> scissorPool = new Pool<>(Scissor::new);
    private final Stack<Scissor> scissorStack = new Stack<>();

    private final Pool<TextOperation> textPool = new Pool<>(TextOperation::new);
    private final List<TextOperation> texts = new ArrayList<>();

    private final List<Runnable> postTasks = new ArrayList<>();

    public String tooltip, lastTooltip;
    public WWidget tooltipWidget;
    private double tooltipAnimProgress;

    private MatrixStack matrixStack;

    public static GuiTexture addTexture(Identifier id) {
        return TEXTURE_PACKER.add(id);
    }

    @PostInit
    public static void init() {
        CIRCLE = addTexture(new MatHaxIdentifier("textures/icons/gui/circle.png"));
        TRIANGLE = addTexture(new MatHaxIdentifier("textures/icons/gui/triangle.png"));
        EDIT = addTexture(new MatHaxIdentifier("textures/icons/gui/edit.png"));
        RESET = addTexture(new MatHaxIdentifier("textures/icons/gui/reset.png"));
        FAVORITE_NO = addTexture(new MatHaxIdentifier("textures/icons/gui/favorite/no.png"));
        FAVORITE_YES = addTexture(new MatHaxIdentifier("textures/icons/gui/favorite/yes.png"));

        TEXTURE = TEXTURE_PACKER.pack();
    }

    public void begin(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;

        GL.enableBlend();
        GL.enableScissorTest();
        scissorStart(0, 0, getWindowWidth(), getWindowHeight());
    }

    public void end(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;

        scissorEnd();

        for (Runnable task : postTasks) {
            task.run();
        }

        postTasks.clear();

        GL.disableScissorTest();
    }

    public void beginRender() {
        colorRenderer.begin();
        textureRenderer.begin();
    }

    public void endRender() {
        colorRenderer.end();
        textureRenderer.end();

        colorRenderer.render(matrixStack);

        GL.bindTexture(TEXTURE.getGlId());
        textureRenderer.render(matrixStack);

        // Normal text
        theme.textRenderer().begin(theme.scale(1));
        for (TextOperation text : texts) {
            if (!text.title) {
                text.run(textPool);
            }
        }
        theme.textRenderer().end(matrixStack);

        // Title text
        theme.textRenderer().begin(theme.scale(1.25));
        for (TextOperation text : texts) {
            if (text.title) {
                text.run(textPool);
            }
        }
        theme.textRenderer().end(matrixStack);

        texts.clear();
    }

    public void scissorStart(double x, double y, double width, double height) {
        if (!scissorStack.isEmpty()) {
            Scissor parent = scissorStack.peek();

            if (x < parent.x) {
                x = parent.x;
            } else if (x + width > parent.x + parent.width) {
                width -= (x + width) - (parent.x + parent.width);
            }

            if (y < parent.y) {
                y = parent.y;
            } else if (y + height > parent.y + parent.height) {
                height -= (y + height) - (parent.y + parent.height);
            }

            parent.apply();
            endRender();
        }

        scissorStack.push(scissorPool.get().set(x, y, width, height));
        beginRender();
    }

    public void scissorEnd() {
        Scissor scissor = scissorStack.pop();

        scissor.apply();
        endRender();
        for (Runnable task : scissor.postTasks) {
            task.run();
        }

        if (!scissorStack.isEmpty()) {
            beginRender();
        }

        scissorPool.free(scissor);
    }

    public boolean renderTooltip(double mouseX, double mouseY, double delta, MatrixStack matrixStack) {
        tooltipAnimProgress += (tooltip != null ? 1 : -1) * delta * 14;
        tooltipAnimProgress = Utils.clamp(tooltipAnimProgress, 0, 1);

        boolean toReturn = false;

        if (tooltipAnimProgress > 0) {
            if (tooltip != null && !tooltip.equals(lastTooltip)) {
                tooltipWidget = theme.tooltip(tooltip);
                tooltipWidget.init();
            }

            tooltipWidget.move(-tooltipWidget.x + mouseX + 12, -tooltipWidget.y + mouseY + 12);

            setAlpha(tooltipAnimProgress);

            begin(matrixStack);
            tooltipWidget.render(this, mouseX, mouseY, delta);
            end(matrixStack);

            setAlpha(1);

            lastTooltip = tooltip;
            toReturn = true;
        }

        tooltip = null;
        return toReturn;
    }

    public void setAlpha(double a) {
        colorRenderer.setAlpha(a);
        textureRenderer.setAlpha(a);

        theme.textRenderer().setAlpha(a);
    }

    public void tooltip(String text) {
        tooltip = text;
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        colorRenderer.quad(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    public void quad(double x, double y, double width, double height, Color colorLeft, Color colorRight) {
        quad(x, y, width, height, colorLeft, colorRight, colorRight, colorLeft);
    }

    public void quad(double x, double y, double width, double height, Color color) {
        quad(x, y, width, height, color, color);
    }

    public void quad(WWidget widget, Color color) {
        quad(widget.x, widget.y, widget.width, widget.height, color);
    }

    public void quad(double x, double y, double width, double height, GuiTexture texture, Color color) {
        textureRenderer.texturedQuad(x, y, width, height, texture.get(width, height), color);
    }

    public void rotatedQuad(double x, double y, double width, double height, double rotation, GuiTexture texture, Color color) {
        textureRenderer.texturedQuad(x, y, width, height, rotation, texture.get(width, height), color);
    }

    public void text(String text, double x, double y, Color color, boolean title) {
        texts.add(getOp(textPool, x, y, color).set(text, theme.textRenderer(), title));
    }

    public void texture(double x, double y, double width, double height, double rotation, Texture texture) {
        post(() -> {
            textureRenderer.begin();
            textureRenderer.texturedQuad(x, y, width, height, rotation, 0, 0, 1, 1, WHITE);
            textureRenderer.end();

            texture.bind();
            textureRenderer.render(matrixStack);
        });
    }

    public void post(Runnable task) {
        scissorStack.peek().postTasks.add(task);
    }

    public void absolutePost(Runnable task) {
        postTasks.add(task);
    }

    private <T extends GuiRenderOperation<T>> T getOp(Pool<T> pool, double x, double y, Color color) {
        T op = pool.get();
        op.set(x, y, color);
        return op;
    }
}
