package com.soarclient.gui.modmenu.pages;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.soarclient.Soar;
import com.soarclient.animation.SimpleAnimation;
import com.soarclient.gui.api.SoarGui;
import com.soarclient.gui.api.page.Page;
import com.soarclient.gui.api.page.impl.LeftRightTransition;
import com.soarclient.gui.api.page.impl.RightLeftTransition;
import com.soarclient.management.color.api.ColorPalette;
import com.soarclient.management.mod.Mod;
import com.soarclient.skia.Skia;
import com.soarclient.skia.font.Fonts;
import com.soarclient.skia.font.Icon;
import com.soarclient.ui.component.api.PressAnimation;
import com.soarclient.utils.ColorUtils;
import com.soarclient.utils.SearchUtils;
import com.soarclient.utils.language.I18n;
import com.soarclient.utils.mouse.MouseUtils;

public class ModsPage extends Page {

	private final List<Item> items = new ArrayList<>();

	public ModsPage(SoarGui parent) {
		super(parent, "text.mods", Icon.INVENTORY_2, new RightLeftTransition(true));
	}

	@Override
	public void init() {
		super.init();

		items.clear();
		
		for (Mod module : Soar.getInstance().getModManager().getMods()) {

			Item i = new Item(module);

			if (module.isEnabled()) {
				i.pressAnimation.setPressed();
			}

			items.add(i);
		}
		
		for (Item i : items) {
			i.xAnimation.setFirstTick(true);
			i.yAnimation.setFirstTick(true);
		}
	}

	@Override
	public void draw(double mouseX, double mouseY) {
		super.draw(mouseX, mouseY);

		ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

		int index = 0;
		float offsetX = 26;
		float offsetY = 0;

		mouseY = mouseY - scrollHelper.getValue();

		Skia.save();
		Skia.translate(0, scrollHelper.getValue());

		for (Item i : items) {

			Mod m = i.mod;
			SimpleAnimation focusAnimation = i.focusAnimation;
			SimpleAnimation xAnimation = i.xAnimation;
			SimpleAnimation yAnimation = i.yAnimation;

			if (m.isHidden()) {
				continue;
			}

			if (!searchBar.getText().isEmpty() && !SearchUtils.isSimilar(I18n.get(m.getName()), searchBar.getText())) {
				continue;
			}

			float itemX = x + offsetX;
			float itemY = y + 96 + offsetY;

			focusAnimation.onTick(
					MouseUtils.isInside(mouseX, mouseY, itemX, itemY, 244, 116 + 35) ? i.pressed ? 0.12F : 0.08F : 0,
					8);
			xAnimation.onTick(itemX, 14);
			yAnimation.onTick(itemY, 14);

			itemX = xAnimation.getValue();
			itemY = yAnimation.getValue();

			Skia.drawRoundedRectVarying(itemX, itemY, 244, 116, 26, 26, 0, 0, palette.getSurface());
			Skia.drawRoundedRectVarying(itemX, itemY + 116, 244, 35, 0, 0, 26, 26, palette.getSurfaceContainerLow());

			Skia.drawRoundedRectVarying(itemX, itemY + 116, 244, 35, 0, 0, 26, 26,
					ColorUtils.applyAlpha(palette.getSurfaceContainerLowest(), focusAnimation.getValue()));

			Skia.save();
			Skia.clip(itemX, itemY + 116, 244, 35, 0, 0, 26, 26);
			i.pressAnimation.draw(itemX, itemY + 116, 224, 35, palette.getPrimaryContainer(), 1);
			Skia.restore();

			Skia.drawFullCenteredText(I18n.get(m.getName()), itemX + ((float) 244 / 2), itemY + 116 + ((float) 35 / 2),
					palette.getOnSurfaceVariant(), Fonts.getRegular(16));
			Skia.drawFullCenteredText(m.getIcon(), itemX + ((float) 244 / 2), itemY + ((float) 116 / 2), palette.getOnSurfaceVariant(),
					Fonts.getIcon(68));

			index++;
			offsetX += 32 + 244;

			if (index % 3 == 0) {
				offsetX = 26;
				offsetY += 22 + 151;
			}
		}

		scrollHelper.setMaxScroll(151, 22, index, 3, height - 96);
		Skia.restore();
	}

	@Override
	public void mousePressed(double mouseX, double mouseY, int button) {
		super.mousePressed(mouseX, mouseY, button);

		for (Item i : items) {

			float itemX = i.xAnimation.getValue();
			float itemY = i.yAnimation.getValue();

			if (i.mod.isHidden()) {
				continue;
			}

			if (!searchBar.getText().isEmpty()
					&& !SearchUtils.isSimilar(I18n.get(i.mod.getName()), searchBar.getText())) {
				continue;
			}

			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

				if (MouseUtils.isInside(mouseX, mouseY, itemX, itemY + 116, 244, 35)) {
					i.pressed = true;
				}
			}
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {

		mouseY = mouseY - scrollHelper.getValue();

		for (Item i : items) {

			Mod m = i.mod;
			float itemX = i.xAnimation.getValue();
			float itemY = i.yAnimation.getValue();

			if (i.mod.isHidden()) {
				continue;
			}

			if (!searchBar.getText().isEmpty()
					&& !SearchUtils.isSimilar(I18n.get(i.mod.getName()), searchBar.getText())) {
				continue;
			}

			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

				if (MouseUtils.isInside(mouseX, mouseY, itemX, itemY + 116, 244, 35)) {
					m.toggle();

					if (m.isEnabled()) {
						i.pressAnimation.onPressed(mouseX, mouseY, itemX, itemY + 116);
					} else {
						i.pressAnimation.onReleased(mouseX, mouseY, itemX, itemY + 116);
					}
				}

				if (MouseUtils.isInside(mouseX, mouseY, itemX, itemY, 244, 116)
						&& !Soar.getInstance().getModManager().getSettingsByMod(m).isEmpty()) {
					parent.setCurrentPage(new SettingsImplPage(parent, this.getClass(), m));
					this.setTransition(new LeftRightTransition(true));
				}
			}

			i.pressed = false;
		}
	}

	@Override
	public void onClosed() {
		this.setTransition(new RightLeftTransition(true));
	}

	private static class Item {

		private final Mod mod;
		private final SimpleAnimation focusAnimation = new SimpleAnimation();
		private final SimpleAnimation xAnimation = new SimpleAnimation();
		private final SimpleAnimation yAnimation = new SimpleAnimation();
		private final PressAnimation pressAnimation = new PressAnimation();
		private boolean pressed;

		private Item(Mod mod) {
			this.mod = mod;
			this.pressed = false;
		}
	}
}
