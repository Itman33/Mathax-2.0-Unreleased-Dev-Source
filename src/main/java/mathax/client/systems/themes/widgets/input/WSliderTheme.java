package mathax.client.systems.themes.widgets.input;

import mathax.client.gui.renderer.GuiRenderer;
import mathax.client.systems.themes.Theme;
import mathax.client.systems.themes.widgets.WidgetTheme;
import mathax.client.gui.widgets.input.WSlider;

public class WSliderTheme extends WSlider implements WidgetTheme {
    public WSliderTheme(double value, double min, double max) {
        super(value, min, max);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double valueWidth = valueWidth();

        renderBar(renderer, valueWidth);
        renderHandle(renderer, valueWidth);
    }

    private void renderBar(GuiRenderer renderer, double valueWidth) {
        Theme theme = theme();

        double scale = theme.scale(3);
        double handleSize = handleSize();

        double x = this.x + handleSize / 2;
        double y = this.y + height / 2 - scale / 2;

        renderer.quad(x, y, valueWidth, scale, theme.sliderLeftSetting.get());
        renderer.quad(x + valueWidth, y, width - valueWidth - handleSize, scale, theme.sliderRightSetting.get());
    }

    private void renderHandle(GuiRenderer renderer, double valueWidth) {
        Theme theme = theme();
        double handleSize = handleSize();

        renderer.quad(x + valueWidth, y, handleSize, handleSize, GuiRenderer.CIRCLE, theme.sliderHandleSetting.get(dragging, handleMouseOver));
    }
}
