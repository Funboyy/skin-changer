package de.funboyy.skin.changer.gui.activity;

import de.funboyy.skin.changer.SkinChangerAddon;
import de.funboyy.skin.changer.config.SkinChange;
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
import net.labymod.api.client.session.MinecraftServices.SkinVariant;
import net.labymod.api.labynet.models.textures.Skin;
import net.labymod.core.client.gui.screen.widget.widgets.customization.PlayerModelWidget;

@AutoActivity
@Link("manage.lss")
@Link("overview.lss")
public class SkinChangeActivity extends Activity {

  private static final Pattern NAME_PATTERN = Pattern.compile("[\\w_]{0,16}");
  private static final Skin DEFAULT_SKIN = new Skin(SkinVariant.CLASSIC, "66088fe456abc1215cb0e918d8fe5bef");

  private final SkinChangerAddon addon;
  private final VerticalListWidget<SkinChangeWidget> skinChangeList;
  private final Map<String, SkinChangeWidget> skinChangeWidgets;

  private SkinChangeWidget selectedSkinChange;
  private SkinChangeWidget creationSkinChange;

  private ButtonWidget removeButton;
  private ButtonWidget editButton;

  private FlexibleContentWidget inputWidget;

  private Action action;

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
      this.addon.getNameCache().remove(skinChangeWidget.getUserName());
      this.skinChangeWidgets.remove(skinChangeWidget.getUserName());
      this.skinChangeList.listSession().setSelectedEntry(null);
      this.setAction(null);
    }));

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.cancel", () -> this.setAction(null)));
    this.inputWidget.addContent(menu);

    return this.inputWidget;
  }

  private DivWidget initializeManageContainer(final SkinChangeWidget skinChangeWidget) {
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
    nameTextField.setText(skinChangeWidget.getUserName());
    nameTextField.validator(newValue -> NAME_PATTERN.matcher(newValue).matches());
    nameTextField.updateListener(newValue -> {
      doneButton.setEnabled(!newValue.trim().isEmpty() &&
          skinChange.hasSkin());

      if (newValue.equals(skinChangeWidget.getUserName())) {
        return;
      }

      skinChangeWidget.setUserName(newValue);
    });

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
    checkBoxWidget.setState(skinChange.isEnabled() ? State.CHECKED : State.UNCHECKED);
    checkBoxDiv.addChild(checkBoxWidget);
    settingsWrapper.addChild(checkBoxDiv);
    inputWrapper.addChild(settingsWrapper);

    browseButton.setPressable(() -> {
      skinChange.setEnabled(checkBoxWidget.state() == State.CHECKED);
      skinChangeWidget.setSkinChange(skinChange);

      this.displayScreen(new SkinBrowseActivity(skinChangeWidget));
      this.setAction(null);
    });

    final DivWidget modelWrapper = new DivWidget();
    modelWrapper.addId("model-wrapper");

    final PlayerModelWidget playerModel = new PlayerModelWidget();
    playerModel.addId("model");
    playerModel.setRenderCosmetics(false);
    playerModel.setModel(skinChange.hasSkin() ? new Skin(skinChange.getSkinVariant(),
        skinChange.getImageHash()) : DEFAULT_SKIN);
    modelWrapper.addChild(playerModel);
    inputWrapper.addChild(modelWrapper);
    this.inputWidget.addContent(inputWrapper);

    final HorizontalListWidget buttonList = new HorizontalListWidget();
    buttonList.addId("edit-button-menu");

    doneButton.setEnabled(!nameTextField.getText().trim().isEmpty() && skinChange.hasSkin());

    doneButton.setPressable(() -> {
      skinChange.setEnabled(checkBoxWidget.state() == State.CHECKED);
      skinChangeWidget.setSkinChange(skinChange);

      if (this.action == Action.ADD || this.action == Action.CREATE) {
        this.skinChangeWidgets.put(skinChangeWidget.getUserName(), skinChangeWidget);
        this.skinChangeList.listSession().setSelectedEntry(skinChangeWidget);
      }

      if (!skinChangeWidget.getOriginalName().equals(skinChangeWidget.getUserName())) {
        this.addon.configuration().getSkinChanges().remove(skinChangeWidget.getOriginalName());
        this.addon.getNameCache().remove(skinChangeWidget.getOriginalName());
      }

      this.addon.configuration().getSkinChanges().put(skinChangeWidget.getUserName(), skinChange);
      this.addon.getNameCache().add(skinChangeWidget.getUserName());

      this.addon.configuration().removeInvalidSkinChanges();

      skinChangeWidget.applyUserName();
      this.setAction(null);
    });

    buttonList.addEntry(doneButton);
    buttonList.addEntry(ButtonWidget.i18n("labymod.ui.button.cancel", () -> {
      skinChangeWidget.setUserName(skinChangeWidget.getOriginalName());
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
    setAction(Action.CREATE);
  }

  private enum Action {
    ADD, CREATE, EDIT, REMOVE
  }

}
