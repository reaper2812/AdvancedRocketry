package zmaster587.advancedRocketry.Inventory.modules;

import java.util.LinkedList;
import java.util.List;

import zmaster587.advancedRocketry.Inventory.TextureResources;
import zmaster587.advancedRocketry.util.DataStorage;
import zmaster587.advancedRocketry.util.IDataInventory;
import zmaster587.libVulpes.gui.GuiImageButton;
import zmaster587.libVulpes.util.IconResource;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

public class ModuleData extends ModuleBase implements IButtonInventory {

	static final int barYSize = 38;
	static final int barXSize = 6;
	static final int textureOffsetX = 0;
	static final int textureOffsetY = 215;

	DataStorage data[];
	int prevData[];
	int prevDataType;
	int slot;
	IDataInventory chipStorage;
	IconResource icon = TextureResources.ioSlot;
	ModuleButton buttonStore, buttonLoad;

	public ModuleData(int offsetX, int offsetY, int slot, IDataInventory chipStorage, DataStorage ... data) {
		super(offsetX, offsetY);
		this.data = data;
		prevData = new int[data.length];
		this.chipStorage = chipStorage;
		this.slot = slot;

		buttonStore = new ModuleButton(10 + offsetX, offsetY, 0, "", this, TextureResources.buttonDown, "Store to Chip", 16, 10);
		buttonLoad = new ModuleButton(12 + offsetX, 36 + offsetY, 1, "", this, TextureResources.buttonLeft, "Store to buffer", 10, 16);
	}

	@Override
	public List<GuiButton> addButtons(int x, int y) {
		List<GuiButton> list = buttonLoad.addButtons(x, y);
		list.addAll(buttonStore.addButtons(x, y));
		return list;
	}

	@Override
	public void onInventoryButtonPressed(int buttonId) {
		if(buttonId == 0) {
			chipStorage.storeData();
		}
		else if(buttonId == 1) {
			chipStorage.loadData();
		}
	}

	@Override
	public void actionPerform(GuiButton button) {
		buttonStore.actionPerform(button);
		buttonLoad.actionPerform(button);
	}

	@Override
	public List<Slot> getSlots(Container container) {
		slotList.add(new Slot(chipStorage, slot, offsetX + 10, offsetY + 16));
		return slotList;
	}

	@Override
	public int numberOfChangesToSend() {
		return data.length + 1;
	}

	@Override
	protected boolean needsUpdate(int localId) {
		if(localId < data.length)
			return data[localId].getData() != prevData[localId];
		return data[0].getDataType().ordinal() != prevDataType;
	}

	@Override
	protected void updatePreviousState(int localId) {
		if(localId < data.length) 
			prevData[localId] = data[localId].getData();

		else
			prevDataType = data[0].getDataType().ordinal();
	}

	@Override
	public void sendChanges(Container container, ICrafting crafter,
			int variableId, int localId) {
		if(localId < data.length)
			crafter.sendProgressBarUpdate(container, variableId, data[localId].getData());
		else
			crafter.sendProgressBarUpdate(container, variableId, data[0].getDataType().ordinal());
	}

	@Override
	public void onChangeRecieved(int slot, int value) {
		if(slot < data.length)
			data[slot].setData(value, DataStorage.DataType.UNDEFINED);
		else
			data[0].setDataType(DataStorage.DataType.values()[value]);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderForeground(int mouseX, int mouseY, float zLevel, GuiContainer gui, FontRenderer font) {

		buttonLoad.renderForeground(mouseX, mouseY, zLevel, gui, font);
		buttonStore.renderForeground(mouseX, mouseY, zLevel, gui, font);

		int relativeX = mouseX - offsetX;
		int relativeY = mouseY - offsetY;

		//Handles data tooltip
		if( relativeX > 0 && relativeX < barXSize && relativeY > 0 && relativeY < barYSize) {
			int totalData = 0, totalMaxData = 0;

			for(DataStorage datum : data) {
				totalData += datum.getData();
				totalMaxData += datum.getMaxData();
			}

			List<String> list = new LinkedList<String>();
			list.add(totalData + " / " + totalMaxData + " Data");
			list.add("Type: " +  I18n.format(data[0].getDataType().toString(), new Object[0]));

			this.drawTooltip(gui, list, mouseX, mouseY, zLevel, font);
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(GuiContainer gui, int x, int y, FontRenderer font) {
		buttonLoad.renderBackground(gui, x, y, font);
		buttonStore.renderBackground(gui, x, y, font);

		for(Slot slot : slotList) {
			gui.drawTexturedModalRect(x + slot.xDisplayPosition - 1, y + slot.yDisplayPosition - 1, icon.getxLoc(), icon.getyLoc(), icon.getxSize(), icon.getySize());
		}

		int totalData = 0, totalMaxData = 0;

		for(DataStorage datum : data) {
			totalData += datum.getData();
			totalMaxData += datum.getMaxData();
		}

		float percent = totalData/(float)totalMaxData;

		gui.drawTexturedModalRect(offsetX + x, offsetY + y, 176, 18, 8, 40);
		gui.drawTexturedModalRect(offsetX + x - 1, offsetY + y + barYSize + 4, 19, 171, 10, 10);

		gui.drawTexturedModalRect(offsetX + x + 1, 1 + offsetY + y + (barYSize-(int)(percent*barYSize)), textureOffsetX, barYSize- (int)(percent*barYSize) + textureOffsetY, barXSize, (int)(percent*barYSize));
	}
}
