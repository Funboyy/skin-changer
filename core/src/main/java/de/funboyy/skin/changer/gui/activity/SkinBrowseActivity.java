package de.funboyy.skin.changer.gui.activity;

import de.funboyy.skin.changer.SkinChangerAddon;
import de.funboyy.skin.changer.gui.widget.SkinChangeWidget;
import de.funboyy.skin.changer.gui.widget.SkinPreviewWidget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.labymod.api.Constants.Urls;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.action.ListSession;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.TilesGridFeedWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.labynet.models.textures.Skin;
import net.labymod.api.labynet.models.textures.TextureResult;
import net.labymod.api.labynet.models.textures.TextureResult.Order;
import net.labymod.api.labynet.models.textures.TextureResult.Type;
import net.labymod.api.metadata.Metadata;
import net.labymod.api.util.Debounce;
import net.labymod.api.util.io.web.UrlBuilder;
import net.labymod.api.util.io.web.request.Callback;
import net.labymod.api.util.io.web.request.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoActivity
@Link("skin-browse.lss")
public class SkinBrowseActivity extends Activity {

  /**
   * This class was inspired by
   * {@code net.labymod.core.client.gui.screen.activity.activities.labymod.child.player.SkinBrowseActivity}
   * **/

  private static final int PAGE_SIZE = 42;
  private static final Map<Order, SkinBrowserOrderCache> ORDER_CACHES = new HashMap<>();

  private final SkinChangeWidget skinChangeWidget;
  private final ListSession<SkinPreviewWidget> session;
  private final TilesGridFeedWidget<SkinPreviewWidget> feedWidget;
  private final TextFieldWidget searchField;
  private SkinBrowserOrderCache selectedCache;
  private SkinCacheCollection selectedCollection;
  private int page;

  public SkinBrowseActivity(final SkinChangeWidget skinChangeWidget) {
    this.skinChangeWidget = skinChangeWidget;
    this.selectedCache = ORDER_CACHES.get(Order.TRENDING);
    this.selectedCollection = this.selectedCache.collection("");
    this.session = new ListSession<>();
    this.feedWidget = new TilesGridFeedWidget<>(this::refreshFeed);
    this.feedWidget.addId("feed");
    this.feedWidget.doRefresh(false);
    this.searchField = new TextFieldWidget();
    this.searchField.addId("search-field");
    this.searchField.placeholder(Component.translatable("labymod.ui.textfield.search"));
    this.searchField.updateListener((string) -> Debounce.of("skins-browse-search", 300L,
        () -> this.labyAPI.minecraft().executeOnRenderThread(() -> {
          this.selectedCollection = this.selectedCache.collection(string);
          this.session.setScrollPositionY(0.0F);
          this.reload();
        })));
  }

  @Override
  public void initialize(final Parent parent) {
    super.initialize(parent);
    this.page = 0;
    this.loadPage(this.page);

    final FlexibleContentWidget contentWidget = new FlexibleContentWidget();
    contentWidget.addId("content");

    final HorizontalListWidget contentHeader = new HorizontalListWidget();
    contentHeader.addId("content-header");
    contentHeader.addEntry(this.searchField);

    final DropdownWidget<Order> orderDropdown = new DropdownWidget<>();
    orderDropdown.addId("order-dropdown");
    orderDropdown.addAll(Order.values());
    orderDropdown.setSelected(this.selectedCache.order);
    orderDropdown.setTranslationKeyPrefix("skinchanger.gui.manage.skin.order");
    orderDropdown.setChangeListener((order) -> {
      if (this.selectedCache.order != order) {
        this.selectedCache = ORDER_CACHES.get(order);
        this.selectedCollection = this.selectedCache.collection(this.searchField.getText());
        this.session.setScrollPositionY(0.0F);
        this.reload();
      }
    });

    contentHeader.addEntry(orderDropdown);
    contentWidget.addContent(contentHeader);
    contentWidget.addFlexibleContent((new ScrollWidget(this.feedWidget, this.session)).addId("player-scroll"));
    this.document.addChild(contentWidget);
  }

  @Override
  public boolean shouldHandleEscape() {
    return true;
  }

  @Override
  public boolean keyPressed(final Key key, final InputType type) {
    if (key == Key.ESCAPE) {
      this.displayPreviousScreen();
      return true;
    }

    return super.keyPressed(key, type);
  }

  private boolean refreshFeed(final TilesGridFeedWidget<SkinPreviewWidget> feedWidget, final Consumer<SkinPreviewWidget> add) {
    return this.loadPage(++this.page);
  }

  private boolean loadPage(final int page) {
    final SkinCache skinCache = this.selectedCollection.getOrLoadPage(page, (cache) -> {
      if (!cache.getTextures().isEmpty()) {
        this.labyAPI.minecraft().executeOnRenderThread(() -> this.fillFeed(cache, true));
      }
    });

    if (skinCache != null && !skinCache.getTextures().isEmpty()) {
      this.fillFeed(skinCache, false);
      return true;
    }

    return false;
  }

  private void fillFeed(final SkinCache skinCache, final boolean withDebounce) {
    for (final Skin texture : skinCache.getTextures()) {
      final SkinPreviewWidget skinPreviewWidget = new SkinPreviewWidget(this, texture);

      if (this.feedWidget.isInitialized()) {
        this.feedWidget.addTileInitialized(skinPreviewWidget);
      }

      else {
        this.feedWidget.addTile(skinPreviewWidget);
      }
    }

    if (this.feedWidget.isInitialized()) {
      this.feedWidget.updateBounds();
    }

    if (!withDebounce) {
      this.feedWidget.doRefresh(skinCache.getTextures().size() == PAGE_SIZE);
    }

    else {
      Debounce.of("skins-browse-refresh", 500L, () ->
          this.labyAPI.minecraft().executeOnRenderThread(() ->
              this.feedWidget.doRefresh(skinCache.getTextures().size() == PAGE_SIZE)));
    }
  }

  static {
    Order.VALUES.forEach(order -> ORDER_CACHES.put(order, new SkinBrowserOrderCache(order)));
  }

  private static class SkinBrowserOrderCache {
    private final List<SkinCacheCollection> skinBrowserCaches = new ArrayList<>();
    private final SkinCacheCollection defaultBrowserCache;
    private final Order order;

    private SkinBrowserOrderCache(final Order order) {
      this.order = order;
      this.defaultBrowserCache = new SkinCacheCollection(order, null);
    }

    public SkinCacheCollection collection(final String query) {
      final String trim = query.toLowerCase().trim();

      if (trim.length() == 0) {
        return this.defaultBrowserCache;
      }

      return this.skinBrowserCaches.stream()
          .filter(collection -> collection.search.equals(trim))
          .findFirst().orElseGet(() -> {
            final SkinCacheCollection collection = new SkinCacheCollection(this.order, trim);
            this.skinBrowserCaches.add(collection);
            return collection;
          });
    }
  }

  private static class SkinCacheCollection {
    private final List<SkinCache> skinCaches = new ArrayList<>();
    private final String search;
    private final Order order;

    private SkinCacheCollection(final Order order, final String search) {
      this.search = search;
      this.order = order;
    }

    private SkinCache getOrLoadPage(final int page, final Consumer<SkinCache> callback) {
      final int offset = page * PAGE_SIZE;

      for (final SkinCache skinCache : this.skinCaches) {
        if (skinCache.getOffset() == offset) {
          return skinCache;
        }
      }

      loadTextures(this.search, this.order, offset, response -> {
        final SkinCache skinCache = new SkinCache(offset, response.isPresent()
            ? response.get().getTextures() : new ArrayList<>());
        this.skinCaches.add(skinCache);
        callback.accept(skinCache);
      });

      return null;
    }

    private static void loadTextures(
        @Nullable final String search,
        @NotNull final Order order,
        final int offset,
        @NotNull final Callback<TextureResult> response
    ) {
      final UrlBuilder url = new UrlBuilder(String.format(Urls.LABYNET_TEXTURE_SEARCH,
          Type.SKIN.name(), order.name().toLowerCase()));

      if (SkinBrowseActivity.PAGE_SIZE != 0) {
        url.addParameter("limit", SkinBrowseActivity.PAGE_SIZE);
      }

      if (offset != 0) {
        url.addParameter("offset", offset);
      }

      if (search != null && !search.isEmpty()) {
        url.addParameter("input", search);
      }

      Request.ofGson(TextureResult.class).url(url.build()).async().execute(response);
    }

  }

  private static class SkinCache {

    private final int offset;
    private final List<Skin> textures;

    private SkinCache(final int offset, final List<Skin> textures) {
      this.offset = offset;
      this.textures = textures;
    }

    public List<Skin> getTextures() {
      return this.textures;
    }

    public int getOffset() {
      return this.offset;
    }

  }

  public void setSkin(final Skin skin) {
    final Metadata metadata = this.skinChangeWidget.metadata();
    metadata.set("skin_variant", skin.skinVariant());
    metadata.set("image_hash", skin.getImageHash());

    final Activity activity = SkinChangerAddon.get().configuration().openSkinChanges();
    this.displayScreen(activity);

    if (activity instanceof SkinChangeActivity skinChangeActivity) {
      skinChangeActivity.openSkinChangeWidget(this.skinChangeWidget);
    }
  }

}
