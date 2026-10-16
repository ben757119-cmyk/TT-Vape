package com.ttvape.gui;

import com.ttvape.TTClient;
import com.ttvape.modules.Category;
import com.ttvape.modules.Module;
import com.ttvape.modules.client.ClickGUIModule;
import com.ttvape.settings.BoolSetting;
import com.ttvape.settings.ColorSetting;
import com.ttvape.settings.ModeSetting;
import com.ttvape.settings.NumberSetting;
import com.ttvape.settings.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Vape-styled ClickGUI — dark panels, purple accent. */
public class ClickGUIScreen extends Screen {
    private static final int PANEL_BG = 0xF00C0C0E, HEADER_BG = 0xFF0A0A0C, TEXT_DIM = 0xFF7A7A8C, ROW_OFF = 0xFFB0B0C0;
    private static final int SLIDER_BG = 0xFF16161C, SEARCH_BG = 0xF00A0A0C, ROW_ON_BG = 0x338B5CF6;
    private static final int SEARCH_X = 12, SEARCH_Y = 10, SEARCH_W = 160, SEARCH_H = 16;

    private final List<Panel> panelList = new ArrayList<>();
    private String searchQuery = "";
    private boolean searchFocused;
    private Module bindingModule;
    private Module hoveredModule;
    private Setting<?> typing;
    private String typeBuffer = "";

    public ClickGUIScreen() {
        super(Component.literal("vape"));
    }

    private int accent() {
        if (TTClient.modules == null) return 0xFF8B5CF6;
        ClickGUIModule gui = TTClient.modules.getModule(ClickGUIModule.class);
        if (gui != null && gui.accent != null) return gui.accent.get();
        return 0xFF8B5CF6;
    }

    @Override
    protected void init() {
        panelList.clear();
        int x = 12;
        int y = 34;
        for (Category cat : Category.values()) {
            List<Module> mods = TTClient.modules != null ? TTClient.modules.getByCategory(cat) : List.of();
            if (mods.isEmpty()) continue;
            Panel p = new Panel(cat, mods, x, y);
            panelList.add(p);
            x += 118;
            if (x + 110 > this.width) {
                x = 12;
                y += 220;
            }
        }
    }

    @Override public void extractTransparentBackground(GuiGraphicsExtractor graphics) {}
    @Override protected void extractMenuBackground(GuiGraphicsExtractor graphics) {}

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        hoveredModule = null;
        graphics.fill(SEARCH_X, SEARCH_Y, SEARCH_X + SEARCH_W, SEARCH_Y + SEARCH_H, SEARCH_BG);
        graphics.fill(SEARCH_X, SEARCH_Y, SEARCH_X + 2, SEARCH_Y + SEARCH_H, accent());
        String shown = searchQuery.isEmpty() ? (searchFocused ? "search..." : "search modules") : searchQuery + (searchFocused ? "_" : "");
        graphics.text(this.font, shown, SEARCH_X + 8, SEARCH_Y + 5, searchQuery.isEmpty() ? TEXT_DIM : 0xFFE8EAED, false);
        graphics.text(this.font, "vape  " + TTClient.VERSION + "  |  rshift", SEARCH_X + SEARCH_W + 14, SEARCH_Y + 5, accent(), false);
        for (Panel panel : panelList) panel.render(graphics, mouseX, mouseY);
        if (hoveredModule != null && typing == null) {
            String title = hoveredModule.getName();
            String desc = hoveredModule.getDescription();
            int pad = 8, tw = Math.max(font.width(title), font.width(desc)), boxW = tw + pad * 2, boxH = 28;
            int tx = Math.min(mouseX + 12, this.width - boxW - 4), ty = Math.min(mouseY + 12, this.height - boxH - 4);
            graphics.fill(tx, ty, tx + boxW, ty + boxH, 0xF0080A0E);
            graphics.fill(tx, ty, tx + 2, ty + boxH, accent());
            graphics.text(font, title, tx + pad, ty + 4, accent(), false);
            graphics.text(font, desc, tx + pad, ty + 15, TEXT_DIM, false);
        }
        if (bindingModule != null) {
            String msg = "bind  " + bindingModule.getName() + "  |  esc clear | mouse ok";
            int w = font.width(msg) + 16;
            int bx = (this.width - w) / 2;
            int by = this.height - 40;
            graphics.fill(bx, by, bx + w, by + 18, 0xF00A0A0C);
            graphics.fill(bx, by, bx + 2, by + 18, accent());
            graphics.text(font, msg, bx + 8, by + 5, accent(), false);
        }
        if (typing != null) {
            String msg = typing.getName() + " = " + typeBuffer + "_";
            int w = font.width(msg) + 16;
            int bx = (this.width - w) / 2;
            int by = this.height - 62;
            graphics.fill(bx, by, bx + w, by + 18, 0xF00A0A0C);
            graphics.text(font, msg, bx + 8, by + 5, 0xFFE8EAED, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= SEARCH_X && mouseX <= SEARCH_X + SEARCH_W && mouseY >= SEARCH_Y && mouseY <= SEARCH_Y + SEARCH_H) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        if (bindingModule != null) {
            if (button >= 0) {
                bindingModule.setKeyBind(2000 + button);
                bindingModule = null;
                return true;
            }
        }
        for (Panel panel : panelList) {
            if (panel.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (Panel panel : panelList) {
            if (panel.mouseDragged(mouseX, mouseY, button)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panelList) panel.mouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule.setKeyBind(0);
                bindingModule = null;
                return true;
            }
            if (keyCode != GLFW.GLFW_KEY_LEFT_SHIFT && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT
                    && keyCode != GLFW.GLFW_KEY_LEFT_CONTROL && keyCode != GLFW.GLFW_KEY_RIGHT_CONTROL
                    && keyCode != GLFW.GLFW_KEY_LEFT_ALT && keyCode != GLFW.GLFW_KEY_RIGHT_ALT) {
                bindingModule.setKeyBind(keyCode);
                bindingModule = null;
                return true;
            }
        }
        if (typing != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                typing = null;
                typeBuffer = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitTyping();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !typeBuffer.isEmpty()) {
                typeBuffer = typeBuffer.substring(0, typeBuffer.length() - 1);
                return true;
            }
            return true;
        }
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typing != null) {
            if (codePoint >= 32 && codePoint != 127) {
                typeBuffer += codePoint;
                return true;
            }
        }
        if (searchFocused) {
            if (codePoint >= 32 && codePoint != 127) {
                searchQuery += codePoint;
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void commitTyping() {
        if (typing instanceof NumberSetting ns) {
            try {
                double v = Double.parseDouble(typeBuffer.trim());
                ns.set(v);
            } catch (Exception ignored) {}
        }
        typing = null;
        typeBuffer = "";
    }

    private boolean matchesSearch(Module m) {
        if (searchQuery.isEmpty()) return true;
        String q = searchQuery.toLowerCase(Locale.ROOT);
        return m.getName().toLowerCase(Locale.ROOT).contains(q)
                || m.getDescription().toLowerCase(Locale.ROOT).contains(q);
    }

    private class Panel {
        final Category category;
        final List<Module> modules;
        int x, y;
        boolean dragging;
        double dragOffX, dragOffY;
        Module openSettings;
        NumberSetting sliding;

        Panel(Category category, List<Module> modules, int x, int y) {
            this.category = category;
            this.modules = modules;
            this.x = x;
            this.y = y;
        }

        void render(GuiGraphicsExtractor g, int mx, int my) {
            int h = 18 + modules.stream().mapToInt(m -> matchesSearch(m) ? 14 : 0).sum();
            if (openSettings != null) h += openSettings.getSettings().size() * 14 + 4;
            g.fill(x, y, x + 112, y + h + 4, PANEL_BG);
            g.fill(x, y, x + 112, y + 16, HEADER_BG);
            g.fill(x, y, x + 2, y + 16, accent());
            g.text(font, category.name().toLowerCase(Locale.ROOT), x + 8, y + 4, accent(), false);

            int cy = y + 18;
            for (Module m : modules) {
                if (!matchesSearch(m)) continue;
                boolean on = m.isEnabled();
                boolean hover = mx >= x && mx <= x + 112 && my >= cy && my <= cy + 13;
                if (hover) hoveredModule = m;
                if (on) g.fill(x + 2, cy, x + 112, cy + 13, ROW_ON_BG);
                g.text(font, m.getName(), x + 8, cy + 3, on ? accent() : (hover ? 0xFFE8EAED : ROW_OFF), false);
                if (openSettings == m) {
                    cy += 14;
                    for (Setting<?> s : m.getSettings()) {
                        renderSetting(g, s, x + 6, cy, mx, my);
                        cy += 14;
                    }
                } else {
                    cy += 14;
                }
            }
        }

        void renderSetting(GuiGraphicsExtractor g, Setting<?> s, int sx, int sy, int mx, int my) {
            if (s instanceof BoolSetting b) {
                String t = s.getName() + ": " + (b.get() ? "on" : "off");
                g.text(font, t, sx, sy + 2, b.get() ? accent() : TEXT_DIM, false);
            } else if (s instanceof NumberSetting n) {
                g.text(font, s.getName(), sx, sy + 2, TEXT_DIM, false);
                int barX = sx + 56;
                int barW = 48;
                double pct = (n.get() - n.getMin()) / Math.max(0.0001, n.getMax() - n.getMin());
                g.fill(barX, sy + 5, barX + barW, sy + 9, SLIDER_BG);
                g.fill(barX, sy + 5, barX + (int) (barW * pct), sy + 9, accent());
                String val = n.getStep() >= 1 ? String.valueOf(n.getInt()) : String.format(Locale.ROOT, "%.2f", n.get());
                g.text(font, val, barX + barW + 4, sy + 2, 0xFFE8EAED, false);
            } else if (s instanceof ModeSetting m) {
                g.text(font, s.getName() + ": " + m.get(), sx, sy + 2, accent(), false);
            } else if (s instanceof ColorSetting) {
                g.text(font, s.getName(), sx, sy + 2, accent(), false);
            } else {
                g.text(font, s.getName(), sx, sy + 2, TEXT_DIM, false);
            }
        }

        boolean mouseClicked(double mx, double my, int button) {
            if (mx >= x && mx <= x + 112 && my >= y && my <= y + 16) {
                dragging = true;
                dragOffX = mx - x;
                dragOffY = my - y;
                return true;
            }
            int cy = y + 18;
            for (Module m : modules) {
                if (!matchesSearch(m)) continue;
                if (mx >= x && mx <= x + 112 && my >= cy && my <= cy + 13) {
                    if (button == 0) {
                        m.toggle();
                        return true;
                    }
                    if (button == 1) {
                        openSettings = openSettings == m ? null : m;
                        return true;
                    }
                    if (button == 2) {
                        bindingModule = m;
                        return true;
                    }
                }
                if (openSettings == m) {
                    cy += 14;
                    for (Setting<?> s : m.getSettings()) {
                        if (mx >= x + 6 && mx <= x + 110 && my >= cy && my <= cy + 13) {
                            if (s instanceof BoolSetting b) {
                                b.set(!b.get());
                                return true;
                            }
                            if (s instanceof ModeSetting mode) {
                                mode.cycle();
                                return true;
                            }
                            if (s instanceof NumberSetting n) {
                                if (button == 1) {
                                    typing = n;
                                    typeBuffer = String.valueOf(n.get());
                                    return true;
                                }
                                sliding = n;
                                applySlider(n, mx);
                                return true;
                            }
                        }
                        cy += 14;
                    }
                } else {
                    cy += 14;
                }
            }
            return false;
        }

        boolean mouseDragged(double mx, double my, int button) {
            if (dragging) {
                x = (int) (mx - dragOffX);
                y = (int) (my - dragOffY);
                return true;
            }
            if (sliding != null) {
                applySlider(sliding, mx);
                return true;
            }
            return false;
        }

        void mouseReleased() {
            dragging = false;
            sliding = null;
        }

        void applySlider(NumberSetting n, double mx) {
            int barX = x + 62;
            int barW = 48;
            double pct = Math.max(0, Math.min(1, (mx - barX) / barW));
            double val = n.getMin() + pct * (n.getMax() - n.getMin());
            n.set(val);
        }
    }
}
