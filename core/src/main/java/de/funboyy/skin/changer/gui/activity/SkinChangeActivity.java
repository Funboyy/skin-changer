package de.funboyy.skin.changer.gui.activity;

import de.funboyy.skin.changer.SkinChangerAddon;
import de.funboyy.skin.changer.config.SkinChange;
import de.funboyy.skin.changer.gui.widget.PlayerModelWidget;
import de.funboyy.skin.changer.gui.widget.SkinChangeWidget;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.labynet.models.textures.Skin;
import net.labymod.api.metadata.Metadata;

@AutoActivity
@Link("manage.lss")
@Link("overview.lss")
public class SkinChangeActivity extends Activity {

  /**
   * This class was inspired by
   * {@code net.labymod.addons.customnametags.gui.activity.NameTagActivity}
   * **/

  private static final Pattern NAME_PATTERN = Pattern.compile("[\\w_]{0,16}");

  private final SkinChangerAddon addon;
  private final VerticalListWidget<SkinChangeWidget> skinChangeList;
  private final Map<String, SkinChangeWidget> skinChangeWidgets;

  private SkinChangeWidget selectedSkinChange;
  private SkinChangeWidget creationSkinChange;

  private ButtonWidget removeButton;
  private ButtonWidget editButton;

  private FlexibleContentWidget inputWidget;

  private Action action;
  private boolean updateRequired;

  public SkinChangeActivity() {
    this.addon = SkinChangerAddon.get();

    this.skinChangeWidgets = new HashMap<>();
    this.addon.configuration().getSkinChanges().forEach((userName, skinChange) ->
      this.skinChangeWidgets.put(userName, new SkinChangeWidget(userName, skinChange)));

    this.skinChangeList = new VerticalListWidget<>();
    this.skinChangeList.addId("skin-change-list");
    this.skinChangeList.setSelectCallback(skinChangeWidget -> {
      final SkinChangeWidget selectedNameTag = this.skinChangeList.listSession().getSelectedEntry();

      if (selectedNameTag == null || selectedNameTag.getSkinChange() != skinChangeWidget.getSkinChange()) {
        this.editButton.setEnabled(true);
        this.removeButton.setEnabled(true);
      }
    });

    this.skinChangeList.setDoubleClickCallback(skinChangeWidget -> this.setAction(Action.EDIT));
  }

  @Override
  public void initialize(final Parent parent) {
    super.initialize(parent);

    final FlexibleContentWidget container = new FlexibleContentWidget();
    container.addId("skin-change-container");

    for (final SkinChangeWidget skinChangeWidget : this.skinChangeWidgets.values()) {
      this.skinChangeList.addChild(skinChangeWidget);
    }

    container.addFlexibleContent(new ScrollWidget(this.skinChangeList));

    this.selectedSkinChange = this.skinChangeList.listSession().getSelectedEntry();
    final HorizontalListWidget menu = new HorizontalListWidget();
    menu.addId("overview-button-menu");

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.add",
        () -> this.setAction(Action.ADD)));

    this.editButton = ButtonWidget.i18n("labymod.ui.button.edit",
        () -> this.setAction(Action.EDIT));

    this.editButton.setEnabled(this.selectedSkinChange != null);
    menu.addEntry(this.editButton);

    this.removeButton = ButtonWidget.i18n("labymod.ui.button.remove",
        () -> this.setAction(Action.REMOVE));

    this.removeButton.setEnabled(this.selectedSkinChange != null);
    menu.addEntry(this.removeButton);

    container.addContent(menu);
    this.document().addChild(container);

    if (this.action == null) {
      return;
    }

    final DivWidget manageContainer = new DivWidget();
    manageContainer.addId("manage-container");

    final Widget overlayWidget;

    switch (this.action) {
      default:
      case ADD:
        final SkinChangeWidget newSkinChange = new SkinChangeWidget("", SkinChange.createDefault());
        overlayWidget = this.initializeManageContainer(newSkinChange);
        break;
      case CREATE:
        overlayWidget = this.initializeManageContainer(this.creationSkinChange);
        break;
      case EDIT:
        overlayWidget = this.initializeManageContainer(this.selectedSkinChange);
        break;
      case REMOVE:
        overlayWidget = this.initializeRemoveContainer(this.selectedSkinChange);
        break;
    }

    manageContainer.addChild(overlayWidget);
    this.document().addChild(manageContainer);
  }

  private FlexibleContentWidget initializeRemoveContainer(final SkinChangeWidget skinChangeWidget) {
    this.inputWidget = new FlexibleContentWidget();
    this.inputWidget.addId("remove-container");

    final ComponentWidget confirmationWidget = ComponentWidget.i18n(
        "skinchanger.gui.manage.remove.title");
    confirmationWidget.addId("remove-confirmation");
    this.inputWidget.addContent(confirmationWidget);

    final SkinChangeWidget previewWidget = new SkinChangeWidget(skinChangeWidget.getUserName(),
        skinChangeWidget.getSkinChange());
    previewWidget.addId("remove-preview");
    this.inputWidget.addContent(previewWidget);

    final HorizontalListWidget menu = new HorizontalListWidget();
    menu.addId("remove-button-menu");

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.remove", () -> {
      this.addon.configuration().getSkinChanges().remove(skinChangeWidget.getUserName());
      this.addon.getSkinChange().remove(skinChangeWidget.getUserName());
      this.skinChangeWidgets.remove(skinChangeWidget.getUserName());
      this.skinChangeList.listSession().setSelectedEntry(null);
      this.updateRequired = true;
      this.setAction(null);
    }));

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.cancel", () -> this.setAction(null)));
    this.inputWidget.addContent(menu);

    return this.inputWidget;
  }

  private DivWidget initializeManageContainer(final SkinChangeWidget skinChangeWidget) {
    final Metadata meta = skinChangeWidget.metadata();
    final SkinChange skinChange = skinChangeWidget.getSkinChange();
    final ButtonWidget doneButton = ButtonWidget.i18n("labymod.ui.button.done");

    final DivWidget inputContainer = new DivWidget();
    inputContainer.addId("input-container");

    this.inputWidget = new FlexibleContentWidget();
    this.inputWidget.addId("input-list");

    final DivWidget inputWrapper = new DivWidget();
    inputWrapper.addId("input-wrapper");

    final VerticalListWidget<Widget> settingsWrapper = new VerticalListWidget<>();
    settingsWrapper.addId("settings-wrapper");

    final ComponentWidget labelName = ComponentWidget.i18n("skinchanger.gui.manage.name");
    labelName.addId("label-name");
    settingsWrapper.addChild(labelName);

    final TextFieldWidget nameTextField = new TextFieldWidget();
    nameTextField.addId("input-name");
    nameTextField.maximalLength(16);
    nameTextField.setText(meta.has("user_name") ? meta.get("user_name") : skinChangeWidget.getUserName());
    nameTextField.validator(newValue -> NAME_PATTERN.matcher(newValue).matches());
    nameTextField.updateListener(newValue -> doneButton.setEnabled(!newValue.trim().isEmpty()));

    settingsWrapper.addChild(nameTextField);

    final ButtonWidget browseButton = ButtonWidget.i18n("skinchanger.gui.manage.skin.title");
    browseButton.addId("browse-skin");
    settingsWrapper.addChild(browseButton);

    final DivWidget checkBoxDiv = new DivWidget();
    checkBoxDiv.addId("checkbox-div");

    final ComponentWidget checkBoxText = ComponentWidget.i18n("skinchanger.gui.manage.enabled.name");
    checkBoxText.addId("checkbox-name");
    checkBoxDiv.addChild(checkBoxText);

    final CheckBoxWidget checkBoxWidget = new CheckBoxWidget();
    checkBoxWidget.addId("checkbox-item");

    final boolean enabled = meta.has("enabled") ? meta.get("enabled") : skinChange.isEnabled();
    checkBoxWidget.setState(enabled ? State.CHECKED : State.UNCHECKED);
    checkBoxDiv.addChild(checkBoxWidget);
    settingsWrapper.addChild(checkBoxDiv);
    inputWrapper.addChild(settingsWrapper);

    browseButton.setPressable(() -> {
      final Metadata metadata = skinChangeWidget.metadata();
      metadata.set("user_name", nameTextField.getText());
      metadata.set("enabled", checkBoxWidget.state() == State.CHECKED);
      metadata.set("skin_variant", skinChange.getSkinVariant());
      metadata.set("image_hash", skinChange.getImageHash());
      skinChangeWidget.metadata(metadata);

      this.displayScreen(new SkinBrowseActivity(skinChangeWidget));
      this.setAction(null);
    });

    final DivWidget modelWrapper = new DivWidget();
    modelWrapper.addId("model-wrapper");

    final PlayerModelWidget playerModel = new PlayerModelWidget();
    playerModel.addId("model");

    final Skin skin = meta.has("skin_variant") && meta.has("image_hash") ?
        new Skin(meta.get("skin_variant"), meta.get("image_hash")) :
        new Skin(skinChange.getSkinVariant(), skinChange.getImageHash());
    playerModel.setModel(skin);
    modelWrapper.addChild(playerModel);
    inputWrapper.addChild(modelWrapper);
    this.inputWidget.addContent(inputWrapper);

    final HorizontalListWidget buttonList = new HorizontalListWidget();
    buttonList.addId("edit-button-menu");

    doneButton.setEnabled(!nameTextField.getText().trim().isEmpty());

    doneButton.setPressable(() -> {
      final String oldName = skinChangeWidget.getUserName();
      skinChange.setEnabled(checkBoxWidget.state() == State.CHECKED);
      skinChange.setSkinVariant(playerModel.getSkin().skinVariant());
      skinChange.setImageHash(playerModel.getSkin().getImageHash());
      skinChangeWidget.setUserName(nameTextField.getText());
      skinChangeWidget.setSkinChange(skinChange);
      skinChangeWidget.resetMeta();

      if (this.action == Action.ADD || this.action == Action.CREATE) {
        this.skinChangeWidgets.put(nameTextField.getText(), skinChangeWidget);
        this.skinChangeList.listSession().setSelectedEntry(skinChangeWidget);
      }

      if (!oldName.equals(skinChangeWidget.getUserName())) {
        this.addon.configuration().getSkinChanges().remove(oldName);
        this.addon.getSkinChange().remove(oldName);
      }

      this.addon.configuration().getSkinChanges().put(skinChangeWidget.getUserName(), skinChange);
      this.addon.getSkinChange().add(skinChangeWidget.getUserName());

      this.addon.configuration().removeInvalidSkinChanges();
      this.updateRequired = true;
      this.setAction(null);
    });

    buttonList.addEntry(doneButton);
    buttonList.addEntry(ButtonWidget.i18n("labymod.ui.button.cancel", () -> {
      skinChangeWidget.resetMeta();

      this.setAction(null);
    }));
    this.inputWidget.addContent(buttonList);
    inputContainer.addChild(this.inputWidget);

    return inputContainer;
  }

  @Override
  public boolean mouseClicked(final MutableMouse mouse, final MouseButton mouseButton) {
    try {
      if (this.action != null) {
        return this.inputWidget.mouseClicked(mouse, mouseButton);
      }

      return super.mouseClicked(mouse, mouseButton);
    } finally {
      this.selectedSkinChange = this.skinChangeList.listSession().getSelectedEntry();
      this.removeButton.setEnabled(this.selectedSkinChange != null);
      this.editButton.setEnabled(this.selectedSkinChange != null);
    }
  }

  @Override
  public boolean keyPressed(final Key key, final InputType type) {
    if (key.getId() == 256 && this.action != null) {
      this.setAction(null);
      return true;
    }

    return super.keyPressed(key, type);
  }

  private void setAction(final Action action) {
    this.action = action;
    this.reload();
  }

  public void openSkinChangeWidget(final SkinChangeWidget skinChangeWidget) {
    this.creationSkinChange = skinChangeWidget;
    this.setAction(Action.CREATE);
  }

  @Override
  public void onCloseScreen() {
    super.onCloseScreen();
    if (this.updateRequired) {
      this.addon.getSkinChange().reloadTextures();
    }
  }

  private enum Action {
    ADD, CREATE, EDIT, REMOVE
  }

}
